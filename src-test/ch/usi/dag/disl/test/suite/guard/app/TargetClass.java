package ch.usi.dag.disl.test.suite.guard.app;

public class TargetClass {
	
	public TargetClass() {
		System.out.println("app: TargetClass()");
	}
	
	public void method(Object o, int i) {
		System.out.println("app: TargetClass.method(..)");
	}
	
	public static void main(String[] args) {
		System.out.println("app: TargetClass.main(..)");
		TargetClass t = new TargetClass();
		t.method(new Integer(0), 0);		
	}
}
