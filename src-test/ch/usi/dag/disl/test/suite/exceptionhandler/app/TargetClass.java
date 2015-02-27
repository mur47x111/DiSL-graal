package ch.usi.dag.disl.test.suite.exceptionhandler.app;

public class TargetClass {

	public void print(boolean branch) {
		try {
			if (branch) {
				throw new Exception();
			}
			Integer.valueOf("1.0");
		} catch (NumberFormatException e) {
			System.out.println("app: TargetClass.print(..) - catch:NumberFormatException");
		} catch (Exception e) {
			System.out.println("app: TargetClass.print(..) - catch:Exception");
		}
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		System.out.println("app: TargetClass.main(..) - .print(false)");
		t.print(false);
		System.out.println("app: TargetClass.main(..) - .print(true)");
		t.print(true);
	}
}
