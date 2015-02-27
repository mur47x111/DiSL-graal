package ch.usi.dag.disl.test.suite.cflow.app;


public class TargetClass {
	

	public void foo() {
		System.out.println("app: TargetClass.foo() - begin");
		goo();
		System.out.println("app: TargetClass.foo() - end");
	}
	
	public void goo() {
		System.out.println("app: TargetClass.goo()");
	}
	
	public void hoo() {
		System.out.println("app: TargetClass.hoo() - begin");	
		goo();
		System.out.println("app: TargetClass.hoo() - end");
	}
	
	public static void main(String[] args) {
		TargetClass c = new TargetClass();
		c.foo();
		c.hoo();
	}
}
