package ch.usi.dag.disl.test.suite.cflow.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.ThreadLocal;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {
	
	@ThreadLocal
    static int counter;
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.foo()", order=5)
	public static void precondition() {
		 counter++;
	}
	

	@After(marker = BodyMarker.class, scope = "TargetClass.foo()", order=5)
	public static void postcondition() {
		counter--;
	}	
	
	@Before(marker = BodyMarker.class, scope = "TargetClass.*(..)", order=1)
	public static void printer(MethodStaticContext sc) {
		if (counter>0) {
			System.out.println("disl: IN CFLOW OF foo() " + sc.thisMethodFullName());
		} else {
			System.out.println("disl: NOT IN CFLOW OF foo() " + sc.thisMethodFullName());
		}		
	}
}
