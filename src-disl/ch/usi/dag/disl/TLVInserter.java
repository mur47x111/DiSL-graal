package ch.usi.dag.disl;

import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import ch.usi.dag.disl.localvar.ThreadLocalVar;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.Constants;

final class TLVInserter extends ClassVisitor {

    private final Set<ThreadLocalVar> threadLocalVars;

    public TLVInserter(ClassVisitor cv, Set<ThreadLocalVar> tlvs) {
        super(Opcodes.ASM4, cv);
        this.threadLocalVars = tlvs;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc,
            String sig, String[] exceptions) {

        // add field initialization
        if (Constants.isConstructorName (name)) {
            return new TLVInitializer(super.visitMethod(access, name, desc,
                    sig, exceptions), access, name, desc);
        }

        return super.visitMethod(access, name, desc, sig, exceptions);
    }

    @Override
    public void visitEnd() {

        // add fields
        for (ThreadLocalVar tlv : threadLocalVars) {
            super.visitField(Opcodes.ACC_PUBLIC, tlv.getName(),
                    tlv.getTypeAsDesc(), null, null);
        }

        super.visitEnd();
    }

    private class TLVInitializer extends AdviceAdapter {

        private TLVInitializer(MethodVisitor mv, int access, String name,
                String desc) {

            super(Opcodes.ASM4, mv, access, name, desc);
        }

        @Override
        protected void onMethodEnter() {

            final String THREAD_CLASS_NAME =
                Type.getType(Thread.class).getInternalName();
            final String CURRENTTHREAD_METHOD_NAME = "currentThread";
            final String CURRENTTHREAD_METHOD_SIG =
                "()L" + THREAD_CLASS_NAME + ";";

            // for each thread local var insert initialization
            for (ThreadLocalVar tlv : threadLocalVars) {

                Label getDefaultValue = new Label();
                Label putValue = new Label();

                // put this on the stack - for putfield
                visitVarInsn(ALOAD, 0);

                // -- inherited value --
                if (tlv.isInheritable()) {

                    // put current thread instance on the stack
                    visitMethodInsn(INVOKESTATIC,
                            THREAD_CLASS_NAME,
                            CURRENTTHREAD_METHOD_NAME,
                            CURRENTTHREAD_METHOD_SIG);

                    // if null, go to "get default value"
                    visitJumpInsn(IFNULL, getDefaultValue);

                    // put current thread instance on the stack
                    visitMethodInsn(INVOKESTATIC,
                            THREAD_CLASS_NAME,
                            CURRENTTHREAD_METHOD_NAME,
                            CURRENTTHREAD_METHOD_SIG);

                    // get value from parent thread ant put it on the stack
                    visitFieldInsn(GETFIELD, THREAD_CLASS_NAME, tlv.getName(),
                            tlv.getTypeAsDesc());

                    // go to "put value"
                    visitJumpInsn(GOTO, putValue);
                }

                // -- default value --
                visitLabel(getDefaultValue);

                // put the default value on the stack
                Object defaultVal = tlv.getDefaultValue();
                if (defaultVal != null) {
                    // default value
                    visitLdcInsn(defaultVal);
                }
                else {

                    // if object or array
                    if(AsmHelper.isReferenceType(tlv.getType())) {
                        // insert null
                        visitInsn(ACONST_NULL);
                    }
                    // if basic type
                    else {
                        // insert 0 as default
                        visitLdcInsn(0);
                    }
                }

                // -- put value to the field --
                visitLabel(putValue);

                visitFieldInsn(PUTFIELD, THREAD_CLASS_NAME, tlv.getName(),
                        tlv.getTypeAsDesc());
            }
        }
    }
}
