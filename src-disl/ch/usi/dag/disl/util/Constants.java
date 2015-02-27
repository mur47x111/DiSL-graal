package ch.usi.dag.disl.util;

public final class Constants {

    final public static String STATIC_CONTEXT_METHOD_DELIM = ".";

    final public static char PACKAGE_STD_DELIM = '.';
    final public static char PACKAGE_INTERN_DELIM = '/';

    final public static String CONSTRUCTOR_NAME = "<init>";
    final public static String STATIC_INIT_NAME = "<clinit>";

    final public static String CLASS_DELIM = ".";

    final public static String CLASS_EXT = ".class";

    public static final boolean isConstructorName (final String name) {
        return CONSTRUCTOR_NAME.equals (name);
    }

    public static final boolean isInitializerName (final String name) {
        return STATIC_INIT_NAME.equals (name);
    }

}
