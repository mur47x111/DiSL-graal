package ch.usi.dag.disl.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;


/**
 * Extends the set of primitive integer types (except {@code long}) accepted
 * by an argument processor method:
 * <ul>
 * <li>for {@code int} argument processor, it allows to process also
 * {@code short}, {@code byte}, and {@code boolean} types;
 * <li>for {@code short} argument processor, it allows to process also
 * {@code byte}, and {@code boolean} types.
 * <li>for {@code byte} argument processor, it allows to process also
 * {@code boolean} type.
 * </ul>
 */
@Documented
@Target (ElementType.METHOD)
public @interface ProcessAlso {
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
    // TODO Consider support for {@code long} types.
    //

    /**
     * @see ch.usi.dag.disl.annotation.ProcessAlso
     */
    public enum Type {
        BOOLEAN, BYTE, SHORT
    }


    Type [] types();
}
