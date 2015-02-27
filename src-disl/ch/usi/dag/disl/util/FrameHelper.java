package ch.usi.dag.disl.util;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicInterpreter;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceInterpreter;
import org.objectweb.asm.tree.analysis.SourceValue;
import org.objectweb.asm.tree.analysis.Value;

import ch.usi.dag.disl.exception.DiSLFatalException;

public class FrameHelper {

    // generate a basic analyzer
    public static Analyzer<BasicValue> getBasicAnalyzer() {
        return new Analyzer<BasicValue>(new BasicInterpreter());
    }

    // calculate the stack size
    public static int getOffset(Frame<BasicValue> frame) {
        int offset = 0;

        for (int i = frame.getStackSize() - 1; i >= 0; i--) {

            BasicValue v = frame.getStack(i);
            offset += v.getSize();
        }

        return offset;
    }

    // generate an instruction list to backup the stack
    public static InsnList enter(Frame<BasicValue> frame, int offset) {

        InsnList ilst = new InsnList();

        for (int i = frame.getStackSize() - 1; i >= 0; i--) {

            BasicValue v = frame.getStack(i);

            ilst.add(new VarInsnNode(v.getType().getOpcode(Opcodes.ISTORE),
                    offset));
            offset += v.getSize();
        }

        return ilst;
    }

    // generate an instruction list to restore the stack
    public static InsnList exit(Frame<BasicValue> frame, int offset) {
        InsnList ilst = new InsnList();
        ilst.add(new LabelNode());

        for (int i = frame.getStackSize() - 1; i >= 0; i--) {

            BasicValue v = frame.getStack(i);

            ilst.insertBefore(ilst.getFirst(), new VarInsnNode(v.getType()
                    .getOpcode(Opcodes.ILOAD), offset));
            offset += v.getSize();
        }

        return ilst;
    }

    // generate a source analyzer
    public static Analyzer<SourceValue> getSourceAnalyzer() {
        return new Analyzer<SourceValue>(new SourceInterpreter());
    }

    public static <T extends Value> T getStack(Frame<T> frame, int depth) {

        int index = 0;

        while (depth > 0) {

            depth -= frame.getStack(frame.getStackSize() - 1 - index).getSize();
            index++;
        }

        return frame.getStack(frame.getStackSize() - 1 - index);
    }

    public static <T extends Value> T getStackByIndex(Frame<T> frame, int index) {
        return frame.getStack(frame.getStackSize() - 1 - index);
    }

    // find out where a stack operand is pushed onto stack, and duplicate the
    // operand and store into a local slot.
    public static int dupStack (
        Frame <SourceValue> frame, MethodNode method,
        int operand, Type type, int slot
    ) {
        SourceValue source = getStackByIndex (frame, operand);
        for (final AbstractInsnNode insn : source.insns) {

            // if the instruction duplicates two-size operand(s), weaver should
            // be careful that the operand might be either 2 one-size operands,
            // or 1 two-size operand.
            switch (insn.getOpcode()) {

            case Opcodes.DUP2:
                if (source.size != 1) {
                    break;
                }

                dupStack (frame, method, operand + 2, type, slot);
                continue;

            case Opcodes.DUP2_X1:
                if (source.size != 1) {
                    break;
                }

                dupStack (frame, method, operand + 3, type, slot);
                continue;

            case Opcodes.DUP2_X2:
                if (source.size != 1) {
                    break;
                }

                SourceValue x2 = getStackByIndex (frame, operand + 2);
                dupStack (frame, method, operand + (4 - x2.size), type, slot);
                continue;

            case Opcodes.SWAP:
                if (operand > 0 &&
                    getStackByIndex (frame, operand - 1).insns.contains (insn)
                ) {
                    // insert 'dup' instruction and then store to a local slot
                    method.instructions.insertBefore (
                        insn, new InsnNode (Opcodes.DUP)
                    );

                    method.instructions.insertBefore (
                        insn, AsmHelper.storeVar (type,  slot)
                    );
                    continue;
                }

            default:
                break;
            }

            // insert 'dup' instruction and then store to a local slot
            method.instructions.insert (insn, AsmHelper.storeVar (type, slot));
            method.instructions.insert (
                insn, new InsnNode (source.size == 2 ? Opcodes.DUP2 : Opcodes.DUP)
            );
        }

        return source.size;
    }

    public static <V extends Value> Frame<V>[] getFrames(Analyzer<V> analyzer,
            String clazz, MethodNode method) {

        try {
            analyzer.analyze(clazz, method);
        } catch (AnalyzerException e) {
            throw new DiSLFatalException("Cause by AnalyzerException : \n"
                    + e.getMessage());
        }

        return analyzer.getFrames();
    }

    public static Frame<BasicValue>[] getBasicFrames(String clazz,
            MethodNode method) {
        return getFrames(getBasicAnalyzer(), clazz, method);
    }

    public static Frame<SourceValue>[] getSourceFrames(String clazz,
            MethodNode method) {
        return getFrames(getSourceAnalyzer(), clazz, method);
    }

    public static <V extends Value> Map<AbstractInsnNode, Frame<V>> createMapping(
            Analyzer<V> analyzer, String clazz, MethodNode method) {

        Map<AbstractInsnNode, Frame<V>> mapping;

        mapping = new HashMap<AbstractInsnNode, Frame<V>>();

        Frame<V>[] frames = getFrames(analyzer, clazz, method);

        for (int i = 0; i < method.instructions.size(); i++) {
            mapping.put(method.instructions.get(i), frames[i]);
        }

        return mapping;
    }

    public static Map<AbstractInsnNode, Frame<BasicValue>> createBasicMapping(
            String clazz, MethodNode method) {
        return createMapping(getBasicAnalyzer(), clazz, method);
    }

    public static Map<AbstractInsnNode, Frame<SourceValue>> createSourceMapping(
            String clazz, MethodNode method) {
        return createMapping(getSourceAnalyzer(), clazz, method);
    }

}
