package ch.usi.dag.disl.coderep;

import java.lang.reflect.Method;


public class StaticContextMethod {

    /**
     * The identifier of the static context method. The identifier does not
     * include full method signature, so there can be no method overloading.
     * This is OK, because static context methods cannot have any parameters.
     * <p>
     * TODO LB: Is the id really necessary?
     */
    private final String __id;

    private final Method __method;

    /**
     * The owner of the static context method.
     * <p>
     * TODO LB: What's wrong with {@link Method#getDeclaringClass()}?
     */
    private final Class <?> __referencedClass;


    public StaticContextMethod (
        final String id, final Method method, final Class <?> referencedClass
    ) {
        __id = id;
        __method = method;
        __referencedClass = referencedClass;
    }


    public String getId () {
        return __id;
    }


    public Method getMethod () {
        return __method;
    }


    public Class <?> getReferencedClass () {
        return __referencedClass;
    }

}
