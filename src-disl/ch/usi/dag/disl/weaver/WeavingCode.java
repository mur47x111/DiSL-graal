package ch.usi.dag.disl.weaver;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import ch.usi.dag.disl.classcontext.ClassContext;
import ch.usi.dag.disl.coderep.Code;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.InvalidContextUsageException;
import ch.usi.dag.disl.processor.generator.PIResolver;
import ch.usi.dag.disl.processor.generator.ProcInstance;
import ch.usi.dag.disl.processor.generator.ProcMethodInstance;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Snippet;
import ch.usi.dag.disl.snippet.SnippetCode;
import ch.usi.dag.disl.staticcontext.StaticContext;
import ch.usi.dag.disl.staticcontext.generator.SCGenerator;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.FrameHelper;
import ch.usi.dag.disl.util.Insn;
import ch.usi.dag.disl.weaver.pe.MaxCalculator;
import ch.usi.dag.disl.weaver.pe.PartialEvaluator;

public class WeavingCode {

    final static String PROP_PE = "disl.parteval";

    private final WeavingInfo info;
    private final MethodNode method;
    private final SnippetCode code;
    private final Snippet snippet;
    private final Shadow shadow;
    private final AbstractInsnNode weavingLoc;

    //

    private final InsnList insnList;
    private final AbstractInsnNode[] insnArray;
    private int maxLocals;

    //

    public WeavingCode(
        final WeavingInfo weavingInfo, final MethodNode method, final SnippetCode src,
        final Snippet snippet, final Shadow shadow, final AbstractInsnNode loc
    ) {
        this.info = weavingInfo;
        this.method = method;
        this.code = src.clone();
        this.snippet = snippet;
        this.shadow = shadow;
        this.weavingLoc = loc;

        this.insnList = code.getInstructions();
        this.insnArray = insnList.toArray();
        this.maxLocals = MaxCalculator.getMaxLocal (insnList, method.desc, method.access);
    }


    /**
     * Replaces instruction sequences representing invocations of methods in
     * {@link StaticContext} classes with precomputed constants.
     */
    private void rewriteStaticContextCalls (
        final SCGenerator staticInfoHolder, final InsnList insns
    ) {
        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode insn : insns.toArray ()) {
            //
            // Look for virtual method invocations on static-context classes.
            //
            if (!Insn.INVOKEVIRTUAL.matches (insn)) {
                continue;
            }

            // TODO LB: If owner implements StaticContext, should not missing info be an error?
            final MethodInsnNode invokeInsn = (MethodInsnNode) insn;
            if (! staticInfoHolder.contains (shadow, invokeInsn.owner, invokeInsn.name)) {
                continue;
            }

            //
            // Lookup the results for the given method.
            // If none is found, return null to the client code.
            //
            final Object staticInfo = staticInfoHolder.get (
                shadow, invokeInsn.owner, invokeInsn.name
            );

            // TODO LB: Why insert into code.getInstructions() instead of insns?
            code.getInstructions ().insert (insn,
                (staticInfo != null) ?
                    AsmHelper.loadConst (staticInfo) :
                    AsmHelper.loadNull ()
            );


            // Remove the invocation sequence.
            __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (invokeInsn), insns);
            insns.remove (invokeInsn);
        }
    }


    private static final String __CLASS_CONTEXT_INTERNAL_NAME__ =
        Type.getInternalName (ClassContext.class);


    /**
     * Replaces instruction sequences representing invocations of methods on the
     * {@link ClassContext} interface with constants.
     */
    private void rewriteClassContextCalls (
        final InsnList insns
    ) throws InvalidContextUsageException {
        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode insn : insns.toArray ()) {
            //
            // Look for invocations on the ClassContext interface.
            //
            final MethodInsnNode invokeInsn = __getInvokeInterfaceInsn (
                insn, __CLASS_CONTEXT_INTERNAL_NAME__
            );

            if (invokeInsn == null) {
                continue;
            }

            //
            // Handle individual methods.
            //
            final String methodName = invokeInsn.name;
            if ("asClass".equals (methodName)) {
                //
                //               ALOAD (ClassContext interface reference)
                //               LDC (String class name)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode classNameInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final String internalName = __expectStringConstLoad (
                    classNameInsn, "ClassContext", methodName, "internalName"
                );

                // TODO Check that the literal is actually an internal name.

                //
                // Convert the literal to a type and replace the LDC of the
                // String literal with LDC of a class literal. Then remove the
                // invocation sequence instructions.
                //
                final Type type = Type.getObjectType (internalName);
                insns.insert (invokeInsn, new LdcInsnNode (type));

                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (classNameInsn), insns);
                insns.remove (classNameInsn);
                insns.remove (invokeInsn);

            } else {
                throw new DiSLFatalException (
                    "%s: unsupported ClassContext method %s()",
                    __location (snippet, invokeInsn), methodName
                );
            }
        }
    }

    //

    private static final int INVALID_SLOT = -1;

    private static final Set <String>
        PRIMITIVE_TYPE_NAMES = AsmHelper.PRIMITIVE_TYPES.keySet ();

    private static final String __DYNAMIC_CONTEXT_INTERNAL_NAME__ =
        Type.getInternalName (DynamicContext.class);

    // Search for an instruction sequence that stands for a request for dynamic
    // information, and replace them with a load instruction.
    // NOTE that if the user requests for the stack value, some store
    // instructions will be inserted to the target method, and new local slot
    // will be used for storing this.
    private void rewriteDynamicContextCalls (
        final boolean throwing, final InsnList insns
    ) throws InvalidContextUsageException {
        Frame <BasicValue> basicFrame = info.getBasicFrame (weavingLoc);
        final Frame <SourceValue> sourceFrame = info.getSourceFrame (weavingLoc);

        // Reserve a local variable slot for the exception being thrown.
        final int exceptionSlot = throwing ? method.maxLocals++ : INVALID_SLOT;

        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode insn : insns.toArray ()) {
            //
            // Look for DynamicContext interface method invocations.
            //
            final MethodInsnNode invokeInsn = __getInvokeInterfaceInsn (
                insn, __DYNAMIC_CONTEXT_INTERNAL_NAME__
            );

            if (invokeInsn == null) {
                continue;
            }

            //
            // Handle individual method invocations.
            //
            // The instructions preceding the INVOKEINTERFACE instruction load
            // arguments on the stack, depending on the invoked method.
            //
            // TRICK: in some situations, the following code will generate a
            // VarInsnNode with a negative local slot. This will be
            // corrected in the fixLocalIndex() method.
            //
            // TODO LB: Is this still true? fixLocalIndex() is called after this method!
            // TODO LB: Split up switch legs into separate handler classes.
            //
            final String methodName = invokeInsn.name;
            final Type methodType = Type.getMethodType (invokeInsn.desc);
            final AbstractInsnNode insnAfterInvoke = invokeInsn.getNext ();

            if ("getThis".equals (methodName)) {
                //
                //               ALOAD (DynamicContext interface reference)
                // invokeInsn => INVOKEINTERFACE
                //

                // Ensure getThis() returns null in static methods.
                final boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;
                insns.insert (invokeInsn,
                    isStatic ? AsmHelper.loadNull () : AsmHelper.loadThis ()
                );

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (invokeInsn), insns);
                insns.remove (invokeInsn);

            } else if ("getException".equals (methodName)) {
                //
                //               ALOAD (DynamicContext interface reference)
                // invokeInsn => INVOKEINTERFACE
                //

                // Ensure getException() returns null outside an exception block.
                insns.insert (invokeInsn,
                    throwing ?
                        AsmHelper.loadObjectVar (exceptionSlot) :
                        AsmHelper.loadNull ()
                );

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (invokeInsn), insns);
                insns.remove (invokeInsn);

            } else if ("getStackValue".equals (methodName)) {
                //
                //               ALOAD (DynamicContext interface reference)
                //               ICONST/BIPUSH/LDC (stack item index)
                //               LDC/GETSTATIC (expected object type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode valueTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode itemIndexInsn = Insns.REVERSE.nextRealInsn (valueTypeInsn);

                final Type expectedType = __expectTypeConstLoad (
                    valueTypeInsn, "DynamicContext", methodName, "type"
                );

                if (basicFrame != null) {
                    final int itemIndex = __expectIntConstLoad (
                        itemIndexInsn, "DynamicContext", methodName, "itemIndex"
                    );

                    final int itemCount = basicFrame.getStackSize();

                    // Prevent accessing slots outside the stack frame.
                    if (itemIndex < 0 || itemCount <= itemIndex) {
                        throw new InvalidContextUsageException (
                            "%s: accessing stack (item %d) outside the stack frame (%d items)",
                            __location (snippet, invokeInsn), itemIndex, itemCount
                        );
                    }

                    // Check that the expected type matches the actual type.
                    final Type actualType = FrameHelper.getStackByIndex (basicFrame, itemIndex).getType ();
                    if (expectedType.getSort () != actualType.getSort ()) {
                        throw new InvalidContextUsageException (
                            "%s: expected %s but found %s when accessing stack item %d",
                            __location (snippet, invokeInsn), expectedType, actualType, itemIndex
                        );
                    }

                    //
                    // Duplicate the desired stack value and store it in a local
                    // variable. Then load it back from there in place of the
                    // context method invocation.
                    //
                    final int varSize = FrameHelper.dupStack (
                        sourceFrame, method, itemIndex, expectedType, method.maxLocals
                    );

                    insns.insertBefore (
                        insnAfterInvoke, AsmHelper.loadVar (expectedType, method.maxLocals)
                    );

                    method.maxLocals += varSize;

                } else {
                    //
                    // Cannot access the stack -- return type-specific default.
                    //
                    // TODO warn user that weaving location is unreachable.
                    insns.insertBefore (insnAfterInvoke, AsmHelper.loadDefault (expectedType));
                }

                __boxIfPrimitiveBefore (expectedType, insnAfterInvoke, insns);

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (itemIndexInsn), insns);
                insns.remove (itemIndexInsn);
                insns.remove (valueTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if ("getMethodArgumentValue".equals (methodName)) {
                //
                //               ALOAD (DynamicContext interface reference)
                //               ICONST/BIPUSH/LDC (argument index)
                //               LDC/GETSTATIC (expected object type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode valueTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode paramIndexInsn = Insns.REVERSE.nextRealInsn (valueTypeInsn);

                if (basicFrame == null) {
                    // TODO warn user that weaving location is unreachable.
                    basicFrame = info.getRetFrame();
                }

                // Prevent accessing invalid arguments.
                final int paramIndex = __expectIntConstLoad (
                    paramIndexInsn, "DynamicContext", methodName, "argumentIndex"
                );

                final int paramCount = Type.getArgumentTypes (method.desc).length;

                if (paramIndex < 0 || paramCount <= paramIndex) {
                    throw new InvalidContextUsageException (
                        "%s: accessing invalid parameter %d (method only has %d)",
                        __location (snippet, invokeInsn), paramIndex, paramCount
                    );
                }

                // Check that the expected type matches the actual argument type.
                final Type expectedType = __expectTypeConstLoad (
                    valueTypeInsn, "DynamicContext", methodName, "type"
                );

                final int paramSlot = AsmHelper.getParameterSlot (method, paramIndex);
                final Type actualType = basicFrame.getLocal (paramSlot).getType ();

                if (expectedType.getSort () != actualType.getSort ()) {
                    throw new InvalidContextUsageException (
                        "%s: expected %s but found %s when accessing method parameter %d",
                        __location (snippet, invokeInsn), expectedType, actualType, paramIndex
                    );
                }

                //
                // Load the argument from a local variable slot. Box primitive
                // values -- we remove unnecessary boxing later.
                //
                insns.insertBefore (insnAfterInvoke, AsmHelper.loadVar (expectedType, paramSlot));
                __boxIfPrimitiveBefore (expectedType, insnAfterInvoke, insns);

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (paramIndexInsn), insns);
                insns.remove (paramIndexInsn);
                insns.remove (valueTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if ("getLocalVariableValue".equals (methodName)) {
                //
                //               ALOAD (DynamicContext interface reference)
                //               ICONST/BIPUSH/LDC (variable slot argument)
                //               LDC/GETSTATIC (expected object type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode valueTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode slotIndexInsn = Insns.REVERSE.nextRealInsn (valueTypeInsn);

                if (basicFrame == null) {
                    // TODO warn user that weaving location is unreachable.
                    basicFrame = info.getRetFrame ();
                }

                // Prevent accessing invalid variable slots.
                final int varSlot = __expectIntConstLoad (
                    slotIndexInsn, "DynamicContext", methodName, "slotIndex"
                );

                final int varSlotCount = basicFrame.getLocals ();

                if (varSlot < 0 || varSlotCount <= varSlot) {
                    throw new InvalidContextUsageException (
                        "%s: accessing invalid variable slot (%d) -- method only has %d slots",
                        __location (snippet, invokeInsn), varSlot, varSlotCount
                    );
                }

                // Check that the expected type matches the actual argument type.
                final Type expectedType = __expectTypeConstLoad (
                    valueTypeInsn, "DynamicContext", methodName, "type"
                );
                final Type actualType = basicFrame.getLocal (varSlot).getType ();

                if (expectedType.getSort () != actualType.getSort ()) {
                    throw new InvalidContextUsageException (
                        "%s: expected %s but found %s when accessing variable slot %d",
                        __location (snippet, invokeInsn), expectedType, actualType, varSlot
                    );
                }

                //
                // Load the variable from a local variable slot. Box primitive
                // values -- we remove unnecessary boxing later.
                //
                insns.insertBefore (insnAfterInvoke, AsmHelper.loadVar (expectedType, varSlot));
                __boxIfPrimitiveBefore (expectedType, insnAfterInvoke, insns);

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (slotIndexInsn), insns);
                insns.remove (slotIndexInsn);
                insns.remove (valueTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if (
                "getInstanceFieldValue".equals (methodName)
                && methodType.getArgumentTypes ().length == 4
            ) {
                //
                //               ALOAD (DynamicContext interface reference)
                //               ALOAD (owner reference)
                //               LDC/GETSTATIC (owner type)
                //               LDC (field name String)
                //               LDC/GETSTATIC (field type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode fieldTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode fieldNameInsn = Insns.REVERSE.nextRealInsn (fieldTypeInsn);
                final AbstractInsnNode ownerTypeInsn = Insns.REVERSE.nextRealInsn (fieldNameInsn);

                final Type fieldType = __expectTypeConstLoad (
                    fieldTypeInsn, "DynamicContext", methodName, "fieldType"
                );

                //
                // Get the field value. Box primitive values (based on
                // the field type, not the expected type) -- we remove
                // unnecessary boxing later.
                //

                final String fieldName = __expectStringConstLoad (
                    fieldNameInsn, "DynamicContext", methodName, "fieldName"
                );

                final Type ownerType = __expectTypeConstLoad (
                    ownerTypeInsn, "DynamicContext", methodName, "ownerType"
                );

                insns.insertBefore (insnAfterInvoke, AsmHelper.getField (ownerType, fieldName, fieldType));
                __boxIfPrimitiveBefore (fieldType, insnAfterInvoke, insns);

                //
                // Remove the invocation sequence.
                //
                // BUT, keep the owner reference load instruction.
                //
                final AbstractInsnNode ownerLoadInsn = Insns.REVERSE.nextRealInsn (ownerTypeInsn);
                final AbstractInsnNode ifaceLoadInsn = Insns.REVERSE.nextRealInsn (ownerLoadInsn);

                __removeInsn  (Insn.ALOAD, ifaceLoadInsn, insns);
                // keep the owner load instruction
                insns.remove (ownerTypeInsn);
                insns.remove (fieldNameInsn);
                insns.remove (fieldTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if (
                "getInstanceFieldValue".equals (methodName)
                && methodType.getArgumentTypes ().length == 5
            ) {
                //
                //               ALOAD (DynamicContext interface reference)
                //               ALOAD (owner reference)
                //               LDC (owner name String)
                //               LDC (field name String)
                //               LDC (field descriptor String)
                //               LDC/GETSTATIC (expected value type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode valueTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode fieldDescInsn = Insns.REVERSE.nextRealInsn (valueTypeInsn);
                final AbstractInsnNode fieldNameInsn = Insns.REVERSE.nextRealInsn (fieldDescInsn);
                final AbstractInsnNode ownerNameInsn = Insns.REVERSE.nextRealInsn (fieldNameInsn);

                // TODO Check that the expected type matches the described field type.
                final Type valueType = __expectTypeConstLoad (
                    valueTypeInsn, "DynamicContext", methodName, "type"
                );

                //
                // Get the field value. Box primitive values (based on
                // the field type, not the expected type) -- we remove
                // unnecessary boxing later.
                //
                final String fieldDesc = __expectStringConstLoad (
                    fieldDescInsn, "DynamicContext", methodName, "fieldDesc"
                );

                final String fieldName = __expectStringConstLoad (
                    fieldNameInsn, "DynamicContext", methodName, "fieldName"
                );

                final String ownerName = __expectStringConstLoad (
                    ownerNameInsn, "DynamicContext", methodName, "ownerName"
                );

                insns.insertBefore (insnAfterInvoke, AsmHelper.getField (ownerName, fieldName, fieldDesc));
                __boxIfPrimitiveBefore (Type.getType (fieldDesc), insnAfterInvoke, insns);

                //
                // Remove the invocation sequence.
                //
                // BUT, keep the owner reference load instruction.
                //
                final AbstractInsnNode ownerLoadInsn = Insns.REVERSE.nextRealInsn (ownerNameInsn);
                final AbstractInsnNode ifaceLoadInsn = Insns.REVERSE.nextRealInsn (ownerLoadInsn);

                __removeInsn  (Insn.ALOAD, ifaceLoadInsn, insns);
                // keep the owner load instruction
                insns.remove (ownerNameInsn);
                insns.remove (fieldNameInsn);
                insns.remove (fieldDescInsn);
                insns.remove (valueTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if (
                "getStaticFieldValue".equals (methodName)
                && methodType.getArgumentTypes ().length == 3
            ) {
                //
                //               ALOAD (interface reference)
                //               LDC/GETSTATIC (owner type)
                //               LDC (field name String)
                //               LDC/GETSTATIC (field type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode fieldTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode fieldNameInsn = Insns.REVERSE.nextRealInsn (fieldTypeInsn);
                final AbstractInsnNode ownerTypeInsn = Insns.REVERSE.nextRealInsn (fieldNameInsn);

                //

                final Type ownerType = __expectTypeConstLoad (
                    ownerTypeInsn, "DynamicContext", methodName, "ownerType"
                );

                final String fieldName = __expectStringConstLoad (
                    fieldNameInsn, "DynamicContext", methodName, "fieldName"
                );

                final Type fieldType = __expectTypeConstLoad (
                    fieldTypeInsn, "DynamicContext", methodName, "fieldType"
                );

                //
                // Get the static field value. Box primitive values (based on
                // the field type, not the expected type) -- we remove
                // unnecessary boxing later.
                //

                insns.insertBefore (insnAfterInvoke, AsmHelper.getStatic (ownerType, fieldName, fieldType));
                __boxIfPrimitiveBefore (fieldType, insnAfterInvoke, insns);

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (ownerTypeInsn), insns);
                insns.remove (ownerTypeInsn);
                insns.remove (fieldNameInsn);
                insns.remove (fieldTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else if (
                "getStaticFieldValue".equals (methodName)
                && methodType.getArgumentTypes ().length == 4
            ) {
                //
                //               ALOAD (interface reference)
                //               LDC (owner name String)
                //               LDC (field name String)
                //               LDC (field descriptor String)
                //               LDC/GETSTATIC (expected value type)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode valueTypeInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final AbstractInsnNode fieldDescInsn = Insns.REVERSE.nextRealInsn (valueTypeInsn);
                final AbstractInsnNode fieldNameInsn = Insns.REVERSE.nextRealInsn (fieldDescInsn);
                final AbstractInsnNode ownerNameInsn = Insns.REVERSE.nextRealInsn (fieldNameInsn);

                // TODO Check that the expected type matches the described field type.
                final Type valueType = __expectTypeConstLoad (
                    valueTypeInsn, "DynamicContext", methodName, "valueType"
                );

                //
                // Get the static field value. Box primitive values (based on
                // the field type, not the expected type) -- we remove
                // unnecessary boxing later.
                //
                final String fieldDesc = __expectStringConstLoad (
                    fieldDescInsn, "DynamicContext", methodName, "fieldDesc"
                );

                final String fieldName = __expectStringConstLoad (
                    fieldNameInsn, "DynamicContext", methodName, "fieldName"
                );

                final String ownerName = __expectStringConstLoad (
                    ownerNameInsn, "DynamicContext", methodName, "ownerName"
                );

                insns.insertBefore (insnAfterInvoke, AsmHelper.getStatic (ownerName, fieldName, fieldDesc));
                __boxIfPrimitiveBefore (Type.getType (fieldDesc), insnAfterInvoke, insns);

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (ownerNameInsn), insns);
                insns.remove (ownerNameInsn);
                insns.remove (fieldNameInsn);
                insns.remove (fieldDescInsn);
                insns.remove (valueTypeInsn);
                insns.remove (invokeInsn);

                __removeIfCheckCast (insnAfterInvoke, insns);

            } else {
                throw new DiSLFatalException (
                    "%s: unsupported DynamicContext method %s%s",
                    __location (snippet, invokeInsn), methodName, methodType
                );
            }
        }


        __removeUnecessaryBoxing (insns);

        //
        // In a throwing context, store the exception being thrown into a local
        // variable at the beginning, and re-throw the exception at the end.
        //
        if (throwing) {
            insns.insert (AsmHelper.storeObjectVar (exceptionSlot));

            insns.add (AsmHelper.loadObjectVar (exceptionSlot));
            insns.add (new InsnNode (Opcodes.ATHROW));
        }
    }


    private void __boxIfPrimitiveBefore (
        final Type type, final AbstractInsnNode location, final InsnList insns
    ) {
        if (!AsmHelper.isReferenceType (type)) {
            insns.insertBefore (location, AsmHelper.boxValueOnStack (type));
        }
    }


    /**
     * Removes CHECKCAST instruction that may be used by the compiler before
     * storing the return value from a method with a generic return type. The
     * instruction leaves its argument on the stack, so there is no need to
     * adjust the stack.
     */
    private static void __removeIfCheckCast (
        final AbstractInsnNode insn, final InsnList insns
    ) {
        final AbstractInsnNode checkCastInsn = Insns.FORWARD.firstRealInsn (insn);
        if (Insn.CHECKCAST.matches (checkCastInsn)) {
            insns.remove (checkCastInsn);
        }
    }


    /**
     * Removes unnecessary boxing and un-boxing operations. These are
     * invocations of the static <i>boxType</i>.valueOf() method immediately
     * followed by an invocation of the virtual <i>primitiveType</i>Value()
     * method, with both invocations having the same owner class, and with the
     * argument type of the valueOf() method matching the return type of the
     * <i>primitiveType</i>Value() method.
     */
    private void __removeUnecessaryBoxing (final InsnList insns) {
        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode head : insns.toArray ()) {
            //
            // Check for INVOKEVIRTUAL (head) following INVOKESTATIC (tail).
            //
            if (!Insn.INVOKEVIRTUAL.matches (head)) {
                continue;
            }

            final AbstractInsnNode tail = Insns.REVERSE.nextRealInsn (head);
            if (!Insn.INVOKESTATIC.matches (tail)) {
                continue;
            }

            //
            // Check method names.
            //
            final MethodInsnNode toValueInsn = (MethodInsnNode) head;
            if (!toValueInsn.name.endsWith ("Value")) {
                continue;
            }

            final MethodInsnNode valueOfInsn = (MethodInsnNode) tail;
            if (!valueOfInsn.name.equals ("valueOf")) {
                continue;
            }

            //
            // Check that the valueOf() invocation is done on a class
            // corresponding to a primitive type and that both methods
            // have the same owner class.
            //
            if (! PRIMITIVE_TYPE_NAMES.contains (valueOfInsn.owner)) {
                continue;
            }

            if (! valueOfInsn.owner.equals (toValueInsn.owner)) {
                continue;
            }

            //
            // Check that the argument of the valueOf() method matches
            // the return type of the <targetType>Value() method.
            //
            final Type valueOfArgType = Type.getArgumentTypes (valueOfInsn.desc) [0];
            final Type toValueRetType = Type.getReturnType (toValueInsn.desc);

            if (! valueOfArgType.equals (toValueRetType)) {
                continue;
            }

            //
            // The match is complete -- remove the pair of invocations.
            //
            insns.remove (valueOfInsn);
            insns.remove (toValueInsn);
        }
    }


    // Fix the stack operand index of each stack-based instruction
    // according to the maximum number of locals in the target method node.
    // NOTE that the field maxLocals of the method node will be automatically
    // updated.
    private int fixLocalIndex (final int currentMax, final InsnList insns) {
        __shiftLocalSlots (currentMax, insns);
        return __calcMaxLocals (currentMax, insns);
    }


    private int __calcMaxLocals (final int initial, final InsnList insns) {
        //
        // Calculates the maximum number of referenced local variable slots.
        // TODO LB: Consider reusing MaxCalculator.
        //
        int result = initial;
        for (final AbstractInsnNode insn : Insns.selectAll (insns)) {
            if (insn instanceof VarInsnNode) {
                final VarInsnNode varInsn = (VarInsnNode) insn;

                switch (varInsn.getOpcode()) {
                case Opcodes.LLOAD:
                case Opcodes.DLOAD:
                case Opcodes.LSTORE:
                case Opcodes.DSTORE:
                    result = Math.max (varInsn.var + 2, result);
                    break;

                default:
                    result = Math.max (varInsn.var + 1, result);
                    break;
                }

            } else if (insn instanceof IincInsnNode) {
                final IincInsnNode iincInsn = (IincInsnNode) insn;

                result = Math.max (iincInsn.var + 1, result);
            }
        }

        return result;
    }


    private void __shiftLocalSlots (final int amount, final InsnList insns) {
        //
        // Shifts all local variable slot references by a specified amount.
        //
        for (final AbstractInsnNode insn : Insns.selectAll (insns)) {
            if (insn instanceof VarInsnNode) {
                final VarInsnNode varInsn = (VarInsnNode) insn;
                varInsn.var += amount;

            } else if (insn instanceof IincInsnNode) {
                final IincInsnNode iincInsn = (IincInsnNode) insn;
                iincInsn.var += amount;
            }
        }
    }

    //

    private static String __ARGUMENT_CONTEXT_INTERNAL_NAME__ =
        Type.getInternalName (ArgumentContext.class);


    /**
     * Replaces instruction sequences invoking the {@link ArgumentContext}
     * interface methods with context-specific constants.
     */
    private void rewriteArgumentContextCalls (
        final int position, final int totalCount, final Type type,
        final InsnList insns
    ) throws InvalidContextUsageException {
        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode insn : insns.toArray ()) {
            //
            // Look for ArgumentContext interface method invocations.
            //
            final MethodInsnNode invokeInsn = __getInvokeInterfaceInsn (
                insn, __ARGUMENT_CONTEXT_INTERNAL_NAME__
            );

            if (invokeInsn == null) {
                continue;
            }

            //
            // Handle individual methods.
            //
            // They are all parameter-less and invoked using the same byte-code
            // sequence:
            //
            //               ALOAD (ArgumentContext interface reference)
            // invokeInsn => INVOKEINTERFACE
            //
            final String methodName = invokeInsn.name;
            if ("getPosition".equals (methodName)) {
                insns.insert (insn, AsmHelper.loadConst (position));

            } else if ("getTotalCount".equals (methodName)) {
                insns.insert (insn, AsmHelper.loadConst (totalCount));

            } else if ("getTypeDescriptor".equals (methodName)) {
                insns.insert (insn, AsmHelper.loadConst (type.toString ()));

            } else {
                throw new DiSLFatalException (
                    "%s: unsupported ArgumentContext method %s()",
                    __location (snippet, invokeInsn), methodName
                );
            }

            // Remove the invocation sequence.
            __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (invokeInsn), insns);
            insns.remove (invokeInsn);
        }
    }


    // combine processors into an instruction list
    // NOTE that these processors are for the current method
    private InsnList procInMethod (
        final ProcInstance processor
    ) throws InvalidContextUsageException {
        final InsnList result = new InsnList();

        for (final ProcMethodInstance processorMethod : processor.getMethods ()) {
            final Code code = processorMethod.getCode().clone();

            final int position = processorMethod.getArgIndex ();
            final int totalCount = processorMethod.getArgsCount ();
            final Type type = processorMethod.getKind ().primaryType ();
            final InsnList insns = code.getInstructions();
            rewriteArgumentContextCalls (position, totalCount, type, insns);

            insns.insert (AsmHelper.storeVar (type, 0));
            __shiftLocalSlots (maxLocals, insns);
            maxLocals = __calcMaxLocals (maxLocals + type.getSize (), insns);
            insns.insert (AsmHelper.loadVar (type,
                AsmHelper.getParameterSlot (method, processorMethod.getArgIndex ()) - method.maxLocals
            ));

            result.add (insns);
            method.tryCatchBlocks.addAll (code.getTryCatchBlocks ());
        }

        return result;
    }

    // combine processors into an instruction list
    // NOTE that these processors are for the callee
    private InsnList procAtCallSite(final ProcInstance processor) throws InvalidContextUsageException {

        final Frame <SourceValue> frame = info.getSourceFrame (weavingLoc);

        final InsnList result = new InsnList();
        for (final ProcMethodInstance pmi : processor.getMethods ()) {
            final Code code = pmi.getCode ().clone ();

            final int index = pmi.getArgIndex ();
            final int total = pmi.getArgsCount ();
            final Type type = pmi.getKind ().primaryType (); // TODO LB: Why not get the type directly from pmi?
            final InsnList insns = code.getInstructions ();
            rewriteArgumentContextCalls (index, total, type, insns);

            // Duplicate call site arguments and store them into local vars.
            final SourceValue source = FrameHelper.getStackByIndex (frame, total - 1 - index);
            for (final AbstractInsnNode argLoadInsn : source.insns) {
                // TRICK: the value has to be set properly because
                // method code will be not adjusted by fixLocalIndex
                method.instructions.insert (argLoadInsn, AsmHelper.storeVar (type, method.maxLocals + maxLocals));
                method.instructions.insert (argLoadInsn, new InsnNode (type.getSize () == 2 ? Opcodes.DUP2 : Opcodes.DUP));
            }

            __shiftLocalSlots (maxLocals, insns);
            maxLocals = __calcMaxLocals (maxLocals + type.getSize(), insns);

            result.add (insns);
            method.tryCatchBlocks.addAll (code.getTryCatchBlocks ());
        }

        return result;
    }

    // rewrite calls to ArgumentProcessorContext.apply()
    private void rewriteArgumentProcessorContextApplications (
        final PIResolver piResolver, final InsnList insns
    ) throws InvalidContextUsageException {
        for (final int insnIndex : code.getInvokedProcessors ().keySet ()) {
            final AbstractInsnNode insn = insnArray [insnIndex];

            final ProcInstance processor = piResolver.get (shadow, insnIndex);
            if (processor != null) {
                if (processor.getMode() == ArgumentProcessorMode.METHOD_ARGS) {
                    insns.insert (insn, procInMethod (processor));
                } else {
                    insns.insert (insn, procAtCallSite (processor));
                }
            }

            // Remove the invocation sequence.
            insns.remove (insn.getPrevious ());
            insns.remove (insn.getPrevious ());
            insns.remove (insn.getPrevious ());
            insns.remove (insn);
        }
    }


    private InsnList __createGetArgsCode (
        final String methodDescriptor, final int firstSlot
    ) {
        final InsnList result = new InsnList ();

        // Allocate a new array for the arguments.
        final Type [] argTypes = Type.getArgumentTypes (methodDescriptor);
        result.add (AsmHelper.loadConst (argTypes.length));
        result.add (new TypeInsnNode (Opcodes.ANEWARRAY, "java/lang/Object"));

        int argSlot = firstSlot;
        for (int argIndex = 0; argIndex < argTypes.length; argIndex++) {
            //
            // Top of the stack contains the array reference.
            // Store method argument into an array:
            //
            // DUP
            // ICONST element index
            //      xLOAD from argument slot
            //      optional: box primitive-type values
            // AASTORE
            //
            result.add (new InsnNode (Opcodes.DUP));
            result.add (AsmHelper.loadConst (argIndex));

                final Type argType = argTypes [argIndex];
                result.add (AsmHelper.loadVar (argType, argSlot));
                if (!AsmHelper.isReferenceType (argType)) {
                    result.add (AsmHelper.boxValueOnStack (argType));
                }

            result.add (new InsnNode (Opcodes.AASTORE));

            // advance argument slot according to argument size
            argSlot += argType.getSize ();
        }

        return result;
    }

    //

    private static final String __ARGUMENT_PROCESSOR_CONTEXT_INTERNAL_NAME__ =
        Type.getInternalName (ArgumentProcessorContext.class);

    private void rewriteArgumentProcessorContextCalls (
        final InsnList insns
    ) throws InvalidContextUsageException {
        // Iterate over a copy -- we will be modifying the underlying list.
        for (final AbstractInsnNode insn : insns.toArray ()) {
            //
            // Look for ArgumentProcessorContext interface method invocations.
            //
            final MethodInsnNode invokeInsn = __getInvokeInterfaceInsn (
                insn, __ARGUMENT_PROCESSOR_CONTEXT_INTERNAL_NAME__
            );

            if (invokeInsn == null) {
                continue;
            }

            //
            // Handle individual methods.
            //
            final String methodName = invokeInsn.name;
            final boolean isStatic = (method.access & Opcodes.ACC_STATIC) != 0;

            if ("getArgs".equals (methodName)) {
                //
                //               ALOAD (ArgumentProcessorContext reference)
                //               GETSTATIC (ArgumentProcessorMode enum reference)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode enumLoadInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final ArgumentProcessorMode mode = __expectEnumConstLoad (
                    ArgumentProcessorMode.class, enumLoadInsn,
                    "ArgumentProcessorContext", methodName, "mode"
                );

                InsnList getArgsInsns = null;
                if (mode == ArgumentProcessorMode.METHOD_ARGS) {
                    final int thisOffset = isStatic ? 0 : 1;
                    final int firstSlot = thisOffset - method.maxLocals;
                    getArgsInsns = __createGetArgsCode (method.desc, firstSlot);

                } else if (mode == ArgumentProcessorMode.CALLSITE_ARGS) {
                    final AbstractInsnNode calleeInsn = Insns.FORWARD.firstRealInsn (shadow.getRegionStart());
                    if (!(calleeInsn instanceof MethodInsnNode)) {
                        throw new DiSLFatalException (
                            "%s: unexpected bytecode at call site in %s.%s() "+
                            "when applying ArgumentProcessorContext.getArgs() ",
                            __location (snippet, invokeInsn),
                            AsmHelper.internalToStdName (shadow.getClassNode ().name),
                            shadow.getMethodNode ().name
                        );
                    }

                    final String calleeDesc = ((MethodInsnNode) calleeInsn).desc;
                    final Type [] argTypes = Type.getArgumentTypes (calleeDesc);

                    final Frame <SourceValue> frame = info.getSourceFrame (calleeInsn);
                    if (frame == null) {
                        throw new DiSLFatalException (
                            "%s: failed to obtain source frame at call site in %s.%s() "+
                            "when applying ArgumentProcessorContext.getArgs() ",
                            __location (snippet, invokeInsn),
                            AsmHelper.internalToStdName (shadow.getClassNode ().name),
                            shadow.getMethodNode ().name
                        );
                    }

                    int argSlot = 0;
                    for (int argIndex = 0; argIndex < argTypes.length; argIndex++) {
                        final SourceValue source = FrameHelper.getStackByIndex (frame, argTypes.length - 1 - argIndex);
                        final Type argType = argTypes [argIndex];

                        for (final AbstractInsnNode itr : source.insns) {
                            // TRICK: the value has to be set properly because
                            // method code will be not adjusted by fixLocalIndex
                            method.instructions.insert (itr, AsmHelper.storeVar (argType, method.maxLocals + maxLocals + argSlot));
                            method.instructions.insert (itr, new InsnNode (argType.getSize() == 2 ? Opcodes.DUP2 : Opcodes.DUP));
                        }

                        argSlot += argType.getSize();
                    }

                    getArgsInsns = __createGetArgsCode (calleeDesc, maxLocals);
                    maxLocals = __calcMaxLocals (maxLocals + argSlot, getArgsInsns);

                } else {
                    throw new DiSLFatalException (
                        "%s: unsupported argument processor mode: %s",
                        __location (snippet, invokeInsn), mode
                    );
                }

                //
                // Insert getArgs() code and remove the invocation sequence.
                //
                insns.insert (invokeInsn, getArgsInsns);

                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (enumLoadInsn), insns);
                insns.remove (enumLoadInsn);
                insns.remove (invokeInsn);

            } else if ("getReceiver".equals (methodName)) {
                //
                //               ALOAD (ArgumentProcessorContext reference)
                //               GETSTATIC (ArgumentProcessorMode enum reference)
                // invokeInsn => INVOKEINTERFACE
                //
                final AbstractInsnNode enumLoadInsn = Insns.REVERSE.nextRealInsn (invokeInsn);
                final ArgumentProcessorMode mode = __expectEnumConstLoad (
                    ArgumentProcessorMode.class, enumLoadInsn,
                    "ArgumentProcessorContext", methodName, "mode"
                );

                if (mode == ArgumentProcessorMode.METHOD_ARGS) {
                    // Return null as receiver for static methods.
                    insns.insert (invokeInsn,
                        isStatic ? AsmHelper.loadNull ()
                        : AsmHelper.loadObjectVar (-method.maxLocals)
                    );

                } else if (mode == ArgumentProcessorMode.CALLSITE_ARGS) {
                    final AbstractInsnNode callee = Insns.FORWARD.firstRealInsn (shadow.getRegionStart());

                    if (!(callee instanceof MethodInsnNode)) {
                        throw new DiSLFatalException("In snippet "
                                + snippet.getOriginClassName() + "."
                                + snippet.getOriginMethodName()
                                + " - unexpected bytecode when applying"
                                + " \"ArgumentProcessorContext.getReceiver\"");
                    }

                    final Frame<SourceValue> frame = info.getSourceFrame(callee);

                    if (frame == null) {
                        throw new DiSLFatalException("In snippet "
                                + snippet.getOriginClassName() + "."
                                + snippet.getOriginMethodName()
                                + " - unexpected bytecode when applying"
                                + " \"ArgumentProcessorContext.getReceiver\"");
                    }

                    if (Insn.INVOKESTATIC.matches (callee)) {
                        // Return null receiver for static method invocations.
                        insns.insert (insn, AsmHelper.loadNull ());

                    } else {
                        final String desc = ((MethodInsnNode) callee).desc;
                        final SourceValue source = FrameHelper.getStackByIndex (
                            frame, Type.getArgumentTypes (desc).length
                        );

                        for (final AbstractInsnNode itr : source.insns) {
                            // TRICK: the slot has to be set properly because
                            // method code will be not adjusted by fixLocalIndex
                            method.instructions.insert (itr, AsmHelper.storeObjectVar (method.maxLocals + maxLocals));
                            method.instructions.insert (itr, new InsnNode (Opcodes.DUP));
                        }

                        insns.insert (insn, AsmHelper.loadObjectVar (maxLocals));
                        maxLocals++;
                    }

                } else {
                    throw new DiSLFatalException ("unsupported mode: %s", mode);
                }

                // Remove the invocation sequence.
                __removeInsn (Insn.ALOAD, Insns.REVERSE.nextRealInsn (enumLoadInsn), insns);
                insns.remove (enumLoadInsn);
                insns.remove (invokeInsn);

            } else {
                throw new DiSLFatalException (
                    "%s: unsupported ArgumentProcessorContext method %s()",
                    __location (snippet, invokeInsn), methodName
                );
            }
        }
    }


    public InsnList getiList () {
        return insnList;
    }


    public List <TryCatchBlockNode> getTCBs () {
        return code.getTryCatchBlocks ();
    }


    public void transform (
        final SCGenerator staticInfo, final PIResolver piResolver, final boolean throwing
    ) throws InvalidContextUsageException {
        // TODO LB: Document rewriter ordering and make dependency explicit!
        // TODO LB: Consider augmenting exceptions with location information here.

        rewriteArgumentProcessorContextApplications (piResolver, insnList);
        rewriteArgumentProcessorContextCalls (insnList);
        rewriteStaticContextCalls (staticInfo, insnList);
        rewriteClassContextCalls (insnList);

        method.maxLocals = fixLocalIndex (method.maxLocals, insnList);
        optimize (insnList);

        rewriteDynamicContextCalls (throwing, insnList);
    }


    private void optimize (final InsnList insns) {
        final String peConfig = System.getProperty (PROP_PE, "");
        if ((peConfig.length() < 2) || (!peConfig.toUpperCase ().startsWith ("O"))) {
            return;
        }

        final char option = peConfig.charAt (1);
        final PartialEvaluator pe = new PartialEvaluator (
            insns, code.getTryCatchBlocks(), method.desc, method.access
        );

        if (option >= '1' && option <= '3') {
            OPTIMIZE: for (int i = 0; i < (option - '0'); i++) {
                if (!pe.evaluate ()) {
                    break OPTIMIZE;
                }
            }

        } else if (option == 'x') {
            // Optimize until no changes have been made.
            OPTIMIZE: while (true) {
                if (!pe.evaluate ()) {
                    break OPTIMIZE;
                }
            }
        }
    }

    //

    private <T extends Enum <T>> T __expectEnumConstLoad (
        final Class <T> enumType, final AbstractInsnNode insn,
        final String iface, final String method, final String param
    ) throws InvalidContextUsageException {
        final T result = AsmHelper.getEnumConstOperand (enumType, insn);
        __ensureOperandNotNull (result, insn, iface, method, param, enumType.getSimpleName ());
        return result;
    }

    private int __expectIntConstLoad (
        final AbstractInsnNode insn,
        final String iface, final String method, final String param
    ) throws InvalidContextUsageException {
        final Integer result = AsmHelper.getIntConstOperand (insn);
        __ensureOperandNotNull (result, insn, iface, method, param, "integer");
        return result;
    }


    private String __expectStringConstLoad (
        final AbstractInsnNode insn,
        final String iface, final String method, final String param
    ) throws InvalidContextUsageException{
        final String result = AsmHelper.getStringConstOperand (insn);
        __ensureOperandNotNull (result, insn, iface, method, param, "String");
        return result;
    }


    private Type __expectTypeConstLoad (
        final AbstractInsnNode insn,
        final String iface, final String method, final String param
    ) throws InvalidContextUsageException {
        final Type result = AsmHelper.getTypeConstOperand (insn);
        __ensureOperandNotNull (result, insn, iface, method, param, "Class");
        return result;
    }


    private void __ensureOperandNotNull (
        final Object operand, final AbstractInsnNode insn, final String iface,
        final String method, final String param, final String kind
    ) throws InvalidContextUsageException {
        if (operand == null) {
            throw new InvalidContextUsageException (
                "%s: the '%s' argument of %s.%s() MUST be a %s literal",
                __location (snippet, insn), param, iface, method, kind
            );
        }
    }

    //

    /**
     * Returns a {@link MethodInsnNode} instance if the given instruction
     * invokes the specified interface, {@code null} otherwise. The interface
     * name needs to be in JVM internal representation.
     */
    private static MethodInsnNode __getInvokeInterfaceInsn (
        final AbstractInsnNode insn, final String internalIfName
    ) {
        if (!Insn.INVOKEINTERFACE.matches (insn)) {
            return null;
        }

        final MethodInsnNode invokeInsn = (MethodInsnNode) insn;
        if (!internalIfName.equals (invokeInsn.owner)) {
            return null;
        }

        return invokeInsn;
    }


    /**
     * Removes a matching instruction from the instruction list, otherwise
     * throws a {@link DiSLFatalException}.
     */
    private static void __removeInsn (
        final Insn expectedInsn, final AbstractInsnNode insn, final InsnList insns
    ) {
        final Insn actualInsn = Insn.forNode (insn);
        if (actualInsn == expectedInsn) {
            insns.remove (insn);

        } else {
            throw new DiSLFatalException (
                "refusing to remove instruction: expected %s, found %s",
                expectedInsn, actualInsn
            );
        }
    }

    private static String __location (
        final Snippet snippet, final AbstractInsnNode insn
    ) {
        return String.format (
            "snippet %s.%s%s",
            snippet.getOriginClassName(), snippet.getOriginMethodName(),
            AsmHelper.formatLineNo (" at line %d ", insn)
        );
    }
}
