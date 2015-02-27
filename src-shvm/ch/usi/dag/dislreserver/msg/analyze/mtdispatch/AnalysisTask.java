package ch.usi.dag.dislreserver.msg.analyze.mtdispatch;

import java.util.List;

import ch.usi.dag.dislreserver.msg.analyze.AnalysisInvocation;

/**
 *  Holds unprocessed task data for some thread
 */
class AnalysisTask {

    protected boolean signalsEnd = false;
    protected List<AnalysisInvocation> invocations;
    protected long epoch;

    /**
     * Constructed task signals end of the processing
     */
    public AnalysisTask() {
        signalsEnd = true;
    }

    public AnalysisTask(List<AnalysisInvocation> invocations, long epoch) {
        super();
        this.invocations = invocations;
        this.epoch = epoch;
    }

    public boolean isSignalingEnd() {
        return signalsEnd;
    }

    public List<AnalysisInvocation> getInvocations() {
        return invocations;
    }

    public long getEpoch() {
        return epoch;
    }
}