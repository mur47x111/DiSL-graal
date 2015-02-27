package ch.usi.dag.dislreserver.msg.stringinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.NetReferenceHelper;
import ch.usi.dag.dislreserver.shadow.ShadowClass;
import ch.usi.dag.dislreserver.shadow.ShadowClassTable;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;
import ch.usi.dag.dislreserver.shadow.ShadowString;

public class StringInfoHandler implements RequestHandler {

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            long net_ref = is.readLong();
            String str = is.readUTF();

            ShadowClass klass = ShadowClassTable.get(NetReferenceHelper
                    .get_class_id(net_ref));
            ShadowString sString = new ShadowString(net_ref, str, klass);
            ShadowObjectTable.register(sString, debug);
        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void exit() {

    }

}
