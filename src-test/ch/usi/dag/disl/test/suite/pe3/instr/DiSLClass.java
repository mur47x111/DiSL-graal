package ch.usi.dag.disl.test.suite.pe3.instr;

import org.objectweb.asm.Opcodes;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.staticcontext.BytecodeStaticContext;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@Before(marker = BytecodeMarker.class, args = "putfield, getfield", scope = "TargetClass.public_method", order = 0)
	public static void beforeFieldAccess(DynamicContext dc,
			BytecodeStaticContext bsc, MethodStaticContext msc) {

		String operationID;

		if (bsc.getBytecodeNumber() == Opcodes.PUTFIELD) {
			operationID = "disl: write:";
		} else {
			operationID = "disl: read:";
		}

		operationID += msc.thisClassName() + ":" + msc.thisMethodName() + ":"
				+ msc.thisMethodDescriptor();
		System.out.println(operationID);
	}

}
