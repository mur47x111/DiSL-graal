package ch.usi.dag.disl.staticcontext.uid;

import ch.usi.dag.disl.staticcontext.StaticContext;


/**
 * Provides a {@link StaticContext} implementation that can be used to obtain a
 * unique method identifier. This class provides random method identifiers.
 */
public class RandomMethodUid extends AbstractMethodUid {

    // constructor for static context
    public RandomMethodUid () {
        super ();
    }


    // constructor for singleton
    private RandomMethodUid (
        final AbstractIdCalculator idCalc, final String outputFileName
    ) {
        super (idCalc, outputFileName);
    }

    //

    @Override
    protected AbstractUniqueId _getInstance () {
        return new RandomMethodUid (new RandomId (), "methodid.txt");
    }
}
