package ch.usi.dag.dislreserver.msg.analyze.mtdispatch;

class ObjectFreeTask {

    protected boolean signalsEnd = false;
    protected long[] objFreeIDs;
    protected long closingEpoch;

    /**
     * Constructed task signals end of the processing
     */
    public ObjectFreeTask() {
        signalsEnd = true;
    }

    public ObjectFreeTask(long[] objFreeIDs, long closingEpoch) {
        super();
        this.objFreeIDs = objFreeIDs;
        this.closingEpoch = closingEpoch;
    }

    public boolean isSignalingEnd() {
        return signalsEnd;
    }

    public long[] getObjFreeIDs() {
        return objFreeIDs;
    }

    public long getClosingEpoch() {
        return closingEpoch;
    }
}
