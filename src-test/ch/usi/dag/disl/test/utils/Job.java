package ch.usi.dag.disl.test.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import ch.usi.dag.util.Duration;
import ch.usi.dag.util.Strings;
import ch.usi.dag.util.function.Predicate;


/**
 * Wraps the {@link Process} class provided by Java and adds some more features.
 * Prior to execution, a job saves binaries to the specified location and then
 * executes specified command.
 * <p>
 * It is possible to retrieve the exit value of the process or the state of the
 * process. Normal output stream and error stream can be accessed represented as
 * a {@link String}, but it is only intended for debugging purposes.
 *
 * @author Frantisek Haas
 * @author Lubomir Bulej
 */
final class Job {

    private static final String __EMPTY_STRING__ = "";

    /** Whether to close all streams of the underlying process after start. */
    private static final boolean __CLOSE_STREAMS__ = false;

    //

    /** Builder to create the underlying process. */
    private final ProcessBuilder __builder;

    //

    /** The underlying process. */
    private Process __process;

    /** The contents of the underlying process standard output stream. */
    private String __output;

    /** The contents of the underlying process standard error stream. */
    private String __error;

    /**
     * Waits for {@link #__process} to finish. What that happens, sets
     * {@link #__isRunning} to {@code false}, and notifies all objects waiting
     * on {@link #__isRunning}.
     */
    private Thread __waiter;

    /** The status of the underlying process. */
    private final AtomicBoolean __isRunning = new AtomicBoolean ();

    //

    public Job (final List <String> command) {
        final ProcessBuilder builder = new ProcessBuilder ();
        builder.command (command);
        builder.environment ().clear ();

        //

        __builder = builder;
    }


    /**
     * Executes the command corresponding to this {@link Job}.
     *
     * @throws IOException
     *         if a problem occurs while trying to execute the job command
     */
    public Job start () throws IOException {
        __ensureJobNotStarted();

        //

        if (Runner.TEST_DEBUG) {
            System.out.println ("Starting job:");
            System.out.println (Strings.join (" ", __builder.command ()));
        }

        //

        __process = __builder.start ();
        __isRunning.set (true);

        //
        // It seems that reading streams from the created process is very
        // tricky and hangs the reader. It's better to redirect stdout
        // and stderr to files in the forked process.
        //
        // TODO LB: What exactly does that mean?
        //
        if (__CLOSE_STREAMS__) {
            __process.getInputStream ().close ();
            __process.getOutputStream ().close ();
            __process.getErrorStream ().close ();
        }

        //

        __waiter = new Thread (new Runnable () {
            @Override
            public void run () {
                try {
                    __process.waitFor ();

                    // update the state
                    synchronized (__isRunning) {
                        __isRunning.set (false);
                    }

                } catch (final InterruptedException ie) {
                    // just leave, without updating the state
                }

                // wake any sleepers
                synchronized (__isRunning) {
                    __isRunning.notifyAll ();
                }
            }
        });

        __waiter.start ();
        return this;
    }


    private void __ensureJobNotStarted () {
        if (isStarted ()) {
            throw new IllegalStateException ("the job has been already started");
        }
    }


    /**
     * Kills the process associated with this {@link Job}. The job must have
     * been started first.
     *
     * @throws IllegalStateException
     *         if the job has not been yet started
     */
    public void destroy () {
        __ensureJobStarted ();

        //

        __waiter.interrupt ();
        __process.destroy ();

        __output = null;
        __error = null;
        __process = null;
        __waiter = null;
    }


    private void __ensureJobStarted () {
        if (!isStarted ()) {
            throw new IllegalStateException ("the job needs to be started first");
        }
    }


    /**
     * Provides access to the input stream of the process associated with this
     * {@link Job}. The job must have been started first.
     *
     * @return the input stream of the process associated with this {@link Job}.
     * @throws IllegalStateException
     *         if the job has not been yet started
     */
    public OutputStream getInput () {
        __ensureJobStarted ();

        //

        return __process.getOutputStream ();
    }


    /**
     * Reads out the whole output stream of the underlying process into a
     * {@link String}. May block if the associated process is still running. The
     * job must have been started first.
     *
     * @return {@link String} containing the whole process output.
     * @throws IOException
     *         if an I/O error occurs while reading the process output
     * @throws IllegalStateException
     *         if the job has not been yet started
     */
    public String getOutput () throws IOException {
        __ensureJobStarted ();

        //

        if (__CLOSE_STREAMS__) {
            return __EMPTY_STRING__;
        }

        //

        if (__output == null) {
            __output = Strings.drainStream (__process.getInputStream ());
        }

        return __output;
    }


    /**
     * Reads out the whole error output stream of the underlying process into a
     * {@link String}. May block if the associated process is still running. The
     * job must have been started first.
     *
     * @return {@link String} containing the whole process error output.
     * @throws IOException
     *         if an I/O error occurs while reading the process error output
     * @throws IllegalStateException
     *         if the job has not been yet started
     */
    public String getError () throws IOException {
        __ensureJobStarted ();

        //

        if (__CLOSE_STREAMS__) {
            return __EMPTY_STRING__;
        }

        //

        if (__error == null) {
            __error = Strings.drainStream (__process.getErrorStream ());
        }

        return __error;
    }


    /**
     * Waits for a job to finish, for at most the specified time. Returns
     * {@code true} if a job is finished or has finished within the given time
     * limit.
     *
     * @param duration
     *        the time limit duration
     * @param unit
     *        the unit of the time limit duration
     * @return {@code true} if a job has finished, {@code false} otherwise.
     */
    public boolean waitFor (final Duration duration) {
        __ensureJobStarted ();

        //

        return duration.awaitUninterruptibly (__isRunning, __BOOLEAN_IS_FALSE__);
    }

    private static Predicate <AtomicBoolean> __BOOLEAN_IS_FALSE__ =
        new Predicate <AtomicBoolean> () {
            @Override
            public boolean test (final AtomicBoolean value) {
                return !value.get ();
            }
        };


    /**
     * Determines the status of this {@link Job} and returns {@code true} if the
     * job has been started.
     *
     * @return {@code true} if the job has been started, {@code false}
     *         otherwise.
     */
    public boolean isStarted () {
        return (__process != null);
    }


    /**
     * Determines the status of this {@link Job} and returns {@code true} if the
     * job has been started and is still running.
     *
     * @return {@code true} if a started job is still running, {@code false}
     *         otherwise.
     */
    public boolean isRunning () {
        return isStarted () && __isRunning.get ();
    }


    /**
     * Determines the status of this {@link Job} and returns {@code true} if the
     * job has been started and is finished.
     *
     * @return {@code true} if a started job has finished, {@code false}
     *         otherwise.
     */
    public boolean isFinished () {
        return isStarted () && !__isRunning.get ();
    }


    /**
     * Determines the status of this {@link Job} and returns {@code true} if the
     * job has been started and finished successfully, i.e. it exited with a
     * zero value.
     *
     * @return {@code true} if a started job finished successfully,
     *         {@code false} otherwise.
     */
    public boolean isSuccessful () {
        // The exit value MUST be queried AFTER a process has terminated.
        return isFinished () && __process.exitValue () == 0;
    }
}
