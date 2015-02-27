package ch.usi.dag.disl.exception;

/**
 * Wraps exceptions that occurred during reflective operations.
 */
public final class ReflectionException extends DiSLException {

    private static final long serialVersionUID = 1746507695125084587L;

    //

    public ReflectionException (final String message, final Throwable cause) {
        super (message, cause);
    }


    public ReflectionException (final String format, final Object ... args) {
        super (String.format (format, args));
    }


    public ReflectionException (
        final Throwable cause, final String format, final Object ... args
    ) {
        super (String.format (format, args), cause);
    }

}
