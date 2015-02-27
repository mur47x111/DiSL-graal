package ch.usi.dag.disl;

import ch.usi.dag.disl.exception.DiSLException;


@SuppressWarnings ("serial")
class TransformerException extends DiSLException {

    public TransformerException (
        final Throwable cause, final String format, final Object ... args
    ) {
        super (cause, format, args);
    }

}
