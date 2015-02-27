package ch.usi.dag.disl.test.suite.scope.app;

public class TargetClass {

    private void complete(final String text, final boolean b1, final boolean b2) {
        System.out.println("app: " + text + " " + b1 + " " + b2);
    }

    public static void main(final String[] args) {
        new TargetClass().complete("test", true, false);
    }
}
