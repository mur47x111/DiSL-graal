package ch.usi.dag.disl.test.suite.args.app;


public class TargetClass {
	 

	public void test_int(int i) {
		System.out.println("app: TargetClass.test_int");
	}
	
	public void test_Integer(Integer i) {
		System.out.println("app: TargetClass.test_Integer");
	}
	
	public void test_float(float f) {
		System.out.println("app: TargetClass.test_float");
	}
	
	public void test_Float(Float f) {
		System.out.println("app: TargetClass.test_Float");
	}
	
	public void test_double(double d) {
		System.out.println("app: TargetClass.test_double");
	}
	
	public void test_Double(Double d) {
		System.out.println("app: TargetClass.test_Double");
	}
		
	public void test_String(String s) {
		System.out.println("app: TargetClass.test_String");
	}
	
	public void test_Object(Object o) {
		System.out.println("app: TargetClass.test_Object");
	}
	
	public void test_ObjectArray(Object[] oa) {
		System.out.println("app: TargetClass.test_ObjectArray");
	}
	
	public void test_MultipleObjectArray(Object[] oa1, Object[] oa2) {
		System.out.println("app: TargetClass.test_MultipleObjectArray");
	}
	
	public void test_VariableObjectArray(Object[]... oas) {
		System.out.println("app: TargetClass.test_VaribleObjectArray");
	}

	public static void main(String[] args) {
		System.out.println("app: TargetClass.main - begin");

		TargetClass t = new TargetClass();
		t.test_int(42);
		t.test_Integer(new Integer(42));	
		
		t.test_float(42.42f);
		t.test_Float(new Float(42.42f));	
		
		t.test_double(42.42);
		t.test_Double(new Double(42.42));	
		
		t.test_String("my test string");
		
		t.test_Object("my test object string");		

		t.test_ObjectArray(new Object[] {"str1", "str2", "str3"});

		t.test_MultipleObjectArray(new Object[] {"1str1", "1str2", "1str3"}, new Object[] {"2str1", "2str2", "2str3"});
		
		t.test_VariableObjectArray(new Object[] {"1str1", "1str2", "1str3"}, new Object[] {"2str1", "2str2", "2str3"});
		
		/*t.method1();
		t.method2(1, 2, 3);
		t.method3("object", "string");
		t.method3("object2", "string2");
		t.method3(t, "THIS IS THE REF TO THIS");
		t.method3(1, 2, "object");
		t.method4(new String[] { "hello" }, new int[] { 1, 2, 3, 4, 5 });
		TargetClass.isValidChar('c');*/
		
		System.out.println("app: TargetClass.main - end");
	}
	
}


