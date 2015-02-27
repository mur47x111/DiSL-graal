package ch.usi.dag.disl;

import java.lang.instrument.ClassFileTransformer;


/**
 * Represents a bytecode-level class transformer. A {@link Transfomer} is
 * expected to consume an array of bytes representing the contents of a class
 * file, process it, and return an array of bytes representing the class file
 * contents of the the modified class, or {@code null} if the class was not
 * modified in any way. This mimics the convention established by the
 * {@link ClassFileTransformer} interface.
 * <p>
 * Transformers are applied to classes before being processed by DiSL, and need
 * to be specified using the {@code DiSL-Transformers} attribute in the manifest
 * of a {@code .jar} file containing the instrumentation classes. The attribute
 * lists the names of classes implementing the {@link Transformer} interface,
 * separated by a comma. The transformer are applied in the listed order.
 * <p>
 * <b>Note:</b> The implementation of a {@link Transformer} implementation
 * <b>MUST</b> be thread-safe, and the implementing class <b>MUST</b> provide a
 * default (parameterless) constructor.
 */
public interface Transformer {

    /**
     * Transforms the given class bytecode and returns the bytecode of the
     * transformed class.
     *
     * @param classFileBytes
     *        the class to be transformed
     * @return the bytecode of the modified class or {@code null} if the class
     *         was not modified in any way.
     */
    byte [] transform (byte [] classFileBytes) throws Exception;

}
