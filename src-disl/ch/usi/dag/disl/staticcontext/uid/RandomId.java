package ch.usi.dag.disl.staticcontext.uid;

import java.util.Random;

public class RandomId extends AbstractIdCalculator {

	private Random random;
	
	public RandomId() {
		
		random = new Random();
	}
	
	public RandomId(long seed) {
		
		random = new Random(seed);
	}
	
	protected int getIdImpl() {

		// generate unique random id
		int newId = random.nextInt(Integer.MAX_VALUE); // only positive values
		
		while(alreadyReturnedIds.contains(newId)) {
			newId = random.nextInt(Integer.MAX_VALUE); // only positive values
		}
		
		return newId;
	}

}
