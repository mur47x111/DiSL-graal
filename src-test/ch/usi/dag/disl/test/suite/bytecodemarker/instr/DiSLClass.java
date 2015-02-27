package ch.usi.dag.disl.test.suite.bytecodemarker.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.staticcontext.BytecodeStaticContext;

public class DiSLClass {
	
	@Before(marker = BytecodeMarker.class, args="aload, if_icmpne", scope = "TargetClass.main")
	public static void precondition(BytecodeStaticContext ba) {
		System.out.println(ba.getBytecodeNumber());
	}
}
