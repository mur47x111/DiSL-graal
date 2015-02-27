package ch.usi.dag.disl.test.suite.afterinit2.instr;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.Before;
import ch.usi.dag.disl.marker.AfterInitBodyMarker;
import ch.usi.dag.disl.staticcontext.MethodStaticContext;


public class DiSLClass {

    @Before (marker = AfterInitBodyMarker.class, scope = "*.app.Target*.<init>")
    public static void after (final MethodStaticContext msc) {
        System.out.println (msc.thisClassSimpleName () +"."+ msc.thisMethodName () + " before");
    }


    @After (marker = AfterInitBodyMarker.class, scope = "*.app.Target*.<init>")
    public static void afterThrowning (final MethodStaticContext msc) {
        System.out.println (msc.thisClassSimpleName () +"."+ msc.thisMethodName () +" after");
    }
}
