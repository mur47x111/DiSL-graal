package ch.usi.dag.disl.test.suite.dispatch2.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.BytecodeMarker;

public class DiSLClass {
	
	@Before(marker = BytecodeMarker.class, args= "aastore", scope = "TargetClass.*")
	public static void invokedInstr(DynamicContext dc) {
		
		CodeExecutedRE.intEvent(dc.getStackValue(1, int.class));
	}
	
	@Before(marker = BytecodeMarker.class, args= "aastore", scope = "TargetClass.*")
	public static void testing(DynamicContext dc) {
		
		CodeExecutedRE.objectEvent(dc.getStackValue(0, Object.class));
	}
}
