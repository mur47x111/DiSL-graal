package ch.usi.dag.dislreserver.msg.instr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.instr.InstrMsgReader.InstrClassMessage;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;

public abstract class AbstractInstrumentation implements RequestHandler {

    // used for replays
    private static final byte[] emptyByteArray = new byte[0];

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            InstrClassMessage nm = InstrMsgReader.readMessage(is);

            byte[] instrClass;

            try {

                instrClass = instrument(new String(nm.getControl()),
                        nm.getClassCode());

            }
            catch (DiSLREServerException e) {

                // instrumentation error
                // send the client a description of the server-side error

                String errToReport = e.getMessage();

                // during debug send the whole message
                if(debug) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    errToReport = sw.toString();
                }

                // error protocol:
                // control contains the description of the server-side error
                // class code is an array of size zero
                String errMsg = "Instrumentation error for class "
                        + new String(nm.getControl()) + ": " + errToReport;

                InstrMsgReader.sendMessage(os, new InstrClassMessage(errMsg
                        .getBytes(), emptyByteArray));

                throw e;
            }

            InstrClassMessage replyData = null;

            if(instrClass != null) {
                // class was modified - send modified data
                replyData = new InstrClassMessage(emptyByteArray, instrClass);
            }
            else {
                // zero length means no modification
                replyData = new InstrClassMessage(emptyByteArray, emptyByteArray);
            }

            InstrMsgReader.sendMessage(os, replyData);
        }
        catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    protected abstract byte[] instrument(String string, byte[] classCode)
            throws DiSLREServerException;
}
