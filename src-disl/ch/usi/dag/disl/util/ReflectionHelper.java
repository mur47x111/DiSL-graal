package ch.usi.dag.disl.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.exception.ReflectionException;

public final class ReflectionHelper {

    /**
     * Creates an instance of given class using a constructor with the given
     * parameters.
     *
     * @param classToInstantiate
     *        the class to instantiate
     * @param args
     *        constructor arguments
     * @return new instances of the given class
     * @throws ReflectionException
     *         if the class could not be instantiated
     */
    public static <T> T createInstance(
        final Class<T> classToInstantiate, final Object... args
    ) throws ReflectionException {
        try {
            // resolve constructor argument types
            final Class <?> [] argTypes = new Class <?> [args.length];
            for (int i = 0; i < args.length; ++i) {
                argTypes [i] = args [i].getClass ();
            }

            // find an appropriate constructor
            final Constructor<?> ctor = classToInstantiate.getConstructor(argTypes);

            // make the constructor accessible and invoke it
            ctor.setAccessible (true);
            return classToInstantiate.cast (ctor.newInstance (args));

        } catch (final Exception e) {
            throw new ReflectionException (
                e, "failed to instantiate %s", classToInstantiate.getName ()
            );
        }
    }

    //

    /**
     * Resolves the given ASM {@link Type} to a Java {@link Class}.
     *
     * @param type the type to resolve
     * @return instance of {@link Class} corresponding to the given type
     * @throws ReflectionException if the class could not be resolved
     */
    public static Class <?> resolveClass (final Type type)
    throws ReflectionException {
        try {
            return __classForType (type);

        } catch (final ClassNotFoundException e) {
            throw new ReflectionException (
                e, "failed to resolve class %s", type.getClassName ()
            );
        }
    }


    /**
     * Resolves the given ASM {@link Type} to a Java {@link Class}.
     * Returns {@code null} if the class could not be resolved.
     *
     * @param type the type to resolve
     * @return instance of {@link Class} corresponding to the given type
     */
    public static Class <?> tryResolveClass (final Type type) {
        try {
            return __classForType (type);

        } catch (final ClassNotFoundException e) {
            return null;
        }
    }


    private static Class <?> __classForType (final Type type)
    throws ClassNotFoundException {
        return Class.forName (type.getClassName ());
    }

    //

    /**
     * Looks for a method with the given name in the given class and returns a
     * {@link Method} instance reflecting that method.
     *
     * @param ownerClass the class in which to look for the method
     * @param methodName the method name to look for
     * @return a {@link Method} instance corresponding to the name
     * @throws {@link ReflectionException} if there is no such method.
     */
    public static Method resolveMethod (
        final Class <?> ownerClass, final String methodName
    ) throws ReflectionException {
        try {
            return __getClassMethod (ownerClass, methodName);

        } catch (final NoSuchMethodException e) {
            throw new ReflectionException (
                "could not find method %s in class %s",
                methodName, ownerClass.getName ()
            );
        }
    }


    /**
     * Looks for a method with the given name in the given class and returns a
     * {@link Method} instance reflecting that method.
     *
     * @param ownerClass the class in which to look for the method
     * @param methodName the method name to look for
     * @return a {@link Method} instance corresponding to the name or
     *         {@code null} if there is no such method.
     */
    public static Method tryResolveMethod (
        final Class <?> ownerClass, final String methodName
    ) {
        try {
            return __getClassMethod (ownerClass, methodName);

        } catch (final NoSuchMethodException e) {
            return null;
        }
    }


    private static Method __getClassMethod (
        final Class <?> ownerClass, final String methodName
    ) throws NoSuchMethodException {
        return ownerClass.getMethod (methodName);
    }


    /**
     * Determines whether a given class (or any of its super-classes) implements
     * a particular interface (either directly or through extension).
     *
     * @param classToSearch the class to search for an implemented interface
     * @param interfaceToImplement the interface to check for
     * @return {@code true} if a class implements the given interface
     */
    public static boolean implementsInterface (
        final Class <?> classToSearch, final Class <?> interfaceToImplement
    ) {
        //
        // Look for @interfaceToImplement among interfaces implemented by
        // the classes along the path from @classToSearch towards the root.
        // If a particular interface does not match, look (recursively) also in
        // the set of interfaces it extends.
        //
        for (
            Class <?> currentClass = classToSearch;
            currentClass != null;
            currentClass = currentClass.getSuperclass ()
        ) {
            for (final Class <?> iface : currentClass.getInterfaces ()) {
                if (iface.equals (interfaceToImplement)) {
                    return true;
                } else if (implementsInterface (iface, interfaceToImplement)) {
                    return true;
                }
            }
        }

        return false;
    }

}
