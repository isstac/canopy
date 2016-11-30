package sampling;

import gov.nasa.jpf.symbc.Debug;
import gov.nasa.jpf.vm.Verify;

/**
 * @author Kasper Luckow
 */
public class Simple2 {


  public static void main(String[] args) {
    test(2);
  }

  public static void test(int a) {
    if (a > 0)
      if (a < 10) { // thin
        /*if (a <= 3) {
	    		while (a >= 1) {
	    			System.out.println("1 ---> " + a);
	    			a--;
	    		}
	    	} else if (a <= 5) {
	    		while (a >= 1) {
	    			System.out.println("2 ---> " + a);
	    			a--;
	    		}
	    	} else if (a <= 7) {
	    		while (a >= 1) {
	    			System.out.println("3 ---> " + a);
	    			a--;
	    		}
	    	} else {*/
        if (a >= 2) {
          if (a >= 1)
            System.out.println("1 ");
          System.out.println(" 2");
          // Debug.printPC(" in loop ");
        } else {
          System.out.println("3");
        }
        //}
      } else {   // think
        if (a <= 50) {
          System.out.println("4");
        } else {
          System.out.println("5");
        }
      }
    System.out.println("6");
  }
}
