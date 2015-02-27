package ch.usi.dag.disl.processorcontext;

/**
 * <p>
 * Allows accessing method arguments and applying argument processors.
 * <ul>
 * <li>{@link #apply(Class, ArgumentProcessorMode)}</li>
 * <li>{@link #getReceiver(ArgumentProcessorMode)}</li>
 * <li>{@link #getArgs(ArgumentProcessorMode)}</li>
 * </ul>
 */
public interface ArgumentProcessorContext {

    /**
     * Applies given argument processor to method arguments, either at call-site
     * or within an invoked method.
     *
     * @param argumentProcessorClass
     *        argument processor class to apply
     * @param mode
     *        where to apply the argument processor.
     */
    void apply (Class <?> argumentProcessorClass, ArgumentProcessorMode mode);


    /**
     * Returns the receiver of the method invocation or {@code null} for static
     * methods.
     *
     * @param mode
     *        for which should be the object retrieved
     */
    Object getReceiver (ArgumentProcessorMode mode);


    /**
     * Returns an object array containing the method arguments. Note that
     * primitive types will be boxed.
     *
     * @param mode
     *        for which should be the argument array retrieved
     */
    Object [] getArgs (ArgumentProcessorMode mode);
}
