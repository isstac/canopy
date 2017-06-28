package sampling.evaluation.tc3.drivers;

import java.util.ArrayList;
import java.util.List;

import sampling.evaluation.tc3.sort.DefaultComparator;
import sampling.evaluation.tc3.sort.Sorter;
import gov.nasa.jpf.symbc.Debug;

public class MainSort {

	  public static void main(final String[] args) {

		  final int STRLEN = 2;
		  
		    // N is hashmap size before put
		    int N=Integer.parseInt(args[0]);
		    ArrayList<String> list = new ArrayList<String>();
		    
		    for(int i=0;i<N;i++) {
		    	String s = Debug.makeSymbolicString("in"+i, STRLEN);
		    	list.add(s);
		    }
		    
		    Sorter<String> s = new Sorter<String>(DefaultComparator.STRING);
		    List<String> sorted = s.sort(list);

	}
}
