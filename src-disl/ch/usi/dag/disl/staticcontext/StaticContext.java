package ch.usi.dag.disl.staticcontext;

import ch.usi.dag.disl.snippet.Shadow;

/**
 * <p>
 * The static context provides information derived from code's static analysis.
 * <p>
 * There is a list of already prepared static contexts.
 * <ul>
 * <li>
 * {@link ch.usi.dag.disl.staticcontext.BasicBlockStaticContext
 * BasicBLockStaticContext - experimental}</li>
 * <li>
 * {@link ch.usi.dag.disl.staticcontext.BytecodeStaticContext
 * BytecodeStaticContext}</li>
 * <li>
 * {@link ch.usi.dag.disl.staticcontext.FieldAccessStaticContext
 * FieldAccessStaticContext}</li>
 * <li>
 * {@link ch.usi.dag.disl.staticcontext.MethodStaticContext
 * MethodStaticContext}</li>
 * <li>
 * {@link ch.usi.dag.disl.staticcontext.LoopStaticContext
 * LoopStaticContext - experimental}</li>
 * </ul>
 * <p>
 * Every static context class has to implement this interface. All static
 * context methods must adhere to the following convention:
 * <ul>
 * <li>a static context method does not have parameters</li>
 * <li>the return value can be only a primitive type or a String</li>
 * </ul>
 * In addition, a {@link StaticContext} implementation has to be thread-safe.
 */
public interface StaticContext {

    /**
     * Receives static context data. Call to this method precedes a static
     * context method invocation.
     */
    void staticContextData (Shadow shadow);
}
