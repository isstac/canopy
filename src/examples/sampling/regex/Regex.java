package sampling.regex;

import gov.nasa.jpf.symbc.Debug;
import gov.nasa.jpf.vm.Verify;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

	private static void test(String regex, String txt) {
//		try {
//			CSVParser csvParser = CSVParser.parse(s, CSVFormat.DEFAULT);
//			for (CSVRecord r : csvParser) {
//				//System.out.println(r);
//			}
//			//System.out.println("Done");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//			Debug.printPC("Violation PC");
//		}
    Pattern p = Pattern.compile(regex);
    Matcher matcher = p.matcher(txt);

    boolean matches = matcher.matches();
    System.out.println("Matches: " + matches);
	}
	
	/*
	 * in0[10]in1[10]in2[-1]in3[65534]in4[8]
	 */
	private static String buildStr() {
		int SIZE=5;
		char[] in = new char[SIZE];
		in[0] = 10; System.out.println("in[0] = " + (char)10);
		in[1] = 10; System.out.println("in[1] = " + (char)10);
		in[2] = 100; System.out.println("in[2] = " + (char)100);
		in[3] = '\ufffe'; System.out.println("in[3] = " + '\ufffe');
		in[4] = 8; System.out.println("in[4] = " + (char)8);
		String in_str = new String(in);
		System.out.println("Input str is " + in_str);
		return in_str;
	}
	
	private static void printOutput(String pre, char[] in) {
		Debug.getSolvedPC();
		String str = pre;
		for (int i = 0; i < in.length; i++) 
			str += Debug.getSymbolicIntegerValue(in[i]);
		System.out.println(str);	
	}
	
	public static void main(String[] args) {
		
		//if (Character.isWhitespace(14)) {
		//	System.out.println("Space is a whitespace");
		//}
		
		// TODO Auto-generated method stub
		//test("1,2,3");
		//String s = buildStr();
		//test(s);
		int REGEX_SIZE=6;
    int TXT_SIZE=12;

    String in_str1 = buildInput(REGEX_SIZE, 0); //"(a|a)*"; //
    String in_str2 = buildInput(TXT_SIZE, REGEX_SIZE);

    //String in_str = buildStr();
		
		
		//String in_str = buildFromPartialInput(SIZE);
		//System.out.println("INSTR = " + in_str);
    test(in_str1, in_str2);
    printOutput("", in_str2.toCharArray());
//		try {
//			test(in_str1, in_str2);
//			//printOutput("DONE:",in);
//			ACCEPTED("GOOD");
//		} catch (Exception e) {
//			REJECTED();
//			//printOutput("EXCEPTION:"+e.getMessage(),in);
//		}
	}

	private static void REJECTED() {
		// TODO Auto-generated method stub
		System.out.println("REJECTED");
	}

	private static String buildInput(int size, int start) {
		char[] in = new char[size];
		for (int i=start;i<size;i++) {
			in[i]=Debug.makeSymbolicChar("in"+i);		
			//Debug.assume(in[i] != '\ufffe');
		//	Debug.assume(in[i] != 34);
			//Debug.assume(in[i] != 35);
		//	Debug.assume(in[i] != 13);
		}
		return new String(in);
	}

	/*
	 * Size 4 grammar
	 * R45 = char--1 R17 | char-44 R17 | char-10 R17 |
	 * R17 = char-44 R7 | char--1 R7 | char-10 R7 |
	 * R7 = char-44 R3 | char-10 R3 | char--1 R3 |
	 * R3 = char--1  | char-44  | char-10  |
	 */
	private static String buildFromPartialInput(int size) {
		char[] in = new char[size];
		// R45 = char--1 R17 | char-44 R17 | char-10 R17
		
		in[0] = Debug.makeSymbolicChar("in"+0);
		Debug.assume(in[0] != '\ufffe');
		Debug.assume(in[0] != 13);
		Debug.assume(in[0] != 34);
		
		for (int i = 1; i < size; i++) {
			int choice = Verify.getInt(0,2);
			//System.out.println(i + " CHOICE = " + choice);
			in[i] = Debug.makeSymbolicChar("in"+i);
			switch (choice){
				case 0: Debug.assume(in[i] != 44 && in[i] != 10);
						Debug.assume(in[i] != '\ufffe');
						Debug.assume(in[i] != 13);
						Debug.assume(in[i] != 34);
						break;
				case 1: Debug.assume(in[i] == 44);
						//in[i] = (char)44; 
						break;
				case 2: Debug.assume(in[i] == 10);
						//in[i] = (char)10; 
						break;
			}
		}
		return new String(in);	
	}
	
	private static void ACCEPTED(String str) {
		// TODO Auto-generated method stub
		System.out.println("GOOD");
	}

}
