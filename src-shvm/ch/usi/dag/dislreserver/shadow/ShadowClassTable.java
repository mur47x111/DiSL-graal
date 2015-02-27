package ch.usi.dag.dislreserver.shadow;

import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.asm.Type;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;

public class ShadowClassTable {

    private static final int INITIAL_TABLE_SIZE = 10000;

    final static ShadowObject BOOTSTRAP_CLASSLOADER;

    static ShadowClass JAVA_LANG_CLASS;

    private static ConcurrentHashMap<ShadowObject, ConcurrentHashMap<String, byte[]>> classLoaderMap;
    private static ConcurrentHashMap<Integer, ShadowClass> shadowClasses;

    static {

        BOOTSTRAP_CLASSLOADER = new ShadowObject(0, null);
        JAVA_LANG_CLASS = null;

        classLoaderMap = new ConcurrentHashMap<ShadowObject, ConcurrentHashMap<String, byte[]>>(INITIAL_TABLE_SIZE);
        shadowClasses = new ConcurrentHashMap<Integer, ShadowClass>(INITIAL_TABLE_SIZE);

        classLoaderMap.put(BOOTSTRAP_CLASSLOADER,
                new ConcurrentHashMap<String, byte[]>());
    }

    public static void load(ShadowObject loader, String className,
            byte[] classCode, boolean debug) {

        ConcurrentHashMap<String, byte[]> classNameMap;

        if (loader == null) {
            // bootstrap loader
            loader = BOOTSTRAP_CLASSLOADER;
        }

        classNameMap = classLoaderMap.get(loader);

        if (classNameMap == null) {

            ConcurrentHashMap<String, byte[]> tmp = new ConcurrentHashMap<String, byte[]>();

            if ((classNameMap = classLoaderMap.putIfAbsent(loader, tmp)) == null) {
                classNameMap = tmp;
            }
        }

        if (classNameMap.putIfAbsent(className.replace('/', '.'), classCode) != null) {
            if (debug) {
                System.out.println("DiSL-RE: Reloading/Redefining class "
                        + className);
            }
        }
    }

    public static ShadowClass newInstance(long net_ref, ShadowClass superClass,
            ShadowObject loader, String classSignature, String classGenericStr,
            boolean debug) {

        if (!NetReferenceHelper.isClassInstance(net_ref)) {
            throw new DiSLREServerFatalException("Unknown class instance");
        }

        ShadowClass klass = null;
        Type t = Type.getType(classSignature);

        if (t.getSort() == Type.ARRAY) {
            // TODO unknown array component type
            klass = new ShadowArrayClass(net_ref, loader, superClass, null, t);
        } else if (t.getSort() == Type.OBJECT) {

            ConcurrentHashMap<String, byte[]> classNameMap;

            if (loader == null) {
                // bootstrap loader
                loader = BOOTSTRAP_CLASSLOADER;
            }

            classNameMap = classLoaderMap.get(loader);

            if (classNameMap == null) {
                throw new DiSLREServerFatalException("Unknown class loader");
            }

            byte[] classCode = classNameMap.get(t.getClassName());

            if (classCode == null) {
                throw new DiSLREServerFatalException("Class "
                        + t.getClassName() + " has not been loaded");
            }

            klass = new ShadowCommonClass(net_ref, classSignature, loader,
                    superClass, classCode);
        } else {

            klass = new ShadowPrimitiveClass(net_ref, loader, t);
        }

        int classID = NetReferenceHelper.get_class_id(net_ref);
        ShadowClass exist = shadowClasses.putIfAbsent(classID, klass);

        if (exist == null) {
            ShadowObjectTable.register(klass, debug);
        } else if (!exist.equals(klass)) {
            throw new DiSLREServerFatalException("Duplicated class ID");
        }

        if (JAVA_LANG_CLASS == null
                && "Ljava/lang/Class;".equals(classSignature)) {
            JAVA_LANG_CLASS = klass;
        }

        return klass;
    }

    public static ShadowClass get(int classID) {

        if (classID == 0) {
            // reserved ID for java/lang/Class
            return null;
        }

        ShadowClass klass = shadowClasses.get(classID);

        if (klass == null) {
            throw new DiSLREServerFatalException("Unknown class instance");
        }

        return klass;
    }

    public static void freeShadowObject(ShadowObject obj) {

        if (NetReferenceHelper.isClassInstance(obj.getNetRef())) {
            int classID = NetReferenceHelper.get_class_id(obj.getNetRef());
            shadowClasses.remove(classID);
        } else if (classLoaderMap.keySet().contains(obj)) {
            classLoaderMap.remove(obj);
        }
    }

}
