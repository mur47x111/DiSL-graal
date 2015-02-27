package ch.usi.dag.disl.exception;

/**
 * Thrown when an illegal context usage is detected. This usually happens when a
 * literal argument is expected in a static or dynamic context method
 * invocation, but something else is found instead.
 */
public class InvalidContextUsageException extends DiSLException {

    private static final long serialVersionUID = -4897273691513425444L;


    public InvalidContextUsageException () {
        super ();
    }


    public InvalidContextUsageException (String message) {
        super (message);
    }


    public InvalidContextUsageException (final String format, final Object ... args) {
        super (format, args);
    }

}
