package sampling.evaluation.ngrams.drivers;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.ngrams.sort.DefaultComparator;
import sampling.evaluation.ngrams.sort.Sorter;

public class MainSort {

	  public static void main(final String[] args) {

		  final int STRLEN = 2;
		  
		    // N is hashmap size before put
		    int N=Integer.parseInt(args[0]);
		    ArrayList<String> list = new ArrayList<String>();
		    
		    for(int i=0;i<N;i++) {
//		    	String s = "test";
		    	String s = Debug.makeSymbolicString("in"+i, STRLEN);
		    	list.add(s);
		    }
		    
		    Sorter<String> s = new Sorter<String>(DefaultComparator.STRING);
		    List<String> sorted = s.sort(list);
		    
//		    System.out.print("Sorted list: {");
//		    for(int i=0;i<N;i++) {
//		    	System.out.print(sorted.get(i) + ", ");
//			}
//		    System.out.print("}\n");
	}
}
