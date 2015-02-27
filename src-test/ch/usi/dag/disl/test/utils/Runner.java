package ch.usi.dag.disl.test.utils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ch.usi.dag.dislreserver.DiSLREServer;
import ch.usi.dag.dislserver.DiSLServer;
import ch.usi.dag.util.Duration;
import ch.usi.dag.util.Strings;


public abstract class Runner {

    protected static final Duration _INIT_TIME_LIMIT_ = Duration.of (3, SECONDS);
    protected static final Duration _TEST_TIME_LIMIT_ = Duration.of (10, SECONDS);
    protected static final Duration _WATCH_DELAY_ = Duration.of (100, MILLISECONDS);

    protected static final String _ENV_JAVA_HOME_ = "JAVA_HOME";

    protected static final File _DISL_LIB_DIR_ = new File (System.getProperty ("runner.disl.lib.dir", "lib"));

    protected static final File _DISL_AGENT_LIB_ = __libPath ("runner.disl.agent.lib", "libdislagent.so");
    protected static final File _DISL_BYPASS_JAR_ = __libPath ("runner.disl.bypass.jar", "disl-bypass.jar");
    protected static final File _DISL_SERVER_JAR_ = __libPath ("runner.disl.server.jar", "disl-server.jar");
    protected static final Class <?> _DISL_SERVER_CLASS_ = DiSLServer.class;

    protected static final File _SHVM_AGENT_LIB_ = __libPath ("runner.shvm.agent.lib", "libdislreagent.so");
    protected static final File _SHVM_DISPATCH_JAR_ = __libPath ("runner.shvm.dispatch.jar", "dislre-dispatch.jar");
    protected static final File _SHVM_SERVER_JAR_ = __libPath ("runner.shvm.server.jar", "dislre-server.jar");
    protected static final Class <?> _SHVM_SERVER_CLASS_ = DiSLREServer.class;

    private static File __libPath (final String property, final String defaultValue) {
        return new File (_DISL_LIB_DIR_, System.getProperty (property, defaultValue));
    }

    //

    protected static final File _TEST_LIB_DIR_ = new File (System.getProperty ("runner.lib.dir", "test-jars"));
    static final boolean TEST_DEBUG = Boolean.getBoolean ("runner.debug");

    //

    private final String __testName;
    private final Class <?> __testClass;

    //

    Runner (final Class <?> testClass) {
        __testClass = testClass;
        __testName = __extractTestName (testClass);
    }

    protected static String _getJavaCommand (final String javaHome) {
        final String home = (javaHome != null) ? javaHome : System.getenv (_ENV_JAVA_HOME_);
        if (home != null) {
            if (new File (home, "jre").exists ()) {
                return Strings.join (File.separator, home, "jre", "bin", "java");
            } else {
                return Strings.join (File.separator, home, "bin", "java");
            }
        } else {
            return "java";
        }
    }


    private static String __extractTestName (final Class <?> testClass) {
        final String [] packages = testClass.getPackage ().getName ().split ("\\.");
        return packages [packages.length - 2];
    }

    //

    public void start () throws IOException {
        final File testInstJar = new File (
            _TEST_LIB_DIR_, String.format ("%s-inst.jar", __testName)
        );

        if (!testInstJar.exists ()) {
            throw new FileNotFoundException (String.format (
                "instrumentation jar not found: %s", testInstJar.toString ()
            ));
        }

        final File testAppJar = new File (
            _TEST_LIB_DIR_, String.format ("%s-app.jar", __testName)
        );

        if (!testAppJar.exists ()) {
            throw new FileNotFoundException (String.format (
                "application jar not found: %s", testAppJar.toString ()
            ));
        }

        _start (testInstJar, testAppJar);
    }

    protected abstract void _start (
        final File testInstJar, final File testAppJar
    ) throws IOException;

    //

    public final boolean waitFor () {
        return _waitFor (_TEST_TIME_LIMIT_);
    }

    protected abstract boolean _waitFor (final Duration duration);

    //

    public final void assertIsStarted () {
        _assertIsStarted ();
    }

    protected abstract void _assertIsStarted ();

    //

    public final void assertIsFinished () {
        _assertIsFinished ();
    }

    protected abstract void _assertIsFinished ();

    //

    public final void assertIsSuccessful () {
        _assertIsSuccessful ();
    }

    protected abstract void _assertIsSuccessful ();

    //

    protected String _loadResource (final String name) throws IOException {
        return Strings.loadFromResource (__testClass, name);
    }

    //

    public final void destroyIfRunningAndDumpOutputs () throws IOException {
        _destroyIfRunningAndDumpOutputs ();
    }

    protected abstract void _destroyIfRunningAndDumpOutputs () throws IOException;


    protected final void _destroyIfRunningAndDumpOutputs (
        final Job job, final String prefix
    ) throws IOException {
        if (job.isRunning ()) {
            job.destroy ();
        }

        Strings.storeToFile (
            String.format ("test.%s.%s.out.txt", __testName, prefix),
            job.getOutput ()
        );


        Strings.storeToFile (
            String.format ("test.%s.%s.err.txt", __testName, prefix),
            job.getError ()
        );
    }

    //

    public final void destroy () {
        _destroy ();
    }

    protected abstract void _destroy ();

    //

    public final void assertRestOutErrEmpty () throws IOException {
        _assertRestOutErrEmpty ();
    }

    protected abstract void _assertRestOutErrEmpty () throws IOException;

    //

    static List <String> propertiesStartingWith (final String prefix) {
        final List <String> result = new LinkedList <String> ();

        for (final String key : System.getProperties ().stringPropertyNames ()) {
            if (key.startsWith (prefix)) {
                final Object valueObject = System.getProperty (key);
                if (valueObject instanceof String) {
                    final String value = (String) valueObject;
                    if (! value.isEmpty ()) {
                        result.add (String.format ("-D%s=%s", key, value));
                    }
                }
            }
        }

        return result;
    }


    static String classPath (final File ... paths) {
        return Strings.join (File.pathSeparator, (Object []) paths);
    }


    static boolean watchFile (final File file, final Duration duration) {
        final long watchEnd = System.nanoTime () + duration.to (TimeUnit.NANOSECONDS);
        while (file.exists () &&  System.nanoTime () < watchEnd) {
            _WATCH_DELAY_.sleepUninterruptibly ();
        }

        return file.exists ();
    }

}
