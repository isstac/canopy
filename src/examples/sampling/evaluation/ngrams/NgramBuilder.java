package sampling.evaluation.ngrams;

import java.io.*;

import sampling.evaluation.ngrams.storage.LineFormatException;
import sampling.evaluation.ngrams.storage.NgramStorageStrategy;

public class NgramBuilder
{
    public static Ngram build(final NgramType ngramType, final InputStream inputStream, final NgramStorageStrategy ngramStorageStrategy, final int sizeHint) throws IOException, LineFormatException {
        final Ngram ngram = new Ngram(ngramType, ngramStorageStrategy, sizeHint);
        ngram.load(inputStream);
//        ngram.loadSimple();
        return ngram;
    }
}
