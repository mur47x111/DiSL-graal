package ch.usi.dag.disl.test.suite.gettarget.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;

public class DiSLClass {

	@Before(marker = BytecodeMarker.class, args = "invokestatic", scope = "*.foo")
	public static void getTarget(ArgumentProcessorContext apc) {
		Object target = apc.getReceiver(ArgumentProcessorMode.CALLSITE_ARGS);
		if (target != null) {
			System.out.printf("disl: invokestatic %s\n", target.getClass().getCanonicalName());
		} else {
			System.out.printf("disl: invokestatic null\n");
		}
	}

	@Before(marker = BytecodeMarker.class, args = "invokevirtual", scope = "*.foo")
	public static void getTarget(GetTargetAnalysis gta, DynamicContext dc) {
		Object target = dc.getStackValue(gta.calleeParCount(), Object.class);if (target != null) {
			System.out.printf("disl: invokevirtual %s\n", target.getClass().getCanonicalName());
		} else {
			System.out.printf("disl: invokevirtual null\n");
		}
	}
}
