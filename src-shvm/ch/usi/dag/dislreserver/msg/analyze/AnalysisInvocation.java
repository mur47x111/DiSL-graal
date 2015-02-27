package ch.usi.dag.dislreserver.msg.analyze;

import java.lang.reflect.Method;


public final class AnalysisInvocation {

    private final Method __method;
    private final Object __target;
    private final Object [] __args;

    //

    public AnalysisInvocation (
        final Method method, final Object target, final Object [] args
    ) {
        __method = method;
        __target = target;
        __args = args;
    }

    public void invoke () {
        try {
            __method.invoke (__target, __args);

        } catch (final Exception e) {
            // report error during analysis invocation

            System.err.format (
                "DiSL-RE: exception in analysis %s.%s(): %s\n",
                __method.getDeclaringClass ().getName (),
                __method.getName (), e
            );

            final Throwable cause = e.getCause ();
            if (cause != null) {
                cause.printStackTrace (System.err);
            }
        }
    }
}
