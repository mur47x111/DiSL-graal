package ch.usi.dag.dislserver;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Set;
import java.util.UUID;

import ch.usi.dag.disl.DiSL;
import ch.usi.dag.disl.DiSL.CodeOption;
import ch.usi.dag.disl.exception.DiSLException;
import ch.usi.dag.disl.util.Constants;
import ch.usi.dag.disl.util.Logging;
import ch.usi.dag.util.Strings;
import ch.usi.dag.util.logging.Logger;


final class RequestProcessor {

    private static final Logger __log = Logging.getPackageInstance ();

    //

    private static final String uninstrPath = System.getProperty ("dislserver.uninstrumented");
    private static final String instrPath = System.getProperty ("dislserver.instrumented");
    private static final boolean disableBypass = Boolean.getBoolean ("dislserver.disablebypass");

    //

    private final DiSL __disl;

    //

    private RequestProcessor (final DiSL disl) {
        __disl = disl;
    }

    //

    public Message process (final Message request) throws DiSLServerException {
        final byte [] classBytes = request.payload ();
        final String className = __getClassName (request.control (), classBytes);
        final Set <CodeOption> options = CodeOption.setOf (request.flags ());

        if (__log.traceIsLoggable ()) {
            __log.trace (
                "instrumenting class %s [%d bytes, %s]",
                className.isEmpty () ? "<unknown>" : className,
                classBytes.length, Strings.join ("+", options)
            );
        }

        //
        // If requested, dump the uninstrumented byte code, instrument the
        // class, and again, if requested, dump the instrumented bytecode.
        // Create a response corresponding to the request and re-throw any
        // exception that might have been thrown as an server internal error.
        //
        try {
            if (uninstrPath != null) {
                __dumpClass (className, classBytes, uninstrPath);
            }

            // TODO: instrument the bytecode according to given options
            // byte [] instrCode = disl.instrument (origCode, options);

            final byte [] newClassBytes = __disl.instrument (classBytes);

            if (newClassBytes != null) {
                if (instrPath != null) {
                    __dumpClass (className, newClassBytes, instrPath);
                }

                return Message.createClassModifiedResponse (newClassBytes);

            } else {
                return Message.createNoOperationResponse ();
            }

        } catch (final Exception e) {
            final String message = String.format (
                "error instrumenting %s: %s", className, __getFullMessage (e)
            );

            __log.error (message);

            throw new DiSLServerException (message);
        }
    }


    private static String __getFullMessage (final Throwable t) {
        final StringWriter result = new StringWriter ();
        t.printStackTrace (new PrintWriter (result));
        return result.toString ();
    }


    private static String __getClassName (
        final byte [] nameBytes, final byte [] codeBytes
    ) {
        String result = Strings.EMPTY_STRING;
        if (nameBytes.length > 0) {
            result = new String (nameBytes);
        }

        if (result.isEmpty ()) {
            result = __parseInternalClassName (codeBytes);
            if (result == null || result.isEmpty ()) {
                result = UUID.randomUUID ().toString ();
            }
        }

        return result;
    }


    private static String __parseInternalClassName (final byte [] byteCode) {
        final int CLASS_MAGIC = 0xCAFEBABE;

        final int TAG_CONSTANT_UTF8 = 1;
        final int TAG_CONSTANT_LONG = 5;
        final int TAG_CONSTANT_DOUBLE = 6;
        final int TAG_CONSTANT_CLASS = 7;
        final int TAG_CONSTANT_STRING = 8;
        final int TAG_CONSTANT_METHOD_HANDLE = 15;
        final int TAG_CONSTANT_METHOD_TYPE = 16;

        //

        try (
            final DataInputStream dis = new DataInputStream (
                new ByteArrayInputStream (byteCode)
            );
        ) {
            // verify magic field
            if (dis.readInt () != CLASS_MAGIC) {
                throw new IOException ("invalid class file format");
            }

            // skip minor_version and major_version fields
            dis.readUnsignedShort ();
            dis.readUnsignedShort ();

            //
            // Scan the constant pool to pick up the UTF-8 strings and the
            // class info references to those strings. Skip everything else.
            // Valid index into the constant pool must be greater than 0.
            //
            final int constantCount = dis.readUnsignedShort ();
            final int [] classIndices = new int [constantCount];
            final String [] utfStrings = new String [constantCount];

            for (int poolIndex = 1; poolIndex < constantCount; poolIndex++) {
                final int poolTag = dis.readUnsignedByte ();

                switch (poolTag) {
                case TAG_CONSTANT_UTF8:
                    utfStrings [poolIndex] = dis.readUTF ();
                    break;

                case TAG_CONSTANT_CLASS:
                    classIndices [poolIndex] = dis.readUnsignedShort ();
                    break;

                case TAG_CONSTANT_STRING:
                case TAG_CONSTANT_METHOD_TYPE:
                    // string_index or descriptor_index
                    dis.readUnsignedShort ();
                    break;

                case TAG_CONSTANT_METHOD_HANDLE:
                    // reference_kind & reference_index
                    dis.readUnsignedByte ();
                    dis.readUnsignedShort ();
                    break;

                case TAG_CONSTANT_LONG:
                case TAG_CONSTANT_DOUBLE:
                    // high_bytes & low_bytes
                    dis.readInt ();
                    dis.readInt ();

                    // 64-bit values take up two constant pool slots
                    poolIndex++;
                    break;

                default:
                    // all other constant structures fit into 4 bytes
                    dis.skip (4);
                }
            }

            // skip access_flags field
            dis.readUnsignedShort ();

            // get this_class constant pool index
            final int thisClassIndex = dis.readUnsignedShort ();

            // resolve the (internal) class name
            return utfStrings [classIndices [thisClassIndex]];

        } catch (final IOException ioe) {
            // failed to parse class name
            return null;
        }
    }


    private static void __dumpClass (
        final String className, final byte[] byteCode, final String path
    ) throws IOException {
        // extract the class name and package name
        final int i = className.lastIndexOf (Constants.PACKAGE_INTERN_DELIM);
        final String simpleClassName = className.substring (i + 1);
        final String packageName = className.substring (0, i + 1);

        // construct path to the class
        final String pathWithPkg = path + File.separator + packageName;

        // create directories
        new File (pathWithPkg).mkdirs ();

        // dump the class code
        try (
            final FileOutputStream fo = new FileOutputStream (
                pathWithPkg + simpleClassName + Constants.CLASS_EXT
            );
        ) {
            fo.write(byteCode);
        }
    }

    //

    public void terminate () {
        __disl.terminate ();
    }

    //

    public static RequestProcessor newInstance () throws DiSLServerException {
        try {
            // TODO LB: Configure bypass on a per-request basis.
            if (disableBypass) {
                System.setProperty ("disl.disablebypass", "true");
            }

            final DiSL disl = DiSL.init ();
            return new RequestProcessor (disl);

        } catch (final DiSLException de) {
            throw new DiSLServerException ("failed to initialize DiSL", de);
        }
    }

}
