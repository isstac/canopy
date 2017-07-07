package sampling.evaluation.ngrams.sort;

import java.util.*;

public class DefaultComparator<T extends Comparable<? super T>> implements Comparator<T>
{
    public static final DefaultComparator<String> STRING;
    
    @Override
    public int compare(final T object1, final T object2) {
        return object1.compareTo((T)object2);
    }
    
    static {
        STRING = new DefaultComparator<String>();
    }
}
