package ch.usi.dag.disl.weaver.pe;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.cfg.BasicBlock;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

public class MaxCalculator {

    // From org.objectweb.asm.Frame
    /**
     * The stack size variation corresponding to each JVM instruction. This
     * stack variation is equal to the size of the values produced by an
     * instruction, minus the size of the values consumed by this instruction.
     */
    static final int[] SIZE;

    /**
     * Computes the stack size variation corresponding to each JVM instruction.
     */
    static {
        int i;
        int[] b = new int[202];
        String s = "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDD"
                + "CDCDEEEEEEEEEEEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCD"
                + "CDCEEEEDDDDDDDCDCDCEFEFDDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFED"
                + "DDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
        for (i = 0; i < b.length; ++i) {
            b[i] = s.charAt(i) - 'E';
        }
        SIZE = b;
    }

    public static int getMaxLocal(InsnList ilist, String desc, int access) {

        int maxLocals = (Type.getArgumentsAndReturnSizes(desc) >> 2);

        if ((access & Opcodes.ACC_STATIC) != 0) {
            --maxLocals;
        }

        for (AbstractInsnNode instr : Insns.selectAll (ilist)) {

            if (instr instanceof VarInsnNode) {

                VarInsnNode varInstr = (VarInsnNode) instr;

                switch (varInstr.getOpcode()) {
                case Opcodes.LLOAD:
                case Opcodes.DLOAD:
                case Opcodes.LSTORE:
                case Opcodes.DSTORE:
                    maxLocals = Math.max(varInstr.var + 2, maxLocals);
                    break;

                default:
                    maxLocals = Math.max(varInstr.var + 1, maxLocals);
                    break;
                }
            } else if (instr instanceof IincInsnNode) {

                IincInsnNode iinc = (IincInsnNode) instr;
                maxLocals = Math.max(iinc.var + 1, maxLocals);
            }
        }

        return maxLocals;
    }

    private static int fieldSize(String desc) {
        char c = desc.charAt(0);

        if (c == 'D' || c == 'J') {
            return 2;
        }

        return 1;
    }

    private static int execute(int currentStackSize, AbstractInsnNode instr) {

        int opcode = instr.getOpcode();

        switch (opcode) {
        case -1:
            return currentStackSize;

        case Opcodes.GETSTATIC:
            return currentStackSize + fieldSize(((FieldInsnNode) instr).desc);

        case Opcodes.PUTSTATIC:
            return currentStackSize - fieldSize(((FieldInsnNode) instr).desc);

        case Opcodes.GETFIELD:
            return currentStackSize + fieldSize(((FieldInsnNode) instr).desc)
                    - 1;

        case Opcodes.PUTFIELD:
            return currentStackSize - fieldSize(((FieldInsnNode) instr).desc)
                    - 1;

        case Opcodes.MULTIANEWARRAY:
            return currentStackSize + 1 - ((MultiANewArrayInsnNode) instr).dims;

        case Opcodes.INVOKEVIRTUAL:
        case Opcodes.INVOKESPECIAL:
        case Opcodes.INVOKEINTERFACE: {
            int argSize = Type
                    .getArgumentsAndReturnSizes(((MethodInsnNode) instr).desc);
            return currentStackSize - (argSize >> 2) + (argSize & 0x03);
        }

        case Opcodes.INVOKESTATIC: {
            int argSize = Type
                    .getArgumentsAndReturnSizes(((MethodInsnNode) instr).desc);
            return currentStackSize - (argSize >> 2) + (argSize & 0x03) + 1;
        }

        case Opcodes.INVOKEDYNAMIC: {
            int argSize = Type
                    .getArgumentsAndReturnSizes(((InvokeDynamicInsnNode) instr).desc);
            return currentStackSize - (argSize >> 2) + (argSize & 0x03) + 1;
        }

        default:
            return currentStackSize + SIZE[opcode];
        }
    }

    private static int getMaxStack(int currentStackSize, BasicBlock bb,
            List<BasicBlock> unvisited) {

        if (!unvisited.remove(bb)) {
            return 0;
        }

        int maxStack = currentStackSize;

        for (AbstractInsnNode iter : bb) {

            currentStackSize = execute(currentStackSize, iter);
            maxStack = Math.max(currentStackSize, maxStack);
        }

        for (BasicBlock next : bb.getSuccessors()) {
            maxStack = Math.max(getMaxStack(currentStackSize, next, unvisited),
                    maxStack);
        }

        return maxStack;
    }

    public static int getMaxStack(InsnList ilist,
            List<TryCatchBlockNode> tryCatchBlocks) {

        CtrlFlowGraph cfg = CtrlFlowGraph.build(ilist, tryCatchBlocks);
        List<BasicBlock> unvisited = cfg.getNodes();

        int maxStack = getMaxStack(0, cfg.getBB(ilist.getFirst()), unvisited);

        for (TryCatchBlockNode tcb : tryCatchBlocks) {
            maxStack = Math
                    .max(getMaxStack(1, cfg.getBB(tcb.handler), unvisited),
                            maxStack);
        }

        return maxStack;
    }

    public static int getMaxLocal(MethodNode method) {
        return getMaxLocal(method.instructions, method.desc, method.access);
    }

    public static int getMaxStack(MethodNode method) {
        return getMaxStack(method.instructions, method.tryCatchBlocks);
    }

}
