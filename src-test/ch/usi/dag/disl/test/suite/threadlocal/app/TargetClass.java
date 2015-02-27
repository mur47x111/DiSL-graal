package ch.usi.dag.disl.test.suite.threadlocal.app;

public class TargetClass {
	
	public void foo() {
		
	}
	
	public static void main(String[] args) {
		 
		new TargetClass().foo();
		
		Thread t = new Thread() {
			public void run() {
				new TargetClass().foo();
			}
		};
		t.start();
	}
}
