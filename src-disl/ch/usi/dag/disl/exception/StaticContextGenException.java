package ch.usi.dag.disl.exception;

public class StaticContextGenException extends DiSLException {

    private static final long serialVersionUID = 8195611687932799195L;

    //

    public StaticContextGenException (
        final Throwable cause, final String format, final Object ... args
    ) {
        super (cause, format, args);
    }


    public StaticContextGenException (
        final String format, final Object ... args
    ) {
        super (format, args);
    }

}
