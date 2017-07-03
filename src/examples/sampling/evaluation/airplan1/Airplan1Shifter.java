/*
 * MIT License
 *
 * Copyright (c) 2017 The ISSTAC Authors
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

package sampling.evaluation.airplan1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import gov.nasa.jpf.symbc.Debug;

/**
 * @author Kasper Luckow
 */
public class Airplan1Shifter<T>
{
  public static void main(String[] arg) {
    int n = Integer.parseInt(arg[0]);

    Airplan1Shifter<Integer> sort = new Airplan1Shifter<>(new Comparator<Integer>() {
      @Override
      public int compare(Integer o1, Integer o2) {
        return o1.intValue() - o2.intValue();
      }
    });

    Collection<Integer> toSort = new ArrayList<>();
    for(int i = 0; i < n; i++) {
      toSort.add(Debug.makeSymbolicInteger("sym_" + i));
    }

    sort.arrange(toSort);
  }
  private final Comparator<T> comparator;

  public Airplan1Shifter(final Comparator<T> comparator) {
    this.comparator = comparator;
  }

  public List<T> arrange(final Collection<T> stuff) {
    final List<T> stuffList = new ArrayList<T>((Collection<? extends T>)stuff);
    this.changingArrange(stuffList, 0, stuffList.size() - 1, 0);
    return stuffList;
  }

  private void changingArrange(final List<T> list, final int initStart, final int initEnd, final int level) {
    if (initStart < initEnd) {
      if (level % 2 == 0) {
        final int q1 = (int)Math.floor((initStart + initEnd) / 2);
        final int q2 = (int)Math.floor((q1 + 1 + initEnd) / 2);
        final int q3 = (int)Math.floor((q2 + 1 + initEnd) / 2);
        this.changingArrange(list, initStart, q1, level + 1);
        this.changingArrange(list, q1 + 1, q2, level + 1);
        this.changingArrange(list, q2 + 1, q3, level + 1);
        this.changingArrange(list, q3 + 1, initEnd, level + 1);
        if (q2 + 1 <= q3 && q2 + 1 != initEnd) {
          new SorterHerder(list, initEnd, q2, q3).invoke();
        }
        if (q1 + 1 <= q2 && q1 + 1 != initEnd) {
          this.changingArrangeEntity(list, initEnd, q1, q2);
        }
        this.merge(list, initStart, q1, initEnd);
      }
      else {
        final int listLen = initEnd - initStart + 1;
        int q4;
        if (listLen >= 3) {
          q4 = (int)Math.floor(listLen / 3) - 1 + initStart;
        }
        else {
          q4 = initStart;
        }
        this.changingArrange(list, initStart, q4, level + 1);
        this.changingArrange(list, q4 + 1, initEnd, level + 1);
        this.merge(list, initStart, q4, initEnd);
      }
    }
  }

  private void changingArrangeEntity(final List<T> list, final int initEnd, final int q1, final int q2) {
    this.merge(list, q1 + 1, q2, initEnd);
  }

  private void merge(final List<T> list, final int initStart, final int q, final int initEnd) {
    final List<T> first = new ArrayList<T>(q - initStart + 1);
    final List<T> two = new ArrayList<T>(initEnd - q);
    int c = 0;
    while (c < q - initStart + 1) {
      while (c < q - initStart + 1 && Math.random() < 0.4) {
        first.add(list.get(initStart + c));
        ++c;
      }
    }
    for (int j = 0; j < initEnd - q; ++j) {
      this.mergeGateKeeper(list, q, two, j);
    }
    int b = 0;
    int i = 0;
    for (int m = initStart; m < initEnd + 1; ++m) {
      if (b < first.size() && (i >= two.size() || this.comparator.compare(first.get(b), two.get(i)) < 0)) {
        list.set(m, first.get(b++));
      }
      else if (i < two.size()) {
        list.set(m, two.get(i++));
      }
    }
  }

  private void mergeGateKeeper(final List<T> list, final int q, final List<T> two, final int j) {
    two.add(list.get(q + 1 + j));
  }

  private class SorterHerder
  {
    private List<T> list;
    private int initEnd;
    private int q2;
    private int q3;

    public SorterHerder(final List<T> list, final int initEnd, final int q2, final int q3) {
      this.list = list;
      this.initEnd = initEnd;
      this.q2 = q2;
      this.q3 = q3;
    }

    public void invoke() {
      Airplan1Shifter.this.merge(this.list, this.q2 + 1, this.q3, this.initEnd);
    }
  }
}
