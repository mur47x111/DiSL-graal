package ch.usi.dag.disl.guard;

import ch.usi.dag.disl.exception.GuardException;
import ch.usi.dag.disl.exception.GuardRuntimeException;
import ch.usi.dag.disl.guardcontext.GuardContext;
import ch.usi.dag.disl.processorcontext.ArgumentContext;
import ch.usi.dag.disl.snippet.Shadow;

//used for guard invocation - reduced visibility
class GuardContextImpl implements GuardContext {

    private Shadow shadow;
    private ArgumentContext ac;

    public GuardContextImpl(Shadow shadow, ArgumentContext ac) {
        super();
        this.shadow = shadow;
        this.ac = ac;
    }

    public boolean invoke(Class<?> guardClass) {

        if(guardClass == null) {
            throw new NullPointerException("Guard class cannot be null");
        }

        try {
            return GuardHelper.invokeGuard(guardClass, shadow, ac);
        } catch (GuardException e) {
            // re-throw exception as runtime exception
            throw new GuardRuntimeException(e);
        }
    }

}
