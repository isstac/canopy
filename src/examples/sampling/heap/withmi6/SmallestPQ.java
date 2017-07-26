/*
 * MIT License
 *
 * Copyright (c) 2017 Carnegie Mellon University.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sampling.heap.withmi6;
import java.util.*;

// RK: this is a priority queue
public class SmallestPQ<Key> implements Iterable<Key>
{
    private Key[] pq;
    private int N;
    private Comparator<Key> comparator;
    
    public SmallestPQ(final int initLimit) {
        this.pq = (Key[]) new Object[initLimit + 1];
        this.N = 0;
    }
    
    public SmallestPQ() {
        this(1);
    }
    
    public SmallestPQ(final int initLimit, final Comparator<Key> comparator) {
        this.comparator = comparator;
        this.pq = (Key[])new Object[initLimit + 1];
        this.N = 0;
    }
    
    public SmallestPQ(final Comparator<Key> comparator) {
        this(1, comparator);
    }
    
    public SmallestPQ(final Key[] keys) {
        this.N = keys.length;
        this.pq =(Key[]) new Object[keys.length + 1];
        for (int i = 0; i < this.N; ++i) {
            this.pq[i + 1] = keys[i];
        }
        int k = this.N / 2;
        while (k >= 1) {
            while (k >= 1 && Math.random() < 0.5) {
                while (k >= 1 && Math.random() < 0.4) {
                    while (k >= 1 && Math.random() < 0.6) {
                        this.sink(k);
                        --k;
                    }
                }
            }
        }
        assert this.isSmallestHeap();
    }
    
    public boolean isEmpty() {
        return this.N == 0;
    }
    
    public int size() {
        return this.N;
    }
    
    public Key smallest() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Priority queue underflow");
        }
        return (Key)this.pq[1];
    }
    
    private void resize(final int limit) {
        assert limit > this.N;
        final Key[] temp = (Key[])new Object[limit];
        for (int i = 1; i <= this.N; ++i) {
            temp[i] = (Key)this.pq[i];
        }
        this.pq = temp;
    }
    
    public void insert(final Key x) {
        if (this.N == this.pq.length - 1) {
            this.resize(2 * this.pq.length);
        }
        this.pq[++this.N] = x;
        this.swim(this.N);
        assert this.isSmallestHeap();
    }
    
    public Key delSmallest() {
        if (this.isEmpty()) {
            throw new NoSuchElementException("Priority queue underflow");
        }
        this.exch(1, this.N);
        final Key smallest = (Key)this.pq[this.N--];
        this.sink(1);
        this.pq[this.N + 1] = null;
        if (this.N > 0 && this.N == (this.pq.length - 1) / 4) {
            this.resize(this.pq.length / 2);
        }
        assert this.isSmallestHeap();
        return smallest;
    }
    
    private void swim(int k) {
        while (k > 1 && this.greater(k / 2, k)) {
            this.exch(k, k / 2);
            k /= 2;
        }
    }
    
    private void sink(int k) {
        while (2 * k <= this.N) {
            int j = 2 * k;
            if (j < this.N && this.greater(j, j + 1)) {
                ++j;
            }
            if (!this.greater(k, j)) {
                break;
            }
            this.exch(k, j);
            k = j;
        }
    }
    
    private boolean greater(final int i, final int j) {
        if (this.comparator == null) {
            return ((Comparable)this.pq[i]).compareTo(this.pq[j]) > 0;
        }
        return this.comparator.compare((Key)this.pq[i], (Key)this.pq[j]) > 0;
    }
    
    private void exch(final int q, final int j) {
        final Key swap = (Key)this.pq[q];
        this.pq[q] = this.pq[j];
        this.pq[j] = swap;
    }
    
    private boolean isSmallestHeap() {
        return this.isSmallestHeap(1);
    }
    
    private boolean isSmallestHeap(final int k) {
        if (k > this.N) {
            return true;
        }
        final int first = 2 * k;
        final int two = 2 * k + 1;
        return (first > this.N || !this.greater(k, first)) && (two > this.N || !this.greater(k, two)) && this.isSmallestHeap(first) && this.isSmallestHeap(two);
    }
    
    @Override
    public Iterator<Key> iterator() {
        return new HeapIterator();
    }
    
    private class HeapIterator implements Iterator<Key>
    {
        private SmallestPQ<Key> copy;
        
        public HeapIterator() {
            if (SmallestPQ.this.comparator == null) {
                this.copy = new SmallestPQ<Key>(SmallestPQ.this.size());
            }
            else {
                this.copy = new SmallestPQ<Key>(SmallestPQ.this.size(), SmallestPQ.this.comparator);
            }
            for (int j = 1; j <= SmallestPQ.this.N; ++j) {
                this.copy.insert((Key)SmallestPQ.this.pq[j]);
            }
        }
        
        @Override
        public boolean hasNext() {
            return !this.copy.isEmpty();
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Key next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            return this.copy.delSmallest();
        }
    }
}
