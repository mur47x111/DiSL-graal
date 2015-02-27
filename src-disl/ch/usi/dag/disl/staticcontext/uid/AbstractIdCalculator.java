package ch.usi.dag.disl.staticcontext.uid;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractIdCalculator {

	// we cannot (don't want to) use IdHolder.strToId filed because accessing
	// the id there is really slow
	Set<Integer> alreadyReturnedIds = new HashSet<Integer>();;

	protected int getId() {
		
		// compute id
		int id = getIdImpl();
		
		// store id
		alreadyReturnedIds.add(id);
		
		// return id
		return id;
	}
	
	protected abstract int getIdImpl();
}
