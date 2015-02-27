package ch.usi.dag.disl.test.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import ch.usi.dag.util.Duration;
import ch.usi.dag.util.Lists;

public class ClientServerRunner extends Runner {

    private static final String __CLIENT_JAVA_COMMAND__ = _getJavaCommand (
        System.getProperty ("runner.disl.java.home")
    );

    private static final String __SERVER_JAVA_COMMAND__ = _getJavaCommand (
        System.getProperty ("runner.disl.server.java.home")
    );

    //

    private Job __client;
    private boolean __clientOutEmpty = true;
    private boolean __clientErrEmpty = true;

    private Job __server;
    private boolean __serverOutEmpty = true;
    private boolean __serverErrEmpty = true;

    //

    public ClientServerRunner (final Class <?> testClass) {
        super (testClass);
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

        __client = __startClient (testInstJar, testAppJar);
    }


    private Job __startClient (
        final File testInstJar, final File testAppJar
    ) throws IOException {
        final List <String> command = Lists.newLinkedList (
            __CLIENT_JAVA_COMMAND__,
            String.format ("-agentpath:%s", _DISL_AGENT_LIB_),
            String.format ("-Xbootclasspath/a:%s", Runner.classPath (
                _DISL_BYPASS_JAR_, testInstJar
            ))
        );

        command.addAll (propertiesStartingWith ("disl."));
        command.addAll (Arrays.asList (
            "-jar", testAppJar.toString ()
        ));

        //

        return new Job (command).start ();
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

        return new Job (command).start ();
    }


    @Override
    protected boolean _waitFor (final Duration duration) {
        // FIXME Wait in parallel for the specified duration.
        boolean finished = true;
        finished = finished & __client.waitFor (duration);
        finished = finished & __server.waitFor (duration);
        return finished;
    }


    @Override
    protected void _assertIsStarted () {
        assertTrue ("client not started", __client.isStarted ());
        assertTrue ("server not started", __server.isStarted ());
    }


    @Override
    protected void _assertIsFinished () {
        assertTrue ("client not finished", __client.isFinished ());
        assertTrue ("server not finished", __server.isFinished ());
    }


    @Override
    protected void _assertIsSuccessful () {
        assertTrue ("client failed", __client.isSuccessful ());
        assertTrue ("server failed", __server.isSuccessful ());
    }


    @Override
    protected void _destroyIfRunningAndDumpOutputs () throws IOException {
        _destroyIfRunningAndDumpOutputs (__client, "client");
        _destroyIfRunningAndDumpOutputs (__server, "server");
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


    public void assertServerOut (final String fileName) throws IOException {
        __serverOutEmpty = false;
        assertEquals (
            "server out does not match",
            _loadResource (fileName), __server.getOutput ()
        );
    }


    public void assertServerOutEmpty () throws IOException {
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

        if (__server != null) {
            __server.destroy ();
        }
    }
}
