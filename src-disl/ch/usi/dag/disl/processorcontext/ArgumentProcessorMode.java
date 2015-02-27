package ch.usi.dag.disl.processorcontext;

/**
 * Decides, what argument data should be made available.
 */
public enum ArgumentProcessorMode {

	/**
	 * Arguments of the current method.
	 */
	METHOD_ARGS,
	
	/**
	 * Arguments of the method being invoked.
	 */
	CALLSITE_ARGS
}
