package ch.usi.dag.disl.localvar;

import org.objectweb.asm.Type;


public abstract class AbstractLocalVar {

    private final static String NAME_DELIM = ".";

    //

    private final String className;

    private final String fieldName;

    private final Type type;

    //

    public AbstractLocalVar (
        final String className, final String fieldName, final Type type
    ) {
        this.className = className;
        this.fieldName = fieldName;
        this.type = type;
    }


    public String getID () {
        return fqFieldNameFor (className, fieldName);
    }


    public String getOwner () {
        return className;
    }


    public String getName () {
        return fieldName;
    }

    public Type getType () {
        return type;
    }

    //

    /**
     * Returns a fully qualified internal field name for the given class name
     * and field name.
     *
     * @param ownerClassName
     *      internal name of the field owner class
     * @param fieldName
     *      name of the field within the class
     *
     * @return
     *      Fully qualified field name.
     */
    public static String fqFieldNameFor (
        final String ownerClassName, final String fieldName
    ) {
        return ownerClassName + NAME_DELIM + fieldName;
    }
}
