package sampling.evaluation.ngrams.storage;

import sampling.evaluation.ngrams.NgramType;

public class NgramStorageFactory
{
    public static NgramStorage get(final NgramType ngramType, final NgramStorageStrategy ngramStorageStrategy, final int sizeHint) {
        NgramStorage ngramStorage = null;
        switch (ngramStorageStrategy) {
            case HASHMAP: {
                ngramStorage = new HashMapStorage(ngramType, sizeHint);
                break;
            }
            case TREEMAP: {
                ngramStorage = new TreeMapStorage(ngramType);
                break;
            }
            default: {
                throw new RuntimeException();
            }
        }
        return ngramStorage;
    }
}
