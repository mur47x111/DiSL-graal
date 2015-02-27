package ch.usi.dag.util.logging;

enum Level {

    /**
     * Represents a logging level for errors that typically result in program
     * termination.
     */
    ERROR,

    /**
     * Represents a logging level for problems that are not fatal to program
     * execution, but may impede its function and should be noticed by users.
     */
    WARN,

    /**
     * Represents a logging level for general informative messages.
     */
    INFO,

    /**
     * Represents a logging level for debugging messages. These provide
     * additional information compared to informative messages, which may be
     * useful for high-level debugging.
     */
    DEBUG,

    /**
     * Represents a logging level for trace messages. These provide detailed
     * low-level debugging information for many operations, producing a high
     * amount of log messages.
     */
    TRACE;

    //

    public static Level parse (final String name) {
        if (name != null) {
            return valueOf (name.trim ().toUpperCase ());
        } else {
            return null;
        }
    }

}
