package ch.usi.dag.disl.classparser;

@SuppressWarnings ("serial")
class ParserRuntimeException extends RuntimeException {

    public ParserRuntimeException (final String message) {
        super (message);
    }


    public ParserRuntimeException (final Throwable cause) {
        super (cause);
    }


    public ParserRuntimeException (
        final String format, final Object... args
    ) {
        super (String.format (format, args));
    }


    public ParserRuntimeException (
        final Throwable cause, final String format, final Object... args
    ) {
        super (String.format (format, args), cause);
    }

}
