package ch.usi.dag.dislreserver;

public class DiSLREServerException extends Exception {

    private static final long serialVersionUID = 5272000884539359236L;


    public DiSLREServerException (final String message) {
        super (message);
    }


    public DiSLREServerException (final Throwable cause) {
        super (cause);
    }
}
