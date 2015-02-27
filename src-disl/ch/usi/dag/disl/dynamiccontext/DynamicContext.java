package ch.usi.dag.disl.dynamiccontext;

import ch.usi.dag.disl.annotation.After;
import ch.usi.dag.disl.annotation.AfterThrowing;
import ch.usi.dag.disl.staticcontext.FieldAccessStaticContext;


/**
 * Provides access to dynamic information available to a snippet at runtime.
 * This includes the {@code this} reference, exceptions being caught in a catch
 * block, values on the operand stack, method arguments, local variables, and
 * static and instance fields.
 */
public interface DynamicContext {

    /**
     * Returns {@code this} reference to snippets inlined in an instance method,
     * {@code null} for snippets inlined in a static method.
     */
    Object getThis ();


    /**
     * Returns the exception reference to snippets inlined in the {@link After}
     * the {@link AfterThrowing} context, {@code null} otherwise.
     */
    Throwable getException ();


    /**
     * Returns the value of a particular item on the JVM operand stack.
     * <p>
     * <b>Note:</b> Each item index corresponds to one operand on the stack.
     * Both primitive and wide values are considered to be a single item, i.e.,
     * the index of the corresponding stack slot is determined automatically.
     *
     * @param itemIndex
     *        index of the item on the operand stack, must be positive and not
     *        exceed the number of items on the stack. Index {@code 0} refers to
     *        an item at the top of the stack.
     * @param valueType
     *        the expected type of the accessed value. Primitive types are boxed
     *        into corresponding reference types.
     * @return The value of the selected stack item. Primitive types are boxed
     *         into corresponding reference types.
     */
    <T> T getStackValue (int itemIndex, Class <T> valueType);


    /**
     * Returns the value of a particular method argument.
     * <p>
     * <b>Note:</b> Each argument index corresponds to one method argument, be
     * it primitive or wide, i.e., the index of the corresponding local variable
     * slot is determined automatically.
     *
     * @param argumentIndex
     *        index of the desired method argument, must be positive and not
     *        exceed the number of method arguments. Index {@code 0} refers to
     *        the first argument.
     * @param valueType
     *        the expected type of the accessed value.
     * @return The value of the selected method argument. Primitive types are
     *         boxed into corresponding reference types.
     */
    <T> T getMethodArgumentValue (int argumentIndex, Class <T> valueType);


    /**
     * Returns the value of a local variable occupying a particular local
     * variable slot (or two slots, in case of wide types such as long and
     * double).
     * <p>
     * <b>Note:</b> Each slot index corresponds to one local variable slot. The
     * value of wide values is obtained from two consecutive local variable
     * slots, starting with the given slot index.
     *
     * @param slotIndex
     *        index of the desired local variable slot, must be positive and not
     *        exceed the number of local variable slots. Index {@code 0} refers
     *        to the first local variable slot.
     * @param valueType
     *        the expected type of the accessed value.
     * @return The value of the selected local variable slot. Primitive types
     *         are boxed into corresponding reference types.
     */
    <T> T getLocalVariableValue (int slotIndex, Class <T> valueType);


    /**
     * Returns the value of a given static field in a given class. This method
     * is intended for accessing fields of known types in known classes.
     *
     * @param ownerType
     *        the owner type class literal.
     * @param fieldName
     *        the name of the field to read.
     * @param fieldType
     *        the field type class literal.
     * @return The value of the given static field. Primitive types are boxed
     *         into corresponding value types.
     */
    <T> T getStaticFieldValue (
        Class <?> ownerType, String fieldName, Class <T> fieldType
    );


    /**
     * Returns the value of a given static field in a given class. This method
     * is intended for accessing unknown fields in unknown classes, typically
     * using a {@link FieldAccessStaticContext} when intercepting field
     * accesses.
     *
     * @param ownerName
     *        the internal name of the owner class.
     * @param fieldName
     *        the name of the field to read.
     * @param fieldDesc
     *        the type descriptor of the field.
     * @param valueType
     *        the expected type of the accessed value. This should be a
     *        superclass of the type described by the field descriptor. In case
     *        of primitive types, a superclass of the corresponding value type
     *        is allowed.
     * @return The value of the given static field. Primitive types are boxed
     *         into corresponding value types.
     */
    <T> T getStaticFieldValue (
        String ownerName,
        String fieldName, String fieldDesc, Class <T> valueType
    );


    <T> T getInstanceFieldValue (
        Object instance, Class <?> ownerType,
        String fieldName, Class <T> fieldType
    );


    /**
     * Returns the value of a given instance field in a given object.
     *
     * @param instance
     *        the instance of the owner class to read the field value from.
     * @param ownerClass
     *        the internal name of the owner class.
     * @param fieldName
     *        the name of the field to read.
     * @param fieldDesc
     *        the type descriptor of the field.
     * @param valueType
     *        the expected type of the accessed value. This should be a
     *        superclass of the type described by the field descriptor. In case
     *        of primitive types, a superclass of the corresponding value type
     *        is allowed.
     * @return The value of the given static field. Primitive types are boxed
     *         into corresponding value types.
     */
    <T> T getInstanceFieldValue (
        Object instance, String ownerClass,
        String fieldName, String fieldDesc, Class <T> valueType
    );

}
