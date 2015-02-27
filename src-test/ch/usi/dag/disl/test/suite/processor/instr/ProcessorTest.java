package ch.usi.dag.disl.test.suite.processor.instr;

import ch.usi.dag.disl.annotation.ArgumentProcessor;
import ch.usi.dag.disl.annotation.ProcessAlso;
import ch.usi.dag.disl.annotation.ProcessAlso.Type;
import ch.usi.dag.disl.annotation.SyntheticLocal;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;

@ArgumentProcessor
public class ProcessorTest {

    @SyntheticLocal
    public static String flag;

    public static void objPM(Object c, ArgumentContext ac, MethodStaticContext msc) {
        System.out.println("processor for object in method " + msc.thisMethodFullName());
        System.out.println(ac.getPosition());
        System.out.println(ac.getTotalCount());
        System.out.println(ac.getTypeDescriptor());
        if (c != null) {
            System.out.println(c.getClass().getCanonicalName());
        } else {
            System.out.println("null");
        }
        System.out.println("--------------------");

        DiSLClass.flag = "OMG this is for the End";
    }

    @ProcessAlso(types={Type.SHORT, Type.BYTE, Type.BOOLEAN})
    public static void intPM(int c, ArgumentContext ac, DynamicContext dc) {
        System.out.println("processor for int");
        System.out.println(ac.getPosition());
        System.out.println(ac.getTotalCount());
        System.out.println(ac.getTypeDescriptor());
        if (dc.getThis() != null) {
            System.out.println(dc.getThis().getClass().getCanonicalName());
        } else {
            System.out.println("null");
        }
        System.out.println("--------------------");

        flag = "Processor flag for the End";
    }

    public static void longPM(long c, ArgumentContext ac) {
        System.out.println("processor for long");
        System.out.println(ac.getPosition());
        System.out.println(ac.getTotalCount());
        System.out.println(ac.getTypeDescriptor());
        System.out.println("--------------------");
    }

    public static void doublePM(double c, ArgumentContext ac) {
        System.out.println("processor for double");
        System.out.println(ac.getPosition());
        System.out.println(ac.getTotalCount());
        System.out.println(ac.getTypeDescriptor());
        System.out.println("--------------------");
    }
}
