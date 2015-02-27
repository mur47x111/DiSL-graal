package ch.usi.dag.disl.test.suite.dispatch.instr;

import ch.usi.dag.dislreserver.remoteanalysis.RemoteAnalysis;
import ch.usi.dag.dislreserver.shadow.ShadowClass;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowString;
import ch.usi.dag.dislreserver.shadow.ShadowThread;

// NOTE that this class is not static anymore
public class CodeExecuted extends RemoteAnalysis {

	long totalExecutedBytecodes = 0;

	public void bytecodesExecuted(final int count) {
		totalExecutedBytecodes += count;
	}

	public void testingBasic(final boolean b, final byte by, final char c, final short s, final int i, final long l,
			final float f, final double d) {
		if(b != true) {
			throw new RuntimeException("Incorect transfer of boolean");
		}

		if(by != (byte) 125) {
			throw new RuntimeException("Incorect transfer of byte");
		}

		if(c != 's') {
			throw new RuntimeException("Incorect transfer of char");
		}

		if(s != (short) 50000) {
			throw new RuntimeException("Incorect transfer of short");
		}

		if(i != 100000) {
			throw new RuntimeException("Incorect transfer of int");
		}

		if(l != 10000000000L) {
			throw new RuntimeException("Incorect transfer of long");
		}

		if(f != 1.5F) {
			throw new RuntimeException("Incorect transfer of float");
		}

		if(d != 2.5) {
			throw new RuntimeException("Incorect transfer of double");
		}
	}

	public static void testingAdvanced(final ShadowObject s, final ShadowObject o, final ShadowObject c, final ShadowObject t) {

		if(! (s instanceof ShadowString)) {
			throw new RuntimeException("This string should be transfered as string");
		}

		if(! s.toString().equals("Corect transfer of String")) {
			throw new RuntimeException("Incorect transfer of String");
		}

		// object id should be non 0
		if(o.getId() == 0) {
			throw new RuntimeException("Object id should not be null");
		}

		System.out.println("Received object id: " + o.getId());

		if(! (o instanceof ShadowString)) {
			throw new RuntimeException("This string should be transfered as string");
		}

		if(((ShadowString)o).toString() != null) {
			throw new RuntimeException("This string should be transfered without data");
		}

		if(! (c instanceof ShadowClass)) {
			throw new RuntimeException("This class should be transfered as class");
		}

		if(! (t instanceof ShadowThread)) {
			throw new RuntimeException("This thread should be transfered as thread");
		}

		System.out.println("Received thread: " + ((ShadowThread) t).getName() + " is deamon " + ((ShadowThread) t).isDaemon());
	}

	public static void printClassInfo(final ShadowClass sc) {

		if(sc == null) {
			System.out.println("null");
			return;
		}

		System.out.println("name: " + sc.getName());
	}

	public static void testingAdvanced2(final ShadowObject o1, final ShadowObject o2,
			final ShadowObject o3, final ShadowObject o4, final ShadowClass class1,
			final ShadowClass class2, final ShadowClass class3, final ShadowClass class4) {

		System.out.println("* o1 class *");
		printClassInfo(o1.getShadowClass());

		System.out.println("* o2 class *");
		printClassInfo(o2.getShadowClass());

		System.out.println("* o3 class *");
		printClassInfo(o3.getShadowClass());

		System.out.println("* o4 class *");
		printClassInfo(o4.getShadowClass());

		System.out.println("* class 1 *");
		printClassInfo(class1);

		System.out.println("* class 2 *");
		printClassInfo(class2);

		System.out.println("* class 3 *");
		printClassInfo(class3);

		System.out.println("* class 4 *");
		printClassInfo(class4);
	}

	public static void testingNull(final ShadowString s, final ShadowObject o, final ShadowClass c) {

		if(s != null) {
			throw new RuntimeException("String is not null");
		}

		if(o != null) {
			throw new RuntimeException("Object is not null");
		}

		if(c != null) {
			throw new RuntimeException("Class is not null");
		}
	}

	@Override
    public void atExit() {
		System.out.println("Total number of executed bytecodes: "
				+ totalExecutedBytecodes);
	}

	@Override
    public void objectFree(final ShadowObject netRef) {
		System.out.println("Object free for id " + netRef.getId());

	}
}
