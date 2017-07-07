package sampling.evaluation.ngrams;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import sampling.evaluation.ngrams.storage.LineFormatException;
import sampling.evaluation.ngrams.storage.NgramStorage;
import sampling.evaluation.ngrams.storage.NgramStorageFactory;
import sampling.evaluation.ngrams.storage.NgramStorageStrategy;

public class Ngram
{
    private NgramType ngramType;
    private NgramStorage ngramStorage;
    private long volume;
    private double floor;
    
    protected Ngram(final NgramType ngramType, final NgramStorageStrategy ngramStorageStrategy, final int sizeHint) {
        this.ngramType = ngramType;
        this.ngramStorage = NgramStorageFactory.get(ngramType, ngramStorageStrategy, sizeHint);
    }
    
    protected Ngram load(final InputStream inputStream) throws IOException, LineFormatException {
        if (this.ngramStorage == null) {
            throw new RuntimeException();
        }
//        this.volume = this.ngramStorage.load(inputStream);
        this.volume = this.ngramStorage.loadSimple();
        if (this.volume != 0L) {
            this.loadHelper();
        }
        this.calculateLogFrequences();
        return this;
    }
    
    protected void calculateLogFrequences() {
        this.calculateLogFrequencesHelper();
    }
    
    public ScoreStats score(final String text) {
        if (text == null || text.length() < this.ngramType.length()) {
            throw new IllegalArgumentException();
        }
        if (this.ngramStorage == null) {
            throw new RuntimeException();
        }
        final ScoreStats scoreStats = new ScoreStats();
        final int cnt = text.length() - this.ngramType.length();
        scoreStats.ngramsTotal = cnt + 1;
        for (int i = 0; i <= cnt; ++i) {
//        	System.out.println("Scoring " + text.substring(i, this.ngramType.length() + i));
            final Float ngramScore = this.ngramStorage.get(text.substring(i, this.ngramType.length() + i));
//            System.out.println("Score is " + ngramScore);
            if (ngramScore != null) {
                scoreStats.ngramsFound = scoreStats.ngramsFound+1; // +1 was missing here. Bug or related to vulnerability?
                scoreStats.score += ngramScore;
            }
        }
        scoreStats.minScore = this.floor * scoreStats.ngramsTotal;
        final NgramHelper0 conditionObj0 = new NgramHelper0(0);
        scoreStats.score = ((scoreStats.ngramsFound == conditionObj0.getValue()) ? scoreStats.minScore : (scoreStats.ngramsTotal * (scoreStats.score / scoreStats.ngramsFound)));
        return scoreStats;
    }
    
    public long count() {
        return this.ngramStorage.count();
    }
    
    public double volume() {
        return this.volume;
    }
    
    public double floor() {
        return this.floor;
    }
    
    private void loadHelper() throws IOException, LineFormatException {
        this.floor = Math.log10(0.01 / this.volume);
    }
    
    private void calculateLogFrequencesHelper() {
        for (final Map.Entry<String, Float> entry : this.ngramStorage) {
            entry.setValue(new Float(Math.log10(entry.getValue() / this.volume)));
        }
    }
    
    public static class ScoreStats
    {
        private double score;
        private double minScore;
        private double ngramsTotal;
        private double ngramsFound;
        
        public double getScore() {
            return this.score;
        }
        
        public double getMinScore() {
            return this.minScore;
        }
        
        public double getNgramsTotal() {
            return this.ngramsTotal;
        }
        
        public double getNgramsFound() {
            return this.ngramsFound;
        }
    }
    
    public class NgramHelper0
    {
        private int conditionRHS;
        
        public NgramHelper0(final int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }
        
        public int getValue() {
            return this.conditionRHS;
        }
    }
}
