package ch.usi.dag.disl.test.suite.after3.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {
	
	@AfterThrowing(marker = BodyMarker.class, scope = "TargetClass.e*", order = 0)
	public static void throwing(MethodStaticContext sc) {
		System.out.println("disl: throwing from " + sc.thisMethodName());
	}
	
	@AfterReturning(marker = BodyMarker.class, scope = "TargetClass.e*", order = 0)
	public static void returning(MethodStaticContext sc) {
		System.out.println("disl: returning from " + sc.thisMethodName());
	}
}
