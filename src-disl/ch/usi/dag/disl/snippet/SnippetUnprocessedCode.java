package ch.usi.dag.disl.snippet;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import com.oracle.graal.api.directives.GraalDirectives;

import ch.usi.dag.disl.DiSL.CodeOption;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.coderep.Code;
import ch.usi.dag.disl.coderep.UnprocessedCode;
import ch.usi.dag.disl.dynamicbypass.DynamicBypass;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.exception.ProcessorException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.marker.Marker;
import ch.usi.dag.disl.processor.ArgProcessor;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;


/**
 * Represents a snippet code template. This template contains the original code
 * produced by the Java compiler, potentially wrapped with dynamic bypass
 * control code and an exception handler to catch all exceptions.
 */
public class SnippetUnprocessedCode {
    /**
     * Determines whether the snippet should control dynamic bypass if the
     * dynamic bypass is enabled.
     */
    private final boolean __snippetDynamicBypass;

    /**
     * The code template we are decorating.
     */
    private final UnprocessedCode __template;

    //

    /**
     * Initializes a snippet code template with information about source class,
     * source method, usage of context parameters in the template, and whether
     * the snippet requires automatic control of dynamic bypass.
     */
    public SnippetUnprocessedCode (
        final String className, final MethodNode method,
        final boolean snippetDynamicBypass
    ) {
        __template = new UnprocessedCode (className, method);
        __snippetDynamicBypass = snippetDynamicBypass;
    }



    //

    public String className () {
        return __template.className ();
    }


    public String methodName () {
        return __template.methodName ();
    }

    //

    /**
     * Processes the stored data and creates snippet code structure.
     * @param annotationClass
     */
    public SnippetCode process (
        final LocalVars vars, final Map <Type, ArgProcessor> processors,
        final Marker marker, final Set <CodeOption> options, final Class <?> annotationClass
    ) throws DiSLInitializationException, ProcessorException, ReflectionException  {
        //
        // Pre-process code with local variables.
        //
        final Code code = __template.process (vars);

        //
        // Process code:
        //
        // If required, insert dynamic bypass control around the snippet,
        // or code to catch all exceptions to avoid disrupting program flow.
        //
        // Code processing has to be done before looking for argument processor
        // invocations, otherwise the analysis will produce wrong instruction
        // references.
        //
        final InsnList insns = code.getInstructions ();

        if (options.contains (CodeOption.INSERT_DELIMITATION)) {
            __insertGraalHints (insns, annotationClass, marker);
        }

        if (options.contains (CodeOption.DYNAMIC_BYPASS) && __snippetDynamicBypass) {
            __insertDynamicBypassControl (insns);
        }

        final List <TryCatchBlockNode> tcbs = code.getTryCatchBlocks ();
        if (options.contains (CodeOption.CATCH_EXCEPTIONS)) {
            __insertExceptionHandler (insns, tcbs);
        }

        //
        // Analyze code:
        //
        // Find argument processor invocations so that we can determine the
        // complete set of static context methods invoked within the snippet.
        // This is required later to prepare static context data for all snippet
        // invocations.
        //
        // No other modification should be done to the snippet code before
        // weaving, otherwise the produced instruction references will be
        // invalid.
        //
        // TODO LB: Why do we reference the invocations by bytecode index and
        // not an instruction node reference? Possibly because the index will
        // be still valid after cloning the code.
        //
        final Map <Integer, ProcInvocation> argProcInvocations =
            __collectArgProcInvocations (insns, processors, marker);

        return new SnippetCode (code, argProcInvocations);
    }


    private Map <Integer, ProcInvocation>  __collectArgProcInvocations (
        final InsnList insns, final Map <Type, ArgProcessor> procs, final Marker marker
    ) throws ProcessorException, ReflectionException {
        final Map <Integer, ProcInvocation> result = new HashMap <> ();

        int insnIndex = 0;
        for (final AbstractInsnNode insn : Insns.selectAll (insns)) {
            final ProcessorInfo apInfo = insnInvokesProcessor (
                insn, insnIndex, procs, marker
            );

            if (apInfo != null) {
                result.put (apInfo.insnIndex, apInfo.invocation);
            }

            insnIndex++;
        }

        return result;
    }


    private static class ProcessorInfo {
        final Integer insnIndex;
        final ProcInvocation invocation;

        public ProcessorInfo (final Integer insnIndex, final ProcInvocation invocation) {
            this.insnIndex = insnIndex;
            this.invocation = invocation;
        }
    }


    private ProcessorInfo insnInvokesProcessor (
        final AbstractInsnNode instr, final int i,
        final Map <Type, ArgProcessor> processors, final Marker marker
    ) throws ProcessorException, ReflectionException {
        // check method invocation
        if (!(instr instanceof MethodInsnNode)) {
            return null;
        }

        // check if the invocation is processor invocation
        final MethodInsnNode min = (MethodInsnNode) instr;
        final String apcClassName = Type.getInternalName (ArgumentProcessorContext.class);
        if (!apcClassName.equals (min.owner)) {
            return null;
        }

        if (!"apply".equals (min.name)) {
            return null;
        }

        // resolve load parameter instruction
        final AbstractInsnNode secondParam = Insns.REVERSE.nextRealInsn (instr);
        final AbstractInsnNode firstParam = Insns.REVERSE.nextRealInsn (secondParam);

        // NOTE: object parameter is ignored - will be removed by weaver

        // the first parameter has to be loaded by LDC
        if (firstParam == null || firstParam.getOpcode() != Opcodes.LDC) {
            throw new ProcessorException (
                "%s: pass the first (class) argument to the apply() method "+
                "directly as a class literal", __template.location (min)
            );
        }

        // the second parameter has to be loaded by GETSTATIC
        if (secondParam == null || secondParam.getOpcode() != Opcodes.GETSTATIC) {
            throw new ProcessorException (
                "%s: pass the second (type) argument to the apply() method "+
                "directly as an enum literal", __template.location (min)
            );
        }


        final Object asmType = ((LdcInsnNode) firstParam).cst;
        if (!(asmType instanceof Type)) {
            throw new ProcessorException (
                "%s: unsupported processor type %s",
                __template.location (min), asmType.getClass ().toString ()
            );
        }

        final Type processorType = (Type) asmType;
        final ArgumentProcessorMode procApplyType = ArgumentProcessorMode.valueOf (
            ((FieldInsnNode) secondParam).name
        );

        // if the processor apply type is CALLSITE_ARGS
        // the only allowed marker is BytecodeMarker
        if (ArgumentProcessorMode.CALLSITE_ARGS.equals (procApplyType)
            && marker.getClass () != BytecodeMarker.class
        ) {
            throw new ProcessorException (
                "%s: ArgumentProcessor applied in the CALLSITE_ARGS mode can "+
                "be only used with the BytecodeMarker", __template.location (min)
            );
        }

        final ArgProcessor processor = processors.get (processorType);
        if (processor == null) {
            throw new ProcessorException (
                "%s: unknown processor: %s", __template.location (min),
                processorType.getClassName ()
            );
        }

        //
        // Create an argument processor invocation instance tied to a
        // particular instruction index.
        //
        return new ProcessorInfo (
            i, new ProcInvocation (processor, procApplyType)
        );
    }

    //

    private static final Method __printlnMethod__ = __getMethod (PrintStream.class, "println", String.class);
    private static final Method __printStackTraceMethod__ = __getMethod (Throwable.class, "printStackTrace");
    private static final Method __exitMethod__ = __getMethod (System.class, "exit", int.class);
    private static final Field __errField__ = __getField (System.class, "err");

    /**
     * Inserts a try-finally block for each snippet and fails immediately if a
     * snippet produces an exception.
     */
    private void __insertExceptionHandler (
        final InsnList insns, final List <TryCatchBlockNode> tcbs
    ) {
        //
        // The inserted code:
        //
        // TRY_BEGIN:       try {
        //                      ... original snippet code ...
        //                      goto HANDLER_END;
        // TRY_END:         } finally (e) {
        // HANDLER_BEGIN:       System.err.println(...);
        //                      e.printStackTrace();
        //                      System.exit(666);
        //                      throw e;
        // HANDLER_END:     }
        //
        // In the finally block, the exception will be at the top of the stack.
        //
        final LabelNode tryBegin = new LabelNode();
        insns.insert (tryBegin);

        final LabelNode handlerEnd = new LabelNode ();
        insns.add (AsmHelper.jumpTo (handlerEnd));

        final LabelNode tryEnd = new LabelNode ();
        insns.add (tryEnd);

        final LabelNode handlerBegin = new LabelNode ();
        insns.add (handlerBegin);

        // System.err.println(...);
        insns.add (AsmHelper.getStatic (__errField__));
        insns.add (AsmHelper.loadConst (String.format (
            "%s: failed to handle an exception", __template.location (tryBegin)
        )));
        insns.add (AsmHelper.invokeVirtual (__printlnMethod__));

        // e.printStackTrace();
        insns.add (new InsnNode (Opcodes.DUP));
        insns.add (AsmHelper.invokeVirtual (__printStackTraceMethod__));

        // System.exit(666)
        insns.add (AsmHelper.loadConst (666));
        insns.add (AsmHelper.invokeStatic (__exitMethod__));

        // Re-throw the exception (just for proper stack frame calculation)
        insns.add (new InsnNode (Opcodes.ATHROW));
        insns.add (handlerEnd);

        // Add the exception handler to the list.
        tcbs.add (new TryCatchBlockNode (tryBegin, tryEnd, handlerBegin, null));
    }

    //

    private static final Method __dbActivate__ = __getMethod (DynamicBypass.class, "activate");
    private static final Method __dbDeactivate__ = __getMethod (DynamicBypass.class, "deactivate");

    private static void __insertDynamicBypassControl (final InsnList insns) {
        //
        // Wraps the given list of instructions with code that controls the
        // dynamic bypass. The bypass is enabled before the first instruction
        // and disabled again after the last instruction:
        //
        //      DynamicBypass.activate();
        //      ... original snippet code ...
        //      DynamicBypass.deactivate();
        //
        insns.insert (AsmHelper.invokeStatic (__dbActivate__));
        insns.add (AsmHelper.invokeStatic (__dbDeactivate__));
    }

    private static final Method __graalInstrumentationBegin__ = __getMethod (GraalDirectives.class, "instrumentationBegin", int.class);
    private static final Method __graalInstrumentationEnd__ = __getMethod (GraalDirectives.class, "instrumentationEnd");


    private static void __insertGraalHints (
        final InsnList insns, final Class <?> annotationClass, final Marker marker) {
        if (Before.class.equals (annotationClass)
            && BytecodeMarker.class.isAssignableFrom (marker.getClass ())) {
            insns.insert (AsmHelper.invokeStatic (__graalInstrumentationBegin__));
            insns.insert (new InsnNode (Opcodes.ICONST_1));
            insns.add (AsmHelper.invokeStatic (__graalInstrumentationEnd__));
        } else {
            insns.insert (AsmHelper.invokeStatic (__graalInstrumentationBegin__));
            insns.insert (new InsnNode (Opcodes.ICONST_M1));
            insns.add (AsmHelper.invokeStatic (__graalInstrumentationEnd__));
        }
    }

    //


    private static Method __getMethod (
        final Class <?> owner, final String name, final Class <?> ... types
    ) {
        try {
            return owner.getMethod (name, types);

        } catch (final NoSuchMethodException e) {
            throw new DiSLFatalException (
                "could not find method %s in class %s", name, owner.getName ()
            );
        }
    }

    private static Field __getField (final Class <?> owner, final String name) {
        try {
            return owner.getField (name);

        } catch (final NoSuchFieldException nsfe) {
            throw new DiSLFatalException (
                "could not find field %s in class %s", name, owner.getName ()
            );
        }
    }

}
