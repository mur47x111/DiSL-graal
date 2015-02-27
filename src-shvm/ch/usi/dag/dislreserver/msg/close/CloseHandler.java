package ch.usi.dag.dislreserver.msg.close;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import ch.usi.dag.dislreserver.msg.analyze.AnalysisResolver;
import ch.usi.dag.dislreserver.remoteanalysis.RemoteAnalysis;
import ch.usi.dag.dislreserver.reqdispatch.RequestDispatcher;
import ch.usi.dag.dislreserver.reqdispatch.RequestHandler;


public final class CloseHandler implements RequestHandler {

    @Override
    public void handle (
        final DataInputStream is, final DataOutputStream os, final boolean debug
    ) {
        // call exit on all request handlers - waits for all uncompleted actions
        for (final RequestHandler handler : RequestDispatcher.getAllHandlers ()) {
            handler.exit ();
        }

        // invoke atExit on all analyses
        for (final RemoteAnalysis analysis : AnalysisResolver.getAllAnalyses ()) {
            try {
                analysis.atExit ();

            } catch (final Exception e) {
                // report error during analysis invocation
                System.err.format (
                    "DiSL-RE: exception in analysis %s.atExit(): %s\n",
                    analysis.getClass ().getName (), e.getMessage ()
                );

                final Throwable cause = e.getCause ();
                if (cause != null) {
                    cause.printStackTrace (System.err);
                }
            }
        }
    }

    @Override
    public void exit () {
        // empty
    }

}
