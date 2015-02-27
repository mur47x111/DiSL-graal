package ch.usi.dag.disl.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;


/**
 * Represents a Java bytecode instructions. This enum should be kept in sync
 * with ASM {@link Opcodes}.
 */
public enum Insn {

    //
    // The instructions commented out are not used by ASM.
    //

    NOP (Opcodes.NOP),

    ACONST_NULL (Opcodes.ACONST_NULL),
    ICONST_M1 (Opcodes.ICONST_M1),
    ICONST_0 (Opcodes.ICONST_0),
    ICONST_1 (Opcodes.ICONST_1),
    ICONST_2 (Opcodes.ICONST_2),
    ICONST_3 (Opcodes.ICONST_3),
    ICONST_4 (Opcodes.ICONST_4),
    ICONST_5 (Opcodes.ICONST_5),
    LCONST_0 (Opcodes.LCONST_0),
    LCONST_1 (Opcodes.LCONST_1),
    FCONST_0 (Opcodes.FCONST_0),
    FCONST_1 (Opcodes.FCONST_1),
    FCONST_2 (Opcodes.FCONST_2),
    DCONST_0 (Opcodes.DCONST_0),
    DCONST_1 (Opcodes.DCONST_1),

    BIPUSH (Opcodes.BIPUSH),
    SIPUSH (Opcodes.SIPUSH),

    LDC (Opcodes.LDC),
    // LDC_W (Opcodes.LDC_W),
    // LDC2_W (Opcodes.LDC2_W),

    ILOAD (Opcodes.ILOAD),
    LLOAD (Opcodes.LLOAD),
    FLOAD (Opcodes.FLOAD),
    DLOAD (Opcodes.DLOAD),
    ALOAD (Opcodes.ALOAD),
    // ILOAD_0 (Opcodes.ILOAD_0),
    // ILOAD_1 (Opcodes.ILOAD_1),
    // ILOAD_2 (Opcodes.ILOAD_2),
    // ILOAD_3 (Opcodes.ILOAD_3),
    // LLOAD_0 (Opcodes.LLOAD_0),
    // LLOAD_1 (Opcodes.LLOAD_1),
    // LLOAD_2 (Opcodes.LLOAD_2),
    // LLOAD_3 (Opcodes.LLOAD_3),
    // FLOAD_0 (Opcodes.FLOAD_0),
    // FLOAD_1 (Opcodes.FLOAD_1),
    // FLOAD_2 (Opcodes.FLOAD_2),
    // FLOAD_3 (Opcodes.FLOAD_3),
    // DLOAD_0 (Opcodes.DLOAD_0),
    // DLOAD_1 (Opcodes.DLOAD_1),
    // DLOAD_2 (Opcodes.DLOAD_2),
    // DLOAD_3 (Opcodes.DLOAD_3),
    // ALOAD_0 (Opcodes.ALOAD_0),
    // ALOAD_1 (Opcodes.ALOAD_1),
    // ALOAD_2 (Opcodes.ALOAD_2),
    // ALOAD_3 (Opcodes.ALOAD_3),
    IALOAD (Opcodes.IALOAD),
    LALOAD (Opcodes.LALOAD),
    FALOAD (Opcodes.FALOAD),
    DALOAD (Opcodes.DALOAD),
    AALOAD (Opcodes.AALOAD),
    BALOAD (Opcodes.BALOAD),
    CALOAD (Opcodes.CALOAD),
    SALOAD (Opcodes.SALOAD),

    ISTORE (Opcodes.ISTORE),
    LSTORE (Opcodes.LSTORE),
    FSTORE (Opcodes.FSTORE),
    DSTORE (Opcodes.DSTORE),
    ASTORE (Opcodes.ASTORE),
    // ISTORE_0 (Opcodes.ISTORE_0),
    // ISTORE_1 (Opcodes.ISTORE_1),
    // ISTORE_2 (Opcodes.ISTORE_2),
    // ISTORE_3 (Opcodes.ISTORE_3),
    // LSTORE_0 (Opcodes.LSTORE_0),
    // LSTORE_1 (Opcodes.LSTORE_1),
    // LSTORE_2 (Opcodes.LSTORE_2),
    // LSTORE_3 (Opcodes.LSTORE_3),
    // FSTORE_0 (Opcodes.FSTORE_0),
    // FSTORE_1 (Opcodes.FSTORE_1),
    // FSTORE_2 (Opcodes.FSTORE_2),
    // FSTORE_3 (Opcodes.FSTORE_3),
    // DSTORE_0 (Opcodes.DSTORE_0),
    // DSTORE_1 (Opcodes.DSTORE_1),
    // DSTORE_2 (Opcodes.DSTORE_2),
    // DSTORE_3 (Opcodes.DSTORE_3),
    // ASTORE_0 (Opcodes.ASTORE_0),
    // ASTORE_1 (Opcodes.ASTORE_1),
    // ASTORE_2 (Opcodes.ASTORE_2),
    // ASTORE_3 (Opcodes.ASTORE_3),
    IASTORE (Opcodes.IASTORE),
    LASTORE (Opcodes.LASTORE),
    FASTORE (Opcodes.FASTORE),
    DASTORE (Opcodes.DASTORE),
    AASTORE (Opcodes.AASTORE),
    BASTORE (Opcodes.BASTORE),
    CASTORE (Opcodes.CASTORE),
    SASTORE (Opcodes.SASTORE),

    POP (Opcodes.POP),
    POP2 (Opcodes.POP2),

    DUP (Opcodes.DUP),
    DUP_X1 (Opcodes.DUP_X1),
    DUP_X2 (Opcodes.DUP_X2),
    DUP2 (Opcodes.DUP2),
    DUP2_X1 (Opcodes.DUP2_X1),
    DUP2_X2 (Opcodes.DUP2_X2),
    SWAP (Opcodes.SWAP),

    IADD (Opcodes.IADD),
    LADD (Opcodes.LADD),
    FADD (Opcodes.FADD),
    DADD (Opcodes.DADD),

    ISUB (Opcodes.ISUB),
    LSUB (Opcodes.LSUB),
    FSUB (Opcodes.FSUB),
    DSUB (Opcodes.DSUB),

    IMUL (Opcodes.IMUL),
    LMUL (Opcodes.LMUL),
    FMUL (Opcodes.FMUL),
    DMUL (Opcodes.DMUL),

    IDIV (Opcodes.IDIV),
    LDIV (Opcodes.LDIV),
    FDIV (Opcodes.FDIV),
    DDIV (Opcodes.DDIV),

    IREM (Opcodes.IREM),
    LREM (Opcodes.LREM),
    FREM (Opcodes.FREM),
    DREM (Opcodes.DREM),

    INEG (Opcodes.INEG),
    LNEG (Opcodes.LNEG),
    FNEG (Opcodes.FNEG),
    DNEG (Opcodes.DNEG),

    ISHL (Opcodes.ISHL),
    LSHL (Opcodes.LSHL),

    ISHR (Opcodes.ISHR),
    LSHR (Opcodes.LSHR),

    IUSHR (Opcodes.IUSHR),
    LUSHR (Opcodes.LUSHR),

    IAND (Opcodes.IAND),
    LAND (Opcodes.LAND),

    IOR (Opcodes.IOR),
    LOR (Opcodes.LOR),

    IXOR (Opcodes.IXOR),
    LXOR (Opcodes.LXOR),

    IINC (Opcodes.IINC),

    I2L (Opcodes.I2L),
    I2F (Opcodes.I2F),
    I2D (Opcodes.I2D),

    L2I (Opcodes.L2I),
    L2F (Opcodes.L2F),
    L2D (Opcodes.L2D),

    F2I (Opcodes.F2I),
    F2L (Opcodes.F2L),
    F2D (Opcodes.F2D),

    D2I (Opcodes.D2I),
    D2L (Opcodes.D2L),
    D2F (Opcodes.D2F),

    I2B (Opcodes.I2B),
    I2C (Opcodes.I2C),
    I2S (Opcodes.I2S),

    LCMP (Opcodes.LCMP),
    FCMPL (Opcodes.FCMPL),
    FCMPG (Opcodes.FCMPG),
    DCMPL (Opcodes.DCMPL),
    DCMPG (Opcodes.DCMPG),

    IFEQ (Opcodes.IFEQ),
    IFNE (Opcodes.IFNE),
    IFLT (Opcodes.IFLT),
    IFGE (Opcodes.IFGE),
    IFGT (Opcodes.IFGT),
    IFLE (Opcodes.IFLE),

    IF_ICMPEQ (Opcodes.IF_ICMPEQ),
    IF_ICMPNE (Opcodes.IF_ICMPNE),
    IF_ICMPLT (Opcodes.IF_ICMPLT),
    IF_ICMPGE (Opcodes.IF_ICMPGE),
    IF_ICMPGT (Opcodes.IF_ICMPGT),
    IF_ICMPLE (Opcodes.IF_ICMPLE),
    IF_ACMPEQ (Opcodes.IF_ACMPEQ),
    IF_ACMPNE (Opcodes.IF_ACMPNE),

    GOTO (Opcodes.GOTO),
    JSR (Opcodes.JSR),
    RET (Opcodes.RET),

    TABLESWITCH (Opcodes.TABLESWITCH),
    LOOKUPSWITCH (Opcodes.LOOKUPSWITCH),

    IRETURN (Opcodes.IRETURN),
    LRETURN (Opcodes.LRETURN),
    FRETURN (Opcodes.FRETURN),
    DRETURN (Opcodes.DRETURN),
    ARETURN (Opcodes.ARETURN),
    RETURN (Opcodes.RETURN),

    GETSTATIC (Opcodes.GETSTATIC),
    PUTSTATIC (Opcodes.PUTSTATIC),
    GETFIELD (Opcodes.GETFIELD),
    PUTFIELD (Opcodes.PUTFIELD),

    INVOKEVIRTUAL (Opcodes.INVOKEVIRTUAL),
    INVOKESPECIAL (Opcodes.INVOKESPECIAL),
    INVOKESTATIC (Opcodes.INVOKESTATIC),
    INVOKEINTERFACE (Opcodes.INVOKEINTERFACE),
    INVOKEDYNAMIC (Opcodes.INVOKEDYNAMIC),

    NEW (Opcodes.NEW),
    NEWARRAY (Opcodes.NEWARRAY),
    ANEWARRAY (Opcodes.ANEWARRAY),
    ARRAYLENGTH (Opcodes.ARRAYLENGTH),

    ATHROW (Opcodes.ATHROW),

    CHECKCAST (Opcodes.CHECKCAST),
    INSTANCEOF (Opcodes.INSTANCEOF),

    MONITORENTER (Opcodes.MONITORENTER),
    MONITOREXIT (Opcodes.MONITOREXIT),

    // WIDE (Opcodes.WIDE),

    MULTIANEWARRAY (Opcodes.MULTIANEWARRAY),

    IFNULL (Opcodes.IFNULL),
    IFNONNULL (Opcodes.IFNONNULL),

    // GOTO_W (Opcodes.GOTO_W),
    // JSR_W (Opcodes.JSR_W),
    ;

    private final int __opcode;
    private static final Insn [] __insns;

    static {
        __insns = new Insn [1 << Byte.SIZE];
        for (final Insn insn : Insn.values ()) {
            __insns [insn.opcode ()] = insn;
        }
    }


    private Insn (final int opcode) {
        __opcode = opcode;
    }


    public int opcode () {
        return __opcode;
    }


    public boolean matches (final AbstractInsnNode insn) {
        return (insn != null && this == forNode (insn));
    }

    //

    public static Insn forOpcode (final int opcode) {
        return (0 <= opcode && opcode < __insns.length) ? __insns [opcode] : null;
    }

    public static Insn forNode (final AbstractInsnNode insn) {
        return forOpcode (insn.getOpcode ());
    }

    public static boolean isVirtual (final AbstractInsnNode insn) {
        return (insn.getOpcode() == -1);
    }

}
