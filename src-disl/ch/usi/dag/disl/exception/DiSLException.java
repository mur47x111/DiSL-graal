package ch.usi.dag.disl.exception;

@SuppressWarnings ("serial")
public abstract class DiSLException extends Exception {

    public DiSLException () {
        super ();
    }


    public DiSLException (final String message, final Throwable cause) {
        super (message, cause);
    }


    public DiSLException (final String message) {
        super (message);
    }


    public DiSLException (final Throwable cause) {
        super (cause);
    }

    public DiSLException (final String format, final Object... args) {
        super (String.format (format, args));
    }

    public DiSLException (
        final Throwable cause, final String format, final Object... args
    ) {
        super (String.format (format, args), cause);
    }

}
