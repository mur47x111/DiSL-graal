package ch.usi.dag.disl.test.suite.guard.instr;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@Before(marker = BodyMarker.class, order = 0, scope = "TargetClass.m*", guard=GuardYes.class)
	public static void beforeInvocation(MethodStaticContext ci, ArgumentProcessorContext pc) {		
		System.out.println("disl: beforeInvocation");		
		pc.apply(ProcessorTest.class, ArgumentProcessorMode.METHOD_ARGS);
	}
	
	@Before(marker = BytecodeMarker.class, args="invokevirtual", order = 1, scope = "TargetClass.main", guard=GuardNo.class)
	public static void guarded(MethodStaticContext ci) {
		System.out.println("disl: UNREACHABLE");
	}
	
	@After(marker = BodyMarker.class, scope = "TargetClass.m*", guard=GuardLength.class)
	public static void codeLength(MethodStaticContext ci) {		
		System.out.printf("disl: method %s is longer then 10 instructions\n", ci.thisMethodName());
	}
}
