package ch.usi.dag.disl.test.suite.bbmarker.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BasicBlockMarker;
import ch.usi.dag.disl.marker.PreciseBasicBlockMarker;
import ch.usi.dag.disl.staticcontext.BasicBlockStaticContext;

public class DiSLClass {

	@Before(marker = PreciseBasicBlockMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void preconditiona() {
		System.out.println("disl: a enter bb");
	}

	@AfterReturning(marker = PreciseBasicBlockMarker.class, scope = "TargetClass.print(boolean)", order = 1)
	public static void postconditionb() {
		System.out.println("disl: a exit bb");
	}

	@Before(marker = BasicBlockMarker.class, scope = "TargetClass.print(boolean)", order = 2)
	public static void precondition1(BasicBlockStaticContext bba) {
		System.out.println("disl: b enter bb index=" + bba.getBBindex()	+ " size=" + bba.getBBSize());
	}

	@AfterReturning(marker = BasicBlockMarker.class, scope = "TargetClass.print(boolean)", order = 3)
	public static void postcondition1() {
		System.out.println("disl: b exit bb");
	}
}
