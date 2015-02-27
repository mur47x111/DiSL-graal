package ch.usi.dag.dislserver;

final class DiSLServerException extends Exception {

    private static final long serialVersionUID = 5272000884539359236L;


    public DiSLServerException (final String message) {
        super (message);
    }


    public DiSLServerException (final String message, final Throwable cause) {
        super (message, cause);
    }

}
