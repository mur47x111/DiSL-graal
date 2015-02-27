package ch.usi.dag.disl.exception;

public class StaticContextException extends RuntimeException {

    private static final long serialVersionUID = -6364742721139705511L;

    public StaticContextException() {
        super();
    }

    public StaticContextException(String message, Throwable cause) {
        super(message, cause);
    }

    public StaticContextException(String message) {
        super(message);
    }

    public StaticContextException(Throwable cause) {
        super(cause);
    }
}
