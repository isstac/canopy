package sampling.evaluation.lawdb;

import gov.nasa.jpf.symbc.Debug;

public class Driver {

	public static void main(String[] args){
		BTree tree = new BTree(10);
		CheckRestrictedID checker = new CheckRestrictedID();
	
		// create two concrete unrestricted ids
		int id1 = 64, id2 = 85;
		tree.add(id1, null, false);
		tree.add(id2, null, false);
		
		// create one symbolic restricted id
		int h = Debug.makeSymbolicInteger("h");
		Debug.assume(h!=id1 && h!=id2);
		tree.add(h, null, false);
		checker.add(h);
		
		
		UDPServerHandler handler = new UDPServerHandler(tree,checker);
		int key = Debug.makeSymbolicInteger("key");
		handler.channelRead0(8,key,50,100);
		int noise = Debug.makeSymbolicInteger("noise");

		if(noise > 50){
			// do something to waste cycle
			int count = 0;
			for(int i = 0; i < 100; ++i){
				++count;
			}
		}
	}
}
