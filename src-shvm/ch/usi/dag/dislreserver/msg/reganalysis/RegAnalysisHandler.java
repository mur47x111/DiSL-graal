package ch.usi.dag.dislreserver.msg.reganalysis;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisResolver;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;

public final class RegAnalysisHandler implements RequestHandler {

    public void handle(final DataInputStream is, final DataOutputStream os,
            final boolean debug) throws DiSLREServerException {
        try {
            final short methodId = is.readShort();
            String methodString = is.readUTF();

            // register method
            AnalysisResolver.registerMethodId(methodId, methodString);

            if (debug) {
                System.out.printf(
                        "DiSL-RE: registered %s as analysis method %d\n",
                        methodString.toString(), methodId);
            }

        } catch (final IOException ioe) {
            throw new DiSLREServerException(ioe);
        }
    }

    public void exit() {

    }

}
