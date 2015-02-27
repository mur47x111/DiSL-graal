package ch.usi.dag.dislreserver.msg.objfree;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisHandler;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;

public class ObjectFreeHandler implements RequestHandler {

final AnalysisHandler analysisHandler;

    public ObjectFreeHandler(AnalysisHandler anlHndl) {
        analysisHandler = anlHndl;
    }

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            int freeCount = is.readInt();

            long[] objFreeIDs = new long[freeCount];

            for(int i = 0; i < freeCount; ++i) {

                long netref = is.readLong();

                objFreeIDs[i] = netref;
            }

            analysisHandler.objectsFreed(objFreeIDs);

        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void exit() {

    }

}
