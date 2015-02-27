package ch.usi.dag.disl.localvar;

import org.objectweb.asm.Type;


public class ThreadLocalVar extends AbstractLocalVar {

    private Object defaultValue;
    private boolean inheritable;

    public ThreadLocalVar(String className, String fieldName, Type type,
            boolean inheritable) {

        super(className, fieldName, type);
        this.inheritable = inheritable;
    }

    public String getTypeAsDesc() {
        return getType().getDescriptor();
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isInheritable() {
        return inheritable;
    }
}
