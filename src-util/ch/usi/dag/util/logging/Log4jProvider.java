package ch.usi.dag.util.logging;

import java.util.HashMap;
import java.util.Map;


/**
 * Provides a logging service implemented using the Apache Log4j framework.
 *
 * @author Lubomir Bulej
 */
public final class Log4jProvider implements Provider {

    @SuppressWarnings ("serial")
    private static final Map <Level, org.apache.log4j.Level>
        __levels__ = new HashMap <Level, org.apache.log4j.Level> () {{
            put (Level.TRACE, org.apache.log4j.Level.TRACE);
            put (Level.DEBUG, org.apache.log4j.Level.DEBUG);
            put (Level.INFO, org.apache.log4j.Level.INFO);
            put (Level.WARN, org.apache.log4j.Level.WARN);
            put (Level.ERROR, org.apache.log4j.Level.ERROR);
        }};


    //

    static {
        // TODO Configure logger from external file
        org.apache.log4j.BasicConfigurator.configure ();
    }


    /* ***********************************************************************
     * Provider interface
     * ***********************************************************************/

    @Override
    public String name () {
        return "log4j";
    }


    @Override
    public LevelLogger createLevelLogger (
        final Level level, final String subsystem
    ) {
        return new Log4jLevelLogger (
            __levels__.get (level),
            org.apache.log4j.Logger.getLogger (subsystem)
        );
    }


    @Override
    public void setGlobalLevel (final Level level) {
        org.apache.log4j.Logger.getRootLogger ().setLevel (
            __levels__.get (level)
        );
    }


    /* ***********************************************************************
     * Log4jLevelLogger
     * ***********************************************************************/

    private static final class Log4jLevelLogger implements LevelLogger {
        private final org.apache.log4j.Logger __logger;
        private final org.apache.log4j.Level __level;

        //

        private Log4jLevelLogger (
            final org.apache.log4j.Level level,
            final org.apache.log4j.Logger logger
        ) {
            __logger = logger;
            __level = level;
        }


        @Override
        public boolean isEnabled () {
            return __logger.isEnabledFor (__level);
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
