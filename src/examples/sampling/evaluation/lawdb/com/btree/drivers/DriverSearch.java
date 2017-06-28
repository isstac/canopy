package sampling.evaluation.lawdb.com.btree.drivers;

import java.io.FileNotFoundException;
import java.io.IOException;

import sampling.evaluation.lawdb.com.btree.BTree;

import gov.nasa.jpf.symbc.Debug;

/**
 * @author Kasper Luckow
 *
 */
public class DriverSearch {

  
  public static void main(String args[]) throws FileNotFoundException, IOException {
    int N = Integer.parseInt(args[0]);
    
    BTree b = new BTree(10);
    
//    CheckRestrictedID check = new CheckRestrictedID();
//    Utils.restore(b, "dataset.dump", check);
    
    for(int i = 0; i < N; i++) {
      int symb = Debug.makeSymbolicInteger("symbc" + i);
      b.addMask(symb, null, false);
    }
    
    b.searchRange(Debug.makeSymbolicInteger("min"), Debug.makeSymbolicInteger("max"));
  }
}
