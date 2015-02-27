package ch.usi.dag.disl.dynamicbypass;

/**
 * Determines whether to bypass instrumented code. This version is to be used
 * with dynamic bypass and results in execution of uninstrumented code when in
 * the scope of instrumentation code.
 */
public final class BypassCheck {

    public static boolean executeUninstrumented () {
        return DynamicBypass.isActive ();
    }

}
