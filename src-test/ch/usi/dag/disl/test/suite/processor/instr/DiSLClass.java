package ch.usi.dag.disl.test.suite.processor.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@SyntheticLocal
	public static String flag = "Start";

	@Before(marker = BodyMarker.class, order = 0, scope = "TargetClass.m*")
	public static void insideMethod(MethodStaticContext ci, ArgumentProcessorContext pc, DynamicContext dc) {		
		System.out.println("(In) Method " + ci.thisMethodName() + ": ");
		System.out.println(flag);
		
		pc.apply(ProcessorTest.class, ArgumentProcessorMode.METHOD_ARGS);

		Object receiver = pc.getReceiver(ArgumentProcessorMode.METHOD_ARGS);
		if (receiver != null) { 
			System.out.println("Receiver is " + receiver.getClass().getCanonicalName());
		} else {
			System.out.println("Receiver is null");
		}
		
		if (dc.getThis() != null) {
			System.out.println("This is " + dc.getThis().getClass().getCanonicalName());
		} else {
			System.out.println("This is null");
		}
		
		System.out.println(flag);
		System.out.println(ProcessorTest.flag);
	}
	
	@Before(marker = BytecodeMarker.class, args="invokevirtual", order = 0, scope = "TargetClass.m*")
	public static void beforeInvocation(MethodStaticContext ci, ArgumentProcessorContext pc) {		
		System.out.println("(Before) Method : ");
		
		pc.apply(ProcessorTest.class, ArgumentProcessorMode.CALLSITE_ARGS);
		
		Object receiver = pc.getReceiver(ArgumentProcessorMode.CALLSITE_ARGS);
		if (receiver != null) { 
			System.out.println("Receiver is " + receiver.getClass().getCanonicalName());
		} else {
			System.out.println("Receiver is null");
		}
		
		System.out.println(ProcessorTest.flag);
	}
	
	@Before(marker = BytecodeMarker.class, args="aastore", order = 1, scope = "TargetClass.main")
	public static void beforeArrayStore(MethodStaticContext ci, ArgumentProcessorContext pc) {		
		System.out.println("(Before) Array : ");
		
		pc.apply(ProcessorTest2.class, ArgumentProcessorMode.METHOD_ARGS);
	}
	
	@Before(marker = BodyMarker.class, order = 1, scope = "TargetClass.method1")
	public static void onlyProc(MethodStaticContext ci, ArgumentProcessorContext pc) {		
		pc.apply(ProcessorTest.class, ArgumentProcessorMode.METHOD_ARGS);
	}
}
