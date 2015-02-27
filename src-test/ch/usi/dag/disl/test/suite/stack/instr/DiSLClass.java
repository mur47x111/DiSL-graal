package ch.usi.dag.disl.test.suite.stack.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;

public class DiSLClass {
	@AfterReturning(marker = BytecodeMarker.class, args="new", scope = "TargetClass.*")
	public static void precondition(DynamicContext dc) {
		dc.getStackValue(0, Object.class);
	}
}
