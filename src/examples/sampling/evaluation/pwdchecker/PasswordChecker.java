package sampling.evaluation.pwdchecker;

import gov.nasa.jpf.symbc.Debug;

/**
 * Application that checks a password against previously used passwords. Users are required to change their
 * password at least every year and are not allowed to re-use their previous N passwords. For security reasons,
 * passwords are not stored in the clear, but are hashed. This class compares an input hash (of length BYTES) against
 * hashes of the previous N passwords.
 * 
 * SHA-256 is 32 bytes.
 * 
 * @author rodykers
 *
 */
public class PasswordChecker {

	// size of the hash
	public static final int BYTES = 32;
	
	// nnumber of password hashes to remember
	public static int N;
	
	private static byte prevPwds[][];
	
	public static void main(final java.lang.String[] args) {
		N=Integer.parseInt(args[0]);
		prevPwds = new byte[N][BYTES];

		// make symbolic hashes
		for (int i=0; i<N; i++) {
			for (int j=0; j<BYTES; j++) {
				prevPwds[i][j] = Debug.makeSymbolicByte("b"+i+":"+j);
			}
		}
		
		// make symbolic input hash
		byte hash[] = new byte[BYTES];
		for (int j=0; j<BYTES; j++) {
			hash[j] = Debug.makeSymbolicByte("hash"+j);
		}
		
		// check input password
		boolean prev = previouslyUsed(hash);
		
//		System.out.println("The password was previously used: " + prev);
	}
	
	/**
	 * Checks if password has been used before.
	 * @param hash Hash value of the password to check.
	 * @return true if the password 
	 */
	static boolean previouslyUsed(byte hash[]) {
		assert (hash.length==BYTES);
		for (int i=0; i<N; i++) {
			if (hashEqual(hash,prevPwds[i]))
				return true;
		}
		return false;
	}
	
	static boolean hashEqual(byte hash1[], byte hash2[]) {
		assert (hash1.length==BYTES && hash2.length==BYTES);
		for (int i = 0; i<BYTES; i++) {
			if (hash1[i]!=hash2[i])
				return false;
		}
		return true;
	}
}
