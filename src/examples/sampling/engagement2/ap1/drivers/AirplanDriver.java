/*
 * This is an auto-generated java file for the SPF Worst Case Analyzer driver.
 */
package sampling.engagement2.ap1.drivers;

import gov.nasa.jpf.symbc.Debug;
import sampling.engagement2.ap1.order.DefaultComparator;
import sampling.engagement2.ap1.order.Orderer;


public class AirplanDriver {

   public static void main(final String[] args) throws Exception {

   int N=Integer.parseInt(args[0]);
   final int STRLEN = 2;

        
   // initialize
   final Orderer var0 = new Orderer(DefaultComparator.STRING);
   final java.util.ArrayList var1 = new java.util.ArrayList();
   for (int i = 0; i < N; i++) {
      String data0 = Debug.makeSymbolicString("var1:0:"+i, STRLEN);
      var1.add(data0);
   }


   // the Test method is called once upon exit
   var0.sort(var1);

  }
}
