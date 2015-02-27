package ch.usi.dag.disl.exception;

public class GuardRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 4590178036249914052L;

    public GuardRuntimeException() {
        super();
    }

    public GuardRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuardRuntimeException(String message) {
        super(message);
    }

    public GuardRuntimeException(Throwable cause) {
        super(cause);
    }


}
