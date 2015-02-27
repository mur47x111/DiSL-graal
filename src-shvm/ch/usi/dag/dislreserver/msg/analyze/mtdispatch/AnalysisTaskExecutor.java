package ch.usi.dag.dislreserver.msg.analyze.mtdispatch;

import java.util.LinkedList;
import java.util.Queue;

class AnalysisTaskExecutor {

    // The whole locking fun here is for safe object free processing.
    // If object free arrives (message with free object), it is necessary to
    // first process all events that arrived before this message.

    // So when object free arrives, input thread increments global epoch and
    // "orders" obj free thread to wait using waitForEpochProcessing for all
    // executors to complete processing of events that arrived before (have
    // lower epochs).
    // Then, obj free thread can safely process all object free events from that
    // epoch.

    // getTask() is the main driving method here which either synchronizes
    // counter with global epoch if no task is available (meaning there is no
    // work from lower epochs) or sets executorEpoch to processed event epoch

    // NOTE: no epoch should have this value
    private static final long           THREAD_SHUTDOWN = -1;

    final protected ATEManager          ateManager;

    protected final AnalysisThread      executingThread;

    // !! RULES !!
    // Lock on "this" is protecting globalEpoch, executorEpoch, and taskQueue.
    // All methods working with the values should be synchronized.
    // Every change to any of the variable (queue) should trigger notifyAll().
    // If we follow the rules, it should simply work :)

    // Note that we could use two locking objects.
    // One for announcing executorEpoch changes and one for announcing
    // globalEpoch and taskQueue changes, but it would require more
    // sophisticated locking also.

    protected long                      globalEpoch     = 0;

    protected long                      executorEpoch   = 0;
    protected final Queue<AnalysisTask> taskQueue;

    public AnalysisTaskExecutor(ATEManager ateManager) {
        super();
        this.ateManager = ateManager;
        this.taskQueue = new LinkedList<AnalysisTask>();
        this.executingThread = new AnalysisThread(this);
    }

    public synchronized void addTask(AnalysisTask at) {

        taskQueue.add(at);
        // changed taskQueue -> according to the rules notifyAll
        this.notifyAll();

        // start thread if it is not started
        // we cannot start the thread in the constructor because it has
        // pointer to "this"
        // we could have some init function but this is exposing better API
        if (!executingThread.isAlive()) {
            executingThread.start();
        }
    }

    public synchronized AnalysisTask getTask() throws InterruptedException {

        // executor thread is driving whole executor from this method
        // - the state of the executor epoch is updated here
        // - tasks are requested here
        // - executor is finalized from here

        // note that at the beginning, the new task is added right away
        // and executorEpoch will be updated to some meaningful value

        AnalysisTask atToProcess = taskQueue.poll();

        // waiting for a task
        while (atToProcess == null) {

            // ** no task - epoch updating only **
            // update executorEpoch, notifyAll and wait
            executorEpoch = globalEpoch;
            // changed executorEpoch -> according to the rules notifyAll
            this.notifyAll();

            // wait for new task or globalEpoch update
            this.wait();

            atToProcess = taskQueue.poll();
        }

        // ** executor end **

        // set proper executorEpoch, notifyAll, notify ATEManager, and
        // forward end task
        if (atToProcess.isSignalingEnd()) {

            executorEpoch = THREAD_SHUTDOWN;
            // changed executorEpoch -> according to the rules notifyAll
            this.notifyAll();
            ateManager.executorEndConcurrentCallback(this);
            return atToProcess;
        }

        // ** normal task **

        executorEpoch = atToProcess.epoch;
        // changed executorEpoch -> according to the rules notifyAll
        this.notifyAll();
        return atToProcess;
    }

    // works with executorEpoch -> synchronized
    public synchronized void globalEpochChanged(long newEpoch) {

        globalEpoch = newEpoch;

        // changed globalEpoch -> according to the rules notifyAll
        this.notifyAll();
    }

    // works with executorEpoch -> synchronized
    public synchronized void waitForEpochProcessing(long epochToProcess)
            throws InterruptedException {

        while (true) {

            // epoch was reached or executor thread is shutting down
            if (executorEpoch > epochToProcess
                    || executorEpoch == THREAD_SHUTDOWN) {

                return;
            }

            // wait for executorEpoch change
            this.wait();
        }
    }

    // await for executor to finish all jobs
    public void awaitTermination() throws InterruptedException {
        executingThread.join();
    }
}
