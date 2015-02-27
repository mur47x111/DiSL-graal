package ch.usi.dag.dislreserver.msg.newclass;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.ShadowClassTable;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;

public class NewClassHandler implements RequestHandler {

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            String className = is.readUTF();
            long oid = is.readLong();
            ShadowObject classLoader = ShadowObjectTable.get(oid);
            int classCodeLength = is.readInt();
            byte[] classCode = new byte[classCodeLength];
            is.readFully(classCode);

            ShadowClassTable.load(classLoader, className, classCode, debug);
        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void exit() {

    }

}
