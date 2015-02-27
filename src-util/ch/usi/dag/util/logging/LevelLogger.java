package ch.usi.dag.util.logging;

/**
 * Represents a logger for a particular logging level. The implementation is
 * responsible for mapping the common {@link Level} to the
 * implementation-specific logging level and invoking the corresponding
 * operations on the underlying implementation-specific logger.
 *
 * @author Lubomir Bulej
 */
interface LevelLogger {

    /**
     * Checks if messages for the level represented by this {@link LevelLogger}
     * would be actually logged. The check may be specific underlying logging
     * infrastructure, i.e., it may be local to a logger, or global.
     *
     * @param level
     *        a common logging {@link Level}.
     * @return {@code true} if the given message level is currently being
     *         logged.
     */
    boolean isEnabled ();


    /**
     * Logs a message at the level represented by this {@link LevelLogger}.
     *
     * @param message
     *        a message to be logged.
     */
    void log (String message);


    /**
     * Logs a message at the level represented by this {@link LevelLogger}
     * associated with a particular {@link Throwable}.
     *
     * @param throwable
     *        a {@link Throwable} associated with the message.
     * @param message
     *        a message to be logged.
     */
    void log (Throwable cause, String message);

}
