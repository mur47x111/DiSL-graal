package ch.usi.dag.dislreserver.msg.threadend;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisHandler;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;

public class ThreadEndHandler implements RequestHandler {

    final AnalysisHandler analysisHandler;

    public ThreadEndHandler(AnalysisHandler anlHndl) {
        analysisHandler = anlHndl;
    }

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            long threadId = is.readLong();

            // announce thread end to the analysis handler
            analysisHandler.threadEnded(threadId);

        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void awaitProcessing() {

    }

    public void exit() {

    }
}
