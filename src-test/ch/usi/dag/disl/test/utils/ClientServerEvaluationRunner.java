package ch.usi.dag.disl.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.usi.dag.util.Duration;
import ch.usi.dag.util.Lists;


public class ClientServerEvaluationRunner extends Runner {

    private static final String __CLIENT_JAVA_COMMAND__ = _getJavaCommand (
        System.getProperty ("runner.disl.java.home")
    );

    private static final String __SERVER_JAVA_COMMAND__ = _getJavaCommand (
        System.getProperty ("runner.disl.server.java.home")
    );

    private static final String __SHADOW_JAVA_COMMAND__ = _getJavaCommand (
        System.getProperty ("runner.shvm.server.java.home")
    );

    //

    private Job __client;
    private boolean __clientOutEmpty = true;
    private boolean __clientErrEmpty = true;


    private Job __server;
    private boolean __serverOutEmpty = true;
    private boolean __serverErrEmpty = true;

    private Job __shadow;
    private boolean __shadowOutEmpty = true;
    private boolean __shadowErrEmpty = true;

    //

    public ClientServerEvaluationRunner (final Class <?> testClass) {
        super (testClass);
    }

    private Job __startServer (
        final File testInstJar, final File statusFile
    ) throws IOException {
        final List <String> command = Lists.newLinkedList (
            __SERVER_JAVA_COMMAND__,
            "-classpath", Runner.classPath (_DISL_SERVER_JAR_, testInstJar)
        );

        if (statusFile != null) {
            command.add (String.format (
                "-Dserver.status.file=%s", statusFile
            ));
        }

        command.addAll (propertiesStartingWith ("dislserver."));
        command.addAll (propertiesStartingWith ("disl."));
        command.add (_DISL_SERVER_CLASS_.getName ());

        //

        final Job result = new Job (command);
        result.start ();
        return result;
    }


    private Job __startShadow (
        final File testInstJar, final File statusFile
    ) throws IOException {
        final List <String> command = Lists.newLinkedList (
            __SHADOW_JAVA_COMMAND__, "-Xms1G", "-Xmx2G",
            "-classpath", Runner.classPath (_SHVM_SERVER_JAR_, testInstJar)
        );

        if (statusFile != null) {
            command.add (String.format (
                "-Dserver.status.file=%s", statusFile
            ));
        }

        command.addAll (propertiesStartingWith ("dislreserver."));
        command.add (_SHVM_SERVER_CLASS_.getName ());

        //

        final Job result = new Job (command);
        result.start ();
        return result;
    }


    private Job __startClient (
        final File testInstJar, final File testAppJar
    ) throws IOException {
        final List <String> command = Lists.newLinkedList (
            __CLIENT_JAVA_COMMAND__,
            String.format ("-agentpath:%s", _DISL_AGENT_LIB_),
            String.format ("-agentpath:%s", _SHVM_AGENT_LIB_),
            String.format ("-Xbootclasspath/a:%s", Runner.classPath (
                _DISL_BYPASS_JAR_, _SHVM_DISPATCH_JAR_, testInstJar
            ))
        );

        command.addAll (propertiesStartingWith ("disl."));
        command.addAll (Arrays.asList (
            "-jar", testAppJar.toString ()
        ));

        //

        final Job result = new Job (command);
        result.start ();
        return result;
    }


    @Override
    protected void _start (
        final File testInstJar, final File testAppJar
    ) throws IOException {
        final File serverFile = File.createTempFile ("disl-", ".status");
        serverFile.deleteOnExit ();

        __server = __startServer (testInstJar, serverFile);

        watchFile (serverFile, _INIT_TIME_LIMIT_);

        if (! __server.isRunning ()) {
            throw new IOException ("server failed: "+ __server.getError ());
        }

        //

        final File shadowFile = File.createTempFile ("shvm-", ".status");
        shadowFile.deleteOnExit ();

        __shadow = __startShadow (testInstJar, shadowFile);

        watchFile (shadowFile, _INIT_TIME_LIMIT_);

        if (! __shadow.isRunning ()) {
            throw new IOException ("shadow failed: "+ __shadow.getError ());
        }

        //

        __client = __startClient (testInstJar, testAppJar);
    }


    @Override
    protected boolean _waitFor (final Duration duration) {
        // FIXME Wait in parallel for the specified duration.
        boolean finished = true;
        finished = finished & __client.waitFor (duration);
        finished = finished & __server.waitFor (duration);
        finished = finished & __shadow.waitFor (duration);
        return finished;
    }


    @Override
    protected void _assertIsStarted () {
        assertTrue ("client not started", __client.isStarted ());
        assertTrue ("server not started", __server.isStarted ());
        assertTrue ("shadow not started", __shadow.isStarted ());
    }


    @Override
    protected void _assertIsFinished () {
        assertTrue ("client not finished", __client.isFinished ());
        assertTrue ("server not finished", __server.isFinished ());
        assertTrue ("shadow not finished", __shadow.isFinished ());
    }


    @Override
    protected void _assertIsSuccessful () {
        assertTrue ("client failed", __client.isSuccessful ());
        assertTrue ("server failed", __server.isSuccessful ());
        assertTrue ("shadow failed", __shadow.isSuccessful ());
    }


    @Override
    protected void _destroyIfRunningAndDumpOutputs () throws IOException {
        _destroyIfRunningAndDumpOutputs (__client, "client");
        _destroyIfRunningAndDumpOutputs (__server, "server");
        _destroyIfRunningAndDumpOutputs (__shadow, "shadow");
    }


    public void assertClientOut (final String fileName) throws IOException {
        __clientOutEmpty = false;
        assertEquals (
            "client out does not match",
            _loadResource (fileName), __client.getOutput ()
        );
    }


    public void assertClientOutEmpty () throws IOException {
        __clientOutEmpty = false;
        assertEquals ("client out is not empty", "", __client.getOutput ());
    }


    public void assertClientErr (final String fileName) throws IOException {
        __clientErrEmpty = false;
        assertEquals (
            "client err does not match",
            _loadResource (fileName), __client.getError ()
        );
    }


    public void assertClientErrEmpty () throws IOException {
        __clientErrEmpty = false;
        assertEquals ("client err is not empty", "", __client.getError ());
    }


    public void assertShadowOut (final String fileName) throws IOException {
        __shadowOutEmpty = false;
        assertEquals (
            "shadow out does not match",
            _loadResource (fileName), __shadow.getOutput ()
        );
    }


    public void assertShadowOutEmpty () throws IOException {
        __shadowOutEmpty = false;
        assertEquals ("shadow out is not empty", "", __shadow.getOutput ());
    }


    public void assertShadowErr (final String fileName) throws IOException {
        __shadowErrEmpty = false;
        assertEquals (
            "shadow err does not match",
            _loadResource (fileName), __shadow.getError ()
        );
    }


    public void assertShadowErrEmpty () throws IOException {
        __shadowErrEmpty = false;
        assertEquals ("shadow err is not empty", "", __shadow.getError ());
    }


    public void assertServerOut (final String fileName) throws IOException {
        __serverOutEmpty = false;
        assertEquals (
            "server out does not match",
            _loadResource (fileName), __server.getOutput ()
        );
    }


    public void assertServerOutEmpty ()
    throws IOException {
        __serverOutEmpty = false;
        assertEquals ("server out is not empty", "", __server.getOutput ());
    }


    public void assertServerErr (final String fileName) throws IOException {
        __serverErrEmpty = false;
        assertEquals (
            "server err does not match",
            _loadResource (fileName), __server.getError ()
        );
    }


    public void assertServerErrEmpty () throws IOException {
        __serverErrEmpty = false;
        assertEquals ("server err is not empty", "", __server.getError ());
    }


    @Override
    protected void _assertRestOutErrEmpty () throws IOException {
        if (__clientOutEmpty) {
            assertClientOutEmpty ();
        }

        if (__clientErrEmpty) {
            assertClientErrEmpty ();
        }

        if (__shadowOutEmpty) {
            assertShadowOutEmpty ();
        }
        if (__shadowErrEmpty) {
            assertShadowErrEmpty ();
        }

        if (__serverOutEmpty) {
            assertServerOutEmpty ();
        }
        if (__serverErrEmpty) {
            assertServerErrEmpty ();
        }
    }


    @Override
    protected void _destroy () {
        if (__client != null) {
            __client.destroy ();
        }
        if (__shadow != null) {
            __shadow.destroy ();
        }
        if (__server != null) {
            __server.destroy ();
        }
    }
}
