package ch.usi.dag.disl.test.suite.bodymarker.instr;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;

public class DiSLClass {
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void precondition() {
		
		final String one = "1";
		final String otherOne = "1";
		
		System.out.println("Precondition!");
		
		if(one.equals(otherOne)) {
			System.out.println("Precondition: This should be printed");
			return;
		}
		
		System.out.println("Precondition: This should NOT be printed");
	}
	
	@AfterReturning(marker = BodyMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void postcondition() {
		System.out.println("Postcondition!");
	}
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.print(boolean)", order = 2)
	public static void precondition2() {
		System.out.println("Precondition2!");
	}
	
	@AfterReturning(marker = BodyMarker.class, scope = "TargetClass.print(boolean)", order = 3)
	public static void postcondition2() {
		System.out.println("Postcondition2!");
	}
	
	@After(marker = BodyMarker.class, scope = "TargetClass.empty", order=0)
	public static void emptypostcondition() {
		System.out.println("..");
	}
}
