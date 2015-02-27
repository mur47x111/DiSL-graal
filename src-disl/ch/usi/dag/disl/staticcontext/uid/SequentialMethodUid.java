package ch.usi.dag.disl.staticcontext.uid;

import ch.usi.dag.disl.staticcontext.StaticContext;


/**
 * Provides a {@link StaticContext} implementation that can be used to obtain a
 * unique method identifier. This class provides sequential method identifiers.
 */
public class SequentialMethodUid extends AbstractMethodUid {

    // constructor for static context
    public SequentialMethodUid () {
        super ();
    }


    // constructor for singleton
    private SequentialMethodUid (
        final AbstractIdCalculator idCalc, final String outputFileName
    ) {
        super (idCalc, outputFileName);
    }

    //

    @Override
    protected AbstractUniqueId _getInstance () {
        return new SequentialMethodUid (new SequenceId (), "methodid.txt");
    }
}
