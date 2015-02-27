package ch.usi.dag.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Iterator;

/**
 * Utility class providing miscellaneous string operations.
 *
 * @author Lubomir Bulej
 */
public final class Strings {

    /**
     * Empty string.
     */
    public static final String EMPTY_STRING = "";


    /**
     * Empty string array.
     */
    public static final String [] EMPTY_ARRAY = new String [0];

    //

    private Strings () {
        // pure static class - not to be instantiated
    }

    //

    /**
     * Joins multiple strings using a "glue" string.
     */
    public static String join (final String glue, final String ... fragments) {
        Assert.objectNotNull (glue, "glue");
        Assert.objectNotNull (fragments, "fragments");

        //

        final int fragmentCount = fragments.length;
        if (fragmentCount >= 2) {
            return __joinTwoOrMore (glue, fragmentCount, fragments);

        } else if (fragmentCount == 1) {
            return fragments [0];

        } else {
            return EMPTY_STRING;
        }
    }


    /**
     * Joins an iterable of strings using a "glue" string.
     */
    public static String join (final String glue, final Iterable <String> fragments) {
        Assert.objectNotNull (glue, "glue");
        Assert.objectNotNull (fragments, "fragments");

        //
        // To avoid reallocations in the StringBuilder, count the number of
        // fragments in the iterable and obtain an initial estimate of the
        // length of the result. Bail out quickly if the iterable is empty
        // or if it only contains a single element.
        //
        int resultLength = 0;
        int fragmentCount = 0;

        for (final String fragment : fragments) {
            resultLength = fragment.length ();
            fragmentCount += 1;
        }

        if (fragmentCount == 0) {
            return EMPTY_STRING;
        }

        //

        final Iterator <String> fit = fragments.iterator ();
        if (fragmentCount == 1) {
            return fit.next ();
        }

        //
        // Adjust the length of the result with the combined length of all
        // glue instances, and join the fragments using the glue. Since there
        // are at least two fragments, we can append the first fragment
        // unconditionally outside the loop body.
        //
        resultLength += glue.length () * (fragmentCount - 1);
        final StringBuilder builder = new StringBuilder (resultLength);

        builder.append (fit.next ());
        while (fit.hasNext ()) {
            builder.append (glue);
            builder.append (fit.next ());
        }

        return builder.toString ();
    }


    /**
     * Joins string representations of multiple objects using a "glue" string.
     */
    public static String join (final String glue, final Object ... fragments) {
        Assert.objectNotNull (glue, "glue");
        Assert.objectNotNull (fragments, "fragments");

        //

        final int fragmentCount = fragments.length;
        if (fragmentCount >= 2) {
            //
            // Convert the fragments to strings and the join their string
            // representations.
            //
            final String [] fragmentStrings = new String [fragmentCount];
            for (int i = 0; i < fragmentCount; i++) {
                fragmentStrings [i] = String.valueOf (fragments [i]);
            }

            return __joinTwoOrMore (glue, fragmentCount, fragmentStrings);

        } else if (fragmentCount == 1) {
            return String.valueOf (fragments [0]);

        } else {
            return EMPTY_STRING;
        }
    }


    private static String __joinTwoOrMore (
        final String glue, final int fragmentCount, final String [] fragments
    ) {
        //
        // To avoid reallocations in the StringBuilder, estimate the length
        // of the result to dimension the StringBuilder accordingly.
        //
        int length = glue.length () * (fragmentCount - 1);
        for (final String fragment : fragments) {
            length += fragment.length ();
        }

        //
        // Join the fragments using the glue. Since there are at least two
        // fragments, we can append the first fragment unconditionally
        // outside the loop body.
        //
        final StringBuilder builder = new StringBuilder (length);

        builder.append (fragments [0]);
        for (int i = 1; i < fragmentCount; i++) {
            builder.append (glue);
            builder.append (fragments [i]);
        }

        //

        return builder.toString ();
    }

    //

    public static String drainStream (
        final InputStream input
    ) throws IOException {
        final ByteArrayOutputStream output = new ByteArrayOutputStream ();

        final int bufferSize = 4096;
        final byte [] buffer = new byte [bufferSize];

        READ_LOOP: while (true) {
            final int bytesRead = input.read (buffer, 0, bufferSize);
            if (bytesRead < 1) {
                break READ_LOOP;
            }

            output.write (buffer, 0, bytesRead);
        }

        return output.toString ();
    }

    //

    /**
     * Loads an entire file into a string. This method should be only used for
     * reasonable sized text files.
     *
     * @param fileName
     *        the name of the file to read
     * @return a {@link String} with the contents of the given file
     * @throws FileNotFoundException
     *         if the given file could not be found
     * @throws IOException
     *         if the file could not be read
     */
    public static String loadFromFile (final String fileName) throws IOException {
        return __drainStreamAndClose (new FileInputStream (fileName));
    }


    /**
     * Loads an entire file into a string. This method should be only used for
     * reasonably sized text files.
     *
     * @param fileName
     *        the name of the file to read
     * @return a {@link String} with the contents of the given file
     * @throws FileNotFoundException
     *         if the given file could not be found
     * @throws IOException
     *         if the file could not be read
     */
    public static String loadFromFile (final File fileName) throws IOException {
        return __drainStreamAndClose (new FileInputStream (fileName));
    }


    /**
     * Loads an entire resource associated with a given class into a string.
     * This method should be only used for reasonably sized text resources.
     *
     * @param refClass
     *        the reference class to use when looking for an associated resource
     * @param name
     *        the name of the resource to read
     * @return a {@link String} with the contents of the given resource, or
     *         {@code null} if the resource could not be found
     * @throws FileNotFoundException
     *         if the given resource could not be found
     * @throws IOException
     *         if the resource could not be read
     */
    public static String loadFromResource (
        final Class <?> refClass, final String name
    ) throws IOException {
        final InputStream input = refClass.getResourceAsStream (name);
        if (input != null) {
            return __drainStreamAndClose (input);

        } else {
            throw new FileNotFoundException ("no such resource: "+ name);
        }
    }


    static String __drainStreamAndClose (
        final InputStream input
    ) throws IOException {
        try {
            return drainStream (input);

        } finally {
            input.close ();
        }
    }

    //

    /**
     * Stores a string into a file.
     *
     * @param fileName
     *        the name of the file to write.
     * @param content
     *        the string to write.
     * @throws FileNotFoundException
     *         if the given file could not be created
     * @throws IOException
     *         if an error occurs while writing to the file
     */
    public static void storeToFile (
        final File fileName, final String content
    ) throws FileNotFoundException {
        __printAndClose (new PrintWriter (fileName), content);
    }


    /**
     * Stores a string into a file.
     *
     * @param fileName
     *        the name of the file to write.
     * @param content
     *        the string to write.
     * @throws FileNotFoundException
     *         if the given file could not be created
     * @throws IOException
     *         if an error occurs while writing to the file
     */
    public static void storeToFile (
        final String fileName, final String content
    ) throws FileNotFoundException {
        __printAndClose (new PrintWriter (fileName), content);
    }


    static void __printAndClose (
        final PrintWriter output, final String content
    ) {
        try {
            output.print (content);

        } finally {
            output.close ();
        }
    }

}
