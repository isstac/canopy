package sampling.evaluation.gf4.drivers;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.gf4.hashmap.HashMap;

/**
 * @author Kasper Luckow
 *
 */
public class HashMapDriver {
  public static void main(final java.lang.String[] args) {

    //final int HASH_MAP_SIZE = 8;
    
    // N is hashmap size before put
    int N=Integer.parseInt(args[0]);
    final HashMap<Integer,Integer> hashMap = new HashMap<Integer,Integer>();

//    hashMap.mask = true;
    System.out.println("here");
    for(int i=0;i<N;i++) {
      int key = Debug.makeSymbolicInteger("key"+i);
      int value = Debug.makeSymbolicInteger("value"+i);
//      int key = 0;
//      int value = 0;
      hashMap.put(key, value);
    }
    
//    hashMap.mask = false;

      int key = Debug.makeSymbolicInteger("key");
      int value = Debug.makeSymbolicInteger("value");
//  int key = 0;
//  int value = 0;
//  hashMap.put(key, value);
      hashMap.get(key);
}

}
