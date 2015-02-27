package ch.usi.dag.disl.weaver.pe;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.Frame;
import org.objectweb.asm.tree.analysis.SourceValue;

import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.FrameHelper;
import ch.usi.dag.disl.util.cfg.BasicBlock;
import ch.usi.dag.disl.util.cfg.CtrlFlowGraph;

public class PartialEvaluator {

    private MethodNode method;
    private InsnList ilist;

    public PartialEvaluator(InsnList instructions,
            List<TryCatchBlockNode> tryCatchBlocks, String desc, int access) {

        ilist = instructions;

        method = new MethodNode();

        method.instructions = ilist;
        method.tryCatchBlocks = tryCatchBlocks;
        method.access = access;
        method.desc = desc.substring(0, desc.lastIndexOf(')')) + ")V";
        method.maxLocals = MaxCalculator.getMaxLocal(ilist, desc, access);
        method.maxStack = MaxCalculator.getMaxStack(ilist, tryCatchBlocks);
    }

    private boolean removeUnusedBB(CtrlFlowGraph cfg) {

        boolean isOptimized = false;
        boolean changed = true;
        List<BasicBlock> connected = new LinkedList<BasicBlock>(cfg.getNodes());

        connected.remove(cfg.getBB(ilist.getFirst()));

        for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
            connected.remove(cfg.getBB(tcb.handler));
        }

        while (changed) {

            changed = false;
            List<BasicBlock> removed = new LinkedList<BasicBlock>();

            for (BasicBlock bb : connected) {

                if (bb.getPredecessors().size() > 0) {
                    continue;
                }

                changed = true;
                AbstractInsnNode prev = null;
                AbstractInsnNode iter = bb.getEntrance();

                while (prev != bb.getExit()) {
                    prev = iter;
                    iter = iter.getNext();

                    int opcode = prev.getOpcode();

                    if (opcode != -1 || opcode != Opcodes.RETURN) {
                        isOptimized = true;
                        ilist.remove(prev);
                    }
                }

                for (BasicBlock successor : bb.getSuccessors()) {
                    successor.getPredecessors().remove(bb);
                }

                removed.add(bb);
            }

            connected.removeAll(removed);
        }

        return isOptimized;
    }

    private boolean conditionalReduction(
            Map<AbstractInsnNode, Frame<ConstValue>> frames) {

        boolean isOptimized = false;
        CtrlFlowGraph cfg = CtrlFlowGraph.build(method);

        for (BasicBlock bb : cfg.getNodes()) {

            AbstractInsnNode instr = Insns.REVERSE.firstRealInsn (bb.getExit());
            int opcode = instr.getOpcode();
            Frame<ConstValue> frame = frames.get(instr);

            switch (instr.getType()) {
            case AbstractInsnNode.JUMP_INSN: {

                ConstValue result = null;
                boolean popTwice = false;

                switch (opcode) {
                case Opcodes.JSR:
                case Opcodes.GOTO:
                    continue;

                case Opcodes.IF_ICMPEQ:
                case Opcodes.IF_ICMPNE:
                case Opcodes.IF_ICMPLT:
                case Opcodes.IF_ICMPGE:
                case Opcodes.IF_ICMPGT:
                case Opcodes.IF_ICMPLE:
                case Opcodes.IF_ACMPEQ:
                case Opcodes.IF_ACMPNE: {

                    ConstValue value1 = FrameHelper.getStackByIndex(frame, 1);
                    ConstValue value2 = FrameHelper.getStackByIndex(frame, 0);
                    result = ConstInterpreter.getInstance().binaryOperation(
                            instr, value1, value2);
                    popTwice = true;
                    break;
                }

                default: {

                    ConstValue value = FrameHelper.getStackByIndex(frame, 0);
                    result = ConstInterpreter.getInstance().unaryOperation(
                            instr, value);
                    break;
                }
                }

                if (result.cst == null) {
                    continue;
                }

                if ((Boolean) result.cst) {

                    BasicBlock successor = cfg.getBB(instr.getNext());
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);

                    if (popTwice) {
                        ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                    }

                    ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                    ilist.insertBefore(instr, new JumpInsnNode(Opcodes.GOTO,
                            ((JumpInsnNode) instr).label));
                    bb.setExit(instr.getPrevious());
                    ilist.remove(instr);
                } else {

                    BasicBlock successor = cfg
                            .getBB(((JumpInsnNode) instr).label);
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);

                    if (popTwice) {
                        ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                    }

                    ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                    bb.setExit(instr.getPrevious());
                    ilist.remove(instr);
                }

                isOptimized = true;
                break;
            }

            case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                // Covers LOOKUPSWITCH
                LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) instr;

                ConstValue value = FrameHelper.getStackByIndex(frame, 0);

                if (value.cst == null) {
                    continue;
                }

                int index = lsin.keys.indexOf(value.cst);
                LabelNode label = null;

                if (index >= 0) {

                    BasicBlock successor = cfg.getBB(lsin.dflt);
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);
                } else {
                    label = lsin.dflt;
                }

                for (int i = 0; i < lsin.labels.size(); i++) {

                    if (i == index) {
                        label = lsin.labels.get(i);
                        continue;
                    }

                    BasicBlock successor = cfg.getBB(lsin.labels.get(i));
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);
                }

                ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                ilist.insertBefore(instr, new JumpInsnNode(Opcodes.GOTO, label));
                bb.setExit(instr.getPrevious());
                ilist.remove(instr);
                isOptimized = true;
                break;
            }

            case AbstractInsnNode.TABLESWITCH_INSN: {
                // Covers TABLESWITCH
                TableSwitchInsnNode tsin = (TableSwitchInsnNode) instr;

                ConstValue value = FrameHelper.getStackByIndex(frame, 0);

                if (value.cst == null) {
                    continue;
                }

                int index = (Integer) value.cst;
                LabelNode label = null;

                if (index < tsin.min && index > tsin.max) {

                    BasicBlock successor = cfg.getBB(tsin.dflt);
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);
                } else {
                    label = tsin.dflt;
                }

                for (int i = tsin.min; i <= tsin.max; i++) {

                    if (i == index) {
                        label = tsin.labels.get(i - tsin.min);
                        continue;
                    }

                    BasicBlock successor = cfg.getBB(tsin.labels.get(i
                            - tsin.min));
                    bb.getSuccessors().remove(successor);
                    successor.getPredecessors().remove(bb);
                }

                ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                ilist.insertBefore(instr, new JumpInsnNode(Opcodes.GOTO, label));
                bb.setExit(instr.getPrevious());
                ilist.remove(instr);
                isOptimized = true;
                break;
            }

            default:
                break;
            }
        }

        return removeUnusedBB(cfg) | isOptimized;
    }

    private boolean insertLoadConstant(InsnList ilist,
            AbstractInsnNode location, Object cst) {

        if (cst == null) {
            return false;
        }

        if (cst == ConstValue.NULL) {
            ilist.insertBefore(location, new InsnNode(Opcodes.ACONST_NULL));
            return true;
        }

        ilist.insertBefore(location, AsmHelper.loadConst(cst));
        return true;
    }

    private boolean replaceLoadWithLDC(
            Map<AbstractInsnNode, Frame<ConstValue>> frames) {

        boolean isOptimized = false;

        // TODO LB: iterate over a copy unless we are sure an iterator is OK
        for (AbstractInsnNode instr : ilist.toArray()) {

            Frame<ConstValue> frame = frames.get(instr);

            if (frame == null) {
                continue;
            }

            if (ConstInterpreter.mightBeUnaryConstOperation(instr)) {

                ConstValue value = FrameHelper.getStackByIndex(frame, 0);
                Object cst = ConstInterpreter.getInstance().unaryOperation(
                        instr, value).cst;

                if (insertLoadConstant(ilist, instr, cst)) {

                    ilist.insertBefore(instr.getPrevious(), new InsnNode(
                            value.size == 1 ? Opcodes.POP : Opcodes.POP2));
                    ilist.remove(instr);
                    isOptimized = true;
                }

                continue;
            } else if (ConstInterpreter.mightBeBinaryConstOperation(instr)) {

                ConstValue value1 = FrameHelper.getStackByIndex(frame, 1);
                ConstValue value2 = FrameHelper.getStackByIndex(frame, 0);
                Object cst = ConstInterpreter.getInstance().binaryOperation(
                        instr, value1, value2).cst;

                if (insertLoadConstant(ilist, instr, cst)) {

                    ilist.insertBefore(instr.getPrevious(), new InsnNode(
                            value2.size == 1 ? Opcodes.POP : Opcodes.POP2));
                    ilist.insertBefore(instr.getPrevious(), new InsnNode(
                            value1.size == 1 ? Opcodes.POP : Opcodes.POP2));
                    ilist.remove(instr);
                    isOptimized = true;
                }

                continue;
            }

            switch (instr.getOpcode()) {
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                if (insertLoadConstant(ilist, instr,
                        frame.getLocal(((VarInsnNode) instr).var).cst)) {
                    ilist.remove(instr);
                    isOptimized = true;
                }

                break;

            default:
                break;
            }
        }

        return isOptimized;
    }

    private boolean loadAfterStore(BasicBlock bb, AbstractInsnNode instr,
            int var) {

        AbstractInsnNode prev = instr.getPrevious();

        while (prev != bb.getExit()) {
            switch (instr.getOpcode()) {
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                if (((VarInsnNode) instr).var == var) {
                    return true;
                }

            default:
                prev = instr;
                instr = instr.getNext();
            }
        }

        return false;
    }

    private boolean deadStore(CtrlFlowGraph cfg, VarInsnNode store) {

        BasicBlock bb = cfg.getBB(store);

        if (bb == null) {
            return false;
        }

        if (loadAfterStore(bb, store, store.var)) {
            return false;
        }

        HashSet<BasicBlock> visited = new HashSet<BasicBlock>();
        Queue<BasicBlock> unprocessed = new LinkedList<BasicBlock>(
                bb.getSuccessors());

        while (!unprocessed.isEmpty()) {
            BasicBlock next = unprocessed.poll();

            if (visited.contains(next)) {
                continue;
            }

            if (loadAfterStore(next, next.getEntrance(), store.var)) {
                return false;
            }

            visited.add(next);
        }

        return true;
    }

    private boolean removeDeadStore() {

        CtrlFlowGraph cfg = CtrlFlowGraph.build(method);
        boolean isOptimized = false;

        // TODO LB: iterate over a copy unless we are sure an iterator is OK
        for (AbstractInsnNode instr : ilist.toArray()) {

            switch (instr.getOpcode()) {
            case Opcodes.ISTORE:
            case Opcodes.ASTORE:
            case Opcodes.FSTORE:

                if (deadStore(cfg, (VarInsnNode) instr)) {

                    ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                    ilist.remove(instr);
                    isOptimized = true;
                }

                break;

            case Opcodes.DSTORE:
            case Opcodes.LSTORE:

                if (deadStore(cfg, (VarInsnNode) instr)) {

                    ilist.insertBefore(instr, new InsnNode(Opcodes.POP2));
                    ilist.remove(instr);
                    isOptimized = true;
                }

                break;
            default:
                break;
            }
        }

        return isOptimized;
    }

    private boolean unremovablePop(Set<AbstractInsnNode> sources) {

        for (AbstractInsnNode source : sources) {

            switch (source.getOpcode()) {
            case Opcodes.ACONST_NULL:
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
            case Opcodes.LDC:
            case Opcodes.NEW:
            case Opcodes.ILOAD:
            case Opcodes.LLOAD:
            case Opcodes.FLOAD:
            case Opcodes.DLOAD:
            case Opcodes.ALOAD:
                break;

            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESTATIC:
                if (!InvocationInterpreter.getInstance().isRegistered(
                        (MethodInsnNode) source)) {
                    return true;
                }

                break;
            default:
                return true;
            }
        }

        return false;
    }

    private void tryRemoveInvocation(InsnList ilist, MethodInsnNode instr) {

        if (InvocationInterpreter.getInstance().isRegistered(instr)) {

            MethodInsnNode min = instr;
            String desc = min.desc;

            if (min.getOpcode() == Opcodes.INVOKEVIRTUAL) {
                ilist.insert(min, new InsnNode(Opcodes.POP));
            }

            for (Type arg : Type.getArgumentTypes(desc)) {
                ilist.insert(min, new InsnNode(
                        arg.getSize() == 2 ? Opcodes.POP2 : Opcodes.POP));
            }
        }
    }

    private void tryRemoveAllocation(InsnList ilist, AbstractInsnNode next,
            Map<AbstractInsnNode, Frame<SourceValue>> frames) {

        if (next.getOpcode() != Opcodes.DUP) {
            return;
        }

        // TODO LB: iterate over a copy unless we are sure an iterator is OK
        for (AbstractInsnNode instr : ilist.toArray()) {
            if (instr.getOpcode() == Opcodes.INVOKESPECIAL) {
                Type[] args = Type
                        .getArgumentTypes(((MethodInsnNode) instr).desc);
                Frame<SourceValue> frame = frames.get(instr);
                Set<AbstractInsnNode> sources = FrameHelper.getStackByIndex(
                        frame, args.length).insns;

                if (sources.contains(next)) {

                    for (Type arg : args) {
                        ilist.insert(instr,
                                new InsnNode(arg.getSize() == 2 ? Opcodes.POP2
                                        : Opcodes.POP));
                    }

                    ilist.remove(instr);
                }
            }
        }

        ilist.remove(next);
    }

    private boolean removePop() {

        Map<AbstractInsnNode, Frame<SourceValue>> frames =
                FrameHelper.createSourceMapping(PartialEvaluator.class.getName(), method);

        boolean isOptimized = false;

        // TODO LB: iterate over a copy unless we are sure an iterator is OK
        for (AbstractInsnNode instr : ilist.toArray()) {

            int opcode = instr.getOpcode();

            if (opcode != Opcodes.POP && opcode != Opcodes.POP2) {
                continue;
            }

            Frame<SourceValue> frame = frames.get(instr);

            if (frame == null) {
                continue;
            }

            Set<AbstractInsnNode> sources = FrameHelper.getStackByIndex(frame, 0).insns;

            if (unremovablePop(sources)) {
                continue;
            }

            for (AbstractInsnNode source : sources) {

                switch (source.getOpcode()) {
                case Opcodes.INVOKESPECIAL:
                case Opcodes.INVOKEVIRTUAL:
                case Opcodes.INVOKESTATIC:
                    tryRemoveInvocation(ilist, (MethodInsnNode) source);
                    break;

                case Opcodes.NEW:
                    tryRemoveAllocation(ilist, source.getNext(), frames);
                    break;

                default:
                    break;
                }

                ilist.remove(source);
            }

            ilist.remove(instr);
            isOptimized = true;
        }

        return isOptimized;
    }

    private boolean removeUnusedJump() {

        boolean isOptimized = false;

        // TODO LB: iterate over a copy unless we are sure an iterator is OK
        for (AbstractInsnNode instr : ilist.toArray()) {

            int opcode = instr.getOpcode();

            switch (instr.getType()) {
            case AbstractInsnNode.JUMP_INSN: {

                if (opcode == Opcodes.JSR) {
                    continue;
                }

                if (Insns.REVERSE.firstRealInsn (((JumpInsnNode) instr).label) != instr) {
                    continue;
                }

                if (opcode != Opcodes.GOTO) {
                    ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                }

                ilist.remove(instr);
                isOptimized = true;
                break;
            }

            case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                // Covers LOOKUPSWITCH
                LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) instr;
                boolean flag = false;

                for (LabelNode label : lsin.labels) {
                    if (Insns.REVERSE.firstRealInsn (label) != instr) {
                        flag = true;
                        continue;
                    }
                }

                if (flag || Insns.REVERSE.firstRealInsn (lsin.dflt) != instr) {
                    continue;
                }

                ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                ilist.remove(instr);
                isOptimized = true;
                break;
            }

            case AbstractInsnNode.TABLESWITCH_INSN: {
                // Covers TABLESWITCH
                TableSwitchInsnNode tsin = (TableSwitchInsnNode) instr;

                boolean flag = false;

                for (LabelNode label : tsin.labels) {
                    if (Insns.REVERSE.firstRealInsn (label) != instr) {
                        flag = true;
                        continue;
                    }
                }

                if (flag || Insns.REVERSE.firstRealInsn (tsin.dflt) != instr) {
                    continue;
                }

                ilist.insertBefore(instr, new InsnNode(Opcodes.POP));
                ilist.remove(instr);
                isOptimized = true;
                break;
            }

            default:
                break;
            }
        }

        return isOptimized;
    }

    private boolean removeUnusedHandler() {

        CtrlFlowGraph cfg = CtrlFlowGraph.build(method);
        boolean isOptimized = false;

        for (final TryCatchBlockNode tcb : method.tryCatchBlocks) {
            // TCB start is inclusive, TCB end is exclusive.
            final AbstractInsnNode first = Insns.FORWARD.firstRealInsn (tcb.start);
            final AbstractInsnNode last = Insns.REVERSE.nextRealInsn (tcb.end);
            if (first == last) {
                method.tryCatchBlocks.remove(tcb);
                isOptimized |= removeUnusedBB(cfg);
            }
        }

        return isOptimized;
    }

    public boolean evaluate() {

        ilist.add(new InsnNode(Opcodes.RETURN));
        Analyzer<ConstValue> constAnalyzer = new Analyzer<ConstValue>(
                ConstInterpreter.getInstance());
        Map<AbstractInsnNode, Frame<ConstValue>> frames = FrameHelper
                .createMapping(constAnalyzer, PartialEvaluator.class.getName(),
                        method);

        boolean isOptimized = conditionalReduction(frames);
        isOptimized |= replaceLoadWithLDC(frames);

        boolean removed;

        do {
            removed = false;
            removed |= removeDeadStore();
            removed |= removePop();
        } while (removed);

        isOptimized |= removed;
        isOptimized |= removeUnusedJump();
        isOptimized |= removeUnusedHandler();

        ilist.remove(ilist.getLast());

        return isOptimized;
    }
}
