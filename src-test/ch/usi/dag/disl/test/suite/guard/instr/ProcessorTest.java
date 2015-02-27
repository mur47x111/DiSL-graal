package ch.usi.dag.disl.test.suite.guard.instr;

import ch.usi.dag.disl.annotation.Guarded;
import ch.usi.dag.disl.annotation.ArgumentProcessor;
import ch.usi.dag.disl.processorcontext.ArgumentContext;

@ArgumentProcessor
public class ProcessorTest {

    @Guarded(guard=GuardYes.class)
    public static void stringPM(Object c, ArgumentContext ac) {
        System.out.println("app: processor for Object");
        System.out.println("app: " + ac.getPosition());
        System.out.println("app: " + ac.getTotalCount());
        System.out.println("app: " + c.getClass().getCanonicalName());
        System.out.println("app: --------------------");
    }

    @Guarded(guard=GuardNo.class)
    public static void stringPM(int c, ArgumentContext ac) {
        System.out.println("app: processor for int");
        System.out.println("app: " + ac.getPosition());
        System.out.println("app: " + ac.getTotalCount());
        System.out.println("app: --------------------");
    }
}
