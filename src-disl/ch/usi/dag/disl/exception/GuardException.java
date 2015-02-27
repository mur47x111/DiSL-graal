package ch.usi.dag.disl.exception;

public class GuardException extends DiSLException {

    private static final long serialVersionUID = 7194255601706645284L;

    public GuardException() {
        super();
    }

    public GuardException(String message, Throwable cause) {
        super(message, cause);
    }

    public GuardException(String message) {
        super(message);
    }

    public GuardException(Throwable cause) {
        super(cause);
    }
}
