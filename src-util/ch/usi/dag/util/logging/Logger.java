package ch.usi.dag.util.logging;

import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import ch.usi.dag.util.Assert;
import ch.usi.dag.util.Lists;
import ch.usi.dag.util.Maps;


/**
 * Provides a simple unified logging infrastructure that can use different
 * logging frameworks.
 *
 * @author Lubomir Bulej
 */
public final class Logger {

    /**
     * Property for expressing logging provider preferences.
     */
    private static final String __PROVIDER_PROPERTY__ = "logging.provider";

    /**
     * Property for expressing logging level.
     */
    private static final String __LEVEL_PROPERTY__ = "logging.level";


    /**
     * Fall-back logging provider. Prints logging messages to standard
     * output and standard error streams.
     */
    private static final Provider __FALLBACK_PROVIDER__ = new FallbackProvider ();

    //

    /**
     * Current logging provider. The content of the reference is initialized
     * lazily when first logger instance is requested.
     */
    private static final AtomicReference <Provider> __provider__ = new AtomicReference <Provider> ();

    //
    // Level-specific loggers
    //

    private final LevelLogger __error;
    private final LevelLogger __warn;
    private final LevelLogger __info;
    private final LevelLogger __debug;
    private final LevelLogger __trace;

    //

    private Logger (final Map <Level, LevelLogger> lls) {
        __error = lls.get (Level.ERROR);
        __warn = lls.get (Level.WARN);
        __info = lls.get (Level.INFO);
        __debug = lls.get (Level.DEBUG);
        __trace = lls.get (Level.TRACE);
    }


    /* ***********************************************************************
     * Logger interface
     * ***********************************************************************/

    //
    // Error
    //

    public final boolean errorIsLoggable () {
        return __error.isEnabled ();
    }

    public final void error (final String message) {
        __error.log (message);
    }

    public final void error (final String format, final Object arg) {
        __log (__error, format, arg);
    }

    public final void error (final String format, final Object ... args) {
        __log (__error, format, args);
    }

    public final void error (final Throwable cause, final String message) {
        __error.log (cause, message);
    }

    public final void error (final Throwable cause, final String format, final Object arg) {
        __log (__error, cause, format, arg);
    }

    public final void error (final Throwable cause, final String format, final Object ... args) {
        __log (__error, cause, format, args);
    }


    //
    // Warning
    //

    public final boolean warnIsLoggable () {
        return __warn.isEnabled ();
    }

    public final void warn (final String message) {
        __warn.log (message);
    }

    public final void warn (final String format, final Object arg) {
        __log (__warn, format, arg);
    }

    public final void warn (final String format, final Object ... args) {
        __log (__warn, format, args);
    }

    public final void warn (final Throwable cause, final String message) {
        __warn.log (cause, message);
    }

    public final void warn (final Throwable cause, final String format, final Object arg) {
        __log (__warn, cause, format, arg);
    }

    public final void warn (final Throwable cause, final String format, final Object ... args) {
        __log (__warn, cause, format, args);
    }


    //
    // Info
    //

    public final boolean infoIsLoggable () {
        return __info.isEnabled ();
    }

    public final void info (final String message) {
        __info.log (message);
    }

    public final void info (final String format, final Object arg) {
        __log (__info, format, arg);
    }

    public final void info (final String format, final Object ... args) {
        __log (__info, format, args);
    }

    public final void info (final Throwable cause, final String message) {
        __info.log (cause, message);
    }

    public final void info (final Throwable cause, final String format, final Object arg) {
        __log (__info, cause, format, arg);
    }

    public final void info (final Throwable cause, final String format, final Object ... args) {
        __log (__info, cause, format, args);
    }


    //
    // Debug
    //

    public final boolean debugIsLoggable () {
        return __debug.isEnabled ();
    }

    public final void debug (final String message) {
        __debug.log (message);
    }

    public final void debug (final String format, final Object arg) {
        __log (__debug, format, arg);
    }

    public final void debug (final String format, final Object ... args) {
        __log (__debug, format, args);
    }

    public final void debug (final Throwable cause, final String message) {
        __debug.log (cause, message);
    }

    public final void debug (final Throwable cause, final String format, final Object arg) {
        __log (__debug, cause, format, arg);
    }

    public final void debug (final Throwable cause, final String format, final Object ... args) {
        __log (__debug, cause, format, args);
    }


    //
    // Trace
    //

    public final boolean traceIsLoggable () {
        return __trace.isEnabled ();
    }

    public final void trace (final String message) {
        __trace.log (message);
    }

    public final void trace (final String format, final Object arg) {
        __log (__trace, format, arg);
    }

    public final void trace (final String format, final Object ... args) {
        __log (__trace, format, args);
    }

    public final void trace (final Throwable cause, final String message) {
        __trace.log (cause, message);
    }

    public final void trace (final Throwable cause, final String format, final Object arg) {
        __log (__trace, cause, format, arg);
    }

    public final void trace (final Throwable cause, final String format, final Object ... args) {
        __log (__trace, cause, format, args);
    }


    //
    // Templates
    //

    private static void __log (
        final LevelLogger logger,
        final String format, final Object arg
    ) {
        if (logger.isEnabled ()) {
            logger.log (String.format (format, arg));
        }
    }

    private static void __log (
        final LevelLogger logger,
        final String format, final Object ... args
    ) {
        if (logger.isEnabled ()) {
            logger.log (String.format (format, args));
        }
    }

    private static void __log (
        final LevelLogger logger,
        final Throwable cause, final String format, final Object arg
    ) {
        if (logger.isEnabled ()) {
            logger.log (cause, String.format (format, arg));
        }
    }

    private static void __log (
        final LevelLogger logger,
        final Throwable cause, final String format, final Object ... args
    ) {
        if (logger.isEnabled ()) {
            logger.log (cause, String.format (format, args));
        }
    }


    /* ***********************************************************************
     * PrintStream based logger
     * ***********************************************************************/

    private static final class FallbackProvider implements Provider {
        private final AtomicReference <Level>
            __maxLevel = new AtomicReference <Level> (Level.INFO);

        @Override
        public String name () {
            return "std";
        }

        @Override
        public LevelLogger createLevelLogger (
            final Level level, final String name
        ) {
            final PrintStream printer =
                (level == Level.ERROR) ? System.err : System.out;

            return new PrintStreamLevelLogger (
                level, name, printer, __maxLevel
            );
        }

        @Override
        public void setGlobalLevel (final Level level) {
            __maxLevel.set (level);
        }

    };


    private static final class PrintStreamLevelLogger implements LevelLogger {
        private final Level __level;
        private final String __name;
        private final PrintStream __printer;
        private final AtomicReference <Level> __maxLevel;

        //

        PrintStreamLevelLogger (
            final Level level, final String name,
            final PrintStream printer, final AtomicReference <Level> maxLevel
        ) {
            __level = level;
            __name = name;
            __printer = printer;
            __maxLevel = maxLevel;
        }

        //

        @Override
        public boolean isEnabled () {
            return __level.ordinal () <= __maxLevel.get ().ordinal ();
        }

        @Override
        public void log (final String message) {
            if (isEnabled ()) {
                synchronized (__maxLevel) {
                        __log (__printer, message);
                }
            }
        }

        @Override
        public void log (final Throwable cause, final String message) {
            if (isEnabled ()) {
                synchronized (__maxLevel) {
                    __log (__printer, message, cause);
                }
            }
        }

        //

        private void __log (final PrintStream printer, final String message) {
            printer.print (__name);
            printer.print (": ");
            printer.println (message);
        }

        private void __log (
            final PrintStream printer,
            final String message,
            final Throwable cause
        ) {
            __log (printer, message);
            __log (printer, "Caused by: ");
            __log (printer, cause.getMessage ());
        }

    }


    /* ***********************************************************************
     * Logging provider configuration property aliases
     * ***********************************************************************/

    private static final List <String>
        __providerAliases__ = Lists.newLinkedList (__PROVIDER_PROPERTY__);


    /**
     * Registers a system property alias for selecting a logging provider. When
     * selecting among multiple registered providers, the value of this property
     * is checked against provider names to identify a preferred provider. If
     * the value is not set or a provider with the given name cannot be found,
     * the first registered provider will be used. If there are no registered
     * providers available, a fallback provider will be used which outputs
     * logging messages to standard error and standard output.
     *
     * @param alias
     *        a property alias to register.
     */
    public static void registerProviderAlias (final String alias) {
        Assert.stringNotEmpty (alias, "property alias");

        //

        __prependUnique (alias, __providerAliases__);
    }


    private static final List <String>
        __levelAliases__ = Lists.newLinkedList (__LEVEL_PROPERTY__);


    /**
     * Registers a system property alias for selecting an initial logging level.
     * When a provider is loaded for the first the, the value of this property
     * is used to set the initial global logging level. The logging level is not
     * set (and let to provider-specific default) if this property is not set.
     *
     * @param alias
     *        a property alias to register.
     */
    public static void registerLevelAlias (final String alias) {
        Assert.stringNotEmpty (alias, "property alias");

        //

        __prependUnique (alias, __levelAliases__);
    }

    private static <T> void __prependUnique (
        final T element, final List <? super T> target
    ) {
        synchronized (target) {
            if (! target.contains (element)) {
                target.add (0, element);
            }
        }
    }



    /* ***********************************************************************
     * Creating loggers with arbitrary names
     * ***********************************************************************/

    private static final ConcurrentHashMap <String, Logger>
        __loggers__ = Maps.newConcurrentHashMap ();


    public static Logger getInstance (final String name) {
        Assert.objectNotNull (name, "logger name");

        //
        // Check the cache for loggers with the same name, and if it is
        // not there, use a logging provider to create a logger.
        //
        Logger result = __loggers__.get (name);
        if (result == null) {
            result = __createLogger (name, __getProvider ());
            final Logger previous = __loggers__.putIfAbsent (name, result);
            if (previous != null) {
                result = previous;
            }
        }

        return result;
    }


    private static Provider __getProvider () {
        //
        // If the provider is set, use it, otherwise find a suitable provider.
        // If another thread manages to initialize the provider before us, use
        // its result, otherwise set the global logging level.
        //
        Provider result = __provider__.get ();
        if (result == null) {
            result = __createProvider ();
            if (! __provider__.compareAndSet (null, result)) {
                result = __provider__.get ();

            } else {
                __initializeProvider (result);
            }
        }

        return result;
    }


    /**
     * Obtains a logging provider. If a preferred provider cannot be found, the
     * first registered provider is used. If there are no registered providers,
     * returns a fallback provider which outputs messages to standard output and
     * standard error streams.
     */
    private static Provider __createProvider () {
        final ServiceLoader <Provider> providers = ServiceLoader.load (Provider.class);

        Provider result = __loadPreferredProvider (providers);
        if (result == null) {
            result = __loadFirstProvider (providers);
            if (result == null) {
                result = __FALLBACK_PROVIDER__;
            }
        }

        return result;
    }


    /**
     * Goes through the logging provider property aliases and looks for a
     * configured logging provider name. If found, it tries to obtain a logger
     * factory for the given provider. Returns {@code null} if no provider can
     * be found using the property aliases.
     */
    private static Provider __loadPreferredProvider (
        final ServiceLoader <Provider> providers
    ) {
        synchronized (__providerAliases__) {
            SEARCH: for (final String property : __providerAliases__) {
                final String name = System.getProperty (property);
                if (name == null || name.isEmpty ()) {
                    continue SEARCH;
                }

                final Provider provider = __findProvider (name, providers);
                if (provider != null) {
                    return provider;
                }

                //

                System.err.printf (
                    "warning: logging provider '%s' not found\n", name
                );
            }
        }

        //

        return null;
    }


    /**
     * Returns a provider with the given name or {@code null} if there is no
     * such provider.
     */
    private static Provider __findProvider (
        final String name, final ServiceLoader <Provider> providers
    ) {
        for (final Provider provider : providers) {
            if (name.equals (provider.name ())) {
                return provider;
            }
        }

        return null;
    }


    /**
     * Goes through the logging level property aliases and sets the global
     * logging level for the given provider.
     */
    private static void __initializeProvider (final Provider provider) {
        synchronized (__levelAliases__) {
            SEARCH: for (final String property : __levelAliases__) {
                final String name = System.getProperty (property);
                if (name == null || name.isEmpty ()) {
                    continue SEARCH;
                }

                final Level level = Level.parse (name);
                if (level != null) {
                    provider.setGlobalLevel (level);
                    return;
                }
            }
        }
    }


    /**
     * Returns the first logging provider or {@code null} if there is
     * no registered provider service.
     */
    private static Provider __loadFirstProvider (
        final ServiceLoader <Provider> providers
    ) {
        final Iterator <Provider> pi = providers.iterator ();
        return pi.hasNext () ? pi.next () : null;
    }


    private static Logger __createLogger (
        final String name, final Provider provider
    ) {
        //
        // Create per-level loggers and make sure that there is one for
        // each supported logging level.
        //
        final Map <Level, LevelLogger> lls = new EnumMap <Level, LevelLogger> (Level.class);
        for (final Level level : Level.values ()) {
            lls.put (level, __createLevelLogger (name, provider, level));
        }

        return new Logger (lls);
    }


    private static LevelLogger __createLevelLogger (
        final String name, final Provider provider, final Level level
    ) {
        LevelLogger ll = provider.createLevelLogger (level, name);
        if (ll == null) {
            System.err.printf (
                "warning: missing logger for level %s, using stdout\n", level
            );

            ll = new PrintStreamLevelLogger (
                level, name, System.out, new AtomicReference <Level> (level)
            );
        }

        return ll;
    }


    /* ***********************************************************************
     * Creating loggers by package
     * ***********************************************************************/

    public static Logger getPackageInstance (
        final StackTraceElement caller,
        final String oldPrefix, final String newPrefix
    ) {
        Assert.objectNotNull (caller, "caller stack trace element");
        Assert.objectNotNull (oldPrefix, "old package prefix");
        Assert.objectNotNull (newPrefix, "new package prefix");

        //

        final String loggerName = __getLoggerNameFromPackage (
            caller.getClassName (), oldPrefix, newPrefix
        );

        return Logger.getInstance (loggerName);
    }


    private static String __getLoggerNameFromPackage (
        final String className, final String oldPrefix, final String newPrefix
    ) {
        final String packageName = __getPackageName (className);
        if (packageName.startsWith (oldPrefix)) {
            return newPrefix + packageName.substring (oldPrefix.length ());
        } else {
            return packageName;
        }
    }


    private static String __getPackageName (final String className) {
        Assert.objectNotNull (className, "class name");

        //

        final int lastDotPos = className.lastIndexOf ('.');
        return (lastDotPos > 0) ? className.substring (0, lastDotPos) : "";
    }

}
