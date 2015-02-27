package ch.usi.dag.disl.test.suite.newobjmarker.app;

public class TargetClass {
	
	public final String printMe;
	
	TargetClass(String printMe) {
		this.printMe = printMe;
	}

	public static void main(String[] args) {		
		TargetClass b = new TargetClass("app: hi");
		System.out.println(b.printMe);
	}
}
