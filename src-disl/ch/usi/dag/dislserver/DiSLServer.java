package ch.usi.dag.dislserver;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import ch.usi.dag.disl.util.Logging;
import ch.usi.dag.util.logging.Logger;


public final class DiSLServer {

    private static final Logger __log = Logging.getPackageInstance ();

    //

    private static final String PROP_PORT = "dislserver.port";
    private static final int DEFAULT_PORT = 11217;

    private static final String PROP_CONT = "dislserver.continuous";
    private static final boolean continuous = Boolean.getBoolean(PROP_CONT);

    //

    private static final String __PID_FILE__ = "server.pid.file";
    private static final String __STATUS_FILE__ = "server.status.file";

    //

    private enum ElapsedTime {
        RECEIVE, PROCESS, TRANSMIT;
    }

    //

    private final AtomicInteger __workerCount = new AtomicInteger ();
    private final CounterSet <ElapsedTime> __globalStats = new CounterSet <ElapsedTime> (ElapsedTime.class);

    //

    final class ConnectionHandler implements Runnable {

        private final SocketChannel __clientSocket;
        private final RequestProcessor __requestProcessor;
        private final Thread __serverThread;

        //

        ConnectionHandler (
            final SocketChannel clientSocket,
            final RequestProcessor requestProcessor,
            final Thread serverThread
        ) {
            __clientSocket = clientSocket;
            __requestProcessor = requestProcessor;
            __serverThread = serverThread;
        }

        @Override
        public void run () {
            __workerCount.incrementAndGet ();

            try (
                final MessageChannel mc = new MessageChannel (__clientSocket);
            ) {
                //
                // Process requests until a shutdown request is received, a
                // communication error occurs, or an internal error occurs.
                //
                final CounterSet <ElapsedTime> stats = new CounterSet  <ElapsedTime> (ElapsedTime.class);
                final IntervalTimer <ElapsedTime> timer = new IntervalTimer <ElapsedTime> (ElapsedTime.class);

                REQUEST_LOOP: while (true) {
                    timer.reset ();

                    final Message request = mc.recvMessage ();
                    timer.mark (ElapsedTime.RECEIVE);

                    if (request.isShutdown ()) {
                        break REQUEST_LOOP;
                    }


                    //
                    // Process the request and send the response to the client.
                    // Update the timing stats if everything goes well.
                    //
                    try {
                        final Message response = __requestProcessor.process (request);
                        timer.mark (ElapsedTime.PROCESS);

                        mc.sendMessage (response);
                        timer.mark (ElapsedTime.TRANSMIT);

                        stats.update (timer);

                    } catch (final DiSLServerException dse) {
                        //
                        // Error during instrumentation. Report it to the client
                        // and stop receiving requests from this connection.
                        //
                        mc.sendMessage (
                            Message.createErrorResponse (dse.getMessage ())
                        );

                        break REQUEST_LOOP;
                    }
                }

                //
                // Merge thread-local stats with global stats when leaving
                // the request loop.
                //
                __globalStats.update (stats);

            } catch (final IOException ioe) {
                //
                // Communication error -- just log a message here.
                //
                __log.error (
                    "error communicating with client: %s", ioe.getMessage ()
                );
            }

            //
            // If there are no more workers left and we are not operating
            // in continuous mode, shut the server down.
            //
            if (__workerCount.decrementAndGet () == 0) {
                if (!continuous) {
                    __serverThread.interrupt ();
                }
            }
        }

    }

    //

    void run (
        final ServerSocketChannel serverSocket,
        final ExecutorService executor,
        final RequestProcessor requestProcessor
    ) {
        try {
            final Thread serverThread = Thread.currentThread ();

            while (!serverThread.isInterrupted ()) {
                final SocketChannel clientSocket = serverSocket.accept ();
                clientSocket.setOption (StandardSocketOptions.TCP_NODELAY, true);

                __log.debug (
                    "connection from %s", clientSocket.getRemoteAddress ()
                );

                // client socket handed off to connection handler
                executor.submit (new ConnectionHandler (
                    clientSocket, requestProcessor, serverThread
                ));
            }

        } catch (final ClosedByInterruptException cbie) {
            //
            // The server was interrupted, we are shutting down.
            //

        } catch (final IOException ioe) {
            //
            // Communication error -- just log a message here.
            //
            __log.error ("error accepting a connection: %s", ioe.getMessage ());
        }

        //

        __log.debug ("receiving data took %d ms", __stats (ElapsedTime.RECEIVE));
        __log.debug ("processing took %d ms", __stats (ElapsedTime.PROCESS));
        __log.debug ("transmitting data took %d ms", __stats (ElapsedTime.TRANSMIT));
    }

    private long __stats (final ElapsedTime et) {
        return TimeUnit.MILLISECONDS.convert (
            __globalStats.get (et), TimeUnit.NANOSECONDS
        );
    }

    //

    public static void main (final String [] args) {
        __log.debug ("server starting");
        __serverStarting ();

        final InetSocketAddress address = __getListenAddress ();
        final ServerSocketChannel socket = __getServerSocket (address);

        __log.debug (
            "listening on %s:%d", address.getHostString (), address.getPort ()
        );

        //

        final RequestProcessor processor = __getRequestProcessor ();
        final ExecutorService executor = Executors.newCachedThreadPool ();
        final DiSLServer server = new DiSLServer ();

        __log.debug ("server started");
        __serverStarted ();

        server.run (socket, executor, processor);

        __log.debug ("server shutting down");
        executor.shutdown ();
        processor.terminate ();
        __closeSocket (socket);

        __log.debug ("server finished");
        System.exit (0);
    }


    private static void __serverStarting () {
        final File file = __getFileProperty (__PID_FILE__);
        if (file != null) {
            file.deleteOnExit ();
        }
    }


    private static void __serverStarted () {
        final File file = __getFileProperty (__STATUS_FILE__);
        if (file != null) {
            file.delete ();
        }
    }


    private static File __getFileProperty (final String name) {
        final String value = System.getProperty (name, "").trim ();
        return value.isEmpty () ? null : new File (value);
    }


    private static InetSocketAddress __getListenAddress () {
        final int port = Integer.getInteger (PROP_PORT, DEFAULT_PORT);
        if (port < 1 || port > 65535) {
            __log.error (
                "port must be between 1 and 65535, inclusive (found %d)", port
            );

            System.exit (1);
            // unreachable
        }

        return new InetSocketAddress (port);
    }


    private static ServerSocketChannel __getServerSocket (final SocketAddress addr) {
        try {
            final ServerSocketChannel ssc = ServerSocketChannel.open ();
            ssc.setOption (StandardSocketOptions.SO_REUSEADDR, true);
            ssc.bind (addr);
            return ssc;

        } catch (final IOException ioe) {
            __log.error ("failed to create socket: %s", ioe.getMessage ());
            System.exit (1);

            // unreachable
            return null;
        }
    }


    private static RequestProcessor __getRequestProcessor () {
        try {
            return RequestProcessor.newInstance ();

        } catch (final DiSLServerException dse) {
            __log.error (dse.getMessage ());
            final Throwable cause = dse.getCause ();
            if (cause != null) {
                __log.error (cause.getMessage ());
            }

            System.exit (1);

            // unreachable
            return null;
        }
    }


    private static void __closeSocket (final Closeable socket) {
        try {
            socket.close ();

        } catch (final IOException ioe) {
            __log.warn ("failed to close socket: %s", ioe.getMessage ());
        }
    }

}
