package ch.usi.dag.disl.test.suite.exception.app;

public class TargetClass {

	public void foo(int i, int j) {

	}

	public void bar() {
		this.foo(1, 2);
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		t.bar();
	}
}
