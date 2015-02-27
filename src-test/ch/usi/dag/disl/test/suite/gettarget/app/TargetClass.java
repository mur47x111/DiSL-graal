package ch.usi.dag.disl.test.suite.gettarget.app;

class TargetClassA {
	public void foo() {
		System.out.println("app: TargetClassA.foo(..)");
		TargetClassB.callFoo(0, 1);
	}
}

class TargetClassB {
	public void foo(int i, int j) {
		System.out.println("app: TargetClassB.foo(..)");
	}

	public static void callFoo(int i, int j) {
		TargetClassB b = new TargetClassB();
		b.foo(i, j);
	}
}

public class TargetClass {

	public static void main(String[] args) {
		System.out.println("app: TargetClass.main(..)");
		TargetClassA a = new TargetClassA();
		a.foo();
	}
}
