package ch.usi.dag.disl.marker;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.disl.snippet.Shadow.WeavingRegion;


abstract class AbstractInsnMarker extends AbstractMarker {

    @Override
    public final List <MarkedRegion> mark (final MethodNode methodNode) {
        final List <MarkedRegion> regions = new LinkedList <MarkedRegion> ();

        for (final AbstractInsnNode instruction : markInstruction (methodNode)) {
            final MarkedRegion region = new MarkedRegion (instruction, instruction);
            region.setWeavingRegion (new WeavingRegion (
                instruction, new LinkedList <AbstractInsnNode> (region.getEnds ()),
                instruction, instruction
            ));

            regions.add (region);
        }

        return regions;
    }


    public abstract List <AbstractInsnNode> markInstruction (MethodNode methodNode);

}
