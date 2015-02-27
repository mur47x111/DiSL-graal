package ch.usi.dag.disl.exception;

public class MarkerException extends DiSLException {

    private static final long serialVersionUID = 3079093109650560405L;

    public MarkerException() {
        super();
    }

    public MarkerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MarkerException(String message) {
        super(message);
    }

    public MarkerException(Throwable cause) {
        super(cause);
    }
}
