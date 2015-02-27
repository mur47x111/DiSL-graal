package ch.usi.dag.dislreserver;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import ch.usi.dag.dislreserver.reqdispatch.RequestDispatcher;
import ch.usi.dag.dislreserver.util.Logging;
import ch.usi.dag.util.logging.Logger;


public abstract class DiSLREServer {

    private static final Logger __log = Logging.getPackageInstance ();

    //

    private static final String PROP_DEBUG = "debug";
    private static final boolean debug = Boolean.getBoolean(PROP_DEBUG);

    private static final String PROP_PORT = "dislreserver.port";
    private static final int DEFAULT_PORT = 11218;

    //

    private static final String __PID_FILE__ = "server.pid.file";
    private static final String __STATUS_FILE__ = "server.status.file";

    //

    public static void main (final String [] args) {
        __log.debug ("server starting");
        __serverStarting ();

        final InetSocketAddress address = __getListenAddress ();
        final ServerSocketChannel socket = __getServerSocket (address);

        __log.debug (
            "listening on %s:%d", address.getHostString (), address.getPort ()
        );

        __log.debug ("server started");
        __serverStarted ();

        run (socket);

        __log.debug ("server shutting down");
        __closeSocket (socket);

        __log.debug ("server finished");
        System.exit(0); // to kill other threads
    }


    private static void run (final ServerSocketChannel socket) {
        try {
            final SocketChannel clientSocket = socket.accept ();

            __log.debug (
                "connection from %s", clientSocket.getRemoteAddress ()
            );

            processRequests (clientSocket.socket ());
            clientSocket.close ();

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

        __log.debug ("server shutting down");
    }


    private static void processRequests (final Socket sock) {
        try {
            final DataInputStream is = new DataInputStream (
                new BufferedInputStream (sock.getInputStream ()));
            final DataOutputStream os = new DataOutputStream (
                new BufferedOutputStream (sock.getOutputStream ()));

            REQUEST_LOOP: while (true) {
                final byte requestNo = is.readByte ();
                if (RequestDispatcher.dispatch (requestNo, is, os, debug)) {
                    break REQUEST_LOOP;
                }
            }

        } catch (final Exception e) {
            __logError (e);
        }
    }


    private static void __logError (final Throwable throwable) {
        if (throwable instanceof DiSLREServerException) {
            __logNestedErrors (throwable);

            if (__log.debugIsLoggable ()) {
                __log.debug (__getFullMessage (throwable));
            }

        } else {
            // some other exception
            __log.error ("fatal error: %s", __getFullMessage (throwable));
        }
    }


    private static void __logNestedErrors (final Throwable throwable) {
        String prefix = "server error: ";
        Throwable current = throwable;

        do {
            final String message = throwable.getMessage ();
            if (message != null) {
                __log.error ("%s%s", prefix, message);
            }

            prefix = "\t";
            current = throwable.getCause ();
        } while (current != null);
    }


    private static String __getFullMessage (final Throwable t) {
        final StringWriter result = new StringWriter ();
        t.printStackTrace (new PrintWriter (result));
        return result.toString ();
    }

    //

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


    private static void __closeSocket (final Closeable socket) {
        try {
            socket.close ();

        } catch (final IOException ioe) {
            __log.warn ("failed to close socket: %s", ioe.getMessage ());
        }
    }

}
