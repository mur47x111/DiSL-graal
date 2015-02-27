package ch.usi.dag.disl.dynamicbypass;

/**
 * Determines whether to bypass instrumented code. This version always returns
 * {@code true}, so that the instrumented code is always bypassed. This is
 * required during the JVM bootstrap phase to avoid perturbing class loading
 * before the JVM is fully initialized. After the bootstrap, this class will be
 * redefined to one of the two other versions, one of which completely disables
 * bypassing of instrumented code, and the other checks a thread-local flag to
 * determine whether to bypass instrumented code dynamically.
 */
public final class BypassCheck {

    public static boolean executeUninstrumented () {
        return true;
    }

}
