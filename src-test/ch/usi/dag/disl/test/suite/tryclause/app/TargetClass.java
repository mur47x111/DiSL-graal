package ch.usi.dag.disl.test.suite.tryclause.app;
public class TargetClass {
	
	public void print(boolean branch) {
		try{
			System.out.println("app TargetClass.print(..) - try:begin");
			if (branch){
				System.out.println("app TargetClass.print(..) - throw");
				throw new Exception();
			}			
			System.out.println("app TargetClass.print(..) - try:end");
		}catch (Exception e) {
			System.out.println("app TargetClass.print(..) - catch");
		}
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		System.out.println("app TargetClass.main(..) - .print(false)");
		t.print(false);
		System.out.println("app TargetClass.main(..) - .print(true)");
		t.print(true);
	}
}
