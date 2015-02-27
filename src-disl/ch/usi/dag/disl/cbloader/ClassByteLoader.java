package ch.usi.dag.disl.cbloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import ch.usi.dag.disl.cbloader.ManifestHelper.ManifestInfo;
import ch.usi.dag.disl.exception.DiSLInitializationException;
import ch.usi.dag.disl.exception.ManifestInfoException;

public abstract class ClassByteLoader {

	public static final String PROP_DISL_CLASSES = "disl.classes";

	public static final String DISL_CLASSES_DELIM = ",";
	public static final String DISL_CLASSES_EXT = ".class";
	public static final char CLASS_DELIM = '.';
	public static final char FILE_DELIM = '/';

	// How to use jar support
	// 1) Create jar with a name specified in build.properties (instr.jar.name)
	// 2) Include manifest file that contains names of all used DiSL classes
	//     - for the name of the manifest attribute see ATTR_DISL_CLASSES
	// Jar should contain all additional classes needed for instrumentation
	// like Markers, Static contexts, ...
	//
	// NOTE: Example of the usage is processor test case
	// To build the jar for the processor test case go to the test directory
	// and call "ant package -Dtest.name=processor"
	// To run the test case with the instrumentation located in jar call
	// "./run-pkg.sh processor"

    public static List <InputStream> loadDiSLClasses () throws DiSLInitializationException {
        try {
            List <InputStream> result = loadClassesFromProperty ();
            if (result == null) {
                result = loadClassesFromManifest ();
            }

            return result;

        } catch (final IOException e) {
            throw new DiSLInitializationException (e);
        } catch (final ManifestInfoException e) {
            throw new DiSLInitializationException (e);
        }
    }


    private static List <InputStream> loadClassesFromProperty () throws IOException {
        final String classesList = System.getProperty (PROP_DISL_CLASSES);

        // no classes found
        if ((classesList == null) || classesList.isEmpty ()) {
            return null;
        }

        // get streams from class names
        final List <InputStream> dislClasses = new LinkedList <InputStream> ();
        for (final String fileName : classesList.split (DISL_CLASSES_DELIM)) {
            final File file = new File (fileName);
            dislClasses.add (new FileInputStream (file));
        }

        return dislClasses;
    }


    private static List <InputStream> loadClassesFromManifest ()
    throws IOException, ManifestInfoException {
        // get DiSL manifest info
        final ManifestInfo mi = ManifestHelper.getDiSLManifestInfo ();
        if (mi == null) {
            return null;
        }

        // empty class list in manifest
        final String classesList = mi.getDislClasses ();
        if (classesList.isEmpty ()) {
            return null;
        }

        // get streams from class names
        final List <InputStream> dislClasses = new LinkedList <InputStream> ();
        for (final String className : classesList.split (DISL_CLASSES_DELIM)) {
            // create file name from class name
            final String fileName = className.replace (CLASS_DELIM, FILE_DELIM) + DISL_CLASSES_EXT;
            final ClassLoader cl = ClassByteLoader.class.getClassLoader ();
            dislClasses.add (cl.getResourceAsStream (fileName));
        }

        return dislClasses;
    }

}
