package ch.usi.dag.disl.guardcontext;

/**
 * Guard context is used to invoke guard inside of other guard.
 *
 * Guard implementation has to be thread-safe.
 */
public interface GuardContext {

    /**
     * Invokes guard passed as an argument.
     *
     * @param guardClass guard to invoke
     * @return result of the invoked guard
     */
    boolean invoke(Class<?> guardClass);
}
