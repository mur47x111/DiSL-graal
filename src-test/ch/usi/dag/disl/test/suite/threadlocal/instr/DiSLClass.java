package ch.usi.dag.disl.test.suite.threadlocal.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.ThreadLocal;
import ch.usi.dag.disl.marker.BodyMarker;

public class DiSLClass {
    
	@ThreadLocal
	static String tlv = "ahoj";
	
	
	@Before(marker = BodyMarker.class, scope = "*.foo*", order=0)
	public static void precondition() {
		System.out.println("pre \t" + Thread.currentThread().toString() + " \t tlv " +  tlv);
		tlv = "hello";
	}
	
	@After(marker = BodyMarker.class, scope = "*.foo*", order=0)
	public static void postcondition() {
		System.out.println("post \t" + Thread.currentThread().toString() + " \t tlv "  + tlv);
	}
}
