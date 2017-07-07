package sampling.evaluation.ngrams.storage;

import java.util.TreeMap;

import sampling.evaluation.ngrams.NgramType;

final class TreeMapStorage extends NgramStorage
{
    @Override
    public NgramStorageStrategy getStorageStrategy() {
        return NgramStorageStrategy.TREEMAP;
    }
    
    public TreeMapStorage(final NgramType ngramType) {
        super(ngramType);
        this.storage = new TreeMap<String, Float>();
    }
}
