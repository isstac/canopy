package sampling.evaluation.ngrams.sort;

import java.util.*;

public class Sorter<T>
{
    private final Comparator<T> comparator;
    
    public Sorter(final Comparator<T> comparator) {
        this.comparator = comparator;
    }
    
    public List<T> sort(final Collection<T> stuff) {
        final List<T> stuffList = new ArrayList<T>((Collection<? extends T>)stuff);
        Collections.sort(stuffList, this.comparator);
        return stuffList;
    }
}
