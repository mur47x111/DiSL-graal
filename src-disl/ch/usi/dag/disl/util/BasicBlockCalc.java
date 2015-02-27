package ch.usi.dag.disl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;


public class BasicBlockCalc {

    /**
     * Returns all basic blocks of a given method node.
     */
    public static List <AbstractInsnNode> getAll (
        final InsnList instructions, final List <TryCatchBlockNode> tryCatchBlocks,
        final boolean isPrecise
    ) {
        //
        // A holder for instructions that mark the beginning of a basic block.
        //
        // We override the add() method to automatically skip all virtual
        // instructions that are added.
        //
        // We also override the addAll() method to ensure that our modified
        // add() method is used to add the individual elements, because there
        // is no contract in HashSet or Collection to guarantee that.
        //
        @SuppressWarnings ("serial")
        Set <AbstractInsnNode> bbStarts = new HashSet <AbstractInsnNode> () {
            @Override
            public boolean add (AbstractInsnNode insn) {
                return super.add (Insns.FORWARD.firstRealInsn (insn));
            }

            @Override
            public boolean addAll (Collection <? extends AbstractInsnNode> insns) {
                boolean result = false;
                for (final AbstractInsnNode insn : insns) {
                    final boolean modified = add (insn);
                    result = result || modified;
                }
                return result;
            }
        };

        //
        // The first instruction starts a basic block.
        //
        bbStarts.add (instructions.getFirst ());

        //
        // Scan all the instructions, identify those that terminate their basic
        // block and collect the starting instructions of the basic blocks
        // that follow them.
        //
        for (final AbstractInsnNode insn : Insns.selectAll (instructions)) {
            SWITCH: switch (insn.getType ()) {
            //
            // IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
            // IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE,
            // IF_ACMPEQ, IF_ACMPNE, GOTO, JSR, IFNULL, and IFNONNULL.
            //
            // For all jump instructions, a basic block starts where the
            // instruction jumps.
            //
            // For conditional jumps or jumps to subroutines, a basic block
            // also starts with the next instruction.
            //
            // The GOTO instruction changes the control flow unconditionally,
            // so only one basic block follows from it.
            //
            case AbstractInsnNode.JUMP_INSN: {
                bbStarts.add (((JumpInsnNode) insn).label);
                if (insn.getOpcode () != Opcodes.GOTO) {
                    //
                    // There must be a valid (non-virtual) instruction
                    // following a conditional/subroutine jump instruction.
                    //
                    AbstractInsnNode nextInsn = Insns.FORWARD.nextRealInsn (insn);
                    if (nextInsn != null) {
                        bbStarts.add (nextInsn);
                    }
                }
                break SWITCH;
            }

            //
            // LOOKUPSWITCH, TABLESWITCH
            //
            // For the LOOKUPSWITCH and TABLESWITCH instructions, all the
            // targets in the table represent a new basic block, including
            // the default target.
            //
            // Since they are two unrelated classes in ASM, we have to handle
            // each case separately, yet with the same code.
            //
            case AbstractInsnNode.LOOKUPSWITCH_INSN: {
                final LookupSwitchInsnNode lsInsn = (LookupSwitchInsnNode) insn;

                bbStarts.addAll (lsInsn.labels);
                bbStarts.add (lsInsn.dflt);
                break SWITCH;
            }

            case AbstractInsnNode.TABLESWITCH_INSN: {
                final TableSwitchInsnNode tsInsn = (TableSwitchInsnNode) insn;

                bbStarts.addAll (tsInsn.labels);
                bbStarts.add (tsInsn.dflt);
                break SWITCH;
            }

            //
            // Don't do anything for other instruction types.
            //
            default:
                break SWITCH;
            }

            //
            // In case of precise basic block marking, any instruction that
            // might throw an exception is potentially the last instruction of
            // a basic block, with the next instruction the beginning of the
            // next basic block.
            //
            if (isPrecise && AsmHelper.mightThrowException (insn)) {
                bbStarts.add (insn.getNext ());
            }
        }

        //
        // All exception handlers start a basic block as well.
        //
        for (final TryCatchBlockNode tryCatchBlock : tryCatchBlocks) {
            bbStarts.add (tryCatchBlock.handler);
        }

        //
        // Sort the basic block starting instructions. A LinkedHashSet would
        // not help here, because we were adding entries out-of-order (jumps).
        //
        List <AbstractInsnNode> result = new ArrayList <AbstractInsnNode> ();
        for (final AbstractInsnNode insn : Insns.selectAll (instructions)) {
            if (bbStarts.contains (insn)) {
                result.add (insn);
            }
        }

        return result;
    }

}
