package ch.usi.dag.disl.snippet;

import ch.usi.dag.disl.processor.ArgProcessor;
import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;

public class ProcInvocation {

    private ArgProcessor processor;
    private ArgumentProcessorMode procApplyType;

    public ProcInvocation(ArgProcessor processor, ArgumentProcessorMode procApplyType) {
        super();
        this.processor = processor;
        this.procApplyType = procApplyType;
    }

    public ArgProcessor getProcessor() {
        return processor;
    }

    public ArgumentProcessorMode getProcApplyType() {
        return procApplyType;
    }
}
