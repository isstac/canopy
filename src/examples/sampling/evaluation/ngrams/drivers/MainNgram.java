package sampling.evaluation.ngrams.drivers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.ngrams.NgramType;

public class MainNgram {

	public static NgramType type = NgramType.TRIGRAM;
	public static int N;
	
	static String scores[];// = {"AA","BA","BB","AB","AC","BC","CC","CA","CB"};
	
	public static void main(final String[] args) throws Exception {
		N=Integer.parseInt(args[0]);

		// make symbolic search text
//		char search[] = new char[N];
//		for (int i=0; i<N; i++) {
//			search[i] = Debug.makeSymbolicChar("c"+i);
//		}
//		final String theString = new String(search);
//		String theString = "testABCtestingABteblaABCwerktdiabtheTHEcat?";
		
		
//		final String theString = "ABC";
		
//		 make symbolic string of the length of the NGRAM
//		char search[] = new char[type.length()];
//		for (int i=0; i<type.length(); i++) {
//			search[i] = Debug.makeSymbolicChar("c"+i);
//		}
//		final String theString = new String(search);
//		
//		final TextMeter textMeter = new TextMeter();
//		textMeter.createTextLanguage("en");
//		final TextLanguage en = textMeter.get("en");
//
//		en.getNgram(type, TestUtils.loadResource(null, "symbolic"), NgramStorageStrategy.TREEMAP, -1);
//		final TextScore textScore = en.score(theString);
//		System.out.println("en-based score for english text: " + textScore);
		
		
		
		
		
//		int score = score("AAB");
//		System.out.println("Score = " + score);
		
		// make symbolic scoring array of length N
		scores = new String[N];
		for (int i=0; i<N; i++) {
			char ngram[] = new char[type.length()];
			for (int j = 0; j < type.length(); j++) {
				ngram[j] = Debug.makeSymbolicChar("c"+i+":"+j);
			}
			scores[i] = new String(ngram);
		}
		
		//make symbolic text to score, for now, single NGRAM
		int TEXT_LEN = type.length();
		char scoreText[] = new char[TEXT_LEN];
		for (int j = 0; j < TEXT_LEN; j++) {
			scoreText[j] = Debug.makeSymbolicChar("score"+":"+j);
		}
		String s = new String(scoreText);
		
		//score
		int res = score(s);
//		System.out.println("Score = " + res);
	}
	
	// Simpler version where ngrams are looked up in an array and distance from the start is the score
	private static int score(String s) {
		int total = 0;
		final int cnt = s.length() - type.length() + 1;
        for (int i = 0; i < cnt; ++i) {
//        	System.out.println("Scoring " + s.substring(i, type.length() + i));
            int score = scoreNgram(s.substring(i, type.length() + i));
//            System.out.println("Score is " + score);
            total += score;
        }
        return total;
	}
	
	private static int scoreNgram(String ngram) {
		for (int i = 0; i < scores.length; i++) {
			if (scores[i].equals(ngram))
				return scores.length-i;
		}
		if (ngram.length() < type.length())
			return 0;
		return -1;
	}
}
