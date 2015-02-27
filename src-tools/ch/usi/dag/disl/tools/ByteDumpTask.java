package ch.usi.dag.disl.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.IllegalFormatException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;


public final class ByteDumpTask extends Task {
    private File __srcFile;
    private File __destFile;
    private int __blockLength = 16;
    private String __blockPrefix = "";
    private String __blockSuffix = "\n";
    private String __lastPrefix;
    private String __lastSuffix;
    private String __byteFormat = "%02x ";
    private String __text;
    private boolean __append = false;

    //

    public void setSrcFile (final File file) {
        if (!file.canRead ()) {
            throw new BuildException (
                "file does not exist or is not readable: "+ file,
                getLocation ()
            );
        }

        __srcFile = file;
    }

    public void setDestFile (final File file) {
        __destFile = file;
    }

    public void setBlockLength (final int blockLength) {
        if (blockLength < 1) {
            throw new BuildException (
                "Property 'blockLength' must be greater than zero",
                getLocation ()
            );
        }

        __blockLength = blockLength;
    }

    public void setBlockPrefix (final String blockPrefix) {
        __blockPrefix = blockPrefix;
    }

    public void setBlockSuffix (final String blockSuffix) {
        __blockSuffix = blockSuffix;
    }

    public void setLastPrefix (final String lastPrefix) {
        __lastPrefix = lastPrefix;
    }

    public void setLastSuffix (final String lastSuffix) {
        __lastSuffix = lastSuffix;
    }

    public void setByteFormat (final String byteFormat) {
        try {
            String.format (byteFormat,  0);
        } catch (final IllegalFormatException e) {
            throw new BuildException (
                "illegal byte format: "+ byteFormat, getLocation ()
            );
        }

        __byteFormat = byteFormat;
    }

    public void setAppend (final boolean append) {
        __append = append;
    }

    public void addText (final String rawText) {
        __text = getProject ().replaceProperties (rawText);
    }

    //

    @Override
    public void execute () {
        //
        // Set defaults for last block prefix and suffix.
        //
        if (__lastPrefix == null) {
            __lastPrefix = __blockPrefix;
        }

        if (__lastSuffix == null) {
            __lastSuffix = __blockSuffix;
        }

        //
        // Load the input and dump it to the destination file.
        //
        final ByteBuffer input = ByteBuffer.wrap (__loadInput ());
        final PrintStream output = __createOutput (__destFile, __append);

        log (String.format (
            "Dumping %d bytes, %d per block",
            input.capacity (), __blockLength
        ), Project.MSG_VERBOSE);

        __printBytes (input, output);
    }


    private byte [] __loadInput () {
        if (__text != null) {
            log ("Using embedded text as input", Project.MSG_VERBOSE);
            return __text.getBytes (Charset.forName ("UTF-8"));

        } else if (__srcFile != null) {
            log ("Loading "+ __srcFile, Project.MSG_VERBOSE);
            return __loadFile (__srcFile);

        } else {
            throw new BuildException ("Missing input: neither 'srcFile' nor embedded text specified", getLocation ());
        }
    }


    private static byte [] __loadFile (final File file) {
        try {
            final FileInputStream fis = new FileInputStream (file);

            try {
                return __copyStream (
                    fis, new ByteArrayOutputStream ()
                ).toByteArray ();

            } finally {
                fis.close ();
            }

        } catch (final IOException ioe) {
            throw new BuildException (String.format (
                "error reading %s: %s", file.toString (), ioe.getMessage ()
            ));
        }
    }


    private PrintStream __createOutput (final File file, final boolean append) {
        if (file == null) {
            log ("Using standard output as destination", Project.MSG_VERBOSE);
            return System.out;

        } else {
            try {
                log ("Output file: "+ file.toString (), Project.MSG_VERBOSE);
                return new PrintStream (new FileOutputStream (file, append));

            } catch (final FileNotFoundException e) {
                throw new BuildException (
                    "cannot create output file: "+ file.toString (),
                    getLocation ()
                );
            }
        }
    }


    private void __printBytes (final ByteBuffer bytes, final PrintStream output) {
        __formatBytes (bytes, output);
        output.flush ();

        if (output != System.out) {
            output.close ();
        }
    }


    private void __formatBytes (final ByteBuffer buffer, final PrintStream out) {
        while (buffer.hasRemaining ()) {
            final int lineLength = Math.min (buffer.remaining (), __blockLength);
            out.print (buffer.remaining () > lineLength ? __blockPrefix : __lastPrefix);

            for (int i = 0; i < lineLength; i++) {
                out.printf (__byteFormat, buffer.get ());
            }

            out.print (buffer.hasRemaining () ? __blockSuffix : __lastSuffix);
        }
    }

    private static <T extends OutputStream> T __copyStream (
        final InputStream input, final T output
    ) throws IOException {
        final byte [] buffer = new byte [4096];

        COPY: while (true) {
            final int bytesRead = input.read (buffer);
            if (bytesRead < 0) {
                break COPY;
            }

            output.write (buffer, 0, bytesRead);
        }

        return output;
    }

}
