package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.util.AsmHelper.Insns;
import ch.usi.dag.disl.util.BasicBlockCalc;


/**
 * Marks a basic block.
 * <p>
 * Sets the start at the beginning of a basic block and the end at the end of a
 * basic block. Considers only jump instructions, lookup switch and table
 * switch.
 */
public class BasicBlockMarker extends AbstractDWRMarker {

    protected boolean isPrecise = false;


    @Override
    public List <MarkedRegion> markWithDefaultWeavingReg (final MethodNode methodNode) {
        final List <MarkedRegion> regions = new LinkedList <MarkedRegion> ();
        final List <AbstractInsnNode> seperators = BasicBlockCalc.getAll (
            methodNode.instructions, methodNode.tryCatchBlocks, isPrecise
        );

        final AbstractInsnNode last = Insns.REVERSE.firstRealInsn (
            methodNode.instructions.getLast ()
        );

        seperators.add (last);

        for (int i = 0; i < seperators.size () - 1; i++) {
            final AbstractInsnNode start = seperators.get (i);
            AbstractInsnNode end = seperators.get (i + 1);

            if (i != seperators.size () - 2) {
                end = end.getPrevious ();
            }

            regions.add (new MarkedRegion (
                start, Insns.REVERSE.firstRealInsn (end)
            ));
        }

        return regions;
    }

}
