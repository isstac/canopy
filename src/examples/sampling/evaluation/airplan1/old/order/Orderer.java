package sampling.evaluation.airplan1.old.order;

import java.util.*;

public class Orderer<T>
{
    private final Comparator<T> comparator;
    
    public Orderer(final Comparator<T> comparator) {
        this.comparator = comparator;
    }
    
    public List<T> sort(final Collection<T> stuff) {
        final List<T> stuffList = new ArrayList<T>((Collection<? extends T>)stuff);
        this.changingSort(stuffList, 0, stuffList.size() - 1, 0);
        return stuffList;
    }
    
    private void changingSort(final List<T> list, final int initStart, final int initEnd, final int level) {
        if (initStart < initEnd) {
            if (level % 2 == 0) {
                this.changingSortGuide(list, initStart, initEnd, level);
            }
            else {
                final int listLen = initEnd - initStart + 1;
                int q;
                if (listLen >= 3) {
                    q = (int)Math.floor(listLen / 3) - 1 + initStart;
                }
                else {
                    q = initStart;
                }
                this.changingSort(list, initStart, q, level + 1);
                this.changingSort(list, q + 1, initEnd, level + 1);
                this.merge(list, initStart, q, initEnd);
            }
        }
    }
    
    private void changingSortGuide(final List<T> list, final int initStart, final int initEnd, final int level) {
        final int q1 = (int)Math.floor((initStart + initEnd) / 2);
        final int q2 = (int)Math.floor((q1 + 1 + initEnd) / 2);
        final int q3 = (int)Math.floor((q2 + 1 + initEnd) / 2);
        this.changingSort(list, initStart, q1, level + 1);
        this.changingSort(list, q1 + 1, q2, level + 1);
        this.changingSort(list, q2 + 1, q3, level + 1);
        this.changingSort(list, q3 + 1, initEnd, level + 1);
        if (q2 + 1 <= q3 && q2 + 1 != initEnd) {
            this.merge(list, q2 + 1, q3, initEnd);
        }
        if (q1 + 1 <= q2 && q1 + 1 != initEnd) {
            this.merge(list, q1 + 1, q2, initEnd);
        }
        this.merge(list, initStart, q1, initEnd);
    }
    
    private void merge(final List<T> list, final int initStart, final int q, final int initEnd) {
        final List<T> left = new ArrayList<T>(q - initStart + 1);
        final List<T> two = new ArrayList<T>(initEnd - q);
        for (int b = 0; b < q - initStart + 1; ++b) {
            this.mergeGuide(list, initStart, left, b);
        }
        for (int j = 0; j < initEnd - q; ++j) {
            this.mergeHome(list, q, two, j);
        }
        int k = 0;
        int i = 0;
        for (int m = initStart; m < initEnd + 1; ++m) {
            if (k < left.size() && (i >= two.size() || this.comparator.compare(left.get(k), two.get(i)) < 0)) {
                list.set(m, left.get(k++));
            }
            else if (i < two.size()) {
                list.set(m, two.get(i++));
            }
        }
    }
    
    private void mergeHome(final List<T> list, final int q, final List<T> two, final int j) {
        new SorterCoordinator(list, q, two, j).invoke();
    }
    
    private void mergeGuide(final List<T> list, final int initStart, final List<T> left, final int c) {
        left.add(list.get(initStart + c));
    }
    
    private class SorterCoordinator
    {
        private List<T> list;
        private int q;
        private List<T> two;
        private int j;
        
        public SorterCoordinator(final List<T> list, final int q, final List<T> two, final int j) {
            this.list = list;
            this.q = q;
            this.two = two;
            this.j = j;
        }
        
        public void invoke() {
            this.two.add(this.list.get(this.q + 1 + this.j));
        }
    }
}
