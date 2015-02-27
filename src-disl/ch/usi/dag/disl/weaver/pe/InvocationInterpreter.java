package ch.usi.dag.disl.weaver.pe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.MethodInsnNode;

import ch.usi.dag.disl.util.Constants;

public class InvocationInterpreter {

    private HashSet<String> registeredMethods;

    public InvocationInterpreter() {
        registeredMethods = new HashSet<String>();
    }

    public Object execute(MethodInsnNode instr,
            List<? extends ConstValue> values) {

        int opcode = instr.getOpcode();

        if (opcode == Opcodes.INVOKEINTERFACE) {
            return null;
        }

        if (!registeredMethods.contains(getMethodID(instr))) {
            return null;
        }

        if (opcode == Opcodes.INVOKESPECIAL && instr.name.equals("<clinit>")) {

            if (!(values.get(0).cst instanceof Reference)) {
                return null;
            }

            for (int i = 1; i < values.size(); i++) {
                if (ClassHelper.dereference(values.get(i)) == null) {
                    return null;
                }
            }
        } else {

            for (ConstValue value : values) {
                if (value.cst == null) {
                    return null;
                }
            }
        }

        try {

            Class<?> clazz = Class.forName(instr.owner.replace('/', '.'));
            Class<?>[] parameters = ClassHelper.getClasses(instr.desc);

            if (parameters == null) {
                return null;
            }

            Object[] args = ClassHelper.getArgs(instr, values, parameters);

            if (args == null) {
                return null;
            }

            if (instr.name.equals("<init>")) {
                Reference ref = (Reference) values.get(0).cst;
                ref.setObj(clazz.getConstructor(parameters).newInstance(args));
                return null;
            } else if (!instr.name.equals("<clinit>")) {

                Object caller = ClassHelper.getCaller(instr, values);
                Class<?> retType = ClassHelper.getClassFromType(Type
                        .getReturnType(instr.desc));

                if (retType == null) {
                    return null;
                }

                Object retValue = clazz.getMethod(instr.name, parameters)
                        .invoke(caller, args);

                return ClassHelper.address(retValue, retType);
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void register(String owner, String name, String desc) {
        registeredMethods.add(owner + Constants.CLASS_DELIM + name + desc);
    }

    public void register(Class<?> clazz) {

        String owner = Type.getInternalName(clazz);

        for (Constructor<?> constructor : clazz.getConstructors()) {
            register(owner, "<init>",
                    Type.getConstructorDescriptor(constructor));
        }

        for (Method method : clazz.getMethods()) {
            register(owner, method.getName(), Type.getMethodDescriptor(method));
        }
    }

    private String getMethodID(MethodInsnNode min) {
        return min.owner + Constants.CLASS_DELIM + min.name + min.desc;
    }

    public boolean isRegistered(MethodInsnNode min) {
        return registeredMethods.contains(getMethodID(min));
    }

    private static InvocationInterpreter instance;

    public static InvocationInterpreter getInstance() {

        if (instance == null) {

            instance = new InvocationInterpreter();

            instance.register(Boolean.class);
            instance.register(Byte.class);
            instance.register(Character.class);
            instance.register(Double.class);
            instance.register(Float.class);
            instance.register(Integer.class);
            instance.register(Long.class);
            instance.register(Short.class);
            instance.register(String.class);
            instance.register(StringBuilder.class);
        }

        return instance;
    }
}
