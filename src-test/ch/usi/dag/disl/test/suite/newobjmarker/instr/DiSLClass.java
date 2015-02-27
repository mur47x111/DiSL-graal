package ch.usi.dag.disl.test.suite.newobjmarker.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.NewObjMarker;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;



public class DiSLClass {
	
	@Before(marker = NewObjMarker.class, scope = "TargetClass.main")
	public static void beforeAlloc() {
		System.out.printf("disl: beforeAlloc\n");
	}
	
	@AfterReturning(marker = NewObjMarker.class, scope = "TargetClass.main")
	public static void afterAlloc(DynamicContext dc) {

		ch.usi.dag.disl.test.suite.newobjmarker.app.TargetClass tc = 
				dc.getStackValue(0, ch.usi.dag.disl.test.suite.newobjmarker.app.TargetClass.class);
		System.out.printf("disl: %s\n", tc.printMe);
	}
}
