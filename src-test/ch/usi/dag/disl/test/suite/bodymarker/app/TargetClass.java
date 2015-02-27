package ch.usi.dag.disl.test.suite.bodymarker.app;
public class TargetClass {
	
	public void print(boolean branch) {
		
		System.out.println("This is the body of TargetClass.print");
		
		if(branch) {
			System.out.println("branched");
			
			return;
		}
		
		System.out.println("not branched");
	}
	
	public void empty() {
		
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		t.print(false);
		t.print(true);
	}
}
