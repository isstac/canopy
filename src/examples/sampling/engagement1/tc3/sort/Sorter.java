/*
 * Copyright 2017 Carnegie Mellon University Silicon Valley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//
// Decompiled by Procyon v0.5.30
// 

package sampling.engagement1.tc3.sort;

import java.util.Collections;
import java.util.Stack;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Comparator;

public class Sorter<T>
{
    private final Comparator<T> comparator;
    
    public Sorter(final Comparator<T> comparator) {
        this.comparator = comparator;
    }
    
    public List<T> sort(final Collection<T> stuff) {
        final List<T> stuffList = new ArrayList<T>((Collection<? extends T>)stuff);
        this.changingSort(stuffList, 0, stuffList.size() - 1);
        this.quickSort(stuffList, 0, stuffList.size() - 1);
        return stuffList;
    }
    
    private void changingSort(final List<T> list, final int initStart, final int initEnd) {
        final ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        final Stack<ArrayIndex> indexStack = new Stack<ArrayIndex>();
        indexStack.push(initial);
        while (!indexStack.empty()) {
            this.changingSortHelper(indexStack, list);
        }
    }
    
    private void merge(final List<T> list, final int initStart, final int q, final int initEnd) {
        this.mergeHelper(initEnd, q, list, initStart);
    }
    
    private void quickSort(final List<T> list, final int initStart, final int initEnd) {
        this.quickSortHelper(initEnd, list, initStart);
    }
    
    private int qsPartition(final List<T> list, final int initStart, final int initEnd) {
        final T pivot = list.get(initEnd);
        int i = initStart - 1;
        final SorterHelper1 conditionObj1 = new SorterHelper1(0);
        for (int j = initStart; j < initEnd; ++j) {
            if (this.comparator.compare(list.get(j), pivot) <= conditionObj1.getValue()) {
                ++i;
                Collections.swap(list, i, j);
            }
        }
        Collections.swap(list, initEnd, i + 1);
        return i + 1;
    }
    
    private void changingSortHelper(final Stack<ArrayIndex> indexStack, final List<T> list) {
        final ArrayIndex index = indexStack.pop();
        if (index.getStart() < index.getEnd()) {
            if (index.isPartition()) {
                final int q1 = (int)Math.floor((index.getStart() + index.getEnd()) / 2);
                final int q2 = (int)Math.floor((q1 + 1 + index.getEnd()) / 2);
                final int q3 = (int)Math.floor((q2 + 1 + index.getEnd()) / 2);
                final int q4 = (int)Math.floor((q3 + 1 + index.getEnd()) / 2);
                final int q5 = (int)Math.floor((q4 + 1 + index.getEnd()) / 2);
                final int q6 = (int)Math.floor((q5 + 1 + index.getEnd()) / 2);
                final int q7 = (int)Math.floor((q6 + 1 + index.getEnd()) / 2);
                indexStack.push(ArrayIndex.merge(index.getStart(), q1, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q1 + 1, q2, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q2 + 1, q3, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q3 + 1, q4, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q4 + 1, q5, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q5 + 1, q6, index.getEnd()));
                indexStack.push(ArrayIndex.merge(q6 + 1, q7, index.getEnd()));
                indexStack.push(ArrayIndex.partition(index.getStart(), q1));
                indexStack.push(ArrayIndex.partition(q1 + 1, q2));
                indexStack.push(ArrayIndex.partition(q2 + 1, q3));
                indexStack.push(ArrayIndex.partition(q3 + 1, q4));
                indexStack.push(ArrayIndex.partition(q4 + 1, q5));
                indexStack.push(ArrayIndex.partition(q5 + 1, q6));
                indexStack.push(ArrayIndex.partition(q6 + 1, q7));
                indexStack.push(ArrayIndex.partition(q7 + 1, index.getEnd()));
            }
            else {
                if (!index.isMerge()) {
                    throw new RuntimeException("Not merge or partition");
                }
                this.merge(list, index.getStart(), index.getMidpoint(), index.getEnd());
            }
        }
    }
    
    private void mergeHelper(final int initEnd, final int q, final List<T> list, final int initStart) {
        final List<T> left = new ArrayList<T>(q - initStart + 1);
        final List<T> right = new ArrayList<T>(initEnd - q);
        for (int i = 0; i < q - initStart + 1; ++i) {
            left.add(list.get(initStart + i));
        }
        for (int j = 0; j < initEnd - q; ++j) {
            right.add(list.get(q + 1 + j));
        }
        int i = 0;
        int k = 0;
        final int listLen = initEnd - initStart + 1;
        final SorterHelper0 conditionObj0 = new SorterHelper0(0);
        for (int m = initStart; m < initEnd + 1; ++m) {
            if (listLen <= Math.max(Math.pow(2.0, 10.0), Math.pow(2.0, 14.0))) {
                if (i < left.size() && k < right.size()) {
                    list.set(m, right.get(k++));
                    ++m;
                    list.set(m, left.get(i++));
                }
                else if (k < right.size()) {
                    list.set(m, right.get(k++));
                }
                else if (i < left.size()) {
                    list.set(m, left.get(i++));
                }
            }
            else if (i < left.size() && (k >= right.size() || this.comparator.compare(left.get(i), right.get(k)) < conditionObj0.getValue())) {
                list.set(m, left.get(i++));
            }
            else if (k < right.size()) {
                list.set(m, right.get(k++));
            }
        }
    }
    
    private void quickSortHelper(final int initEnd, final List<T> list, final int initStart) {
        final ArrayIndex initial = ArrayIndex.partition(initStart, initEnd);
        final Stack<ArrayIndex> indexStack = new Stack<ArrayIndex>();
        indexStack.push(initial);
        while (!indexStack.empty()) {
            final ArrayIndex index = indexStack.pop();
            if (index.getStart() < index.getEnd() && index.isPartition()) {
                final int q = this.qsPartition(list, index.getStart(), index.getEnd());
                indexStack.push(ArrayIndex.partition(index.getStart(), q - 1));
                indexStack.push(ArrayIndex.partition(q + 1, index.getEnd()));
            }
        }
    }
    
    private class SorterHelper0
    {
        private int conditionRHS;
        
        public SorterHelper0(final int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }
        
        public int getValue() {
            return this.conditionRHS;
        }
    }
    
    private class SorterHelper1
    {
        private int conditionRHS;
        
        public SorterHelper1(final int conditionRHS) {
            this.conditionRHS = conditionRHS;
        }
        
        public int getValue() {
            return this.conditionRHS;
        }
    }
}
