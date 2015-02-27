package ch.usi.dag.disl.test.suite.dynamicinfo.instr;

import ch.usi.dag.disl.annotation.AfterReturning;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.BodyMarker;
import ch.usi.dag.disl.marker.BytecodeMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

public class DiSLClass {

    @Before(marker = BytecodeMarker.class, args = "isub", scope = "TargetClass.test1", order = 0)
    public static void precondition(DynamicContext di) {
        int i = di.getStackValue(1, int.class);
        int j = di.getStackValue(0, int.class);
        System.out.println("disl: " + i + " - " + j + " = " + (i - j));
    }

    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.test1", order = 1)
    public static void postcondition(DynamicContext di) {
        int ret = di.getLocalVariableValue(1, int.class);
        System.out.println("disl: before return, local a is " + ret);
    }

    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.test2", order = 2)
    public static void postcondition2(DynamicContext di) {
        int ret = di.getStackValue(0, int.class);
        System.out.println("disl: return with " + ret);
    }

    @Before(marker = BytecodeMarker.class, args = "dsub", scope = "TargetClass.test3", order = 3)
    public static void precondition3(DynamicContext di) {
        double i = di.getStackValue(0, double.class);
        double d = di.getStackValue(1, double.class);
        System.out.println("disl: " + d + " - " + i + " = " + (d - i));
    }

    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.test3", order = 4)
    public static void postcondition3(DynamicContext di) {
        double d = di.getLocalVariableValue(1, double.class);
        System.out.println("disl: before return, local d is " + d);
        int i = di.getMethodArgumentValue(1, int.class);
        System.out.println("disl: before return, local i is " + i);
    }

    @AfterReturning(marker = BodyMarker.class, scope = "TargetClass.*", order = 5)
    public static void getThis(DynamicContext di, MethodStaticContext msc) {
        if (di.getThis() != null) {
            System.out.println("disl: " + msc.thisMethodName() + " - this: " + di.getThis().getClass().getCanonicalName());
        } else {
            System.out.println("disl: " + msc.thisMethodName() + " - this: null");
        }
    }
}
