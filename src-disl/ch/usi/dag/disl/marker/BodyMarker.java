package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.snippet.Shadow.WeavingRegion;
import ch.usi.dag.disl.util.AsmHelper;
import ch.usi.dag.disl.util.AsmHelper.Insns;


/**
 * Marks a method body.
 * <p>
 * Sets the start at the beginning of a method and the end at the end of a
 * method.
 */
public class BodyMarker extends AbstractMarker {

    @Override
    public List <MarkedRegion> mark (final MethodNode method) {
        final List <MarkedRegion> regions = new LinkedList <MarkedRegion> ();
        final MarkedRegion region = new MarkedRegion (
            method.instructions.getFirst ()
        );

        for (final AbstractInsnNode insn : Insns.selectAll (method.instructions)) {
            if (AsmHelper.isReturn (insn)) {
                region.addEnd (insn);
            }
        }

        final WeavingRegion wregion = region.computeDefaultWeavingRegion (method);
        wregion.setAfterThrowEnd (method.instructions.getLast ());
        region.setWeavingRegion (wregion);
        regions.add (region);
        return regions;
    }

}
