package ch.usi.dag.dislreserver.reqdispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.DiSLREServerFatalException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisHandler;
import ch.usi.dag.dislreserver.msg.classinfo.ClassInfoHandler;
import ch.usi.dag.dislreserver.msg.close.CloseHandler;
import ch.usi.dag.dislreserver.msg.newclass.NewClassHandler;
import ch.usi.dag.dislreserver.msg.objfree.ObjectFreeHandler;
import ch.usi.dag.dislreserver.msg.reganalysis.RegAnalysisHandler;
import ch.usi.dag.dislreserver.msg.stringinfo.StringInfoHandler;
import ch.usi.dag.dislreserver.msg.threadend.ThreadEndHandler;
import ch.usi.dag.dislreserver.msg.threadinfo.ThreadInfoHandler;


public final class RequestDispatcher {

    /**
     * Request identifiers. MUST be kept in sync with the native agent.
     * <p>
     * TODO Generate a JNI header from a suitable class for the native agent.
     */
    private static final byte __REQUEST_ID_CLOSE__ = 0;
    private static final byte __REQUEST_ID_INVOKE_ANALYSIS__ = 1;
    private static final byte __REQUEST_ID_OBJECT_FREE__ = 2;
    private static final byte __REQUEST_ID_NEW_CLASS__ = 3;
    private static final byte __REQUEST_ID_CLASS_INFO__ = 4;
    private static final byte __REQUEST_ID_STRING_INFO__ = 5;
    private static final byte __REQUEST_ID_REGISTER_ANALYSIS__ = 6;
    private static final byte __REQUEST_ID_THREAD_INFO__ = 7;
    private static final byte __REQUEST_ID_THREAD_END__ = 8;

    //

    private static RequestHandler [] __dispatchTable;
    private static Collection <RequestHandler> __handlers;

    //

    static {
        //
        // Register request handlers.
        // The indices should be in sync with the native agent.
        //
        final Map <Byte, RequestHandler> requestMap = new HashMap <Byte, RequestHandler> ();
        requestMap.put (__REQUEST_ID_CLOSE__, new CloseHandler ());
        AnalysisHandler anlHndl = new AnalysisHandler ();
        requestMap.put (__REQUEST_ID_INVOKE_ANALYSIS__, anlHndl);
        requestMap.put (__REQUEST_ID_OBJECT_FREE__, new ObjectFreeHandler (anlHndl));
        requestMap.put (__REQUEST_ID_NEW_CLASS__, new NewClassHandler ());
        requestMap.put (__REQUEST_ID_CLASS_INFO__, new ClassInfoHandler ());
        requestMap.put (__REQUEST_ID_STRING_INFO__, new StringInfoHandler ());
        requestMap.put (__REQUEST_ID_REGISTER_ANALYSIS__, new RegAnalysisHandler ());
        requestMap.put (__REQUEST_ID_THREAD_INFO__, new ThreadInfoHandler());
        requestMap.put (__REQUEST_ID_THREAD_END__,  new ThreadEndHandler(anlHndl));

        __handlers = Collections.unmodifiableCollection (requestMap.values ());
        __dispatchTable = __createDispatchTable (requestMap);
    }


    private static RequestHandler [] __createDispatchTable (
        final Map <Byte, RequestHandler> requestMap
    ) {
        final RequestHandler [] result = new RequestHandler [Byte.MAX_VALUE];

        for (final Entry <Byte, RequestHandler> entry : requestMap.entrySet ()) {
            final byte requestId = entry.getKey ();
            if (requestId >= 0) {
                result [requestId] = entry.getValue ();
            }
        }

        return result;
    }

    //

    public static boolean dispatch (
        final byte requestId, final DataInputStream is,
        final DataOutputStream os, boolean debug
    ) throws DiSLREServerException {
        //
        // Lookup the request handler and process the request using the handler.
        // Signal to terminate the request loop after handling a close request.
        //
        final RequestHandler rh = __dispatchTable [requestId];
        if (rh != null) {
            if (debug) {
                System.out.printf (
                    "DiSL-RE: dispatching request message (%d) to %s\n",
                    requestId, rh.getClass ().getSimpleName ()
                );
            }

            rh.handle (is, os, debug);
            return requestId == __REQUEST_ID_CLOSE__;

        } else {
            throw new DiSLREServerFatalException (
                "Unsupported message type: "+ requestId
            );
        }
    }


    public static Iterable <RequestHandler> getAllHandlers () {
        return __handlers;
    }

}
