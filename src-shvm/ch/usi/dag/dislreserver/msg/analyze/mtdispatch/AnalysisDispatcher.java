package ch.usi.dag.dislreserver.msg.analyze.mtdispatch;

import java.util.List;

import ch.usi.dag.dislreserver.DiSLREServerFatalException;
import ch.usi.dag.dislreserver.msg.analyze.AnalysisInvocation;

// Each thread has dedicated queue where new tasks are submitted.
public class AnalysisDispatcher {

    // Epoch is used during object free event sending. Each task is assigned
    // with current epoch number. When free event arrives it increments the
    // epoch and adds task for object free thread. The thread has to wait until
    // all threads processed all tasks from this epoch and then can start
    // with
    protected long globalEpoch = 0;

    protected final ATEManager ateManager = new ATEManager();

    protected final ObjectFreeTaskExecutor oftExec =
            new ObjectFreeTaskExecutor(ateManager);

    public AnalysisDispatcher() {
        super();

        // start object free thread
        oftExec.start();
    }

    public void addTask(long orderingID,
            List<AnalysisInvocation> invocations) {

        // add task to the executor

        // create new task to add
        AnalysisTask at = new AnalysisTask(invocations, globalEpoch);

        // add task
        ateManager.getExecutor(orderingID).addTask(at);
    }


    public void objectsFreedEvent(long[] objFreeIDs) {

        // create object free task
        ObjectFreeTask oft = new ObjectFreeTask(objFreeIDs, globalEpoch);

        // send event
        oftExec.addTask(oft);

        // start new epoch
        ++globalEpoch;

        // notify all analysis executors about epoch change
        // this is important for AnalysisTaskExecutors that do not have tasks
        // from new epoch
        ateManager.globalEpochChange(globalEpoch);
    }

    // called by analysis handler when thread ended on the application vm
    public void threadEndedEvent(long threadId) {

        // create end of processing analysis task
        AnalysisTask at = new AnalysisTask();

        // send end of processing
        ateManager.getExecutor(threadId).addTask(at);

        // update ate manager
        ateManager.executorIsEnding(threadId);
    }

    public void exit() {

        // create end of processing analysis task
        AnalysisTask at = new AnalysisTask();

        // broadcast end of processing to all
        for(AnalysisTaskExecutor ate : ateManager.getAllLiveExecutors()) {
            ate.addTask(at);
        }

        // NOTE: we are not updating the executor is ending state because
        // whole shadow vm is ending

        // wait for analysis threads
        ateManager.waitForAllToEnd();

        // wait for free thread
        try {

            // signal end
            oftExec.addTask(new ObjectFreeTask());

            // wait for end
            oftExec.join();

        } catch (InterruptedException e) {
            throw new DiSLREServerFatalException(
                    "Interrupted while waiting on obj free thread to finish",
                    e);
        }
    }
}
