package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * Indicates that a field is used for passing data between several snippets
 * inlined in the same method. Accesses to the field are translated to accesses
 * to a local variable within the method. By default, the local variable is
 * initialized to the assigned value or the default value of a corresponding
 * type. It is possible to disable the initialization using optional
 * "initialize" annotation parameter.
 * <p>
 * <b>Note:</b> Initialization can be done only within field definition. The
 * Java <code>static { ... }</code> construct is not supported for variable
 * initialization and could result in invalid instrumentation.
 * <p>
 * This annotation can be only used with fields. The fields should be
 * declared {@code static} and, unless shared between multiple DiSL classes,
 * {@code private}.
 */
@Documented
@Target (ElementType.FIELD)
public @interface SyntheticLocal {
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
     * @see ch.usi.dag.disl.annotation.SyntheticLocal
     */
    public enum Initialize {
        ALWAYS, NEVER, BEST_EFFORT
    }


    /**
     * Initialization mode of the synthetic local variable.
     *
     * Default value: Initialize.ALWAYS
     */
    Initialize initialize() default (Initialize.ALWAYS);
}
