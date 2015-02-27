package ch.usi.dag.disl.exception;

@SuppressWarnings ("serial")
public class DiSLInitializationException extends DiSLException {

    public DiSLInitializationException (final String message) {
        super (message);
    }

    public DiSLInitializationException (final Throwable cause) {
        super (cause);
    }

    public DiSLInitializationException (
        final String format, final Object ... args
    ) {
        super (format, args);
    }

    public DiSLInitializationException (
        final Throwable cause, final String format, final Object ... args
    ) {
        super (cause, format, args);
    }

}
