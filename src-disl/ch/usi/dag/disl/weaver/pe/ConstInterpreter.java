package ch.usi.dag.disl.weaver.pe;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.analysis.Interpreter;

public class ConstInterpreter extends Interpreter<ConstValue> {

    protected ConstInterpreter() {
        super(Opcodes.ASM4);
    }

    @Override
    public ConstValue newValue(final Type type) {
        if (type == Type.VOID_TYPE) {
            return null;
        }

        return new ConstValue(type == null ? 1 : type.getSize());
    }

    @Override
    public ConstValue newOperation(final AbstractInsnNode insn) {

        switch (insn.getOpcode()) {
        case Opcodes.ACONST_NULL:
            return new ConstValue(1, ConstValue.NULL);
        case Opcodes.ICONST_M1:
            return new ConstValue(1, (Integer) (-1));
        case Opcodes.ICONST_0:
            return new ConstValue(1, (Integer) 0);
        case Opcodes.ICONST_1:
            return new ConstValue(1, (Integer) 1);
        case Opcodes.ICONST_2:
            return new ConstValue(1, (Integer) 2);
        case Opcodes.ICONST_3:
            return new ConstValue(1, (Integer) 3);
        case Opcodes.ICONST_4:
            return new ConstValue(1, (Integer) 4);
        case Opcodes.ICONST_5:
            return new ConstValue(1, (Integer) 5);
        case Opcodes.LCONST_0:
            return new ConstValue(2, new Long(0));
        case Opcodes.LCONST_1:
            return new ConstValue(2, new Long(1));
        case Opcodes.FCONST_0:
            return new ConstValue(1, new Float(0));
        case Opcodes.FCONST_1:
            return new ConstValue(1, new Float(1));
        case Opcodes.FCONST_2:
            return new ConstValue(1, new Float(2));
        case Opcodes.DCONST_0:
            return new ConstValue(2, new Double(0));
        case Opcodes.DCONST_1:
            return new ConstValue(2, new Double(1));
        case Opcodes.BIPUSH:
            return new ConstValue(1, (Integer) (((IntInsnNode) insn).operand));
        case Opcodes.SIPUSH:
            return new ConstValue(1, (Integer) (((IntInsnNode) insn).operand));
        case Opcodes.LDC:
            Object cst = ((LdcInsnNode) insn).cst;
            return new ConstValue(
                    cst instanceof Long || cst instanceof Double ? 2 : 1, cst);
        case Opcodes.GETSTATIC:
            return new ConstValue(Type.getType(((FieldInsnNode) insn).desc)
                    .getSize());
        case Opcodes.NEW:
            return new ConstValue(1, new Reference());

        default:
            return new ConstValue(1);
        }
    }

    @Override
    public ConstValue copyOperation(final AbstractInsnNode insn,
            final ConstValue value) {
        return new ConstValue(value.getSize(), value.cst);
    }

    public static boolean mightBeUnaryConstOperation(final AbstractInsnNode insn) {

        switch (insn.getOpcode()) {
        case Opcodes.INEG:
        case Opcodes.LNEG:
        case Opcodes.FNEG:
        case Opcodes.DNEG:
        case Opcodes.IINC:
        case Opcodes.I2L:
        case Opcodes.I2F:
        case Opcodes.I2D:
        case Opcodes.L2I:
        case Opcodes.L2F:
        case Opcodes.L2D:
        case Opcodes.F2I:
        case Opcodes.F2L:
        case Opcodes.F2D:
        case Opcodes.D2I:
        case Opcodes.D2L:
        case Opcodes.D2F:
        case Opcodes.I2B:
        case Opcodes.I2C:
        case Opcodes.I2S:
        case Opcodes.CHECKCAST:
        case Opcodes.INSTANCEOF:
            return true;
        default:
            return false;
        }
    }

    @Override
    public ConstValue unaryOperation(final AbstractInsnNode insn,
            final ConstValue value) {

        if (value.cst == null) {

            switch (insn.getOpcode()) {
            case Opcodes.LNEG:
            case Opcodes.DNEG:
            case Opcodes.I2L:
            case Opcodes.I2D:
            case Opcodes.L2D:
            case Opcodes.F2L:
            case Opcodes.F2D:
            case Opcodes.D2L:
                return new ConstValue(2);

            case Opcodes.GETFIELD:
                return new ConstValue(Type.getType(((FieldInsnNode) insn).desc)
                        .getSize());

            default:
                return new ConstValue(1);
            }
        }

        switch (insn.getOpcode()) {
        case Opcodes.INEG:
            return new ConstValue(1, (Integer) (-(Integer) value.cst));

        case Opcodes.LNEG:
            return new ConstValue(2, (Long) (-(Long) value.cst));

        case Opcodes.FNEG:
            return new ConstValue(1, (Float) (-(Float) value.cst));

        case Opcodes.DNEG:
            return new ConstValue(2, (Double) (-(Double) value.cst));

        case Opcodes.IINC:
            return new ConstValue(
                    1,
                    (Integer) ((Integer) value.cst + ((IincInsnNode) insn).incr));

        case Opcodes.I2L:
            return new ConstValue(2, (Long) ((long) ((Integer) value.cst)));

        case Opcodes.I2F:
            return new ConstValue(1, (Float) ((float) ((Integer) value.cst)));

        case Opcodes.I2D:
            return new ConstValue(2, (Double) ((double) ((Integer) value.cst)));

        case Opcodes.L2I:
            return new ConstValue(1,
                    (Integer) ((int) (long) ((Long) value.cst)));

        case Opcodes.L2F:
            return new ConstValue(1, (Float) ((float) ((Long) value.cst)));

        case Opcodes.L2D:
            return new ConstValue(2, (Double) ((double) ((Long) value.cst)));

        case Opcodes.F2I:
            return new ConstValue(1,
                    (Integer) ((int) ((float) ((Float) value.cst))));

        case Opcodes.F2L:
            return new ConstValue(2,
                    (Long) ((long) ((float) ((Float) value.cst))));

        case Opcodes.F2D:
            return new ConstValue(2, (Double) (-(Double) value.cst));

        case Opcodes.D2I:
            return new ConstValue(1,
                    (Integer) ((int) (double) ((Double) value.cst)));

        case Opcodes.D2L:
            return new ConstValue(2,
                    (Long) ((long) (double) ((Double) value.cst)));

        case Opcodes.D2F:
            return new ConstValue(1,
                    (Float) ((float) (double) ((Double) value.cst)));

        case Opcodes.I2B:
            return new ConstValue(1,
                    (Byte) ((byte) (int) ((Integer) value.cst)));

        case Opcodes.I2C:
            return new ConstValue(1,
                    (Character) ((char) (int) ((Integer) value.cst)));

        case Opcodes.I2S:
            return new ConstValue(1,
                    (Short) ((short) (int) ((Integer) value.cst)));

        case Opcodes.IFEQ:
            return new ConstValue(1, (Boolean) ((Integer) value.cst == 0));

        case Opcodes.IFNE:
            return new ConstValue(1, (Boolean) ((Integer) value.cst != 0));

        case Opcodes.IFLT:
            return new ConstValue(1, (Boolean) ((Integer) value.cst < 0));

        case Opcodes.IFGE:
            return new ConstValue(1, (Boolean) ((Integer) value.cst >= 0));

        case Opcodes.IFGT:
            return new ConstValue(1, (Boolean) ((Integer) value.cst > 0));

        case Opcodes.IFLE:
            return new ConstValue(1, (Boolean) ((Integer) value.cst <= 0));

        case Opcodes.IFNULL:
            return new ConstValue(1, (Boolean) (value.cst == ConstValue.NULL));

        case Opcodes.IFNONNULL:
            return new ConstValue(1, (Boolean) (value.cst != ConstValue.NULL));

        case Opcodes.CHECKCAST:
            return new ConstValue(1, value.cst);

        case Opcodes.INSTANCEOF:

            Class<? extends Object> clazz = value.cst.getClass();

            while (clazz != null) {

                if (Type.getInternalName(clazz).equals(
                        ((TypeInsnNode) insn).desc)) {
                    return new ConstValue(1, (Integer) 1);
                }

                clazz = clazz.getSuperclass();
            }

            return new ConstValue(1, (Integer) 0);

        default:
            return new ConstValue(1);
        }
    }

    public static boolean mightBeBinaryConstOperation(
            final AbstractInsnNode insn) {

        switch (insn.getOpcode()) {
        case Opcodes.LADD:
        case Opcodes.LSUB:
        case Opcodes.LMUL:
        case Opcodes.LDIV:
        case Opcodes.LREM:
        case Opcodes.LSHL:
        case Opcodes.LSHR:
        case Opcodes.LUSHR:
        case Opcodes.LAND:
        case Opcodes.LOR:
        case Opcodes.LXOR:
        case Opcodes.DADD:
        case Opcodes.DSUB:
        case Opcodes.DMUL:
        case Opcodes.DDIV:
        case Opcodes.DREM:
        case Opcodes.IADD:
        case Opcodes.ISUB:
        case Opcodes.IMUL:
        case Opcodes.IDIV:
        case Opcodes.IREM:
        case Opcodes.ISHL:
        case Opcodes.ISHR:
        case Opcodes.IUSHR:
        case Opcodes.IAND:
        case Opcodes.IOR:
        case Opcodes.IXOR:
        case Opcodes.FADD:
        case Opcodes.FSUB:
        case Opcodes.FMUL:
        case Opcodes.FDIV:
        case Opcodes.FREM:
        case Opcodes.LCMP:
        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
            return true;
        default:
            return false;
        }
    }

    @Override
    public ConstValue binaryOperation(final AbstractInsnNode insn,
            final ConstValue value1, final ConstValue value2) {

        if (value1.cst == null || value2.cst == null) {
            switch (insn.getOpcode()) {
            case Opcodes.LALOAD:
            case Opcodes.DALOAD:
            case Opcodes.LADD:
            case Opcodes.DADD:
            case Opcodes.LSUB:
            case Opcodes.DSUB:
            case Opcodes.LMUL:
            case Opcodes.DMUL:
            case Opcodes.LDIV:
            case Opcodes.DDIV:
            case Opcodes.LREM:
            case Opcodes.DREM:
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                return new ConstValue(2);
            default:
                return new ConstValue(1);
            }
        }

        switch (insn.getOpcode()) {
        case Opcodes.LALOAD:
        case Opcodes.DALOAD:
            return new ConstValue(2);

        case Opcodes.LADD:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst + (Long) value2.cst));

        case Opcodes.LSUB:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst - (Long) value2.cst));

        case Opcodes.LMUL:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst * (Long) value2.cst));

        case Opcodes.LDIV:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst / (Long) value2.cst));

        case Opcodes.LREM:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst % (Long) value2.cst));

        case Opcodes.LSHL:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst << (Integer) value2.cst));

        case Opcodes.LSHR:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst >> (Integer) value2.cst));

        case Opcodes.LUSHR:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst >>> (Integer) value2.cst));

        case Opcodes.LAND:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst & (Long) value2.cst));

        case Opcodes.LOR:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst | (Long) value2.cst));

        case Opcodes.LXOR:
            return new ConstValue(2,
                    (Long) ((Long) value1.cst ^ (Long) value2.cst));

        case Opcodes.DADD:
            return new ConstValue(2,
                    (Double) ((Double) value1.cst + (Double) value2.cst));

        case Opcodes.DSUB:
            return new ConstValue(2,
                    (Double) ((Double) value1.cst - (Double) value2.cst));

        case Opcodes.DMUL:
            return new ConstValue(2,
                    (Double) ((Double) value1.cst * (Double) value2.cst));

        case Opcodes.DDIV:
            return new ConstValue(2,
                    (Double) ((Double) value1.cst / (Double) value2.cst));

        case Opcodes.DREM:
            return new ConstValue(2,
                    (Double) ((Double) value1.cst % (Double) value2.cst));

        case Opcodes.IADD:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst + (Integer) value2.cst));

        case Opcodes.ISUB:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst - (Integer) value2.cst));

        case Opcodes.IMUL:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst * (Integer) value2.cst));

        case Opcodes.IDIV:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst / (Integer) value2.cst));

        case Opcodes.IREM:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst % (Integer) value2.cst));

        case Opcodes.ISHL:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst << (Integer) value2.cst));

        case Opcodes.ISHR:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst >> (Integer) value2.cst));

        case Opcodes.IUSHR:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst >>> (Integer) value2.cst));

        case Opcodes.IAND:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst & (Integer) value2.cst));

        case Opcodes.IOR:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst | (Integer) value2.cst));

        case Opcodes.IXOR:
            return new ConstValue(1,
                    (Integer) ((Integer) value1.cst ^ (Integer) value2.cst));

        case Opcodes.FADD:
            return new ConstValue(1,
                    (Float) ((Float) value1.cst + (Float) value2.cst));

        case Opcodes.FSUB:
            return new ConstValue(1,
                    (Float) ((Float) value1.cst - (Float) value2.cst));

        case Opcodes.FMUL:
            return new ConstValue(1,
                    (Float) ((Float) value1.cst * (Float) value2.cst));

        case Opcodes.FDIV:
            return new ConstValue(1,
                    (Float) ((Float) value1.cst / (Float) value2.cst));

        case Opcodes.FREM:
            return new ConstValue(1,
                    (Float) ((Float) value1.cst % (Float) value2.cst));

        case Opcodes.LCMP:
            if ((Long) value1.cst > (Long) value2.cst) {
                return new ConstValue(1, (Integer) 1);
            } else if ((Long) value1.cst < (Long) value2.cst) {
                return new ConstValue(1, (Integer) (-1));
            } else {
                return new ConstValue(1, (Integer) 0);
            }

        case Opcodes.FCMPL:
        case Opcodes.FCMPG:
            if ((Float) value1.cst > (Float) value2.cst) {
                return new ConstValue(1, (Integer) 1);
            } else if ((Float) value1.cst < (Float) value2.cst) {
                return new ConstValue(1, (Integer) (-1));
            } else {
                return new ConstValue(1, (Integer) 0);
            }

        case Opcodes.DCMPL:
        case Opcodes.DCMPG:
            if ((Double) value1.cst > (Double) value2.cst) {
                return new ConstValue(1, (Integer) 1);
            } else if ((Double) value1.cst < (Double) value2.cst) {
                return new ConstValue(1, (Integer) (-1));
            } else {
                return new ConstValue(1, (Integer) 0);
            }

        case Opcodes.IF_ICMPEQ:
            return new ConstValue(1, value1.cst.equals(value2.cst));

        case Opcodes.IF_ICMPNE:
            return new ConstValue(1, !value1.cst.equals(value2.cst));

        case Opcodes.IF_ICMPLT:
            return new ConstValue(1,
                    (Boolean) ((Integer) value1.cst < (Integer) value2.cst));

        case Opcodes.IF_ICMPGE:
            return new ConstValue(1,
                    (Boolean) ((Integer) value1.cst >= (Integer) value2.cst));

        case Opcodes.IF_ICMPGT:
            return new ConstValue(1,
                    (Boolean) ((Integer) value1.cst > (Integer) value2.cst));

        case Opcodes.IF_ICMPLE:
            return new ConstValue(1,
                    (Boolean) ((Integer) value1.cst <= (Integer) value2.cst));

        case Opcodes.IF_ACMPEQ:
            return new ConstValue(1, (Boolean) (value1.cst == value2.cst));

        case Opcodes.IF_ACMPNE:
            return new ConstValue(1, (Boolean) (value1.cst != value2.cst));

        default:
            return new ConstValue(1);
        }
    }

    @Override
    public ConstValue ternaryOperation(final AbstractInsnNode insn,
            final ConstValue value1, final ConstValue value2,
            final ConstValue value3) {
        return new ConstValue(1);
    }

    @Override
    public ConstValue naryOperation(final AbstractInsnNode insn,
            final List<? extends ConstValue> values) {

        int opcode = insn.getOpcode();

        if (opcode == Opcodes.MULTIANEWARRAY) {
            return new ConstValue(1);
        } else if (opcode == Opcodes.INVOKEDYNAMIC) {
            return new ConstValue(Type.getReturnType(
                    ((InvokeDynamicInsnNode) insn).desc).getSize());
        } else {

            int size = Type.getReturnType(((MethodInsnNode) insn).desc)
                    .getSize();
            Object cst = InvocationInterpreter.getInstance().execute(
                    (MethodInsnNode) insn, values);
            return new ConstValue(size, cst);
        }

    }

    @Override
    public void returnOperation(final AbstractInsnNode insn,
            final ConstValue value, final ConstValue expected) {
    }

    @Override
    public ConstValue merge(final ConstValue d, final ConstValue w) {

        if (d.size == w.size && d.cst != null && d.cst.equals(w.cst)) {
            return d;
        }

        return new ConstValue(Math.min(d.size, w.size));
    }

    private static ConstInterpreter instance;

    public static ConstInterpreter getInstance() {
        if (instance == null) {
            instance = new ConstInterpreter();
        }

        return instance;
    }
}
