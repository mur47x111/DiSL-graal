package ch.usi.dag.dislreserver.msg.classinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.ShadowClass;
import ch.usi.dag.dislreserver.shadow.ShadowClassTable;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;

public class ClassInfoHandler implements RequestHandler {

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            long net_ref = is.readLong();
            String classSignature = is.readUTF();
            String classGenericStr = is.readUTF();
            ShadowObject classLoader = ShadowObjectTable.get(is.readLong());

            ShadowClass superClass = (ShadowClass) ShadowObjectTable.get(is
                    .readLong());
            ShadowClassTable.newInstance(net_ref, superClass, classLoader,
                    classSignature, classGenericStr, debug);
        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void exit() {

    }
}
