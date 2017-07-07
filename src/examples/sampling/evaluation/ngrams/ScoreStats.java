package sampling.evaluation.ngrams;

public class ScoreStats
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
