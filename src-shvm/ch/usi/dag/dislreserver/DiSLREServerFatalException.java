package ch.usi.dag.dislreserver;

public class DiSLREServerFatalException extends RuntimeException {

    private static final long serialVersionUID = -8431771285237240263L;


    public DiSLREServerFatalException (final String message) {
        super (message);
    }


    public DiSLREServerFatalException (final String message, final Throwable cause) {
        super (message, cause);
    }
}
