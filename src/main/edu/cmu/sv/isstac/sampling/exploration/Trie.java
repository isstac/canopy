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

import edu.cmu.sv.isstac.sampling.util.JPFUtil;
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
public class Trie {

  private TrieNode root;
  private int size;

  public static class TrieNode {
    // We currently only use the parent for efficiently propagating
    // pruning information. It is an additional reference that could take up significant mem
    private final TrieNode parent;

    private boolean flag;
    private final int choice;
    private TrieNode[] next;

    public TrieNode(int choice, TrieNode parent, int siblingSize) {
      this.choice = choice;
      this.parent = parent;
      this.next = new TrieNode[siblingSize];
    }

    public TrieNode[] getNext() {
      return next;
    }

    public void setFlag(boolean pruned) {
      this.flag = pruned;
    }

    public TrieNode getParent() {
      return this.parent;
    }

    public boolean isFlagSet() {
      return flag;
    }

    @Override
    public String toString() {
      return "<choice " + choice + "; flag: " + this.flag + ">";
    }
  }

  public TrieNode getRoot() {
    return this.root;
  }

  public TrieNode getNode(Path path) {
    return getNode(root, path, 0);
  }

  private TrieNode getNode(TrieNode x, Path path, int d) {
    if (x == null) return null;
    if (d == path.size()) return x;

    int choice = getChoice(path, d);
    return getNode(x.next[choice], path, d + 1);
  }

  public boolean isFlagSet(Path path) {
    TrieNode node = getNode(root, path, 0);
    if(node != null)
      return node.isFlagSet();
    else
      return false;
  }

  public boolean contains(Path path) {
    return getNode(path) != null;
  }

  private int getNumberOfChoices(Path path, int idx) {
    ChoiceGenerator<?> cg = path.get(idx).getChoiceGenerator();
    return cg.getTotalNumberOfChoices();
  }

  private int getChoice(Path path, int idx) {
    ChoiceGenerator<?> cg = path.get(idx).getChoiceGenerator();
    int choice = JPFUtil.getCurrentChoiceOfCG(cg);
    return choice;
  }

  public void setFlag(Path path, boolean flag) {
    root = put(root, null, path, 0, flag);
  }

  private TrieNode put(TrieNode current, TrieNode parent, Path path, int d, boolean flag) {
    if(current == null) {

      //-1 represents choice for root
      int choice = -1;
      if(d != 0) {
        choice = getChoice(path, d - 1);
      }
      int numberOfChoices = 0;
      if(d < path.size()) {
       numberOfChoices = getNumberOfChoices(path, d);
      }
      current = new TrieNode(choice, parent, numberOfChoices);
    }
    //We are done adding the path---just put the last choice now that is missing from this object
    if (d == path.size()) {
      size++;
      current.setFlag(flag);
      return current;
    } else {
      //by default we dont't set the flag for intermediate nodes
      current.setFlag(false);
    }

    int choice = getChoice(path, d);
    current.next[choice] = put(current.next[choice], current, path, d + 1, flag);
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

