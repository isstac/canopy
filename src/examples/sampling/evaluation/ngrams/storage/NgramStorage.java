package sampling.evaluation.ngrams.storage;

import gov.nasa.jpf.symbc.Debug;
import sampling.evaluation.ngrams.NgramType;
import sampling.evaluation.ngrams.drivers.MainNgram;

import java.io.*;
import java.util.*;

public abstract class NgramStorage implements Iterable<Map.Entry<String, Float>>
{
    protected static int DEFAULT_SIZE_HINT;
    private NgramType ngramType;
    private long count;
    protected AbstractMap<String, Float> storage;
    
    public abstract NgramStorageStrategy getStorageStrategy();
    
    protected NgramStorage(final NgramType ngramType) {
        this.count = 0L;
        this.ngramType = ngramType;
    }
    
    public long load(final InputStream inputStream) throws LineFormatException, IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        this.count = 0L;
        this.storage.clear();
        final String lineRegex = String.format("^[A-Z\u0410-\u042f\u0401]{%d}\\s\\d+$", this.getNgramType().length());
        int lineNo = 0;
        final int freqStart = this.getNgramType().length() + 1;
        long totalOccurences = 0L;
        String line;
        while ((line = br.readLine()) != null) {
            ++lineNo;
            // removed these... Make sure 'line' is in the right format!
//            if (!line.matches(lineRegex)) {
//                throw new LineFormatException(String.format("Ngram resource line %d doesn't match pattern \"%s\"", lineNo, lineRegex));
//            }
            final float ngramFrequency = Long.parseLong(line.substring(freqStart, line.length()));
            this.storage.put(line.substring(0, this.getNgramType().length()), ngramFrequency);
            totalOccurences += (long)ngramFrequency;
        }
        this.count = lineNo;
        return totalOccurences;
    }
    
    public long loadSimple() {
        this.storage.clear();
        int NUM_BIGRAMS = MainNgram.N;
        for (int i = 0; i < NUM_BIGRAMS; i++) {
//        	System.out.println("Loop: " + i);
        	char s[] = new char[MainNgram.type.length()];
        	for (int j = 0; j<MainNgram.type.length(); j++) {
        		s[j] = Debug.makeSymbolicChar("gram"+i+":"+j);
        	}
        	String str = new String(s);
        	this.storage.put(str,1.0f);
        }
        return 1L;
    }
    
    public Float get(final String key) {
        return this.storage.get(key);
    }
    
    @Override
    public Iterator<Map.Entry<String, Float>> iterator() {
        return this.storage.entrySet().iterator();
    }
    
    public NgramType getNgramType() {
        return this.ngramType;
    }
    
    public long count() {
        return this.count;
    }
    
    static {
        NgramStorage.DEFAULT_SIZE_HINT = 16;
    }
}
