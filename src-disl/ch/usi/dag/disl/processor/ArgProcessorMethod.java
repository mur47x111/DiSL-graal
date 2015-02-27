package ch.usi.dag.disl.processor;

import java.lang.reflect.Method;
import java.util.Set;

import ch.usi.dag.disl.annotation.ArgumentProcessor;
import ch.usi.dag.disl.coderep.Code;
import ch.usi.dag.disl.coderep.UnprocessedCode;
import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.snippet.Snippet;


/**
 * Represents an {@link ArgumentProcessor} method, which is analogous to a
 * {@link Snippet}.
 */
public class ArgProcessorMethod {

    private final Set <ArgProcessorKind> __types;
    private final Method __guard;
    private final UnprocessedCode __template;

    private Code __code;

    //

    public ArgProcessorMethod (
        final Set <ArgProcessorKind> types, final Method guard,
        final UnprocessedCode template
    ) {
        __types = types;
        __guard = guard;

        __template = template;
    }

    //

    /**
     * @return The canonical name of the class in which the argument processor
     *         was defined.
     */
    public String getOriginClassName () {
        return __template.className ();
    }


    /**
     * @return The name of the argument processor method.
     */
    public String getOriginMethodName () {
        return __template.methodName ();
    }


    /**
     * @return A fully qualified name of the argument processor method.
     */
    public String getOriginName () {
        return __template.className () +"."+ __template.methodName ();
    }

    //

    public boolean handlesType (final ArgProcessorKind kind) {
        return __types.contains (kind);
    }


    public Method getGuard () {
        return __guard;
    }


    /**
     * Returns the instantiated argument processor method code. Before calling
     * this method, the argument processor method must be initialized using the
     * {@link #init(LocalVars)} method.
     *
     * @return A {@link Code} instance representing the code of the instantiated
     *         argument processor method.
     */
    public Code getCode () {
        if (__code == null) {
            throw new IllegalStateException ("argument processor method not initialized");
        }

        return __code;
    }

    //

    public void init (final LocalVars locals) throws DiSLInitializationException {
        __code = __template.process (locals);
    }

}
