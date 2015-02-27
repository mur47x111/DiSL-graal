package ch.usi.dag.dislreserver.msg.threadinfo;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import ch.usi.dag.dislreserver.DiSLREServerException;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;
import ch.usi.dag.dislreserver.shadow.NetReferenceHelper;
import ch.usi.dag.dislreserver.shadow.ShadowClass;
import ch.usi.dag.dislreserver.shadow.ShadowClassTable;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;
import ch.usi.dag.dislreserver.shadow.ShadowThread;

public class ThreadInfoHandler implements RequestHandler {

    public void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException {

        try {

            long net_ref = is.readLong();
            String name = is.readUTF();
            boolean isDaemon = is.readBoolean();

            ShadowClass klass = ShadowClassTable.get(NetReferenceHelper
                    .get_class_id(net_ref));

            ShadowThread sThread = new ShadowThread(net_ref, name, isDaemon,
                    klass);
            ShadowObjectTable.register(sThread, debug);
        } catch (IOException e) {
            throw new DiSLREServerException(e);
        }
    }

    public void exit() {

    }
}
