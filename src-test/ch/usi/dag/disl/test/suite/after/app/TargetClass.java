package ch.usi.dag.disl.test.suite.after.app;

public class TargetClass {

    public void print(final boolean flag) {
        try {
            System.out.println("app: TargetClass.print(..) - try:begin");

            if (flag) {
                final String float_one = "1.0";
                final Integer int_one = Integer.valueOf(float_one);
                System.out.println("app: UNREACHABLE " + int_one);
            }

            System.out.println("app: TargetClass.print(..) - try:end");
        } finally {
            System.out.println("app: TargetClass.print(..) - finally");
        }
    }

    public static void main (final String [] args) {
        try {
            final TargetClass t = new TargetClass ();
            System.out.println ("app: TargetClass.main(..) - .print(false)");
            t.print (false);
            System.out.println ("app: TargetClass.main(..) - .print(true)");
            t.print (true);
        } catch (final Throwable e) {
            System.out.println ("app: TargetClass.main(..) - catch");
        }
    }
}
