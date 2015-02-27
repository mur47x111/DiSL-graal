package ch.usi.dag.disl.test.suite.staticinfo.app;
public class TargetClass {
	
	public void this_is_a_method_name() {
		System.out.println("This is the body of TargetClass.this_is_a_method_name");
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		t.this_is_a_method_name();
	}
}
