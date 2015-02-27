package ch.usi.dag.disl;

import ch.usi.dag.disl.exception.DiSLInitializationException;


@SuppressWarnings ("serial")
class TransformerInitializationException extends DiSLInitializationException {

    public TransformerInitializationException (
        final String format, final Object ... args
    ) {
        super (format, args);
    }

    public TransformerInitializationException (
        final Throwable cause, final String format, final Object ... args
    ) {
        super (cause, format, args);
    }

}
