package ch.usi.dag.disl.test.suite.scope.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;

public class DiSLClass {
	@Before(marker = BodyMarker.class, scope = "ch.usi.dag.disl.test.suite.scope.app.TargetClass.complete(java.lang.String,boolean,boolean)")
	public static void beforeComplete() {
	    System.out.println("disl: before");
    }
}
