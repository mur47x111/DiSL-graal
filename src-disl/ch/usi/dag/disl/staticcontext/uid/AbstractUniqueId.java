package ch.usi.dag.disl.staticcontext.uid;

import ch.usi.dag.disl.staticcontext.AbstractStaticContext;


public abstract class AbstractUniqueId extends AbstractStaticContext {

	private IdHolder idHolder;


	// constructor for static context
	protected AbstractUniqueId () {
		idHolder = null;
	}


	// constructor for singleton
	protected AbstractUniqueId (
		AbstractIdCalculator idCalc, String outputFileName
	) {
		idHolder = new IdHolder(idCalc, outputFileName);
	}


	public int get() {
		return getSingleton().idHolder.getID(idFor());
	}


	// String value for which the id will be computed
	protected abstract String idFor ();


	// Singleton that holds the data
	protected abstract AbstractUniqueId getSingleton ();

}
