package ch.usi.dag.disl.marker;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import ch.usi.dag.disl.exception.MarkerException;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.snippet.Shadow.WeavingRegion;
import ch.usi.dag.disl.snippet.Snippet;


/**
 * Simplifies {@link Marker} implementation by providing a
 * {@link #mark(MethodNode)} method that returns a list of {@link MarkedRegion}
 * instances instead of {@link Shadow} instances. The {@link MarkedRegion} class
 * itself supports automatic computation of weaving region based on simplified
 * region specification.
 */
public abstract class AbstractMarker implements Marker {

    /**
     * values where the weaving region can be precomputed by
     * computeDefaultWeavingRegion method.
     */
    public static class MarkedRegion {

        private AbstractInsnNode start;
        private final List <AbstractInsnNode> ends;

        private WeavingRegion weavingRegion;


        /**
         * Returns region start.
         */
        public AbstractInsnNode getStart () {
            return start;
        }


        /**
         * Set region start.
         */
        public void setStart (final AbstractInsnNode start) {
            this.start = start;
        }


        /**
         * Returns the list of region ends.
         */
        public List <AbstractInsnNode> getEnds () {
            return ends;
        }


        /**
         * Appends a region to the list of region ends.
         */
        public void addEnd (final AbstractInsnNode exitpoint) {
            this.ends.add (exitpoint);
        }


        /**
         * Returns the weaving region.
         */
        public WeavingRegion getWeavingRegion () {
            return weavingRegion;
        }


        /**
         * Sets the weaving region.
         */
        public void setWeavingRegion (final WeavingRegion weavingRegion) {
            this.weavingRegion = weavingRegion;
        }


        /**
         * Creates a {@link MarkedRegion} with start.
         */
        public MarkedRegion (final AbstractInsnNode start) {
            this.start = start;
            this.ends = new LinkedList <AbstractInsnNode> ();
        }


        /**
         * Creates a {@link MarkedRegion} with start and a single end.
         */
        public MarkedRegion (
            final AbstractInsnNode start, final AbstractInsnNode end
        ) {
            this.start = start;
            this.ends = new LinkedList <AbstractInsnNode> ();
            this.ends.add (end);
        }


        /**
         * Creates a {@link MarkedRegion} with start and a list of ends.
         */
        public MarkedRegion (
            final AbstractInsnNode start, final List <AbstractInsnNode> ends
        ) {
            this.start = start;
            this.ends = ends;
        }


        /**
         * Creates a {@link MarkedRegion} with start, multiple ends, and a
         * weaving region.
         */
        public MarkedRegion (final AbstractInsnNode start,
            final List <AbstractInsnNode> ends, final WeavingRegion weavingRegion
        ) {
            this.start = start;
            this.ends = ends;
            this.weavingRegion = weavingRegion;
        }


        /**
         * Test if all required fields are filled
         */
        public boolean valid () {
            return start != null && ends != null && weavingRegion != null;
        }


        /**
         * Computes the default {@link WeavingRegion} for this
         * {@link MarkedRegion}. The computed {@link WeavingRegion} instance
         * will NOT be automatically associated with this {@link MarkedRegion}.
         */
        public WeavingRegion computeDefaultWeavingRegion (final MethodNode methodNode) {

            final AbstractInsnNode wstart = start;
            // wends is set to null - see WeavingRegion for details

            // compute after throwing region

            // set start
            final AbstractInsnNode afterThrowStart = start;
            AbstractInsnNode afterThrowEnd = null;

            // get end that is the latest in the method instructions
            final Set<AbstractInsnNode> endsSet = new HashSet<AbstractInsnNode>(ends);

            // get end that is the latest in the method instructions
            AbstractInsnNode instr = methodNode.instructions.getLast();

            while (instr != null) {
                if (endsSet.contains(instr)) {
                    afterThrowEnd = instr;
                    break;
                }

                instr = instr.getPrevious();
            }

            // skip the label nodes which are the end of try-catch blocks
            if (afterThrowEnd instanceof LabelNode) {
                final Set<AbstractInsnNode> tcb_ends = new HashSet<AbstractInsnNode>();

                for (final TryCatchBlockNode tcb : methodNode.tryCatchBlocks) {
                    tcb_ends.add (tcb.end);
                }

                while (tcb_ends.contains (afterThrowEnd)) {
                    afterThrowEnd = afterThrowEnd.getPrevious ();
                }
            }

            return new WeavingRegion (
                wstart, null,
                afterThrowStart, afterThrowEnd
            );
        }
    }

    @Override
    public List <Shadow> mark (
        final ClassNode classNode, final MethodNode methodNode,
        final Snippet snippet
    ) throws MarkerException {
        // use simplified interface
        final List<MarkedRegion> regions = mark(methodNode);
        final List<Shadow> result = new LinkedList<Shadow>();

        // convert marked regions to shadows
        for (final MarkedRegion mr : regions) {
            if (!mr.valid()) {
                throw new MarkerException("Marker " + this.getClass()
                        + " produced invalid MarkedRegion (some MarkedRegion" +
                        " fields where not set)");
            }

            result.add (new Shadow (
                classNode, methodNode, snippet,
                mr.getStart (), mr.getEnds (), mr.getWeavingRegion ()
            ));
        }

        return result;
    }


    /**
     * Implementation of this method should return list of {@link MarkedRegion}
     * instances with start, ends, and the weaving region filled.
     *
     * @param methodNode
     *        method node of the marked class
     * @return returns list of MarkedRegion
     */
    public abstract List <MarkedRegion> mark (MethodNode methodNode);
}
