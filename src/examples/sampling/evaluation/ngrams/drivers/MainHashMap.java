package sampling.evaluation.ngrams.drivers;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.ngrams.hashmap.HashMap;

public class MainHashMap {

	  public static void main(final String[] args) {

		    final int HASH_MAP_SIZE = 1;
		    
		    // N is hashmap size before put
		    int N=Integer.parseInt(args[0]);
		    final HashMap<Integer,Integer> hashMap = new HashMap<Integer,Integer>(HASH_MAP_SIZE);
	
		    hashMap.mask = true;
		    
		    for(int i=0;i<N;i++) {
		      int key = Debug.makeSymbolicInteger("key"+i);
		      int value = Debug.makeSymbolicInteger("value"+i);
//		    	int key = 0;
//		    	int value = 0;
		      hashMap.putMask(key, value);
		    }
		    
		    hashMap.mask = false;

		      int key = Debug.makeSymbolicInteger("key");
		      int value = Debug.makeSymbolicInteger("value");
//			int key = 0;
//			int value = 0;
			hashMap.put(key, value);
	}
}
