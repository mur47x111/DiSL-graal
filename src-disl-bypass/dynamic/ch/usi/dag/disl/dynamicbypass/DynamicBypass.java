package ch.usi.dag.disl.dynamicbypass;

public final class DynamicBypass {

    private static final boolean debug = Boolean.getBoolean ("debug");


    //

    public static boolean isActive () {
        return Thread.currentThread ().bypass;
    }


    public static void activate () {
        if (debug) {
            // bypass should be disabled in this state
            if (Thread.currentThread ().bypass) {
                throw new RuntimeException (
                    "fatal error: dynamic bypass activated twice");
            }
        }

        Thread.currentThread ().bypass = true;
    }


    public static void deactivate () {
        if (debug) {
            // bypass should be enabled in this state
            if (!Thread.currentThread ().bypass) {
                throw new RuntimeException (
                    "fatal error: dynamic bypass deactivated twice");
            }
        }

        Thread.currentThread ().bypass = false;
    }

}
