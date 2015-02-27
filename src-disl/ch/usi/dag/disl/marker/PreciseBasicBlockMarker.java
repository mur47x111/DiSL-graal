package ch.usi.dag.disl.marker;

/**
 * <p>
 * Marks basic block.
 * 
 * <p>
 * Sets the start at the beginning of a basic block and the end at the end of a
 * basic block. Considers all instructions that may throw an exception.
 */
public class PreciseBasicBlockMarker extends BasicBlockMarker {

    public PreciseBasicBlockMarker() {
        super();
        isPrecise = true;
    }
}
