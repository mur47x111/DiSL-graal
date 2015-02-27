package ch.usi.dag.dislreserver.reqdispatch;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import ch.usi.dag.dislreserver.DiSLREServerException;

public interface RequestHandler {

    void handle(DataInputStream is, DataOutputStream os, boolean debug)
            throws DiSLREServerException;

    // invoked at exit
    void exit();
}
