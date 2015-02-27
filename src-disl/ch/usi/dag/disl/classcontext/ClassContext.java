package ch.usi.dag.disl.classcontext;

/**
 * Allows converting {@link String} literals to {@link Class} instances.
 */
public interface ClassContext {

    /**
     * Converts a {@link String} literal representing an internal class name to
     * a {@link Class} instance.
     *
     * @param name an internal name of the class
     * @return {@link Class} instance corresponding to the class name, or
     *         {@code null} if the class name could not be resolved.
     */
    Class <?> asClass (String internalName);

}
