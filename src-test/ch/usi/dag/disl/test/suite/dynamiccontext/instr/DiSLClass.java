package ch.usi.dag.disl.test.suite.dynamiccontext.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.staticcontext.FieldAccessStaticContext;
import ch.usi.dag.disl.test.suite.dynamiccontext.app.TargetClass;

public class DiSLClass {

    /**
     * Prints the names and values of static fields of this class accessed
     * by the test method.
     */
    @AfterReturning(marker = BytecodeMarker.class, args = "GETSTATIC", scope = "TargetClass.printStaticFields", order = 0)
    public static void printStaticFieldsRead (final FieldAccessStaticContext fasc, final DynamicContext dc) {
        if ("ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass".equals (fasc.getOwnerInternalName ())) {
            System.out.printf ("disl: %s=%s\n", fasc.getName (), dc.getStaticFieldValue (
                fasc.getOwnerInternalName (), fasc.getName (), fasc.getDescriptor (), Object.class
            ));
        }
    }


    /**
     * Prints the names and values of selected static fields of this class.
     */
    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.printStaticFields", order = 1)
    public static void printSpecificStaticFieldsTedious (final DynamicContext dc) {
        final String format = "disl: tedious %s=%s\n";

        //

        final Class <?> staticType = dc.getStaticFieldValue (
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "STATIC_TYPE", "Ljava/lang/Class;", Class.class
        );

        System.out.printf (format, "STATIC_TYPE", staticType);

        //

        final String staticName = dc.getStaticFieldValue (
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "STATIC_NAME", "Ljava/lang/String;", String.class
        );

        System.out.printf (format, "STATIC_NAME", staticName);

        //

        final int staticRand = dc.getStaticFieldValue (
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "STATIC_RAND", "I", int.class
        );

        System.out.printf (format, "STATIC_RAND", staticRand);

        //

        final double staticMath = dc.getStaticFieldValue (
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "STATIC_MATH", "D", double.class
        );

        System.out.printf (format, "STATIC_MATH", staticMath);
    }

    /**
     * Prints the names and values of selected static fields of this class.
     */
    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.printStaticFields", order = 2)
    public static void printSpecificStaticFieldConcise (final DynamicContext dc) {
        final String format = "disl: concise %s=%s\n";

        //

        final Class <?> staticType = dc.getStaticFieldValue (
            TargetClass.class, "STATIC_TYPE", Class.class
        );

        System.out.printf (format, "STATIC_TYPE", staticType);

        //

        final String staticName = dc.getStaticFieldValue (
            TargetClass.class, "STATIC_NAME", String.class
        );

        System.out.printf (format, "STATIC_NAME", staticName);

        //

        final int staticRand = dc.getStaticFieldValue (
            TargetClass.class, "STATIC_RAND", int.class
        );

        System.out.printf (format, "STATIC_RAND", staticRand);

        //

        final double staticMath = dc.getStaticFieldValue (
            TargetClass.class, "STATIC_MATH", double.class
        );

        System.out.printf (format, "STATIC_MATH", staticMath);
    }

    //

    /**
     * Prints the names and values of instance fields of this class accessed
     * by the test method.
     */
    @AfterReturning(marker = BytecodeMarker.class, args = "GETFIELD", scope = "TargetClass.printInstanceFields", order = 0)
    public static void printInstanceFieldsRead (final FieldAccessStaticContext fasc, final DynamicContext dc) {
        if ("ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass".equals (fasc.getOwnerInternalName ())) {
            System.out.printf ("disl: %s=%s\n", fasc.getName (), dc.getInstanceFieldValue (
                dc.getThis (), fasc.getOwnerInternalName (), fasc.getName (), fasc.getDescriptor (), Object.class
            ));
        }
    }

    /**
     * Prints the names and values of selected instance fields of this class.
     */
    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.printInstanceFields", order = 1)
    public static void printSpecificInstanceFieldsTedious (final DynamicContext dc) {
        final String format = "disl: tedious %s=%s\n";

        final Class <?> instType = dc.getInstanceFieldValue (
            dc.getThis (),
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "instType", "Ljava/lang/Class;", Class.class
        );

        System.out.printf (format, "instType", instType);

        //

        final String instName = dc.getInstanceFieldValue (
            dc.getThis (),
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "instName", "Ljava/lang/String;", String.class
        );

        System.out.printf (format, "instName", instName);

        //

        final int instRand = dc.getInstanceFieldValue (
            dc.getThis (),
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "instRand", "I", int.class
        );

        System.out.printf (format, "instRand", instRand);

        //

        final double instMath= dc.getInstanceFieldValue (
            dc.getThis (),
            "ch/usi/dag/disl/test/suite/dynamiccontext/app/TargetClass",
            "instMath", "D", double.class
        );

        System.out.printf (format, "instMath", instMath);
    }


    /**
     * Prints the names and values of selected instance fields of this class.
     */
    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.printInstanceFields", order = 2)
    public static void printSpecificInstanceFieldsConcise (final DynamicContext dc) {
        final String format = "disl: concise %s=%s\n";

        final Class <?> instType = dc.getInstanceFieldValue (
            dc.getThis (), TargetClass.class, "instType", Class.class
        );

        System.out.printf (format, "instType", instType);

        //

        final String instName = dc.getInstanceFieldValue (
            dc.getThis (), TargetClass.class, "instName", String.class
        );

        System.out.printf (format, "instName", instName);

        //

        final int instRand = dc.getInstanceFieldValue (
            dc.getThis (), TargetClass.class, "instRand", int.class
        );

        System.out.printf (format, "instRand", instRand);

        //

        final double instMath= dc.getInstanceFieldValue (
            dc.getThis (), TargetClass.class, "instMath", double.class
        );

        System.out.printf (format, "instMath", instMath);
    }

}
