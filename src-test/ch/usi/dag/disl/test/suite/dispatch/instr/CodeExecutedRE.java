package ch.usi.dag.disl.test.suite.dispatch.instr;

import ch.usi.dag.dislre.REDispatch;

// Optimally, this class is automatically created on analysis machine
// and redefines during loading the CodeExecuted class on the client vm

// Even more optimally, this is automatically generated native class with same
// functionality
public class CodeExecutedRE {

	private static short beId = REDispatch.registerMethod(
			"ch.usi.dag.disl.test.suite.dispatch.instr.CodeExecuted.bytecodesExecuted");

	private static short tbId = REDispatch.registerMethod(
			"ch.usi.dag.disl.test.suite.dispatch.instr.CodeExecuted.testingBasic");

	private static short taId = REDispatch.registerMethod(
			"ch.usi.dag.disl.test.suite.dispatch.instr.CodeExecuted.testingAdvanced");

	private static short ta2Id = REDispatch.registerMethod(
			"ch.usi.dag.disl.test.suite.dispatch.instr.CodeExecuted.testingAdvanced2");

	private static short tnId = REDispatch.registerMethod(
			"ch.usi.dag.disl.test.suite.dispatch.instr.CodeExecuted.testingNull");

	public static void bytecodesExecuted(final int count) {

		final byte orderingid = 1;
		REDispatch.analysisStart(beId, orderingid);

		REDispatch.sendInt(count);

		REDispatch.analysisEnd();
	}

	public static void testingBasic(final boolean b, final byte by, final char c, final short s, final int i,
			final long l, final float f, final double d) {
		REDispatch.analysisStart(tbId);

		REDispatch.sendBoolean(b);
		REDispatch.sendByte(by);
		REDispatch.sendChar(c);
		REDispatch.sendShort(s);
		REDispatch.sendInt(i);
		REDispatch.sendLong(l);
		REDispatch.sendFloat(f);
		REDispatch.sendDouble(d);

		REDispatch.analysisEnd();
	}

	public static void testingAdvanced(final String s, final Object o, final Class<?> c,
			final Thread t) {

		REDispatch.analysisStart(taId);

		REDispatch.sendObjectPlusData(s);
		REDispatch.sendObject(o);
		REDispatch.sendObject(c);
		REDispatch.sendObjectPlusData(t);

		REDispatch.analysisEnd();
	}

	public static void testingAdvanced2(final Object o1, final Object o2, final Object o3,
			final Object o4, final Class<?> class1, final Class<?> class2,
			final Class<?> class3, final Class<?> class4) {

		REDispatch.analysisStart(ta2Id);

		REDispatch.sendObject(o1);
		REDispatch.sendObject(o2);
		REDispatch.sendObject(o3);
		REDispatch.sendObject(o4);
		REDispatch.sendObject(class1);
		REDispatch.sendObject(class2);
		REDispatch.sendObject(class3);
		REDispatch.sendObject(class4);

		REDispatch.analysisEnd();
	}

	public static void testingNull(final String s, final Object o, final Class<?> c) {

		REDispatch.analysisStart(tnId);

		REDispatch.sendObjectPlusData(s);
		REDispatch.sendObject(o);
		REDispatch.sendObject(c);

		REDispatch.analysisEnd();
	}
}
