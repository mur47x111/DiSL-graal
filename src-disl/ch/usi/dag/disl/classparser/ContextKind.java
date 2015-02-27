package ch.usi.dag.disl.classparser;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.classcontext.ClassContext;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.staticcontext.StaticContext;
import ch.usi.dag.disl.util.ReflectionHelper;

public enum ContextKind {

    STATIC (StaticContext.class) {
        private final Map <Type, Boolean> __cache = new HashMap <Type, Boolean> ();

        @Override
        public boolean matches (final Type type) {
            //
            // Static context has to implement the StaticContext interface.
            //
            final Boolean cachedResult = __cache.get (type);
            if (cachedResult != null) {
                return cachedResult;
            } else {
                final boolean result = __implementsStaticContext (type);
                __cache.put (type,  result);
                return result;
            }
        }

        private boolean __implementsStaticContext (final Type type) {
            final Class <?> typeClass = ReflectionHelper.tryResolveClass (type);
            if (typeClass != null) {
                return ReflectionHelper.implementsInterface (typeClass, _itfClass);
            } else {
                return false;
            }
        }
    },

    DYNAMIC (DynamicContext.class),

    CLASS (ClassContext.class),

    ARGUMENT (ArgumentContext.class),

    ARGUMENT_PROCESSOR (ArgumentProcessorContext.class);

    //

    protected final Class <?> _itfClass;

    protected final Type _itfType;

    //

    private ContextKind (final Class <?> itfClass) {
        _itfClass = itfClass;
        _itfType = Type.getType (_itfClass);
    }


    public boolean matches (final Type type) {
        return _itfType.equals (type);
    }

    public static ContextKind forType (final Type type) {
        //
        // Find the context that matches the type
        //
        for (final ContextKind context : ContextKind.values ()) {
            if (context.matches (type)) {
                return context;
            }
        }

        return null;
    }

}
