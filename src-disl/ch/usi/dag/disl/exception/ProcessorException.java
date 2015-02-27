package ch.usi.dag.disl.exception;

public class ProcessorException extends DiSLException {

    private static final long serialVersionUID = -1156580898744340578L;


    public ProcessorException () {
        super ();
    }


    public ProcessorException (final String message) {
        super (message);
    }


    public ProcessorException (final String format, final Object... args) {
        super (String.format (format, args));
    }

}
