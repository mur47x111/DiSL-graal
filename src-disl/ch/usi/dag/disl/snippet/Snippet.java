package ch.usi.dag.disl.snippet;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import ch.usi.dag.disl.DiSL.CodeOption;
import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.exception.ProcessorException;
import ch.usi.dag.disl.exception.ReflectionException;
import ch.usi.dag.disl.localvar.LocalVars;
import ch.usi.dag.disl.marker.Marker;
import ch.usi.dag.disl.processor.ArgProcessor;
import ch.usi.dag.disl.processor.ArgProcessorMethod;
import ch.usi.dag.disl.scope.Scope;


/**
 * Holds all the information about a snippet. Is analogous to
 * {@link ArgProcessorMethod}.
 */
public class Snippet implements Comparable <Snippet> {

    private final Class <?> annotationClass;
    private final Marker marker;
    private final Scope scope;
    private final Method guard;
    private final int order;
    private final SnippetUnprocessedCode __template;

    private SnippetCode __code;


    /**
     * Creates snippet structure.
     */
    public Snippet (
        final Class <?> annotationClass, final Marker marker,
        final Scope scope, final Method guard,
        final int order, final SnippetUnprocessedCode template
    ) {
        this.annotationClass = annotationClass;
        this.marker = marker;
        this.scope = scope;
        this.guard = guard;
        this.order = order;

        __template = template;
    }

    //

    /**
     * @return The canonical name of the class in which the snippet was defined.
     */
    public String getOriginClassName() {
        return __template.className ();
    }


    /**
     * @return The name of the snippet method.
     */
    public String getOriginMethodName() {
        return __template.methodName ();
    }


    /**
     * @return A fully qualified name of the snippet method.
     */
    public String getOriginName () {
        return __template.className () +"."+ __template.methodName ();
    }


    /**
     * @return The snippet annotation class.
     */
    public Class <?> getAnnotationClass () {
        return annotationClass;
    }


    /**
     * @return The marker associated with the snippet.
     */
    public Marker getMarker () {
        return marker;
    }


    /**
     * @returns The scope in which the snippet should be applied.
     */
    public Scope getScope () {
        return scope;
    }


    /**
     * @returns The guard which determines where the snippet should be applied.
     */
    public Method getGuard () {
        return guard;
    }


    /**
     * Returns the snippet weaving order. The lower the order, the closer a
     * snippet gets to the marked code location.
     *
     * @return The snippet weaving order.
     */
    public int getOrder () {
        return order;
    }


    /**
     * Returns the instantiated snippet code. Before calling this method, the
     * snippet must be initialized using the {@link #init(LocalVars, Map, Set)
     * init()} method.
     *
     * @return {@link SnippetCode} representing an instantiate snippet.
     */
    public SnippetCode getCode () {
        if (__code == null) {
            throw new IllegalStateException ("snippet not initialized");
        }

        return __code;
    }


    /**
     * Compares a snippet to another snippet. The natural ordering of snippets
     * is determined by their weaving order. A snippet with a lower weaving
     * order is considered greater and vice-versa.
     */
    @Override
    public int compareTo (final Snippet that) {
        return Integer.compare (that.order, this.order);
    }


    /**
     * Prepares a snippet for weaving by instantiating the snippet code template
     * with the given local variables, argument processors, and code options.
     * <p>
     * TODO LB: Consider actually returning an initialized copy of this
     * snippet and making this class immutable. Moreover, the state of a
     * snippet should not be determined by its static type.
     */
    public void init (
        final LocalVars locals, final Map <Type, ArgProcessor> processors,
        final Set <CodeOption> options
    ) throws DiSLInitializationException, ProcessorException, ReflectionException  {
        __code = __template.process (locals, processors, marker, options, annotationClass);
    }

}
