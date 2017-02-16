/*
 * This is an auto-generated java file for the SPF Worst Case Analyzer driver.
 */
package drivers;

import gov.nasa.jpf.symbc.Debug;
import edu.cyberapex.order.Orderer;




public class Driver_airplan_1 {

   public static void main(final java.lang.String[] args) throws Exception {

   int N=Integer.parseInt(args[0]);
   final int STRLEN = 2;

        
   // initialize
   final Orderer var0 = new Orderer(edu.cyberapex.order.DefaultComparator.STRING);
   final java.util.ArrayList var1 = new java.util.ArrayList();
   for (int i = 0; i < N; i++) {
      String data0 = Debug.makeSymbolicString("var1:0:"+i, STRLEN);
      var1.add(data0);
   }


   // the Test method is called once upon exit
   var0.sort(var1);

  }
}
