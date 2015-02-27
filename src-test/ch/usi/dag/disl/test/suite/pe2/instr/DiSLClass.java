package ch.usi.dag.disl.test.suite.pe2.instr;

import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

	@Before(marker = BodyMarker.class, scope = "TargetClass.*", order = 0)
	public static void precondition(MethodStaticContext msc) {

		if (!msc.thisMethodName().equals("<clinit>")
				&& !(msc.thisMethodName().equals("<init>") && (msc
						.thisClassName().equals("java/lang/Object") || msc
						.thisClassName().equals("java/lang/Thread")))) {
			System.out.println("disl: go ahead");
		} else {
			System.out.println("disl: not a good idea to weave some code here");
		}

		if (msc.thisMethodName().endsWith("init>")) {
			System.out.println("disl: init or clinit");
		}

		if (String.valueOf(true).equals("true")) {
			System.out.println("disl: this should be printed");
		}
	}
}
