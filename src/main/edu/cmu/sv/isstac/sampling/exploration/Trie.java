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

import com.google.common.cache.CacheBuilder;

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

  // Bound number of choices so we can efficiently store children in an int[] instead of hashmap.
  private static final int siblingsSize = 5;

  private TrieNode<V> root;
  private int size;

  private static class TrieNode<S> {
    private final S data;
    private final int choice;
    private TrieNode<S>[] next = new TrieNode[siblingsSize];

    public TrieNode(int choice, S data) {
      this.choice = choice;
      this.data = data;
    }

    @Override
    public String toString() {
      return "<choice " + choice + "; data: " + data.toString() + ">";
    }
  }

  public V get(Path path, int lastChoice) {
    return get(root, path, lastChoice, 0);
  }

  private V get(TrieNode<V> x, Path path, int lastChoice, int d) {
    if (x == null) return null;
    if (d == path.size() + 1) return x.data;

    int choice = -1;
    if (d < path.size()) {
      choice = getChoice(path, d);
    } else {
      choice = lastChoice;
    }

    return get(x.next[choice], path, lastChoice, d + 1);
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

  public void add(Path path, int lastChoice, V value) {
    root = add(root, path, lastChoice, 0, value);
  }

  private TrieNode<V> add(TrieNode<V> x, Path path, int lastChoice, int d, V value) {
    if(x == null) {

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
      x = new TrieNode<>(choice, value);
    }
    //We are done adding the path---just add the last choice now that is missing from this object
    if (d == path.size() + 1) {
      return x;
    }

    int choice;
    if(d == path.size()) {
      choice = lastChoice;
    } else {
      choice = getChoice(path, d);
    }

    x.next[choice] = add(x.next[choice], path, lastChoice, d + 1, value);
    return x;
  }

  public int size() {
    return size;
  }

  public boolean isEmpty() {
    return size() == 0;
  }
}

