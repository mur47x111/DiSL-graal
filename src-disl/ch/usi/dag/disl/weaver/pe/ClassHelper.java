package ch.usi.dag.disl.weaver.pe;

import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

public class ClassHelper {

    public static final HashSet<String> VALUE_TYPES = new HashSet<String>();

    static {
        VALUE_TYPES.add("java/lang/Boolean");
        VALUE_TYPES.add("java/lang/Byte");
        VALUE_TYPES.add("java/lang/Character");
        VALUE_TYPES.add("java/lang/Double");
        VALUE_TYPES.add("java/lang/Float");
        VALUE_TYPES.add("java/lang/Integer");
        VALUE_TYPES.add("java/lang/Long");
        VALUE_TYPES.add("java/lang/Short");
        VALUE_TYPES.add("java/lang/String");
    }

    public static Class<?> getClassFromType(Type type) {

        switch (type.getSort()) {

        case Type.BOOLEAN:
            return boolean.class;
        case Type.BYTE:
            return byte.class;
        case Type.CHAR:
            return char.class;
        case Type.DOUBLE:
            return double.class;
        case Type.FLOAT:
            return float.class;
        case Type.INT:
            return int.class;
        case Type.LONG:
            return long.class;
        case Type.SHORT:
            return short.class;
        case Type.OBJECT:
            try {
                return Class.forName(type.getClassName());
            } catch (ClassNotFoundException e) {
                return null;
            }
        case Type.ARRAY:
            // TODO handler for ARRAY
        default:
            return null;
        }
    }

    public static boolean isValueType(Type type) {

        switch (type.getSort()) {

        case Type.BOOLEAN:
        case Type.BYTE:
        case Type.CHAR:
        case Type.DOUBLE:
        case Type.FLOAT:
        case Type.INT:
        case Type.LONG:
        case Type.SHORT:
            return true;

        case Type.OBJECT:
            return VALUE_TYPES.contains(type.getInternalName());

        default:
            return false;
        }
    }

    public static Class<?>[] getClasses(String desc)
            throws ClassNotFoundException {

        Type[] types = Type.getArgumentTypes(desc);
        Class<?>[] classes = new Class<?>[types.length];

        for (int i = 0; i < types.length; i++) {

            classes[i] = getClassFromType(types[i]);

            if (classes[i] == null) {
                return null;
            }
        }

        return classes;
    }

    public static Object i2wrapper(Integer obj, Class<?> clazz) {

        int i = obj.intValue();

        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return i == 1;
        }

        if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
            return (byte) i;
        }

        if (clazz.equals(Character.class) || clazz.equals(char.class)) {
            return (char) i;
        }

        if (clazz.equals(Short.class) || clazz.equals(short.class)) {
            return (short) i;
        }

        return obj;
    }

    public static Object wrapper2i(Object obj, Class<?> clazz) {

        if (clazz.equals(Boolean.class) || clazz.equals(boolean.class)) {
            return ((Boolean) obj) ? 1 : 0;
        }

        if (clazz.equals(Byte.class) || clazz.equals(byte.class)) {
            return (int) (byte) (Byte) obj;
        }

        if (clazz.equals(Character.class) || clazz.equals(char.class)) {
            return (int) (char) (Character) obj;
        }

        if (clazz.equals(Short.class) || clazz.equals(short.class)) {
            return (int) (short) (Short) obj;
        }

        return obj;
    }

    public static Object dereference(Object obj) {

        if (obj instanceof Reference) {
            return ((Reference) obj).getObj();
        } else {
            return obj;
        }
    }

    public static Object dereference(Object obj, Class<?> type) {

        if (obj instanceof Integer) {
            return ClassHelper.i2wrapper((Integer) obj, type);
        } else {
            return dereference(obj);
        }
    }

    public static Object address(Object obj, Class<?> clazz) {

        if (isValueType(Type.getType(clazz))) {
            return wrapper2i(obj, clazz);
        } else {
            return new Reference(obj);
        }
    }

    public static Object getCaller(MethodInsnNode instr,
            List<? extends ConstValue> values) {

        if (instr.getOpcode() == Opcodes.INVOKESTATIC) {
            return null;
        } else {
            return dereference(values.get(0).cst);
        }
    }

    public static Object[] getArgs(MethodInsnNode instr,
            List<? extends ConstValue> values, Class<?>[] parameters) {

        if (instr.getOpcode() == Opcodes.INVOKESTATIC) {

            Object[] args = new Object[values.size()];

            for (int i = 0; i < args.length; i++) {
                args[i] = dereference(values.get(i).cst, parameters[i]);
            }

            return args;
        } else {

            Object[] args = new Object[values.size() - 1];

            for (int i = 0; i < args.length; i++) {
                args[i] = dereference(values.get(i + 1).cst, parameters[i]);
            }

            return args;
        }
    }
}
