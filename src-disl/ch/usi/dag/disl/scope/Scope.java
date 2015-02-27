package ch.usi.dag.disl.scope;

/**
 * Represents a snippet scope and allows matching the scope against classes and
 * methods. The implementation <b>MUST</b> be thread-safe.
 */
public interface Scope {

    /**
     * Determines whether this scope matches the given class name (including
     * package name), method name, and method type descriptor.
     *
     * @param className
     *        standard or internal name of the class to match
     * @param methodName
     *        name of the method to match
     * @param methodDesc
     *        type descriptor of the method to match
     * @return {@code true} if the scope matches the given class name, method
     *         name, and method type descriptor, {@code false} otherwise.
     */
    boolean matches (String className, String methodName, String methodDesc);

}
