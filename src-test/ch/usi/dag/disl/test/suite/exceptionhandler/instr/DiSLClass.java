package ch.usi.dag.disl.test.suite.exceptionhandler.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.ExceptionHandlerMarker;

public class DiSLClass {
	
	@Before(marker = ExceptionHandlerMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void before() {
		System.out.println("disl: before");
	}
	
	@AfterReturning(marker = ExceptionHandlerMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void afterReturning() {
		System.out.println("disl: afterReturning");
	}
}
