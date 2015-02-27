package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import ch.usi.dag.disl.classcontext.ClassContext;
import ch.usi.dag.disl.dynamiccontext.DynamicContext;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.staticcontext.StaticContext;


/**
 * Indicates that the class contains methods for processing method arguments
 * with corresponding types. Methods corresponding to argument types will be
 * invoked with the type and value of the argument. The invocations of the
 * argument processor methods will be inlined into each snippet that uses the
 * argument processor.
 * <p>
 * An argument processor <b>method</b> has the argument type as its first
 * parameter. Only primitive, {@link String}, and {@link Object} types are
 * allowed. In some cases, the type accepted by the argument processor method
 * can be extended using the {@link ProcessAlso} annotation. At runtime, the
 * second parameter of the argument processor will contain the value of the
 * argument being processed.
 * <p>
 * Additional information (e.g. position) about the argument can be obtained
 * using the {@link ArgumentContext} interface. To use it, an argument processor
 * method must have a parameter of type {@link ArgumentContext}. Other allowed
 * contexts are {@link StaticContext}, {@link DynamicContext}, and
 * {@link ClassContext}.
 * <p>
 * All argument processor methods must be static, may not return any values, and
 * may not throw any exceptions.
 */
@Documented
@Target (ElementType.TYPE)
public @interface ArgumentProcessor {

}
