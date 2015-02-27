package ch.usi.dag.disl.marker;

import java.util.List;

import org.objectweb.asm.tree.MethodNode;

/**
 * AbstractDWRMarker is an enhancement of AbstractMarker automatically computing
 * weaving region. This includes correct position of end region (not after jump)
 * and meaningful try block.
 *
 * <p>
 * User has to implement markWithDefaultWeavingReg method.
 */
public abstract class AbstractDWRMarker extends AbstractMarker {

    public final List<MarkedRegion> mark(MethodNode methodNode) {

        List<MarkedRegion> mrs = markWithDefaultWeavingReg(methodNode);

        // automatically compute default weaving region
        for (MarkedRegion mr : mrs) {
            mr.setWeavingRegion(mr.computeDefaultWeavingRegion(methodNode));
        }

        return mrs;
    }

    /**
     * Implementation of this method should return list of marked regions with
     * filled start and end of the region.
     *
     * <p>
     * The regions will get automatic after throw computation.
     * <p>
     * The regions will get automatic branch skipping at the end.
     */
    public abstract List<MarkedRegion> markWithDefaultWeavingReg(
            MethodNode methodNode);
}
