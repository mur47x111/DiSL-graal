package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import ch.usi.dag.disl.classcontext.ClassContext;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.marker.Marker;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorContext;
import ch.usi.dag.disl.staticcontext.StaticContext;


/**
 * Marks a method as a DiSL snippet to be inserted after the marked code region.
 * The snippet code will be executed after an exit cause by an exception.
 * <p>
 * <b>Note:</b> This is a general contract. The actual implementation depends on
 * the particular marker used with the snippet.
 * <p>
 * The annotation has the following parameters which control the inlining of a
 * snippet into target code:
 * <ul>
 * <li>{@link #marker}
 * <li>{@link #args}
 * <li>{@link #guard}
 * <li>{@link #scope}
 * <li>{@link #order}
 * <li>{@link #dynamicBypass}
 * </ul>
 * <p>
 * This annotation can be only used with methods. In particular, a method
 * representing a snippet must be {@code static}, must not return any value, and
 * must not throw any exceptions.
 * <p>
 * The method can declare parameters the following types:
 * <ul>
 * <li>{@link StaticContext} (or another type implementing it),
 * <li>{@link DynamicContext},
 * <li>{@link ClassContext}, and
 * <li>{@link ArgumentProcessorContext}.
 * </ul>
 * <p>
 * The ordering and the number of the parameters is arbitrary.
 */
@Documented
@Target (ElementType.METHOD)
public @interface AfterThrowing {
    //
    // NOTE
    //
    // If you change any names here, you also need to change them
    // in the DiSL class parser. Only do that if absolutely necessary,
    // because this annotation is part of the DiSL public API.
    //
    // Also note that the defaults are not retrieved from here, but
    // are set in the DiSL class parser.
    //

    /**
     * Selects the marker class. A marker determines the region of code within a
     * method where to apply the snippet.
     *
     * @see ch.usi.dag.disl.marker.Marker Implementation details
     */
    Class <? extends Marker> marker();


    /**
     * Optional argument for the marker class, passed as a {@link String}.
     * <p>
     * Default value: {@code ""}, means "no arguments".
     */
    String args() default "";


    /**
     * Selects methods in which to apply the snippet.
     * <p>
     * See the {@link ch.usi.dag.disl.scope} package for more information about
     * the scoping language.
     * <p>
     * Default value: {@code "*"}, means "everywhere".
     */
    String scope() default "*";


    /**
     * Selects the guard class. A guard class determines whether a snippet
     * will be inlined at a particular location or not. In general, guards
     * provide more fine-grained control compared to scopes.
     * <p>
     * Default value: {@code void.class}, means "no guard used".
     */
    Class <? extends Object> guard() default void.class;


    /**
     * Determines snippet order when multiple snippets are to be inlined
     * at the same location. The smaller the number, the closer to the boundary
     * of the marked code region will be the snippet inlined.
     * <p>
     * Default value: {@code 100}
     */
    int order() default 100;


    /**
     * Controls automatic bypass activation. This is an advanced option that
     * allows to turn off automatic bypass activation for inlined snippets. This
     * can be used when a snippet does not use any other (instrumented) classes,
     * or when manual control over bypass activation is desired.
     * <p>
     * NOTE: Usage of dynamic bypass is determined by the underlying
     * instrumentation framework.
     * <p>
     * Default value: {@code true}, means "automatic bypass activation".
     */
    boolean dynamicBypass() default true;

}
