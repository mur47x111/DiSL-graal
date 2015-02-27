package ch.usi.dag.disl.util;

import ch.usi.dag.disl.DiSL;
import ch.usi.dag.util.logging.Logger;


/**
 * Utility class to provide logging services tailored to the needs of the
 * framework.
 *
 * @author Lubomir Bulej
 */
public final class Logging {

    /**
     * Package name of the framework entry class.
     */
    private static final String
        __OLD_PREFIX__ = DiSL.class.getPackage ().getName ();

    /**
     * Default prefix for top-level logs.
     */
    private static final String
        __NEW_PREFIX__ = "disl";

    /**
     * Register provider property alias with the logging class.
     */
    static {
        Logger.registerProviderAlias ("disl.logging.provider");
        Logger.registerLevelAlias ("disl.logging.level");
    }

    //

    private Logging () {
        // pure static class - not to be instantiated
    }

    //

    public static Logger getPackageInstance () {
        //
        // Determine the package this method was called from and strip common
        // prefix to get tighter, more local names.
        //
        final StackTraceElement caller =
            Thread.currentThread ().getStackTrace () [2];

        return Logger.getPackageInstance (
            caller, __OLD_PREFIX__, __NEW_PREFIX__
        );
    }

}
