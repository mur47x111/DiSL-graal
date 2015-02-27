package ch.usi.dag.disl.coderep;

import org.objectweb.asm.tree.MethodInsnNode;


@SuppressWarnings ("serial")
class InvalidStaticContextInvocationException extends RuntimeException {

    private final MethodInsnNode __insn;

    //

    public InvalidStaticContextInvocationException (
        final String message, final MethodInsnNode insn
    ) {
        super (message);
        __insn = insn;
    }

    public MethodInsnNode getInsn () {
        return __insn;
    }
}
