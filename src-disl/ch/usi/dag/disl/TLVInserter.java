package ch.usi.dag.disl;

import ch.usi.dag.disl.localvar.ThreadLocalVar;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.Constants;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Set;


final class TLVInserter extends ClassVisitor {

    private final Set <ThreadLocalVar> threadLocalVars;


    public TLVInserter (final ClassVisitor cv, final Set <ThreadLocalVar> tlvs) {
        super (
            Opcodes.ASM5, cv);
        this.threadLocalVars = tlvs;
    }


    @Override
    public MethodVisitor visitMethod (
        final int access, final String name, final String desc,
        final String sig, final String [] exceptions) {

        // add field initialization
        if (Constants.isConstructorName (name)) {
            return new TLVInitializer (super.visitMethod (access, name, desc,
                sig, exceptions), access, name, desc);
        }

        return super.visitMethod (access, name, desc, sig, exceptions);
    }


    @Override
    public void visitEnd () {

        // add fields
        for (final ThreadLocalVar tlv : threadLocalVars) {
            super.visitField (Opcodes.ACC_PUBLIC, tlv.getName (),
                tlv.getTypeAsDesc (), null, null);
        }

        super.visitEnd ();
    }


    private class TLVInitializer extends AdviceAdapter {

        private TLVInitializer (
            final MethodVisitor mv, final int access, final String name,
            final String desc) {

            super (
                Opcodes.ASM5, mv, access, name, desc);
        }


        @Override
        protected void onMethodEnter () {

            final String THREAD_CLASS_NAME = Type.getType (
                Thread.class).getInternalName ();
            final String CURRENTTHREAD_METHOD_NAME = "currentThread";
            final String CURRENTTHREAD_METHOD_SIG = "()L" + THREAD_CLASS_NAME + ";";

            // for each thread local var insert initialization
            for (final ThreadLocalVar tlv : threadLocalVars) {

                final Label getDefaultValue = new Label ();
                final Label putValue = new Label ();

                // put this on the stack - for putfield
                visitVarInsn (ALOAD, 0);

                // -- inherited value --
                if (tlv.isInheritable ()) {

                    // put current thread instance on the stack
                    visitMethodInsn (INVOKESTATIC,
                        THREAD_CLASS_NAME,
                        CURRENTTHREAD_METHOD_NAME,
                        CURRENTTHREAD_METHOD_SIG,
                        false);

                    // if null, go to "get default value"
                    visitJumpInsn (IFNULL, getDefaultValue);

                    // put current thread instance on the stack
                    visitMethodInsn (INVOKESTATIC,
                        THREAD_CLASS_NAME,
                        CURRENTTHREAD_METHOD_NAME,
                        CURRENTTHREAD_METHOD_SIG,
                        false);

                    // get value from parent thread ant put it on the stack
                    visitFieldInsn (GETFIELD, THREAD_CLASS_NAME, tlv.getName (),
                        tlv.getTypeAsDesc ());

                    // go to "put value"
                    visitJumpInsn (GOTO, putValue);
                }

                // -- default value --
                visitLabel (getDefaultValue);

                // put the default value on the stack
                final Object defaultVal = tlv.getDefaultValue ();
                if (defaultVal != null) {
                    // default value
                    visitLdcInsn (defaultVal);

                } else {

                    // if object or array
                    if (AsmHelper.isReferenceType (tlv.getType ())) {
                        // insert null
                        visitInsn (ACONST_NULL);
                    }
                    // if basic type
                    else {
                        final char c = tlv.getTypeAsDesc ().charAt (0);
                        final boolean longOrDouble = c == 'J' || c == 'D';
                        // insert 0 as default
                        if (longOrDouble) {
                            visitLdcInsn (0L);
                        } else {
                            visitLdcInsn (0);
                        }
                    }
                }

                // -- put value to the field --
                visitLabel (putValue);

                visitFieldInsn (PUTFIELD, THREAD_CLASS_NAME, tlv.getName (),
                    tlv.getTypeAsDesc ());
            }
        }
    }
}
