package ch.usi.dag.disl.exception;

public class SnippetParserException extends ParserException {

    private static final long serialVersionUID = -2826083567381934062L;

    public SnippetParserException() {
        super();
    }

    public SnippetParserException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnippetParserException(String message) {
        super(message);
    }

    public SnippetParserException(Throwable cause) {
        super(cause);
    }
}
