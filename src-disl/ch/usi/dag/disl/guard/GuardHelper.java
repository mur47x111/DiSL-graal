package ch.usi.dag.disl.guard;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Set;

import ch.usi.dag.disl.exception.DiSLFatalException;
import ch.usi.dag.disl.exception.GuardException;
import ch.usi.dag.disl.exception.GuardRuntimeException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.guardcontext.GuardContext;
import ch.usi.dag.disl.processor.generator.ProcMethodInstance;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.resolver.GuardMethod;
import ch.usi.dag.disl.resolver.GuardResolver;
import ch.usi.dag.disl.resolver.SCResolver;
import ch.usi.dag.disl.snippet.Shadow;
import ch.usi.dag.disl.staticcontext.StaticContext;
import ch.usi.dag.disl.util.ReflectionHelper;

public abstract class GuardHelper {

    public static Method findAndValidateGuardMethod (
        final Class <?> guardClass, final Set <Class <?>> validArgs
    ) throws GuardException {
        if (guardClass == null) {
            return null;
        }

        // TODO LB: Cache validated methods and don't validate them again?
        final GuardMethod guardMethod = GuardResolver.getInstance ().getGuardMethod (guardClass);
        validateGuardMethod (guardMethod, validArgs);
        return guardMethod.getMethod ();
    }


    private static void validateGuardMethod (
        final GuardMethod guardMethod, final Set <Class <?>> validArgs
    ) throws GuardException {
        // quick validation
        if(guardMethod.getArgTypes() != null) {
            // only valid argument types are in the method - ok
            if (validArgs.containsAll (guardMethod.getArgTypes ())) {
                return;
            }

            // we have some additional argument types then only valid ones

            // prepare invalid argument type set
            final Set <Class <?>> invalidArgTypes =
                new HashSet <Class <?>> (guardMethod.getArgTypes ());
            invalidArgTypes.removeAll (validArgs);

            // construct the error message
            throw new GuardException (String.format (
                "Guard %s is using interface %s not allowed in this "+
                "particular case (misused guard?)",
                guardMethod.getMethod().getDeclaringClass().getName(),
                invalidArgTypes.iterator().next().getName()
            ));
        }

        // validate properly
        final Method method = guardMethod.getMethod();
        final String methodName = __fullMethodName (method);
        if (!method.getReturnType ().equals (boolean.class)) {
            throw new GuardException (
                "Guard method "+ methodName +" MUST return boolean type");
        }

        if (!Modifier.isStatic (method.getModifiers ())) {
            throw new GuardException(
                "Guard method "+ methodName +" MUST be static");
        }

        // remember argument types for quick validation
        final Set <Class <?>> argTypes = new HashSet <Class <?>> ();
        for (final Class <?> argType : method.getParameterTypes ()) {
            // throws exception in the case of invalidity
            argTypes.add (validateArgument (methodName, argType, validArgs));
        }

        guardMethod.setArgTypes (argTypes);
    }


    private static Class <?> validateArgument (
        final String guardMethodName, final Class <?> argClass, final Set <Class <?>> validArgClasses
    ) throws GuardException {
        // validate that implements one of the allowed interfaces
        for (final Class <?> allowedInterface : validArgClasses) {
            // valid
            if (argClass.equals (allowedInterface)) {
                return allowedInterface;
            }

            // valid - note that static context has to be implemented
            if (
                allowedInterface.equals (StaticContext.class) &&
                ReflectionHelper.implementsInterface (argClass, allowedInterface)
            ) {
                return allowedInterface;
            }
        }

        // invalid interface - construct the error message
        @SuppressWarnings ("resource")
        final Formatter message = new Formatter ().format (
            "Guard argument %s in %s is not in the set of "+
            "allowed interface (misused guard?): ",
            argClass.getName(), guardMethodName
        );

        String comma = "";
        for (final Class <?> allowedInterface : validArgClasses) {
            message.format ("%s%s", comma, allowedInterface.getName ());
            comma = ", ";
        }

        throw new GuardException (message.toString ());
    }

    // *** Methods tight with processor or snippet guard ***

    public static Set <Class <?>> snippetContextSet () {
        final Set <Class <?>> allowedSet = new HashSet <Class <?>> ();

        allowedSet.add (GuardContext.class);
        allowedSet.add (StaticContext.class);

        return allowedSet;
    }


    public static Set <Class <?>> processorContextSet () {
        final Set <Class <?>> allowedSet = new HashSet <Class <?>> ();

        allowedSet.add (GuardContext.class);
        allowedSet.add (StaticContext.class);
        allowedSet.add (ArgumentContext.class);

        return allowedSet;
    }


    // invoke guard method for snippet guard
    public static boolean guardApplicable (final Method guardMethod, final Shadow shadow) {
        if (guardMethod == null) {
            return true;
        }

        // no method validation needed - already validated
        return invokeGuardMethod (guardMethod, shadow, null);
    }


    // invoke guard method for processor guard
    public static boolean guardApplicable (
        final Method guardMethod, final Shadow shadow,
        final ProcMethodInstance pmi
    ) {
        if (guardMethod == null) {
            return true;
        }

        // no method validation needed - already validated
        return invokeGuardMethod (
            guardMethod, shadow, new ArgumentContextImpl (pmi)
        );
    }


    // invoke guard for processor or snippet guard
    // this is just helper method for GuardContextImpl - reduced visibility
    static boolean invokeGuard (
        final Class <?> guardClass, final Shadow shadow, final ArgumentContext ac
    ) throws GuardException {
        //
        // Find and validate the guard method first, then invoke it.
        //
        // The validation set depends on whether we are in a snippet (no
        // processor context supplied) or in an argument processor.
        //
        final GuardMethod guardMethod = GuardResolver.getInstance ().
            getGuardMethod (guardClass);

        final Set <Class <?>> validationSet = (ac == null) ?
            snippetContextSet () : processorContextSet ();

        validateGuardMethod (guardMethod, validationSet);

        return invokeGuardMethod (guardMethod.getMethod (), shadow, ac);

    }


    // invoke guard method for processor or snippet guard
    // NOTE: all calling methods should guarantee using validation method,
    // that if ArgumentContext is needed, it cannot be null
    private static boolean invokeGuardMethod (
        final Method guardMethod, final Shadow shadow, final ArgumentContext ac
    ) {
        final Class <?> [] paramTypes = guardMethod.getParameterTypes ();
        final Object [] arguments = new Object [paramTypes.length];

        for (int argIndex = 0; argIndex < arguments.length; argIndex++) {
            final Class <?> parameterType = paramTypes [argIndex];

            //
            // Provide various types of context arguments required by a guard.
            //
            if (ArgumentContext.class.equals (parameterType)) {
                if (ac == null) {
                    //
                    // Argument context is required, but none is provided.
                    //
                    throw new DiSLFatalException ("Missing argument context");
                }

                arguments [argIndex] = ac;

            } else if (GuardContext.class.equals (parameterType)) {
                arguments [argIndex] = new GuardContextImpl (shadow, ac);

            } else {
                //
                // The guard method passed validation, so here it can only
                // require static context. Get a static context instance
                // for the shadow location.
                //
                try {
                    arguments [argIndex] = SCResolver.getInstance ().
                        getStaticContextInstance (parameterType, shadow);

                } catch (final ReflectionException re) {
                    final String message = String.format (
                        "Static context initialization for guard %s failed",
                        __fullMethodName (guardMethod)
                    );
                    throw new GuardRuntimeException (message, re);
                }
            }
        }

        //
        // Invoke the guard methods with context arguments.
        //
        try {
            return (Boolean) guardMethod.invoke (null, arguments);

        } catch (final Exception e) {
            final String message = String.format (
                "Invocation of guard method %s failed",
                __fullMethodName (guardMethod)
            );
            throw new GuardRuntimeException (message, e);
        }
    }


    private static String __fullMethodName (final Method method) {
        return method.getDeclaringClass ().getName () +"."+ method.getName ();
    }

}
