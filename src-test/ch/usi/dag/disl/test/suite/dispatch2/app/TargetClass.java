package ch.usi.dag.disl.test.suite.dispatch2.app;
public class TargetClass {

	public static void main(final String[] args) throws InterruptedException {

		final long start = System.nanoTime();

		final int COUNT = 2000000;

		final TargetClass ta[] = new TargetClass[COUNT];

		int i;

		for(i = 0; i < COUNT; ++i) {
			ta[i] = new TargetClass();
		}

        System.out.println("Allocated " + i + " objects");
		//System.out.println("Allocated " + i + " objects in "
		//		+ (System.nanoTime() - start) / 1000000 + " ms");
	}
}
