package ch.usi.dag.disl.guard;

import ch.usi.dag.disl.processor.generator.ProcMethodInstance;
import ch.usi.dag.disl.processorcontext.ArgumentContext;

// used for guard invocation - reduced visibility
class ArgumentContextImpl implements ArgumentContext {

    private final ProcMethodInstance __pmi;


    public ArgumentContextImpl (final ProcMethodInstance pmi) {
        __pmi = pmi;
    }

    @Override
    public int getPosition() {
        return __pmi.getArgIndex ();
    }

    @Override
    public String getTypeDescriptor() {
        return __pmi.getArgType ().getDescriptor ();
    }

    @Override
    public int getTotalCount() {
        return __pmi.getArgsCount ();
    }

}
