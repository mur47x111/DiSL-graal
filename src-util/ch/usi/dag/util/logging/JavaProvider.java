package ch.usi.dag.util.logging;

import java.util.HashMap;
import java.util.Map;


/**
 * Provides a logging service implemented using the Java logging.
 *
 * @author Lubomir Bulej
 */
public final class JavaProvider implements Provider {

    @SuppressWarnings ("serial")
    private static final Map <Level, java.util.logging.Level>
        __levels__ = new HashMap <Level, java.util.logging.Level> () {{
            put (Level.TRACE, java.util.logging.Level.FINEST);
            put (Level.DEBUG, java.util.logging.Level.FINE);
            put (Level.INFO, java.util.logging.Level.INFO);
            put (Level.WARN, java.util.logging.Level.WARNING);
            put (Level.ERROR, java.util.logging.Level.SEVERE);
        }};


    /* ***********************************************************************
     * Provider interface
     * ***********************************************************************/

    @Override
    public String name () {
        return "java";
    }


    @Override
    public LevelLogger createLevelLogger (
        final Level level, final String subsystem
    ) {
        return new JavaLevelLogger (
            __levels__.get (level),
            java.util.logging.Logger.getLogger (subsystem)
        );
    }


    @Override
    public void setGlobalLevel (final Level level) {
        java.util.logging.Logger.getGlobal ().setLevel (
            __levels__.get (level)
        );
    }


    /* ***********************************************************************
     * JavaLevelLogger
     * ***********************************************************************/

    private static final class JavaLevelLogger implements LevelLogger {
        private final java.util.logging.Logger __logger;
        private final java.util.logging.Level __level;

        //

        JavaLevelLogger (
            final java.util.logging.Level level,
            final java.util.logging.Logger logger
        ) {
            __logger = logger;
            __level = level;
        }

        @Override
        public boolean isEnabled () {
            return __logger.isLoggable (__level);
        }

        @Override
        public void log (final String message) {
            __logger.log (__level, message);
        }

        @Override
        public void log (final Throwable cause, final String message) {
            __logger.log (__level, message, cause);
        }

    }

}
