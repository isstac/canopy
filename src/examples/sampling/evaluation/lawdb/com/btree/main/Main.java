package sampling.evaluation.lawdb.com.btree.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import sampling.evaluation.lawdb.com.btree.BTree;
import sampling.evaluation.lawdb.com.btree.utils.CheckRestrictedID;
import sampling.evaluation.lawdb.com.btree.utils.Utils;


/**
 * @author Kasper Luckow
 *
 */
public class Main {

  CheckRestrictedID restricted;
  
  public static void main(String[] args) throws FileNotFoundException, IOException {
    new Main().run();
  }
  
  
  private BTree btree;
  
  public void run() throws FileNotFoundException, IOException {
    restore();
    
    
    int minID = 100000;
    int maxID = 40000000;
    int currID = 39876345;
    for(int i = 0; i < 198; i++) {
      btree.add(currID+i, null, false);
    }
    
    long start = System.currentTimeMillis();
    
    final List<Integer> search = btree.toList(minID, maxID);
    
    btree.printOutWholetree(-1);
    
    long end = System.currentTimeMillis();
    
    double elapsedTime = (end-start)/1000.0;
    System.out.println("time taken: " + elapsedTime);
  }
  
  private void restore() throws FileNotFoundException, IOException {
    this.btree = new BTree(10);
    this.restricted = new CheckRestrictedID();
    Utils.restore(btree, "dataset.dump", this.restricted);
    System.out.println("done");
  }
}
