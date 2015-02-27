package ch.usi.dag.disl.resolver;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ch.usi.dag.disl.exception.GuardException;

/**
 * Note that all methods accessing and working with singleton has to be
 * thread-safe.
 */
public class GuardResolver {

    private static GuardResolver instance = null;

    // Guard to guard method map
    private Map<Class<?>, GuardMethod> guardToMethod =
            new HashMap<Class<?>, GuardMethod>();

    public synchronized GuardMethod getGuardMethod(
            Class<?> guardClass) throws GuardException {

        GuardMethod guardMethod = guardToMethod.get(guardClass);

        // resolved from cache
        if(guardMethod != null) {
            return guardMethod;
        }

        // no cache hit

        // check all methods
        for(Method method : guardClass.getMethods()) {

            if(method.isAnnotationPresent(
                    ch.usi.dag.disl.annotation.GuardMethod.class)) {

                // detect multiple annotations
                if(guardMethod != null) {
                    throw new GuardException("Detected several "
                            + GuardMethod.class.getName()
                            + " annotations on guard class "
                            + guardClass.getName());
                }

                guardMethod = new GuardMethod(method);
            }
        }

        // detect no annotation
        if(guardMethod == null) {
            throw new GuardException("No "
                    + ch.usi.dag.disl.annotation.GuardMethod.class.getName()
                    + " annotation on some public method in guard class "
                    + guardClass.getName());
        }

        // make the method accessible and put it into cache
        guardMethod.getMethod ().setAccessible (true);
        guardToMethod.put(guardClass, guardMethod);

        return guardMethod;
    }

    public static synchronized GuardResolver getInstance() {

        if (instance == null) {
            instance = new GuardResolver();
        }
        return instance;
    }
}
