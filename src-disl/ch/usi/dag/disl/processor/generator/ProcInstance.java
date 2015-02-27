package ch.usi.dag.disl.processor.generator;

import java.util.List;

import ch.usi.dag.disl.processorcontext.ArgumentProcessorMode;

public class ProcInstance {
    private final ArgumentProcessorMode __mode;
    private final List <ProcMethodInstance> __methods;

    //

    public ProcInstance (
        final ArgumentProcessorMode mode,
        final List <ProcMethodInstance> methods
    ) {
        __mode = mode;
        __methods = methods;
    }

    //

    public ArgumentProcessorMode getMode () {
        return __mode;
    }


    public List <ProcMethodInstance> getMethods () {
        return __methods;
    }

}
