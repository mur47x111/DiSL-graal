package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Marks a guard method. Guard methods allow filtering the locations in program
 * code where a snippet should be applied.
 * <p>
 * This annotation can be only used with methods.
 * <p>
 * A guard method should be {@code static} and stateless.
 * <p>
 * Method arguments can be of type {@link Shadow}, {@link StaticContext},
 * {@link GuardContext}. For an {@link ArgumentProcessor} guard,
 * the {@link ArgumentContext} type is also permitted.
 */
@Documented
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME) // to resolve annotation using reflection
public @interface GuardMethod {

}
