package ch.usi.dag.disl.exception;

public class ParserException extends DiSLException {

    private static final long serialVersionUID = -2826083567381934062L;


    public ParserException () {
        super ();
    }


    public ParserException (final String message, final Throwable cause) {
        super (message, cause);
    }


    public ParserException (final String message) {
        super (message);
    }


    public ParserException (final Throwable cause) {
        super (cause);
    }

    public ParserException (
        final Throwable cause, final String format, final Object... args
    ) {
        super (cause, format, args);
    }

    public ParserException (final String format, final Object... args) {
        super (format, args);
    }

}
