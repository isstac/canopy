package sampling.evaluation.ngrams.storage;


import sampling.evaluation.ngrams.NgramType;
import sampling.evaluation.ngrams.hashmap.HashMap;

final class HashMapStorage extends NgramStorage
{
    public HashMapStorage(final NgramType ngramType, final int sizeHint) {
        super(ngramType);
        this.storage = new HashMap<String, Float>((sizeHint < HashMapStorage.DEFAULT_SIZE_HINT) ? HashMapStorage.DEFAULT_SIZE_HINT : sizeHint);
    }
    
    @Override
    public NgramStorageStrategy getStorageStrategy() {
        return NgramStorageStrategy.HASHMAP;
    }
}
