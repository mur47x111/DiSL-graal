package ch.usi.dag.disl.test.suite.bbmarker.app;

public class TargetClass {

	public void print(boolean branch) {
		if (branch){
			System.out.println("app: TargetClass.print(..) - true");
		}else{
			System.out.println("app: TargetClass.print(..) - false");
		}
		
		return;
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		System.out.println("app: TargetClass.main(..) - .print(false)");
		t.print(false);
		System.out.println("app: TargetClass.main(..) - ~print(false)");
		System.out.println("app: TargetClass.main(..) - .print(true)");
		t.print(true);
		System.out.println("app: TargetClass.main(..) - ~print(true)");
	}
}
