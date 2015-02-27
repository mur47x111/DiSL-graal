package ch.usi.dag.disl.util;

import java.io.IOException;
import java.io.InputStream;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;


/**
 * Utility class for creating ASM {@link ClassNode} instances. Encapsulates
 * different {@link ClassReader} configurations for different use cases, as well
 * as a method to duplicate existing {@link ClassNode} instances.
 *
 * @author Lubomir Bulej
 */
public enum ClassNodeHelper {
    /**
     * Parses class structure only, without method code. This takes
     * approximately 40% of the time required by the {@link #SNIPPET} helper,
     * and approximately 33% of the time required by the {@link $FULL} helper.
     */
    OUTLINE (ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES),

    /**
     * Parses class code and debug information, but skips stack map frames. This
     * takes approximately 85% of the time required by the {@link FULL} helper.
     */
    SNIPPET (ClassReader.SKIP_FRAMES),

    /**
     * Parses class code and debug information, and expands stack map
     * frames.
     */
    FULL (ClassReader.EXPAND_FRAMES);

    //

    private final int __flags;

    private ClassNodeHelper (final int flags) {
        __flags = flags;
    }

    //

    /**
     * Creates a new {@link ClassNode} instance by parsing class-file bytes
     * from the given array.
     */
    public ClassNode unmarshal (final byte [] bytes) {
        final ClassNode result = new ClassNode (Opcodes.ASM5);
        new ClassReader (bytes).accept (result, __flags);
        return result;
    }


    /**
     * Creates a new {@link ClassNode} instance by parsing class-file bytes
     * from the given input stream.
     */
    public ClassNode unmarshal (final InputStream is) throws IOException {
        final ClassNode result = new ClassNode (Opcodes.ASM5);
        new ClassReader (is).accept (result, __flags);
        return result;
    }


    /**
     * Creates a new {@link ClassNode} instance by loading class-file bytes
     * as a resource using the system class loader.
     */
    public ClassNode load (final String className) throws IOException {
        final ClassNode result = new ClassNode (Opcodes.ASM5);
        new ClassReader (className).accept (result, __flags);
        return result;
    }


    /**
     * Creates a new {@link ClassNode} instance as a duplicate of the
     * given instance.
     *
     * @param source
     *        the {@link ClassNode} instance to duplicate
     * @return a new {@link ClassNode} instance
     */
    public static ClassNode duplicate (final ClassNode source) {
        final ClassNode result = new ClassNode (Opcodes.ASM5);
        source.accept (result);
        return result;
    }


    /**
     * Marshals the given {@link ClassNode} instance into an array of bytes
     * representing the contents of a class file.
     *
     * @param source
     *        the {@link ClassNode} to marshal.
     * @return array of bytes representing the contents of a class file.
     */
    public static byte [] marshal (final ClassNode source) {
        //
        // DiSL uses instructions only available in later class versions.
        // We therefore produce at least Java 5 class files.
        //
        final int requiredVersion = Opcodes.V1_5;
        final int requiredMajorVersion = requiredVersion & 0xFFFF;
        final int classMajorVersion = source.version & 0xFFFF;
        if (classMajorVersion < requiredMajorVersion) {
            source.version = requiredVersion;
        }

        //
        // We need to compute stack frame maps for Java 7 (and newer) classes.
        // For older class version, we only compute the maximum stack sizes.
        //
        final int java7MajorVersion = Opcodes.V1_7;
        final int writerFlags = (classMajorVersion >= java7MajorVersion) ?
            ClassWriter.COMPUTE_FRAMES : ClassWriter.COMPUTE_MAXS;

        final ClassWriter writer = new ClassWriter (writerFlags) {
            @Override
            protected String getCommonSuperClass (
                final String type1, final String type2
            ) {
                //
                // Use java.lang.Object as the common superclass to
                // avoid loading the two classes.
                //
                return __OBJECT_CLASS_NAME__;
            }
        };

        source.accept (writer);
        return writer.toByteArray ();
    }

    private static final String __OBJECT_CLASS_NAME__ = Type.getInternalName (Object.class);

}
