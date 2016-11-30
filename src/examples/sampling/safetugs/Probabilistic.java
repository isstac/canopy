package sampling.safetugs;

import gov.nasa.jpf.annotation.FilterField;
import gov.nasa.jpf.symbc.Debug;


public class Probabilistic {

	// Probabilistic stuff start
	
		public static void observe(boolean b) {}
		public static int uniform(String name, int low, int high) { 
			return Debug.makeSymbolicInteger(name);
		}
		//static int coins=0;
		
		public static void addp(double ratio) {}
		public static void addpn(double ratio) {}
		
		/*
		public static boolean bernoulli(double ratio) { 
			int v = uniform("coin"+coins++, 1, 10);
			int threshould = (int)(ratio*10.0);
			System.out.println("threshold " + threshould);
			return (v <= threshould) ? true : false; // #(1..5) = #(6..10) so should be <= not < 
		}
		*/
		@FilterField static int coins=0;
		public static boolean bernoulli(double ratio) { 
			int v = uniform("coin"+coins++, 1, 10);
			int threshold = (int)(ratio*10.0);
			//System.out.println("threshold " + threshold);
			
			if (v <= threshold) {
				addp(ratio);
				return true;
			} else {
				addp(1-ratio);
				return false;
			}
		}
	// Probabilistic stuff end	
	
}
