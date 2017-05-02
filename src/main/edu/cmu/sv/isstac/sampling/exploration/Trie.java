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

/**
 * Compilation:  javac TrieST.java Execution:    java TrieST < words.txt Dependencies: StdIn.java
 * Data files:   http://algs4.cs.princeton.edu/52trie/shellsST.txt
 *
 * A string symbol table for extended ASCII strings, implemented using a 256-way trie.
 *
 * % java TrieST < shellsST.txt by 4 sea 6 sells 1 she 0 shells 3 shore 7 the 5
 *
 *
 * The {@code TrieST} class represents an symbol table of key-value pairs, with string keys and
 * generic values. It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 * <em>delete</em>, <em>size</em>, and <em>is-empty</em> methods. It also provides character-based
 * methods for finding the string in the symbol table that is the <em>longest prefix</em> of a given
 * prefix, finding all strings in the symbol table that <em>start with</em> a given prefix, and
 * finding all strings in the symbol table that <em>match</em> a given pattern. A symbol table
 * implements the <em>associative array</em> abstraction: when associating a value with a key that
 * is already in the symbol table, the convention is to replace the old value with the new value.
 * Unlike {@link java.util.Map}, this class uses the convention that values cannot be {@code
 * null}—setting the value associated with a key to {@code null} is equivalent to deleting the key
 * from the symbol table. <p> This implementation uses a 256-way trie. The <em>put</em>,
 * <em>contains</em>, <em>delete</em>, and <em>longest prefix</em> operations take time proportional
 * to the length of the key (in the worst case). Construction takes constant time. The
 * <em>size</em>, and <em>is-empty</em> operations take constant time. Construction takes constant
 * time. <p> For additional documentation, see <a href="http://algs4.cs.princeton.edu/52trie">Section
 * 5.2</a> of <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
/**
 *  The {@code TrieST} class represents an symbol table of key-value
 *  pairs, with string keys and generic values.
 *  It supports the usual <em>put</em>, <em>get</em>, <em>contains</em>,
 *  <em>delete</em>, <em>size</em>, and <em>is-empty</em> methods.
 *  It also provides character-based methods for finding the string
 *  in the symbol table that is the <em>longest prefix</em> of a given prefix,
 *  finding all strings in the symbol table that <em>start with</em> a given prefix,
 *  and finding all strings in the symbol table that <em>match</em> a given pattern.
 *  A symbol table implements the <em>associative array</em> abstraction:
 *  when associating a value with a key that is already in the symbol table,
 *  the convention is to replace the old value with the new value.
 *  Unlike {@link java.util.Map}, this class uses the convention that
 *  values cannot be {@code null}—setting the
 *  value associated with a key to {@code null} is equivalent to deleting the key
 *  from the symbol table.
 *  <p>
 *  This implementation uses a 256-way trie.
 *  The <em>put</em>, <em>contains</em>, <em>delete</em>, and
 *  <em>longest prefix</em> operations take time proportional to the length
 *  of the key (in the worst case). Construction takes constant time.
 *  The <em>size</em>, and <em>is-empty</em> operations take constant time.
 *  Construction takes constant time.
 *  <p>
 *  For additional documentation, see <a href="http://algs4.cs.princeton.edu/52trie">Section 5.2</a> of
 *  <i>Algorithms, 4th Edition</i> by Robert Sedgewick and Kevin Wayne.
 */
package edu.cmu.sv.isstac.sampling.exploration;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Path;

/**
 *
 * @author Kasper Luckow
 *
 * Inspired by:
 * http://algs4.cs.princeton.edu/52trie/TrieST.java
 * By: Robert Sedgewick and Kevin Wayne.
 */
public class Trie<V> {

  private final int siblingSize;

  private TrieNode<V> root;
  private int size;

  public static class TrieNode<S> {
    // We currently only use the parent for efficiently propagating
    // pruning information. It is an additional reference that could take up a lot of memory for
    // very big programs
    private final TrieNode<S> parent;

    private S data;
    private final int choice;
    private TrieNode<S>[] next;

    public TrieNode(int choice, TrieNode<S> parent, S data, int siblingSize) {
      this.choice = choice;
      this.data = data;
      this.parent = parent;
      this.next = new TrieNode[siblingSize];
    }

    public TrieNode<S>[] getNext() {
      return next;
    }

    public void setData(S data) {
      this.data = data;
    }

    public TrieNode<S> getParent() {
      return this.parent;
    }

    public S getData() {
      return data;
    }

    @Override
    public String toString() {
      return "<choice " + choice + "; data: " + data.toString() + ">";
    }
  }

  public Trie(int siblingsSize) {
    this.siblingSize = siblingsSize;
  }

  public Trie() {
    // Bound number of choices so we can efficiently store children in an int[] instead of hashmap.
    // 3 is used based on the intuition that---currently---no PC choice is generated with more than
    // 3 choices (except for floating point comparisons, all PC decisions have 2 choices)
    this(3);
  }

  public TrieNode<V> getRoot() {
    return this.root;
  }

  public TrieNode<V> getNode(Path path, int lastChoice) {
    return getNode(root, path, lastChoice, 0);
  }

  private TrieNode<V> getNode(TrieNode<V> x, Path path, int lastChoice, int d) {
    if (x == null) return null;
    if (d == path.size() + 1) return x;

    int choice = -1;
    if (d < path.size()) {
      choice = getChoice(path, d);
    } else {
      choice = lastChoice;
    }

    return getNode(x.next[choice], path, lastChoice, d + 1);
  }

  public V get(Path path, int lastChoice) {
    TrieNode<V> node = getNode(root, path, lastChoice, 0);
    if(node != null)
      return node.data;
    else
      return null;
  }

  public boolean contains(Path path, int lastChoice) {
    return get(path, lastChoice) != null;
  }

  private int getChoice(Path path, int idx) {
    //BIG FAT WARNING:
    //This is in general UNSAFE to do,
    //because there is NO guarantee that choices are selected
    //incrementally! However, there does not seem to be another
    //way of obtaining a lightweight representation of the path
    //i.e. a sequence of decisions (represented by ints)
    //I think it is safe for ThreadChoiceFromSet (currently our only nondeterministic choice)
    //and PCChoiceGenerator
    ChoiceGenerator<?> cg = path.get(idx).getChoiceGenerator();
    int choice = cg.getProcessedNumberOfChoices() - 1;
    return choice;
  }

  public void put(Path path, int lastChoice, V value) {
    root = put(root, null, path, lastChoice, 0, value);
  }

  private TrieNode<V> put(TrieNode<V> current, TrieNode<V> parent, Path path,
                          int lastChoice, int d, V value) {
    if(current == null) {

      //-1 represents choice for root
      int choice = -1;
      if(d != 0) {
        if (d <= path.size()) { //i.e., it is *not* the root
          choice = getChoice(path, d - 1);
        } else {
          size++;
          choice = lastChoice;
        }
      }
      current = new TrieNode<>(choice, parent, value, this.siblingSize);
    }
    //We are done adding the path---just put the last choice now that is missing from this object
    if (d == path.size() + 1) {
      return current;
    }

    int choice;
    if(d == path.size()) {
      choice = lastChoice;
    } else {
      choice = getChoice(path, d);
    }

    current.next[choice] = put(current.next[choice], current, path, lastChoice, d + 1, value);
    return current;
  }

  public int size() {
    return size;
  }

  public void clear() {
    //Should be enough. GC to the rescue
    root = null;
    size = 0;
  }

  public boolean isEmpty() {
    return size() == 0;
  }
}

