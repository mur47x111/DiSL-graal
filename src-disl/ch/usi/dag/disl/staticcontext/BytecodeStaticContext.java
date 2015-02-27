package ch.usi.dag.disl.staticcontext;

/**
 * <b>NOTE: This class is work in progress</b>
 * <br>
 * <br>
 * Provides static context information about instrumented bytecode.
 */
public class BytecodeStaticContext extends AbstractStaticContext {

	/**
	 * Returns (ASM) integer number of the instrumented bytecode.
	 */
	public int getBytecodeNumber() {
		
		return staticContextData.getRegionStart().getOpcode();
	}
}
