package ch.usi.dag.disl.test.suite.loop.app;

public class TargetClass {

	public void print() {
		
		for (int i = 0; i<4; i++){
			System.out.printf("app: %d\n", i);
		}
		
		return;
	}

	public static void main(String[] args) {
		TargetClass t = new TargetClass();
		t.print();
	}
}
