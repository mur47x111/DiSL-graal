package ch.usi.dag.disl.test.suite.tryclause.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.TryClauseMarker;

public class DiSLClass {
	
	@Before(marker = TryClauseMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void before() {
		System.out.println("disl: before");
	}
	
	@AfterReturning(marker = TryClauseMarker.class, scope = "TargetClass.print(boolean)", order = 1)
	public static void afterReturning() {
		System.out.println("disl: afterReturning");
	}
	
	@AfterThrowing(marker = TryClauseMarker.class, scope = "TargetClass.print(boolean)", order = 2)
	public static void afterThrowing() {
		System.out.println("disl: afterThrowing");
	}
}
