package ch.usi.dag.dislreserver.msg.analyze.mtdispatch;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisResolver;
import ch.usi.dag.dislreserver.remoteanalysis.RemoteAnalysis;
import ch.usi.dag.dislreserver.shadow.ShadowObject;
import ch.usi.dag.dislreserver.shadow.ShadowObjectTable;

class ObjectFreeTaskExecutor extends Thread {

    protected final ATEManager ateManager;

    protected final BlockingQueue<ObjectFreeTask> taskQueue =
            new LinkedBlockingQueue<ObjectFreeTask>();

    public ObjectFreeTaskExecutor(ATEManager ateManager) {
        super();
        this.ateManager = ateManager;
    }

    public void addTask(ObjectFreeTask oft) {
        taskQueue.add(oft);
    }

    private void invokeObjectFreeAnalysisHandlers(long objectFreeID) {

        // TODO free events should be sent to analysis that sees the shadow object

        // retrieve shadow object
        ShadowObject obj = ShadowObjectTable.get(objectFreeID);

        // get all analysis objects
        Set<RemoteAnalysis> raSet = AnalysisResolver.getAllAnalyses();

        // invoke object free
        for (RemoteAnalysis ra : raSet) {
            try {
                ra.objectFree(obj);

            } catch (final Exception e) {
                // report error during analysis invocation
                System.err.format (
                    "DiSL-RE: exception in analysis %s.objectFree(): %s\n",
                    ra.getClass ().getName (), e.getMessage ()
                );

                final Throwable cause = e.getCause ();
                if (cause != null) {
                    cause.printStackTrace (System.err);
                }
            }
        }

        // release shadow object
        ShadowObjectTable.freeShadowObject(obj);
    }

    public void run() {

        try {

            ObjectFreeTask oft = taskQueue.take();

            // main working loop
            while(! oft.isSignalingEnd()) {

                // wait for all analysis executors to finish the closing epoch
                ateManager.waitForAllToProcessEpoch(oft.getClosingEpoch());

                // invoke object free analysis handler for each free object
                for(long objectFreeID : oft.getObjFreeIDs()) {
                    invokeObjectFreeAnalysisHandlers(objectFreeID);
                }

                // get task to process
                oft = taskQueue.take();
            }

        } catch (InterruptedException e) {
            throw new DiSLREServerFatalException(
                    "Object free thread interupted while waiting on task", e);
        }
    }
}
