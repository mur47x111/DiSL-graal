package ch.usi.dag.disl.test.suite.loop.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BasicBlockMarker;
import ch.usi.dag.disl.staticcontext.LoopStaticContext;

public class DiSLClass {

	@Before(marker = BasicBlockMarker.class, scope = "TargetClass.print()", order = 0)
	public static void precondition(final LoopStaticContext lsc) {
		System.out.println("disl: Entering basic block."
				+ " Is this a loopstart? " + (lsc.isFirstOfLoop() ? "true" : "false"));
	}
}
