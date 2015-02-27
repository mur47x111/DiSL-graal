package ch.usi.dag.disl.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;


/**
 * Extends {@link Thread} with a "bypass" variable and writes its new bytecode
 * to a class file in a given directory. This is required to compile DiSL bypass
 * code, which checks the state of the "bypass" variable.
 */
public final class ExtendThread {

    private static abstract class FieldDefinition {

        private final int __access;
        private final String __name;
        private final Class <?> __type;


        FieldDefinition (
            final int access, final String name, final Class <?> type
        ) {
            __access = access;
            __name = name;
            __type = type;
        }

        int access () {
            return __access;
        }

        String name () {
            return __name;
        }

        String desc () {
            return Type.getDescriptor (__type);
        }

        Type type () {
            return Type.getType (__type);
        }

        protected abstract void initialize (Type owner, AdviceAdapter mv);
    }


    private static final class FieldExtender extends ClassVisitor {
        private final FieldDefinition __fd;
        private Type __owner;

        public FieldExtender (final FieldDefinition fd, final ClassWriter cw) {
            super (Opcodes.ASM5, cw);
            __fd = fd;
        }

        @Override
        public void visit (
            final int version, final int access, final String name,
            final String signature, final String superName,
            final String [] interfaces
        ) {
            super.visit (version, access, name, signature, superName, interfaces);
            __owner = Type.getObjectType (name);
        };


        @Override
        public MethodVisitor visitMethod (
            final int access, final String name, final String desc,
            final String signature, final String [] exceptions
        ) {
            final MethodVisitor mv = super.visitMethod (
                access, name, desc, signature, exceptions
            );

            if ("<init>".equals (name)) {
                // Initialize the field.
                return new AdviceAdapter (Opcodes.ASM5, mv, access, name, desc) {
                    @Override
                    protected void onMethodEnter () {
                        __fd.initialize (__owner, this);
                    }
                };
            } else {
                return mv;
            }
        };


        @Override
        public void visitEnd () {
            // Create the field.
            super.visitField (
                __fd.access (), __fd.name (), __fd.desc (),
                null, null
            );

            super.visitEnd ();
        }
    }


    public static void main (final String... args) throws Exception {
        if (args.length < 1) {
            System.err.println ("usage: ExtendThread <output-directory>");
            System.exit (1);
        }

        final File outputDir = new File (args [0]);
        if (!outputDir.isDirectory ()) {
            System.err.printf (
                "error: %s does not exist or is not a directory!\n", outputDir);
            System.exit (1);
        }

        //
        // Define a thread-local non-inheritable boolean variable named
        // "bypass", with a default value of false,
        //
        final FieldDefinition bypassField = new FieldDefinition (
            Opcodes.ACC_PUBLIC, "bypass", boolean.class) {
            @Override
            protected void initialize (final Type owner, final AdviceAdapter mv) {
                mv.loadThis ();
                mv.push (false);
                mv.putField (owner, name (), type ());
            }
        };

        //
        // Extend Thread and dump the new bytecode into the given directory.
        //
        final byte [] extendedThread = __extendThread (bypassField);
        __writeThread (outputDir, extendedThread);
    }


    private static byte [] __extendThread (final FieldDefinition fd)
    throws IOException {
        final ClassReader cr = new ClassReader (Thread.class.getName ());
        final ClassWriter cw = new ClassWriter (cr, 0);
        cr.accept (new FieldExtender (fd, cw), 0);
        return cw.toByteArray ();
    }


    private static void __writeThread (
        final File baseDir, final byte [] bytes
    ) throws IOException {
        final Class <Thread> tc = Thread.class;
        final String pkgName = tc.getPackage ().getName ();
        final String dirName = pkgName.replace ('.', File.separatorChar);

        final File outputDir = new File (baseDir, dirName);
        outputDir.mkdirs ();

        final String fileName = String.format ("%s.class", tc.getSimpleName ());
        final File outputFile = new File (outputDir, fileName);

        final FileOutputStream fos = new FileOutputStream (outputFile);
        try {
            fos.write (bytes);
        } finally {
            fos.close ();
        }
    }
}
