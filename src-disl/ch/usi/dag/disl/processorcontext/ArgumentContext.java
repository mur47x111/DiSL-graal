package ch.usi.dag.disl.processorcontext;

/**
 * Allows accessing information about particular argument.
 */
public interface ArgumentContext {

    /**
     * Returns position of the processed argument.
     */
    int getPosition ();


    /**
     * Returns type descriptor of the processed argument.
     */
    String getTypeDescriptor ();


    /**
     * Returns total number of processed arguments.
     */
    int getTotalCount ();
}
