/*
 * Decompiled with CFR 0_114.
 */
package sampling.evaluation.gabfeed5.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;

public class Sorter<T> {
    private final Comparator<T> comparator;

    public Sorter(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public List<T> sort(Collection<T> stuff) {
        ArrayList<T> stuffList = new ArrayList<T>(stuff);
        this.changingSort(stuffList, 0, stuffList.size() - 1);
        return stuffList;
    }

    private void changingSort(List<T> list, int initStart, int initEnd) {
        this.changingSortHelper(initEnd, list, initStart);
    }

    private void merge(List<T> list, int initStart, int q, int initEnd) {
        int i;
        ArrayList<T> left = new ArrayList<T>(q - initStart + 1);
        ArrayList<T> right = new ArrayList<T>(initEnd - q);
        for (i = 0; i < q - initStart + 1; ++i) {
            left.add(list.get(initStart + i));
        }
        for (int j = 0; j < initEnd - q; ++j) {
            this.mergeHelper(q, list, j, right);
        }
        i = 0;
        int j2 = 0;
        for (int m = initStart; m < initEnd + 1; ++m) {
            if (i < left.size() && (j2 >= right.size() || this.comparator.compare(left.get(i), right.get(j2)) < 0)) {
                list.set(m, left.get(i++));
                continue;
            }
            if (j2 >= right.size()) continue;
            list.set(m, right.get(j2++));
        }
    }

    private void changingSortHelper(int initEnd, List<T> list, int initStart) {
        ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        Stack<ArrayIndex> indexStack = new Stack<ArrayIndex>();
        indexStack.push(initial);
        while (!indexStack.empty()) {
            ArrayIndex index = (ArrayIndex)indexStack.pop();
            if (index.getStart() >= index.getEnd()) continue;
            if (index.isPartition()) {
                int listLen = index.getEnd() - index.getStart() + 1;
                int q = listLen >= 8 ? (int)Math.floor(listLen / 8) - 1 + index.getStart() : index.getStart();
                indexStack.push(ArrayIndex.merge(index.getStart(), q, index.getEnd()));
                indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
                indexStack.push(ArrayIndex.partition(index.getStart(), q));
                continue;
            }
            if (index.isMerge()) {
                this.merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
                continue;
            }
            throw new RuntimeException("Not merge or partition");
        }
    }

    private void mergeHelper(int q, List<T> list, int j, List<T> right) {
        right.add(list.get(q + 1 + j));
    }
}

