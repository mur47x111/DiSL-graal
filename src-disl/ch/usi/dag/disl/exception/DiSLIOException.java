package ch.usi.dag.disl.exception;

public class DiSLIOException extends DiSLException {

    private static final long serialVersionUID = 5353311026326100989L;

    public DiSLIOException() {
        super();
    }

    public DiSLIOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiSLIOException(String message) {
        super(message);
    }

    public DiSLIOException(Throwable cause) {
        super(cause);
    }
}
