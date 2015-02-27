package ch.usi.dag.disl.test.suite.classinfo.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.classcontext.ClassContext;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@Before(marker = BodyMarker.class, scope = "TargetClass.print(boolean)", order = 0)
	public static void precondition(MethodStaticContext msc, ClassContext cc) {
		System.out.println("disl: " + cc.asClass( msc.thisClassName()));
	}

}
