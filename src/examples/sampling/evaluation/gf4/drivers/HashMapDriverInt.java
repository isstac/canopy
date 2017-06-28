package sampling.evaluation.gf4.drivers;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.gf4.hashmap.HashMap;

/**
 * @author Kasper Luckow
 *
 */
public class HashMapDriverInt {
  public static void main(final java.lang.String[] args) {

    int N=Integer.parseInt(args[0]);
    final HashMap<Integer,Integer> hashMap = new HashMap<>();

    
    for(int i=0;i<N;i++) {
      int key = Debug.makeSymbolicInteger("key"+i);
      int value = Debug.makeSymbolicInteger("value"+i);
      hashMap.put(key, value);
    }

    int key = Debug.makeSymbolicInteger("key");
    int value = Debug.makeSymbolicInteger("value");
    hashMap.get(key);
  }

}
