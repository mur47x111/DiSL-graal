package ch.usi.dag.disl.staticcontext;

import org.objectweb.asm.Opcodes;

import ch.usi.dag.disl.util.Constants;


/**
 * Provides static context information about the instrumented method.
 */
public class MethodStaticContext extends AbstractStaticContext {

    // *** Class ***

    /**
     * Returns the internal name of the instrumented class, i.e., a fully
     * qualified class name, with packages delimited by the '/' character.
     */
    // XXX LB: This would be better named "thisClassInternalName".
    public String thisClassName () {
        return __classInternalName ();
    }


    /**
     * Returns the simple name of the instrumented class, i.e., a class name
     * without the package part of the name.
     */
    public String thisClassSimpleName () {
        final String name = __classInternalName ();
        final int start = name.lastIndexOf (Constants.PACKAGE_INTERN_DELIM);
        return (start >= 0) ? name.substring (start + 1) : name;
    }


    /**
     * Returns the canonical name of the instrumented class, i.e., a fully
     * qualified class name, with packages delimited by the '.' character.
     */
    public String thisClassCanonicalName () {
        return __classInternalName ().replace (
            Constants.PACKAGE_INTERN_DELIM, Constants.PACKAGE_STD_DELIM
        );
    }


    /**
     * Returns the internal name of the class enclosing the instrumented class,
     * or {@code null} if the instrumented class is not enclosed in another
     * class.
     */
    public String thisClassOuterClass () {
        return staticContextData.getClassNode ().outerClass;
    }


    /**
     * Returns the name of the method enclosing the instrumented class, or
     * {@code null} if the class is not enclosed in a method.
     */
    public String thisClassOuterMethod () {
        return staticContextData.getClassNode ().outerMethod;
    }


    /**
     * Returns outer method descriptor of the instrumented class.
     */
    public String thisClassOuterMethodDesc () {
        return staticContextData.getClassNode ().outerMethodDesc;
    }


    /**
     * Returns the signature of the instrumented class, or {@code null} if the
     * class is not a generic type.
     */
    public String thisClassSignature () {
        return staticContextData.getClassNode ().signature;
    }


    /**
     * Returns the name of the source file containing the instrumented class.
     */
    public String thisClassSourceFile () {
        return staticContextData.getClassNode ().sourceFile;
    }


    /**
     * Returns the internal name of the super class of the instrumented class,
     * i.e., a fully qualified class name, with package names delimited by the
     * '/' character.
     */
    public String thisClassSuperName () {
        return staticContextData.getClassNode ().superName;
    }


    /**
     * Returns class version as (ASM) integer of the instrumented class.
     */
    public int thisClassVersion () {
        return staticContextData.getClassNode ().version;
    }


    /**
     * Returns {@code true} if the instrumented class is abstract.
     */
    public boolean isClassAbstract () {
        return __classAccessFlag (Opcodes.ACC_ABSTRACT);
    }


    /**
     * Returns {@code true} if the instrumented class is an annotation.
     */
    public boolean isClassAnnotation () {
        return __classAccessFlag (Opcodes.ACC_ANNOTATION);
    }


    /**
     * Returns {@code true} if the instrumented class is an enum.
     */
    public boolean isClassEnum () {
        return __classAccessFlag (Opcodes.ACC_ENUM);
    }


    /**
     * Returns {@code true} if the instrumented class is final.
     */
    public boolean isClassFinal () {
        return __classAccessFlag (Opcodes.ACC_FINAL);
    }


    /**
     * Returns {@code true} if the instrumented class is an interface.
     */
    public boolean isClassInterface () {
        return __classAccessFlag (Opcodes.ACC_INTERFACE);
    }


    /**
     * Returns {@code true} if the instrumented class is private.
     */
    public boolean isClassPrivate () {
        return __classAccessFlag (Opcodes.ACC_PRIVATE);
    }


    /**
     * Returns {@code true} if the instrumented class is protected.
     */
    public boolean isClassProtected () {
        return __classAccessFlag (Opcodes.ACC_PROTECTED);
    }


    /**
     * Returns {@code true} if the instrumented class is public.
     */
    public boolean isClassPublic () {
        return __classAccessFlag (Opcodes.ACC_PUBLIC);
    }


    /**
     * Returns {@code true} if the instrumented class is synthetic.
     */
    public boolean isClassSynthetic () {
        return __classAccessFlag (Opcodes.ACC_SYNTHETIC);
    }


    // *** Method ***

    /**
     * Returns the name of the instrumented method.
     */
    public String thisMethodName () {
        return __methodName ();
    }


    /**
     * Returns the fully qualified (internal) name of the instrumented method,
     * i.e., including the (internal) name of the class containing the method.
     */
    public String thisMethodFullName () {
        return __classInternalName () + Constants.STATIC_CONTEXT_METHOD_DELIM + __methodName ();
    }


    /**
     * Returns the descriptor of the instrumented method.
     */
    public String thisMethodDescriptor () {
        return staticContextData.getMethodNode ().desc;
    }


    /**
     * Returns the signature of the instrumented method.
     */
    public String thisMethodSignature () {
        return staticContextData.getMethodNode ().signature;
    }


    /**
     * Returns {@code true} if this method is a constructor.
     */
    public boolean isMethodConstructor () {
        return Constants.isConstructorName (__methodName ());
    }


    /**
     * Returns {@code true} if this method is a class initializer.
     */
    public boolean isMethodInitializer () {
        return Constants.isInitializerName (__methodName ());
    }


    /**
     * Returns {@code true} if the instrumented method is a bridge.
     */
    public boolean isMethodBridge () {
        return __methodAccessFlag (Opcodes.ACC_BRIDGE);
    }


    /**
     * Returns {@code true} if the instrumented method is final.
     */
    public boolean isMethodFinal () {
        return __methodAccessFlag (Opcodes.ACC_FINAL);
    }


    /**
     * Returns {@code true} if the instrumented method is private.
     */
    public boolean isMethodPrivate () {
        return __methodAccessFlag (Opcodes.ACC_PRIVATE);
    }


    /**
     * Returns {@code true} if the instrumented method is protected.
     */
    public boolean isMethodProtected () {
        return __methodAccessFlag (Opcodes.ACC_PROTECTED);
    }


    /**
     * Returns {@code true} if the instrumented method is public.
     */
    public boolean isMethodPublic () {
        return __methodAccessFlag (Opcodes.ACC_PUBLIC);
    }


    /**
     * Returns {@code true} if the instrumented method is static.
     */
    public boolean isMethodStatic () {
        return __methodAccessFlag (Opcodes.ACC_STATIC);
    }


    /**
     * Returns {@code true} if the instrumented method is synchronized.
     */
    public boolean isMethodSynchronized () {
        return __methodAccessFlag (Opcodes.ACC_SYNCHRONIZED);
    }


    /**
     * Returns {@code true} if the instrumented method accepts a variable number
     * of arguments.
     */
    public boolean isMethodVarArgs () {
        return __methodAccessFlag (Opcodes.ACC_VARARGS);
    }


    //

    private String __classInternalName () {
        return staticContextData.getClassNode ().name;
    }


    private boolean __classAccessFlag (final int flagMask) {
        final int access = staticContextData.getClassNode ().access;
        return (access & flagMask) != 0;
    }


    private String __methodName () {
        return staticContextData.getMethodNode ().name;
    }


    private boolean __methodAccessFlag (final int flagMask) {
        final int access = staticContextData.getMethodNode ().access;
        return (access & flagMask) != 0;
    }

}
