package sampling.evaluation.ngrams;

import java.util.*;

public class TextScore
{
    private EnumMap<NgramType, Ngram.ScoreStats> ngramScores;
    
    public TextScore() {
        this.ngramScores = new EnumMap<NgramType, Ngram.ScoreStats>(NgramType.class);
    }
    
    public EnumMap<NgramType, Ngram.ScoreStats> getNgramScores() {
        return this.ngramScores;
    }
    
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<NgramType, Ngram.ScoreStats> entry : this.ngramScores.entrySet()) {
            if (entry.getValue() != null) {
                this.toStringHelper(entry, sb);
            }
        }
        return sb.toString();
    }
    
    private void toStringHelper(final Map.Entry<NgramType, Ngram.ScoreStats> entry, final StringBuilder sb) {
        sb.append(String.format("%s: %.5f (min: %.5f total: %.0f found: %.0f)", entry.getKey(), entry.getValue().getScore(), entry.getValue().getMinScore(), entry.getValue().getNgramsTotal(), entry.getValue().getNgramsFound()));
    }
}
