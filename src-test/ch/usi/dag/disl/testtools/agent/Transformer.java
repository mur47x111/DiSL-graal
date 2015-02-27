package ch.usi.dag.disl.testtools.agent;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import ch.usi.dag.disl.DiSL;
import ch.usi.dag.disl.exception.DiSLException;


public class Transformer implements ClassFileTransformer {

    private static DiSL __disl;

    static {
        try {
            // don't use dynamic bypass
            System.setProperty ("disl.disablebypass", "true");
            __disl = DiSL.init ();

        } catch (final DiSLException e) {
            throw new RuntimeException (e);
        }
    }

    //

    @Override
    public byte [] transform (
        final ClassLoader loader, final String className,
        final Class <?> classBeingRedefined,
        final ProtectionDomain protectionDomain,
        final byte [] classfileBuffer
    ) throws IllegalClassFormatException {
        byte [] instrumentedClass = null;

        try {
            instrumentedClass = __disl.instrument (classfileBuffer);
            if (instrumentedClass != null) {
                /*
                // print class
                ClassReader cr = new ClassReader(instrumentedClass);
                TraceClassVisitor tcv = new TraceClassVisitor(new PrintWriter(System.out));
                cr.accept(tcv, 0);
                /**/

                /*
                // check class
                ClassReader cr2 = new ClassReader(instrumentedClass);
                ClassWriter cw = new ClassWriter(cr2, ClassWriter.COMPUTE_MAXS);
                cr2.accept(new CheckClassAdapter(cw), 0);
                /**/

                /*
                // output class
                try {
                    File f = new File("ModifiedClass.class");
                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(instrumentedClass);
                    fos.flush();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /**/
            }

        } catch (final Throwable e) {
            e.printStackTrace ();
        }

        return instrumentedClass;
    }

}
