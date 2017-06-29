package sampling.evaluation.crime;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import gov.nasa.jpf.symbc.Debug;

public class CRIMESimple {

//	static int SIZE = 1;

//	static {
//		// Hack for test: initialize the bounds on the secret from .jpf file
//		String line = null;
//
//		try {
//
//			FileInputStream fstream = new FileInputStream(
//					"src/examples/challenge1/Challenge1Simple.jpf");
//			BufferedReader br = new BufferedReader(new InputStreamReader(
//					fstream));
//
//			line = br.readLine();
//
//			while (line != null) {
//
//				if (line.contains("sidechannel.high_input_size") && line.trim().charAt(0) != '#') {
//					String value = line.split("=")[1].trim();
//					SIZE = Integer.parseInt(value);
//				}
//
//				line = br.readLine();
//			}
//			br.close();
//		} catch (Exception e) {
//			System.out.println("Error is in >>>>>" + line + "<<<<<");
//			e.printStackTrace();
//		}
//	}
//
//	public static void testConcrete() throws Exception {
//
//		byte[] h = new byte[SIZE];
//		byte[][] l = new byte[1][SIZE];
//
//		h[0] = 2;
//		h[1] = 3;
//		h[2] = 5;
//		h[3] = 4;
//
//		l[0][0] = 4;
//		l[0][1] = 2;
//		l[0][2] = 3;
//		l[0][3] = 1;
//
//		System.out.print("Cost ");
//
//		final byte[] all = Arrays.copyOf(h, h.length + l[0].length);
//		System.arraycopy((Object) l[0], 0, (Object) all, h.length, l[0].length);
//		final byte[] compressed = LZ77T.compressOriginal(all);
//
//		System.out.print(compressed.length + " ");
//		if (compressed.length == 7) {
//
//			for (int k = 0; k < all.length; k++) {
//				System.out.println("all concrete " + all[k] + " " + k);
//
//			}
//
//			for (int k = 0; k < compressed.length; k++) {
//
//				System.out.println("compressed concrete " + compressed[k] + " "
//						+ k);
//			}
//
//		}
//
//		System.out.print("\n");
//	}

	public static void test(int size) throws Exception {

		byte[] h = new byte[size];
		byte[] l = new byte[size];

		byte j;
		for (j = 0; j < size; j++) {
			h[j] = Debug.makeSymbolicByte("h" + j);
			// Debug.assume(h[j] >= MIN_HIGH && h[j] <= MAX_HIGH);
		}

		for (j = 0; j < size; j++) {
			l[j] = j;
			// Debug.assume(l[j] >= MIN_HIGH && l[j] <= MAX_HIGH);
		}

		byte[] cookie = { 'c', 'o', 'o', 'k', 'i', 'e' };

		byte[] h_cookie = Arrays.copyOf(h, h.length + cookie.length);
		byte[] l_cookie = Arrays.copyOf(l, l.length + cookie.length);
		System.arraycopy(cookie, 0, h_cookie, h.length, cookie.length);
		System.arraycopy(cookie, 0, l_cookie, l.length, cookie.length);

		final byte[] all = Arrays.copyOf(h_cookie, h_cookie.length
				+ l_cookie.length);
		System.arraycopy(l_cookie, 0, all, h_cookie.length, l_cookie.length);

		final byte[] compressed = LZ77T.compress(all);
		
		int noise = Debug.makeSymbolicInteger("noise");
		int observable = compressed.length;
		if(noise > 6){
			observable = observable + 2;
		}
				
//		Observable.add(observable);

		// System.out.println(">>>>> Cost is " + observable);

	}

	public static void main(String[] args) throws Exception {
	  int n = Integer.parseInt(args[0]);
		test(n);
	}

}
