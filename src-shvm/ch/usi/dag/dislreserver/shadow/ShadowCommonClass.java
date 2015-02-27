package ch.usi.dag.dislreserver.shadow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.MethodNode;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;

class ShadowCommonClass extends ShadowClass {

    // TODO ! is this implementation of methods really working ??

    private ShadowClass superClass;
    private ClassNode   classNode;

    private int         access;
    private String      name;

    ShadowCommonClass(long net_ref, String classSignature,
            ShadowObject classLoader, ShadowClass superClass, byte[] classCode) {
        super(net_ref, classLoader);

        this.superClass = superClass;

        if (classCode == null || classCode.length == 0) {
            throw new DiSLREServerFatalException("Creating class info for "
                    + classSignature + " with no code provided");
        }

        initializeClassInfo(classCode);
    }

    private List<MethodInfo> methods;
    private List<MethodInfo> public_methods;
    private List<FieldInfo>  fields;
    private List<FieldInfo>  public_fields;
    private List<String>     innerclasses;

    private void initializeClassInfo(byte[] classCode) {

        ClassReader classReader = new ClassReader(classCode);
        classNode = new ClassNode(Opcodes.ASM4);
        classReader.accept(classNode, ClassReader.SKIP_DEBUG
                | ClassReader.EXPAND_FRAMES);

        access = classNode.access;
        name = classNode.name.replace('/', '.');

        methods = new ArrayList<MethodInfo>(classNode.methods.size());
        public_methods = new LinkedList<MethodInfo>();

        for (MethodNode methodNode : classNode.methods) {

            MethodInfo methodInfo = new MethodInfo(methodNode);
            methods.add(methodInfo);

            if (methodInfo.isPublic()) {
                public_methods.add(methodInfo);
            }
        }

        fields = new ArrayList<FieldInfo>(classNode.fields.size());
        public_fields = new LinkedList<FieldInfo>();

        for (FieldNode fieldNode : classNode.fields) {

            FieldInfo fieldInfo = new FieldInfo(fieldNode);
            fields.add(fieldInfo);

            if (fieldInfo.isPublic()) {
                public_fields.add(fieldInfo);
            }
        }

        if (getSuperclass() != null) {

            for (MethodInfo methodInfo : getSuperclass().getMethods()) {
                public_methods.add(methodInfo);
            }

            for (FieldInfo fieldInfo : getSuperclass().getFields()) {
                public_fields.add(fieldInfo);
            }
        }

        innerclasses = new ArrayList<String>(classNode.innerClasses.size());

        for (InnerClassNode innerClassNode : classNode.innerClasses) {
            innerclasses.add(innerClassNode.name);
        }
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
        // return equals(obj.getSClass());
        throw new DiSLREServerFatalException(
                "ShadowCommonClass.isInstance not implemented");
    }

    @Override
    public boolean isAssignableFrom(ShadowClass klass) {
        // while (klass != null) {
        //
        // if (klass.equals(this)) {
        // return true;
        // }
        //
        // klass = klass.getSuperclass();
        // }
        //
        // return false;
        throw new DiSLREServerFatalException(
                "ShadowCommonClass.isAssignableFrom not implemented");
    }

    @Override
    public boolean isInterface() {
        return (access & Opcodes.ACC_INTERFACE) != 0;
    }

    @Override
    public boolean isPrimitive() {
        return false;
    }

    @Override
    public boolean isAnnotation() {
        return (access & Opcodes.ACC_ANNOTATION) != 0;
    }

    @Override
    public boolean isSynthetic() {
        return (access & Opcodes.ACC_SYNTHETIC) != 0;
    }

    @Override
    public boolean isEnum() {
        return (access & Opcodes.ACC_ENUM) != 0;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCanonicalName() {
        throw new DiSLREServerFatalException(
                "ShadowCommonClass.getCanonicalName not implemented");
    }

    @Override
    public String[] getInterfaces() {
        return classNode.interfaces.toArray(new String[0]);
    }

    @Override
    public String getPackage() {

        int i = name.lastIndexOf('.');

        if (i != -1) {
            return name.substring(0, i);
        } else {
            return null;
        }
    }

    @Override
    public ShadowClass getSuperclass() {
        return superClass;
    }

    public FieldInfo[] getFields() {

        // to have "checked" array :(
        return public_fields.toArray(new FieldInfo[0]);
    }

    public FieldInfo getField(String fieldName) throws NoSuchFieldException {

        for (FieldInfo fieldInfo : fields) {
            if (fieldInfo.isPublic() && fieldInfo.getName().equals(fieldName)) {
                return fieldInfo;
            }
        }

        if (getSuperclass() == null) {
            throw new NoSuchFieldException(name + "." + fieldName);
        }

        return getSuperclass().getField(fieldName);
    }

    public MethodInfo[] getMethods() {

        // to have "checked" array :(
        return public_methods.toArray(new MethodInfo[0]);
    }

    public MethodInfo getMethod(String methodName, String[] argumentNames)
            throws NoSuchMethodException {

        for (MethodInfo methodInfo : public_methods) {
            if (methodName.equals(methodInfo.getName())
                    && Arrays.equals(argumentNames,
                            methodInfo.getParameterTypes())) {
                return methodInfo;
            }
        }

        throw new NoSuchMethodException(name + "." + methodName
                + argumentNamesToString(argumentNames));
    }

    public FieldInfo[] getDeclaredFields() {
        return fields.toArray(new FieldInfo[0]);
    }

    public FieldInfo getDeclaredField(String fieldName)
            throws NoSuchFieldException {

        for (FieldInfo fieldInfo : fields) {
            if (fieldInfo.getName().equals(fieldName)) {
                return fieldInfo;
            }
        }

        throw new NoSuchFieldException(name + "." + fieldName);
    }

    public MethodInfo[] getDeclaredMethods() {
        return methods.toArray(new MethodInfo[methods.size()]);
    }

    public String[] getDeclaredClasses() {
        return innerclasses.toArray(new String[innerclasses.size()]);
    }

    public MethodInfo getDeclaredMethod(String methodName,
            String[] argumentNames) throws NoSuchMethodException {

        for (MethodInfo methodInfo : methods) {
            if (methodName.equals(methodInfo.getName())
                    && Arrays.equals(argumentNames,
                            methodInfo.getParameterTypes())) {
                return methodInfo;
            }
        }

        throw new NoSuchMethodException(name + "." + methodName
                + argumentNamesToString(argumentNames));
    }

}
