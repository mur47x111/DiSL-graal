package ch.usi.dag.disl.test.suite.after3.app;
public class TargetClass {
	
	public static void main(String[] args) {

		try {
			System.out.println("app: TargetClass.main(..) - .ethrowing1()");
			ethrowing1();
			System.out.println("app: TargetClass.main(..) - ~ethrowing1()");
		} catch (Exception e) {
			System.out.println("app: TargetClass.main(..) - catch");
		}
		
		try {
			System.out.println("app: TargetClass.main(..) - .ereturning1()");
			ereturning1();
			System.out.println("app: TargetClass.main(..) - ~ereturning1()");
		} catch (Exception e) {
			System.out.println("app: TargetClass.main(..) - catch");
		}
		
		try {
			System.out.println("app: TargetClass.main(..) - .ethrowing2()");
			ethrowing2();
			System.out.println("app: TargetClass.main(..) - ~ethrowing2()");
		} catch (Exception e) {
			System.out.println("app: TargetClass.main(..) - catch");
		}
		
		try {
			System.out.println("app: TargetClass.main(..) - ~ereturning2()");
			ereturning2();
			System.out.println("app: TargetClass.main(..) - ~ereturning2()");
		} catch (Exception e) {
			System.out.println("app: TargetClass.main(..) - catch");
		}
	}
	
	public static void ethrowing1() 
			throws Exception {
		try {
			if("a".equals("a")) {
				throw new Exception();
			}
			return;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void ereturning1() 
			throws Exception {
		try {
			if("a".equals("a")) {
				//throw new Exception();
			}
			return;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public static void ethrowing2() 
			throws Exception {
		throw new Exception();
	}
	
	public static void ereturning2() 
			throws Exception {
		//throw new Exception();
	}
}
