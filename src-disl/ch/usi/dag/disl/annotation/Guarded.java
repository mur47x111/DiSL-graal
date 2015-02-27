package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * Annotation used in {@link ArgumentProcessor} to guard specific methods.
 * <p>
 * This annotation can be only used with methods.
 */
@Documented
@Target (ElementType.METHOD)
public @interface Guarded {
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
     * The guard class defining if the processor method will be inlined or not.
     */
    Class <? extends Object> guard();

}
