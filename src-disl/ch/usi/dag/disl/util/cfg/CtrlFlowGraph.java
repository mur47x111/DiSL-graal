package ch.usi.dag.disl.util.cfg;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.BasicBlockCalc;

public class CtrlFlowGraph {

    private final static int NOT_FOUND = -1;
    private final static int NEW = -2;

    // basic blocks of a method
    private List<BasicBlock> nodes;

    // a basic block is marked as connected after visited
    private List<BasicBlock> connected_nodes;

    // size of connected basic blocks since last visit
    private int connected_size;

    // basic blocks that ends with a 'return' or 'athrow'
    private Set<BasicBlock> method_exits;

    // Initialize the control flow graph.
    public CtrlFlowGraph(InsnList instructions,
            List<TryCatchBlockNode> tryCatchBlocks) {

        nodes = new LinkedList<BasicBlock>();
        connected_nodes = new LinkedList<BasicBlock>();
        connected_size = 0;

        method_exits = new HashSet<BasicBlock>();

        // Generating basic blocks
        List<AbstractInsnNode> separators = BasicBlockCalc.getAll(instructions,
                tryCatchBlocks, false);
        AbstractInsnNode last = instructions.getLast();
        separators.add(last);

        for (int i = 0; i < separators.size() - 1; i++) {
            AbstractInsnNode start = separators.get(i);
            AbstractInsnNode end = separators.get(i + 1);

            if (i != separators.size() - 2) {
                end = end.getPrevious();
            }

            end = Insns.REVERSE.firstRealInsn (end);
            nodes.add(new BasicBlock(i, start, end));
        }
    }

    // Initialize the control flow graph.
    public CtrlFlowGraph(MethodNode method) {
        this(method.instructions, method.tryCatchBlocks);
    }

    public List<BasicBlock> getNodes() {
        return nodes;
    }

    // Return the index of basic block that contains the input instruction.
    // If not found, return NOT_FOUND.
    public int getIndex(AbstractInsnNode instr) {

        BasicBlock bb = getBB(instr);

        if (bb == null) {
            return NOT_FOUND;
        } else {
            return bb.getIndex();
        }
    }

    // Return a basic block that contains the input instruction.
    // If not found, return null.
    public BasicBlock getBB(AbstractInsnNode instr) {
        instr = Insns.FORWARD.firstRealInsn (instr);

        while (instr != null) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getEntrance().equals(instr)) {
                    return nodes.get(i);
                }
            }

            instr = instr.getPrevious();
        }

        return null;
    }

    // Visit a successor.
    // If the basic block, which starts with the input 'node',
    // is not found, return NOT_FOUND;
    // If the basic block has been visited, then returns its index;
    // Otherwise return NEW.
    private int tryVisit(BasicBlock current, AbstractInsnNode node) {

        BasicBlock bb = getBB(node);

        if (bb == null) {
            return NOT_FOUND;
        }

        if (connected_nodes.contains(bb)) {

            int index = connected_nodes.indexOf(bb);

            if (current != null) {
                if (index < connected_size) {
                    current.getJoins().add(bb);
                } else {
                    current.getSuccessors().add(bb);
                    bb.getPredecessors().add(current);
                }
            }

            return index;
        }

        if (current != null) {
            current.getSuccessors().add(bb);
            bb.getPredecessors().add(current);
        }

        connected_nodes.add(bb);
        return NEW;
    }

    // Try to visit a successor. If it is visited last build, then regards
    // it as an exit.
    private void tryVisit(BasicBlock current, AbstractInsnNode node,
            AbstractInsnNode exit, List<AbstractInsnNode> joins) {
        int ret = tryVisit(current, node);

        if (ret >= 0 && ret < connected_size) {
            joins.add(exit);
        }
    }

    // Generate a control flow graph.
    // Returns a list of instruction that stands for the exit point
    // of the current visit.
    // For the first time this method is called, it will generate
    // the normal return of this method.
    // Otherwise, it will generate the join instruction between
    // the current visit and a existing visit.
    public List<AbstractInsnNode> visit(AbstractInsnNode root) {

        List<AbstractInsnNode> joins = new LinkedList<AbstractInsnNode>();

        if (tryVisit(null, root) == NOT_FOUND) {
            return joins;
        }

        for (int i = connected_size; i < connected_nodes.size(); i++) {

            BasicBlock current = connected_nodes.get(i);
            AbstractInsnNode exit = current.getExit();

            int opcode = exit.getOpcode();

            switch (exit.getType()) {
            case AbstractInsnNode.JUMP_INSN: {
                // Covers IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
                // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
                // IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, and IFNONNULL.
                tryVisit(current, ((JumpInsnNode) exit).label, exit, joins);

                // goto never returns.
                if (opcode != Opcodes.GOTO) {
                    tryVisit(current, exit.getNext(), exit, joins);
                }

                break;
            }

            case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                // Covers LOOKUPSWITCH
                LookupSwitchInsnNode lsin = (LookupSwitchInsnNode) exit;

                for (LabelNode label : lsin.labels) {
                    tryVisit(current, label, exit, joins);
                }

                tryVisit(current, lsin.dflt, exit, joins);

                break;
            }

            case AbstractInsnNode.TABLESWITCH_INSN: {
                // Covers TABLESWITCH
                TableSwitchInsnNode tsin = (TableSwitchInsnNode) exit;

                for (LabelNode label : tsin.labels) {
                    tryVisit(current, label, exit, joins);
                }

                tryVisit(current, tsin.dflt, exit, joins);
                break;
            }

            default:
                if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                        || opcode == Opcodes.ATHROW) {
                    method_exits.add(current);
                } else {
                    tryVisit(current, exit.getNext(), exit, joins);
                }

                break;
            }
        }

        connected_size = connected_nodes.size();

        return joins;
    }

    public List<AbstractInsnNode> getEnds() {

        List<AbstractInsnNode> ends = new LinkedList<AbstractInsnNode>();

        for (BasicBlock bb : nodes) {

            if (bb.getSuccessors().size() == 0) {
                ends.add(bb.getExit());
            }
        }

        return ends;
    }

    // Build up a control flow graph using instruction list and exception
    // handlers.
    public static CtrlFlowGraph build(InsnList instructions,
            List<TryCatchBlockNode> tryCatchBlocks) {
        CtrlFlowGraph cfg = new CtrlFlowGraph(instructions, tryCatchBlocks);

        cfg.visit(instructions.getFirst());

        for (TryCatchBlockNode tcb : tryCatchBlocks) {
            cfg.visit(tcb.handler);
        }

        return cfg;
    }

    // Build up a control flow graph using a method node.
    public static CtrlFlowGraph build(MethodNode method) {
        return build(method.instructions, method.tryCatchBlocks);
    }

}
