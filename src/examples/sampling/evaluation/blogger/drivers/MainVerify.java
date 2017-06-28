package sampling.evaluation.blogger.drivers;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.blogger.fi.iki.elonen.URIVerifier;

public class MainVerify {
      
    public static int N;
    
    public static void main(final String[] args) {
    	
    	N = Integer.parseInt(args[0]);
    	
    	// check complexity of URIVerifier.verify()
       	String input = Debug.makeSymbolicString("in",N);
       	URIVerifier v = new URIVerifier();
       	v.verify(input);
    }
}
