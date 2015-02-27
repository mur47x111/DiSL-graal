package ch.usi.dag.disl.testtools.onplace;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import ch.usi.dag.disl.DiSL;

public class OnPlaceTransformer {

    public static void main(String[] args) throws Exception {

        // INSTRUCTIONS: Under Eclipse call me with these jvm params (example)
        // -Ddisltest.transform=bin/ch/usi/dag/disl/test/bodymarker/TargetClass.class
        // -Ddisl.classes=bin/ch/usi/dag/disl/test/bodymarker/DiSLClass.class

        // do not use dynamic bypass
        System.setProperty ("disl.disablebypass", "true");
        DiSL disl = DiSL.init ();

        String classToTransform = null;

        if(args.length == 1) {
            classToTransform = args[0];
        }

        if(classToTransform == null) {
            classToTransform = System.getProperty("disltest.transform");
        }

        if(classToTransform == null) {
            System.err.println("No class to transform...");
            System.exit(1);
        }

        // get code as bytes
        byte[] origCode = loadAsBytes(classToTransform);

        // check class first
        ClassReader cr = new ClassReader(origCode);
        cr.accept(new CheckClassAdapter(
                new TraceClassVisitor(new PrintWriter(System.out))), 0);

        // instrument class
        byte[] instrCode = disl.instrument(origCode);

        if(instrCode != null) {

            FileOutputStream fos = new FileOutputStream("ModifiedClass.class");
            fos.write(instrCode);
            fos.close();
        }
    }

    // thx: http://www.java2s.com/Tutorial/Java/0180__File/Loadfiletobytearray.htm
    public final static byte[] loadAsBytes(String file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        return loadAsBytes(fin);
    }

    public final static byte[] loadAsBytes(InputStream is) throws IOException {

        byte readBuf[] = new byte[512 * 1024];

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        int readCnt = is.read(readBuf);
        while (0 < readCnt) {
            bout.write(readBuf, 0, readCnt);
            readCnt = is.read(readBuf);
        }

        is.close();

        return bout.toByteArray();
    }
}
