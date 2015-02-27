package ch.usi.dag.disl.test.suite.guard.instr;

import ch.usi.dag.disl.annotation.GuardMethod;
import ch.usi.dag.disl.guardcontext.GuardContext;

public abstract class GuardLength {

	@GuardMethod
	public static boolean isApplicable(CodeSC csc, GuardContext gc) {

		if(gc.invoke(GuardYes.class)) {
			// code length is higher then 10 instructions
			return csc.codeLength() > 10;
		}
		
		return false;
	}
}
