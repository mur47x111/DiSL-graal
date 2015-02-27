package ch.usi.dag.disl.dynamicbypass;

/**
 * Determines whether to bypass instrumented code. This version always returns
 * {@code false}, so that the instrumented code is never bypassed. This version
 * is to be used when dynamic bypass is disabled.
 */
public final class BypassCheck {

    public static boolean executeUninstrumented () {
        return false;
    }

}
