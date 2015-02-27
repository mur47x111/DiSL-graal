package ch.usi.dag.disl.exclusion;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import ch.usi.dag.disl.cbloader.ManifestHelper;
import ch.usi.dag.disl.cbloader.ManifestHelper.ManifestInfo;
import ch.usi.dag.disl.exception.ExclusionPrepareException;
import ch.usi.dag.disl.exception.ManifestInfoException;
import ch.usi.dag.disl.exception.ScopeParserException;
import ch.usi.dag.disl.scope.Scope;
import ch.usi.dag.disl.scope.ScopeImpl;
import ch.usi.dag.disl.util.Constants;


public abstract class ExclusionSet {

    private static final String excListPath =
        System.getProperty ("disl.exclusionList", null);

    private static final String JAR_PATH_BEGIN = "/";

    private static final String JAR_PATH_END = "!";

    private static final char JAR_ENTRY_DELIM = '/';

    private static final char CLASS_DELIM = '.';

    private static final String ALL_METHODS = ".*";


    public static Set <Scope> prepare ()
    throws ScopeParserException, ManifestInfoException,
    ExclusionPrepareException {
        final Set <Scope> exclSet = defaultExcludes ();
        exclSet.addAll (instrumentationJar ());
        exclSet.addAll (readExlusionList ());
        return exclSet;
    }


    private static Set <Scope> defaultExcludes () throws ScopeParserException {
        final String [] excludedScopes = new String [] {
            //
            // Our classes.
            //
            "ch.usi.dag.dislagent.*.*" /* DiSL agent classes */,
            "ch.usi.dag.disl.dynamicbypass.*.*" /* dynamic bypass classes */,
            "ch.usi.dag.dislre.*.*" /* Shadow VM classes */,

            //
            // The following cause trouble when instrumented.
            //
            "sun.instrument.*.*" /* Sun instrumentation classes */,
            "java.lang.Object.finalize" /* Object finalizer */
        };

        final Set <Scope> exclSet = new HashSet <Scope> ();
        for (final String excludedScope : excludedScopes) {
            exclSet.add (new ScopeImpl (excludedScope));
        }

        return exclSet;
    }


    private static Set <Scope> instrumentationJar ()
    throws ManifestInfoException, ExclusionPrepareException,
    ScopeParserException {
        try {
            // add classes from instrumentation jar
            final Set <Scope> exclSet = new HashSet <Scope> ();

            // get DiSL manifest info
            final ManifestInfo mi = ManifestHelper.getDiSLManifestInfo ();

            // no manifest found
            if (mi == null) {
                return exclSet;
            }

            // get URL of the instrumentation jar manifest
            final URL manifestURL = ManifestHelper.getDiSLManifestInfo ().getResource ();

            // manifest path contains "file:" + jar name + "!" + manifest path
            final String manifestPath = manifestURL.getPath ();

            // extract jar path
            final int jarPathBegin = manifestPath.indexOf (JAR_PATH_BEGIN);
            final int jarPathEnd = manifestPath.indexOf (JAR_PATH_END);
            final String jarPath = manifestPath.substring (jarPathBegin, jarPathEnd);

            // open jar...
            final JarFile jarFile = new JarFile (jarPath);

            // ... and iterate over items
            final Enumeration <JarEntry> entries = jarFile.entries ();
            while (entries.hasMoreElements ()) {
                final JarEntry entry = entries.nextElement ();

                // get entry name
                final String entryName = entry.getName ();

                // add all classes to the exclusion list
                if (entryName.endsWith (Constants.CLASS_EXT)) {
                    String className = entryName.replace (JAR_ENTRY_DELIM, CLASS_DELIM);

                    // remove class ext
                    final int classNameEnd = className.lastIndexOf (Constants.CLASS_EXT);
                    className = className.substring (0, classNameEnd);

                    // add exclusion for all methods
                    final String classExcl = className + ALL_METHODS;

                    exclSet.add (new ScopeImpl (classExcl));
                }
            }

            jarFile.close ();
            return exclSet;

        } catch (final IOException e) {
            throw new ExclusionPrepareException (e);
        }
    }


    private static Set <Scope> readExlusionList ()
    throws ExclusionPrepareException, ScopeParserException {
        final String COMMENT_START = "#";

        try {
            final Set <Scope> exclSet = new HashSet <Scope> ();

            // if exclusion list path exits
            if (excListPath != null) {
                // read exclusion list line by line
                final Scanner scanner = new Scanner (new FileInputStream (excListPath));
                while (scanner.hasNextLine ()) {
                    final String line = scanner.nextLine ().trim ();
                    if (!line.isEmpty () && !line.startsWith (COMMENT_START)) {
                        exclSet.add (new ScopeImpl (line));
                    }
                }

                scanner.close ();
            }

            return exclSet;

        } catch (final FileNotFoundException e) {
            throw new ExclusionPrepareException (e);
        }
    }
}
