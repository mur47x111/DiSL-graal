package ch.usi.dag.dislreserver.msg.analyze;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisResolver.AnalysisMethodHolder;
import ch.usi.dag.dislreserver.msg.analyze.mtdispatch.AnalysisDispatcher;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;


public final class AnalysisHandler implements RequestHandler {

    private AnalysisDispatcher dispatcher = new AnalysisDispatcher ();

    public AnalysisDispatcher getDispatcher() {
        return dispatcher;
    }

    public void handle (
        final DataInputStream is, final DataOutputStream os, final boolean debug
    ) throws DiSLREServerException {

        try {
            // get net reference for the thread
            long orderingID = is.readLong ();

            // read and create method invocations
            final int invocationCount = is.readInt ();
            if (invocationCount < 0) {
                throw new DiSLREServerException (String.format (
                    "invalid number of analysis invocation requests: %d",
                    invocationCount
                ));
            }

            List <AnalysisInvocation> invocations = __unmarshalInvocations (
                invocationCount, is, debug
            );

            dispatcher.addTask (orderingID, invocations);

        } catch (final IOException ioe) {
            throw new DiSLREServerException(ioe);
        }
    }


    private List <AnalysisInvocation> __unmarshalInvocations (
        final int invocationCount, final DataInputStream is, final boolean debug
    ) throws DiSLREServerException {
        final List <AnalysisInvocation> result =
            new LinkedList <AnalysisInvocation> ();

        for (int i = 0; i < invocationCount; ++i) {
            result.add (__unmarshalInvocation (is, debug));
        }

        return result;
    }


    private AnalysisInvocation __unmarshalInvocation (
        final DataInputStream is, final boolean debug
    ) throws DiSLREServerException {
        try {
            // *** retrieve method ***

            // read method id from network and retrieve method
            final short methodId = is.readShort ();
            AnalysisMethodHolder amh = AnalysisResolver.getMethod (methodId);

            // *** retrieve method argument values ***

            final Method method = amh.getAnalysisMethod ();

            // read the length of argument data in the request
            final short argsLength = is.readShort ();
            if (argsLength < 0) {
                throw new DiSLREServerException (String.format (
                    "invalid length of marshaled argument data for analysis method %d (%s.%s): %d",
                    methodId, method.getDeclaringClass ().getName (),
                    method.getName (), argsLength
                ));
            }

            // read argument values data according to argument types
            final Class <?> [] paramTypes = method.getParameterTypes ();
            final Object [] args = new Object [paramTypes.length];
            
            int argsRead = 0;
            int argIndex = 0;
            for (final Class <?> argClass : paramTypes) {
                argsRead += unmarshalArgument (
                    is, argClass, method, argIndex, args
                );
                
                argIndex += 1;
            }

            if (argsRead != argsLength) {
                throw new DiSLREServerException (String.format (
                    "received %d, but unmarshalled %d bytes of argument data for analysis method %d (%s.%s)",
                    argsLength, argsRead, methodId, method.getDeclaringClass ().getName (),
                    method.getName ()
                ));
            }

            // *** create analysis invocation ***

            if(debug) {
                System.out.printf (
                    "DiSL-RE: dispatching analysis method (%d) to %s.%s()\n",
                    methodId, amh.getAnalysisInstance().getClass().getSimpleName (),
                    method.getName()
                );
            }
            
            return new AnalysisInvocation (
                method, amh.getAnalysisInstance (), args
            );

        } catch (final IOException ioe) {
            throw new DiSLREServerException (ioe);
        } catch (final IllegalArgumentException iae) {
            throw new DiSLREServerException (iae);
        }
    }


    private int unmarshalArgument (
        final DataInputStream is, final Class <?> argClass,
        final Method analysisMethod, final int index, final Object [] args
    ) throws IOException, DiSLREServerException {

        if (boolean.class.equals (argClass)) {
            args [index] = is.readBoolean ();
            return Byte.SIZE / Byte.SIZE;
        }

        if (char.class.equals (argClass)) {
            args [index] = is.readChar ();
            return Character.SIZE / Byte.SIZE;
        }

        if (byte.class.equals (argClass)) {
            args [index] = is.readByte ();
            return Byte.SIZE / Byte.SIZE;
        }

        if (short.class.equals (argClass)) {
            args [index] = is.readShort ();
            return Short.SIZE / Byte.SIZE;
        }

        if (int.class.equals (argClass)) {
            args [index] = is.readInt ();
            return Integer.SIZE / Byte.SIZE;
        }

        if (long.class.equals (argClass)) {
            args [index] = is.readLong ();
            return Long.SIZE / Byte.SIZE;
        }

        if (float.class.equals (argClass)) {
            args [index] = is.readFloat ();
            return Float.SIZE / Byte.SIZE;
        }

        if (double.class.equals (argClass)) {
            args [index] = is.readDouble ();
            return Double.SIZE / Byte.SIZE;
        }

        if (ShadowObject.class.isAssignableFrom (argClass)) {
            // zero tag means null
            final long tag = is.readLong();
            args [index] = (tag == 0) ? null : ShadowObjectTable.get (tag);
            return Long.SIZE / Byte.SIZE;
        }

        throw new DiSLREServerException (String.format (
            "Unsupported data type %s in analysis method %s.%s",
            argClass.getName (), analysisMethod.getDeclaringClass ().getName (),
            analysisMethod.getName ()
        ));
    }

    public void threadEnded(long threadId) {
        dispatcher.threadEndedEvent(threadId);
    }

    public void objectsFreed(long[] objFreeIDs) {
        dispatcher.objectsFreedEvent(objFreeIDs);
    }

    public void exit() {
        dispatcher.exit();
    }
}
