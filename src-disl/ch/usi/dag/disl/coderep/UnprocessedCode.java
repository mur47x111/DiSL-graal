package ch.usi.dag.disl.coderep;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.localvar.AbstractLocalVar;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.localvar.SyntheticLocalVar;
import ch.usi.dag.disl.localvar.ThreadLocalVar;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.Constants;
import ch.usi.dag.disl.util.Insn;
import ch.usi.dag.disl.util.ReflectionHelper;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;


/**
 * Represents a snippet or argument processor code template. This template only
 * contains the original code, without any modifications related, e.g., to the
 * use of thread local variables. A instance of the template containing the
 * modified/processed code can be obtained on demand using the
 * {@link #process(LocalVars) process()} method.
 */
public class UnprocessedCode {

    /** Canonical name of the class where the snippet was defined. */
    private final String __className;

    /** Method node containing the snippet code. */
    private final MethodNode __method;

    //

    public UnprocessedCode (
        final String className, final MethodNode method
    ) {
        __className = className;
        __method = method;
    }

    //

    public String className () {
        return __className;
    }

    public String methodName () {
        return __method.name;
    }

    public String location (final AbstractInsnNode insn) {
        return String.format (
            "snippet %s.%s%s", __className, __method.name,
            AsmHelper.formatLineNo (":%d ", insn)
        );
    }

    //

    public Code process (final LocalVars vars)
    throws DiSLInitializationException {
        //
        // Analyze code:
        //
        // Determine the kinds of contexts used in the snippet parameters.
        //
        // Collect a set of static context methods invoked by the snippet code.
        // This is done first, because it can result in initialization failure.
        //
        // Then collect the sets of referenced synthetic local and thread local
        // variables, and finally determine if there is any exception handler in
        // the code that handles the exception and does not propagate it.
        //
        final ContextUsage ctxs = ContextUsage.forMethod (__method);
        final Set <StaticContextMethod> scms = __collectStaticContextMethods (
            __method.instructions, ctxs.staticContextTypes ()
        );

        final Set <SyntheticLocalVar> slvs = __collectReferencedVars (
            __method.instructions, vars.getSyntheticLocals ()
        );

        final Set <ThreadLocalVar> tlvs = __collectReferencedVars (
            __method.instructions, vars.getThreadLocals ()
        );

        final boolean handlesExceptions = __handlesExceptionWithoutThrowing (
            __method.instructions, __method.tryCatchBlocks
        );

        //
        // Process code:
        //
        // Clone the method code so that we can transform it, then replace all
        // RETURN instructions with a GOTO to the end of a method, and rewrite
        // accesses to thread-local variables.
        //
        // Finally create an instance of processed code.
        //
        final MethodNode method = AsmHelper.cloneMethod (__method);
        __replaceReturnsWithGoto (method.instructions);
        __rewriteThreadLocalVarAccesses (method.instructions, tlvs);

        return new Code (
            method, slvs, tlvs, scms, handlesExceptions
        );
    }


    /**
     * Collects instances of unique static method invocations from the given
     * list of byte code instructions. Throws an exception if any of the
     * invocations is invalid.
     *
     * @param insns
     *        instructions to analyze
     * @param scTypes
     *        a set of known static context types
     * @return A set of {@link StaticContextMethod} instances.
     * @throws DiSLInitializationException
     *         if the static context method invocation is invalid, i.e., it
     *         contains arguments, has an invalid return type, or any of the
     *         referenced classes or methods could not be found via reflection
     */
    private Set <StaticContextMethod> __collectStaticContextMethods (
        final InsnList insns, final Set <Type> scTypes
    ) throws DiSLInitializationException {
        try {
            final ConcurrentMap <String, Boolean> seen = new ConcurrentHashMap <> ();
            return Insns.asList (insns).parallelStream ().unordered ()
                //
                // Select instructions representing method invocations on known
                // static context classes.
                //
                .filter (insn -> insn instanceof MethodInsnNode)
                .map (insn -> (MethodInsnNode) insn)
                .filter (insn -> scTypes.contains (Type.getObjectType (insn.owner)))
                //
                // Ensure ensure that each static context method invocation is
                // valid. This means that it does not have any parameters and only
                // returns either a primitive type or a String.
                //
                .filter (insn -> {
                    // Throws InvalidStaticContextInvocationException.
                    __ensureInvocationHasNoArguments (insn);
                    __ensureInvocationReturnsAllowedType (insn);
                    return true;
                })
                //
                // And finally create an instance of static method invocation, but
                // only for methods we have not seen so far.
                //
                .filter (insn -> seen.putIfAbsent (__methodId (insn), true) == null)
                .map (insn -> {
                    // Throws InvalidStaticContextInvocationException.
                    final Class <?> ownerClass = __resolveClass (insn);
                    final Method contextMethod = __resolveMethod (insn, ownerClass);
                    return new StaticContextMethod (
                        __methodId (insn), contextMethod, ownerClass
                    );
                })
                .collect (Collectors.toSet ());

        } catch (final InvalidStaticContextInvocationException e) {
            final MethodInsnNode insn = e.getInsn ();
            throw new DiSLInitializationException (
                "%s: invocation of static context method %s.%s: %s",
                location (insn), AsmHelper.internalToStdName (insn.owner),
                insn.name, e.getMessage ()
            );
        }
    }


    private static String __methodId (final MethodInsnNode methodInsn) {
        return methodInsn.owner + Constants.STATIC_CONTEXT_METHOD_DELIM + methodInsn.name;
    }


    private void __ensureInvocationHasNoArguments (final MethodInsnNode insn) {
        if (Type.getArgumentTypes (insn.desc).length != 0) {
            throw new InvalidStaticContextInvocationException (
                "arguments found, but NONE allowed", insn
            );
        }
    }


    private void __ensureInvocationReturnsAllowedType (final MethodInsnNode insn) {
        final Type returnType = Type.getReturnType (insn.desc);
        if (! __ALLOWED_RETURN_TYPES__.contains (returnType)) {
            throw new InvalidStaticContextInvocationException (
                "return type MUST be a primitive type or a String", insn
            );
        }
    }


    @SuppressWarnings ("serial")
    private static final Set <Type> __ALLOWED_RETURN_TYPES__ = Collections.unmodifiableSet (
        new HashSet <Type> (9) {{
            add (Type.BOOLEAN_TYPE);
            add (Type.BYTE_TYPE);
            add (Type.CHAR_TYPE);
            add (Type.SHORT_TYPE);
            add (Type.INT_TYPE);
            add (Type.LONG_TYPE);

            add (Type.FLOAT_TYPE);
            add (Type.DOUBLE_TYPE);

            add (Type.getType (String.class));
        }}
    );


    private Class <?> __resolveClass (final MethodInsnNode insn) {
        try {
            return ReflectionHelper.resolveClass (Type.getObjectType (insn.owner));

        } catch (final ReflectionException e) {
            throw new InvalidStaticContextInvocationException (e.getMessage (), insn);
        }
    }


    private Method __resolveMethod (
        final MethodInsnNode insn, final Class <?> ownerClass
    ) {
        try {
            return ReflectionHelper.resolveMethod (ownerClass,  insn.name);

        } catch (final ReflectionException e) {
            throw new InvalidStaticContextInvocationException (e.getMessage (), insn);
        }
    }

    //

    /**
     * Scans the given instruction sequence for field accesses and collects a
     * set of special local variables referenced by instructions in the
     * sequence. The local variables are identified by a fully qualified field
     * name.
     *
     * @param <T> type of the return value
     * @param insn the instruction sequence to scan
     * @param vars mapping between fully qualified field names and variables
     * @return a set of variables references by the code.
     */
    private <T> Set <T> __collectReferencedVars (
        final InsnList insns, final Map <String, T> vars
    ) {
        return Insns.asList (insns).parallelStream ().unordered ()
            .filter (insn -> insn instanceof FieldInsnNode)
            .map (insn -> {
                final FieldInsnNode fi = (FieldInsnNode) insn;
                return Optional.ofNullable (vars.get (
                    AbstractLocalVar.fqFieldNameFor (fi.owner, fi.name)
                ));
            })
            .filter (o -> o.isPresent ())
            .map (o -> o.get ())
            .collect (Collectors.toSet ());
    }

    //

    /**
     * Determines if the code contains an exception handler that handles
     * exceptions and does not propagate them further. This has to be detected
     * because it can cause stack inconsistency that has to be handled in the
     * weaver.
     */
    private boolean __handlesExceptionWithoutThrowing (
        final InsnList insns, final List <TryCatchBlockNode> tcbs
    ) {
        if (tcbs.size () == 0) {
            return false;
        }

        //
        // Create a control flow graph and check if the control flow continues
        // after an exception handler, which indicates that the handler handles
        // the exception.
        //
        final CtrlFlowGraph cfg = new CtrlFlowGraph (insns, tcbs);
        cfg.visit (insns.getFirst ());

        for (int i = tcbs.size () - 1; i >= 0; --i) {
            final TryCatchBlockNode tcb = tcbs.get (i);
            if (cfg.visit (tcb.handler).size () != 0) {
                return true;
            }
        }

        return false;
    }

    //

    /**
     * Adds a label to the end of the given instruction list and replaces all
     * types of RETURN instructions in the list with a GOTO instruction to jump
     * to the label at the end of the instruction list.
     *
     * @param insns
     *        list of instructions to perform the replacement on
     */
    private static void __replaceReturnsWithGoto (final InsnList insns) {
        //
        // Collect all RETURN instructions.
        //
        final List <AbstractInsnNode> returnInsns = Insns.asList (insns)
            .parallelStream ().unordered ()
            .filter (insn -> AsmHelper.isReturn (insn))
            .collect (Collectors.toList ());

        if (returnInsns.size () > 1) {
            //
            // Replace all RETURN instructions with a GOTO instruction
            // that jumps to a label at the end of the instruction list.
            //
            final LabelNode targetLabel = new LabelNode ();

            returnInsns.forEach (insn -> {
                insns.insertBefore (insn, AsmHelper.jumpTo (targetLabel));
                insns.remove (insn);
            });

            insns.add (targetLabel);

        } else if (returnInsns.size () == 1) {
            // there is only the return at the end of a method
            insns.remove (returnInsns.get (0));
        }
    }

    //

    private static void __rewriteThreadLocalVarAccesses (
        final InsnList insns, final Set <ThreadLocalVar> tlvs
    ) {
        //
        // Generate a set of TLV identifiers for faster lookup.
        //
        // TODO LB: We do this for every class - make LocalVars support the check.
        //
        final Set <String> tlvIds = tlvs.parallelStream ().unordered ()
            .map (tlv -> tlv.getID ())
            .collect (Collectors.toSet ());

        //
        // Scan the method code for GETSTATIC/PUTSTATIC instructions accessing
        // the static fields marked to be thread locals. Replace all the
        // static accesses with thread variable accesses.
        //
        // First select the instructions, then modify the instruction list.
        //
        final List <FieldInsnNode> fieldInsns = Insns.asList (insns)
            .parallelStream ().unordered ()
            .filter (insn -> AsmHelper.isStaticFieldAccess (insn))
            .map (insn -> (FieldInsnNode) insn)
            .filter (insn -> {
                final String fieldName = ThreadLocalVar.fqFieldNameFor (insn.owner, insn.name);
                return tlvIds.contains (fieldName);
            })
            .collect (Collectors.toList ());

        fieldInsns.forEach (insn -> __rewriteThreadLocalVarAccess (insn, insns));
    }

    //

    private static final Type threadType = Type.getType (Thread.class);
    private static final String currentThreadName = "currentThread";
    private static final Type currentThreadType = Type.getMethodType (threadType);

    private static void __rewriteThreadLocalVarAccess (
        final FieldInsnNode fieldInsn, final InsnList insns
    ) {
        //
        // Issue a call to Thread.currentThread() and access a field
        // in the current thread corresponding to the thread-local
        // variable.
        //
        insns.insertBefore (fieldInsn, AsmHelper.invokeStatic (
            threadType, currentThreadName, currentThreadType
        ));

        if (Insn.GETSTATIC.matches (fieldInsn)) {
            insns.insertBefore (fieldInsn, AsmHelper.getField (
                threadType, fieldInsn.name, fieldInsn.desc
            ));

        } else {
            //
            // We need to execute a PUTFIELD instruction, which requires
            // two operands, but the current thread reference that we
            // currently have on the top of the stack needs to come after
            // the value that is to be stored.
            //
            // We therefore need to swap the two operands on the stack.
            // There is no easier way, unless we want to track where the
            // value to be stored was pushed on the stack and put the
            // currentThread() method invocation before it.
            //
            // For primitive operands, we just swap the values. For wide
            // operands, we need to rearrange 3 slots in total, with the
            // slot 0 becoming slot 2, and slots 1 and 2 becoming 0 and 1.
            //
            if (Type.getType (fieldInsn.desc).getSize () == 1) {
                insns.insertBefore (fieldInsn, new InsnNode (Opcodes.SWAP));

            } else {
                insns.insertBefore (fieldInsn, new InsnNode (Opcodes.DUP_X2));
                insns.insertBefore (fieldInsn, new InsnNode (Opcodes.POP));
            }


            insns.insertBefore (fieldInsn, AsmHelper.putField (
                threadType, fieldInsn.name, fieldInsn.desc
            ));
        }

        //
        // Remove the static field access instruction.
        //
        insns.remove (fieldInsn);
    }

}
