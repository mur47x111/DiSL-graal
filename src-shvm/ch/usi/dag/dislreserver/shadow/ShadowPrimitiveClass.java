package ch.usi.dag.dislreserver.shadow;

import org.objectweb.asm.Type;

public class ShadowPrimitiveClass extends ShadowClass {

    private Type t;

    ShadowPrimitiveClass(long net_ref, ShadowObject classLoader, Type t) {
        super(net_ref, classLoader);

        this.t = t;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public ShadowClass getComponentType() {
        return null;
    }

    @Override
    public boolean isInstance(ShadowObject obj) {
        return false;
    }

    @Override
    public boolean isAssignableFrom(ShadowClass klass) {
        return equals(klass);
    }

    @Override
    public boolean isInterface() {
        return false;
    }

    @Override
    public boolean isPrimitive() {
        return true;
    }

    @Override
    public boolean isAnnotation() {
        return false;
    }

    @Override
    public boolean isSynthetic() {
        return false;
    }

    @Override
    public boolean isEnum() {
        return false;
    }

    @Override
    public String getName() {
        return t.getClassName();
    }

    @Override
    public String getCanonicalName() {
        return getName();
    }

    @Override
    public String[] getInterfaces() {
        return new String[0];
    }

    @Override
    public String getPackage() {
        return null;
    }

    @Override
    public ShadowClass getSuperclass() {
        return null;
    }

    @Override
    public FieldInfo[] getFields() {
        return new FieldInfo[0];
    }

    @Override
    public FieldInfo getField(String fieldName) throws NoSuchFieldException {
        throw new NoSuchFieldException(t.getClassName() + "." + fieldName);
    }

    @Override
    public MethodInfo[] getMethods() {
        return new MethodInfo[0];
    }

    @Override
    public MethodInfo getMethod(String methodName, String[] argumentNames)
            throws NoSuchMethodException {
        throw new NoSuchMethodException(t.getClassName() + "." + methodName
                + argumentNamesToString(argumentNames));
    }

    @Override
    public String[] getDeclaredClasses() {
        return new String[0];
    }

    @Override
    public FieldInfo[] getDeclaredFields() {
        return new FieldInfo[0];
    }

    @Override
    public FieldInfo getDeclaredField(String fieldName)
            throws NoSuchFieldException {
        throw new NoSuchFieldException(t.getClassName() + "." + fieldName);
    }

    @Override
    public MethodInfo[] getDeclaredMethods() {
        return new MethodInfo[0];
    }

    @Override
    public MethodInfo getDeclaredMethod(String methodName,
            String[] argumentNames) throws NoSuchMethodException {
        throw new NoSuchMethodException(t.getClassName() + "." + methodName
                + argumentNamesToString(argumentNames));
    }

}
