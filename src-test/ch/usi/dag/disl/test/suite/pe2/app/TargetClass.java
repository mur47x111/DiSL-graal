package ch.usi.dag.disl.test.suite.pe2.app;

public class TargetClass {
	
	public TargetClass() {
		System.out.println("app: TargetClass()");
	}

	private void private_method() {
		System.out.println("app: TargetClass.private_method()");
	}

	public void public_method() {
		System.out.println("app: TargetClass.public_method()");
	}

	public static void main(String[] args) {
		System.out.println("app: TargetClass.main(..)");
		TargetClass t = new TargetClass();
		t.private_method();
		t.public_method();
	}
}
