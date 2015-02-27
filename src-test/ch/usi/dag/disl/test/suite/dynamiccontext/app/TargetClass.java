package ch.usi.dag.disl.test.suite.dynamiccontext.app;

import java.util.Random;

public class TargetClass {

    static final Class <?> STATIC_TYPE = TargetClass.class;
    static final String STATIC_NAME = STATIC_TYPE.getSimpleName ();
    static final int STATIC_RAND = new Random (42).nextInt ();
    static final double STATIC_MATH = Math.pow (Math.E,  Math.PI);

    final Class <?> instType = getClass ();
    final String instName = instType.getSimpleName ();
    final int instRand = new Random (42).nextInt ();
    final double instMath = Math.pow (Math.PI,  Math.E);


	public static void printStaticFields () {
	    System.out.println ("app: STATIC_TYPE="+ STATIC_TYPE);
	    System.out.println ("app: STATIC_NAME="+ STATIC_NAME);
	    System.out.println ("app: STATIC_RAND="+ STATIC_RAND);
        System.out.println ("app: STATIC_MATH="+ STATIC_MATH);
	}


	public void printInstanceFields () {
        System.out.println ("app: instType="+ instType);
        System.out.println ("app: instName="+ instName);
        System.out.println ("app: instRand="+ instRand);
        System.out.println ("app: instMath="+ instMath);
    }

	public static void main(final String[] args) {
		printStaticFields ();
		new TargetClass().printInstanceFields ();
	}
}
