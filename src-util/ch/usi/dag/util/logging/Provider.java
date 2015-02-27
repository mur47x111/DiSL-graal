package ch.usi.dag.util.logging;

interface Provider {
    /**
     * Returns the name of this logging provider.
     *
     * @return The name of this logging provider.
     */
    String name ();


    /**
     * Creates a new logger for the specified common logging {@link Level},
     * which maps logger operations to implementation-specific logging level
     * corresponding to the common level.
     *
     * @param level
     *        the logging level the logger is created for.
     * @param subsystem
     *        the name of the subsystem the logger is created for.
     */
    LevelLogger createLevelLogger (Level level, String subsystem);


    /**
     * Sets global common logging {@link Level} for loggers produced by this
     * provider.
     *
     * @param level
     *        the logging level to set.
     */
    void setGlobalLevel (Level level);
}
