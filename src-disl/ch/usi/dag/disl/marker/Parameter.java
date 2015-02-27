package ch.usi.dag.disl.marker;

/**
 * <p>
 * Used for marker parameter parsing.
 */
public class Parameter {

    protected String value;

    protected String delim;

    /**
     * Create parameter with a value.
     */
    public Parameter(String value) {
        this.value = value;
    }

    /**
     * Set delimiter for multi-value parsing.
     */
    public void setMultipleValDelim(String delim) {
        this.delim = delim;
    }

    /**
     * Get parameter value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Get array of values split according to the set delimiter.
     */
    public String[] getMultipleValues() {
        return value.split(delim);
    }
}
