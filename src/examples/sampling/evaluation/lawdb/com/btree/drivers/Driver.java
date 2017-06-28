package sampling.evaluation.lawdb.com.btree.drivers;

import sampling.evaluation.lawdb.com.btree.BTree;

import gov.nasa.jpf.symbc.Debug;

/**
 * @author Kasper Luckow
 *
 */
public class Driver {

  
  public static void main(String args[]) {
    int N = Integer.parseInt(args[0]);
    
    BTree b = new BTree(2);
    for(int i = 0; i < N; i++) {
      int symb = Debug.makeSymbolicInteger("symbc" + i);
      b.addMask(symb, null, false);
    }
    
    b.add(Debug.makeSymbolicInteger("tgt"), null, false);
  }
}
