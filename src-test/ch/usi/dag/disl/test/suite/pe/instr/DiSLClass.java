package ch.usi.dag.disl.test.suite.pe.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.*", order = 0)
	public static void precondition(MethodStaticContext msc) {
		
		if (msc.isMethodPrivate()){
			System.out.println("disl: private method");
		} else {
			System.out.println("disl: public method");
		}
	}
}
