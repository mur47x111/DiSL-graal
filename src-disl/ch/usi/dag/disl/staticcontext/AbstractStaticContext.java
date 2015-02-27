package ch.usi.dag.disl.staticcontext;

import ch.usi.dag.disl.snippet.Shadow;


/**
 * Provides a trivial {@link StaticContext} interface implementation, which just
 * holds the static context data in a protected field.
 * <p>
 * TODO LB: This class does not seem to carry its own weight.
 */
public abstract class AbstractStaticContext implements StaticContext {

    protected Shadow staticContextData;


    public void staticContextData (final Shadow shadow) {
        staticContextData = shadow;
    }

}
