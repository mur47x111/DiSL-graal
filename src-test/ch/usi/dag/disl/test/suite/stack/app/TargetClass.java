package ch.usi.dag.disl.test.suite.stack.app;

public class TargetClass {
    public void test() {
        Object obj = new Object();
        System.out.println(obj.getClass().getCanonicalName());
    }

	public static void main(String[] args) {
		new TargetClass().test();
	}
}
