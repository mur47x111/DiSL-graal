package ch.usi.dag.disl.resolver;

import java.util.HashMap;
import java.util.Map;

import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.staticcontext.StaticContext;
import ch.usi.dag.disl.util.ReflectionHelper;


public class SCResolver {

    // NOTE: This is internal DiSL cache. For user static context cache see
    // ch.usi.dag.disl.staticcontext.cache.StaticContextCache

    private static SCResolver instance;

    // list of static context instances
    // validity of an instance is for whole instrumentation run
    // instances are created lazily when needed
    private Map <Class <?>, Object>
        staticContextInstances = new HashMap <Class <?>, Object> ();


    public synchronized StaticContext getStaticContextInstance (
        final Class <?> staticContextClass, final Shadow shadow
    ) throws ReflectionException {
        //
        // Get a static context instance from cache, or create a new one and
        // cache it for later use. Populate it with shadow data and return it
        // as StaticContext interface.
        //
        Object sc = staticContextInstances.get (staticContextClass);
        if (sc == null) {
            sc = ReflectionHelper.createInstance (staticContextClass);
            staticContextInstances.put (staticContextClass, sc);
        }

        final StaticContext result = (StaticContext) sc;
        result.staticContextData (shadow);
        return result;
    }


    public static synchronized SCResolver getInstance () {
        if (instance == null) {
            instance = new SCResolver ();
        }
        return instance;
    }

}
