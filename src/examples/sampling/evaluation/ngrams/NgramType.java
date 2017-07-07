package sampling.evaluation.ngrams;

public enum NgramType
{
    UNIGRAM(1), 
    BIGRAM(2), 
    TRIGRAM(3), 
    QUADGRAM(4), 
    QUINTGRAM(5);
    
    private int length;
    
    private NgramType(final int length) {
        this.length = length;
    }
    
    public int length() {
        return this.length;
    }
}
