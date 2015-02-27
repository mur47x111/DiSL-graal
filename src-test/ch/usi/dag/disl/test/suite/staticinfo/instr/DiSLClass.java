package ch.usi.dag.disl.test.suite.staticinfo.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.this_is_a_method_name", order = 0)
	public static void precondition(MethodStaticContext ci) {
		
		String mid = ci.thisMethodName();
		System.out.println(mid);
		
		// caching test
		String mid2 = ci.thisMethodName();
		System.out.println(mid2);
	}
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.this_is_a_method_name", order = 1)
	public static void secondPrecondition(MethodStaticContext ci) {
		
		// caching test
		String mid3 = ci.thisMethodName();
		System.out.println(mid3);
	}
}
