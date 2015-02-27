package ch.usi.dag.disl.exception;

/**
 * Represents a DiSL internal error, which means that there is something bad in
 * the implementation of DiSL itself. Users will hopefully encounter this
 * exception very rarely.
 */
public class DiSLFatalException extends RuntimeException {

    private static final long serialVersionUID = -1367296993008634784L;

    //

    public DiSLFatalException (final Throwable cause) {
        super (cause);
    }


    public DiSLFatalException (final String message) {
        super (message);
    }


    public DiSLFatalException (final String message, final Throwable cause) {
        super (message, cause);
    }


    public DiSLFatalException (final String format, final Object ... args) {
        super (String.format (format, args));
    }

}
