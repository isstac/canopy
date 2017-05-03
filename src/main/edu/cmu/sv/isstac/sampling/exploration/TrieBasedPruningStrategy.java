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

package edu.cmu.sv.isstac.sampling.exploration;

import java.util.ArrayList;

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 * 
 */
public class TrieBasedPruningStrategy implements ChoicesStrategy, PruningStrategy {

  private Trie prunedPaths = new Trie();

  private static TrieBasedPruningStrategy instance;

  public static TrieBasedPruningStrategy getInstance() {
    if(instance == null) {
      instance = new TrieBasedPruningStrategy();
    }
    return instance;
  }

  private TrieBasedPruningStrategy() { }

  public void reset() {
    this.prunedPaths.clear();
  }

  @Override
  public ArrayList<Integer> getEligibleChoices(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {


    int lastChoice = cg.getProcessedNumberOfChoices() - 1;
    Trie.TrieNode node = this.prunedPaths.getNode(path);
    if(node == null) {
      // can happen for example for the first choice. In this case, by definition, none of the
      // choices are pruned
      ArrayList<Integer> eligibleChoices = new ArrayList<>();
      for(int choice = 0; choice < cg.getTotalNumberOfChoices(); choice++) {
        eligibleChoices.add(choice);
      }
      return eligibleChoices;
    } else {
      ArrayList<Integer> eligibleChoices = new ArrayList<>();
      Trie.TrieNode[] nxtNodes = node.getNext();
      for (int choice = 0; choice < nxtNodes.length; choice++) {
        if (nxtNodes[choice] == null || !nxtNodes[choice].isFlagSet()) {
          //i.e., this path has not been pruned
          eligibleChoices.add(choice);
        }
      }
      return eligibleChoices;
    }
  }

  @Override
  public boolean hasTerminatedPathBeenExplored(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    // We just return false here, because, by construction, a path can never be explored multiple
    // times when using pruning. The "safe" way of doing this would be to actually consult the
    // prunedPaths set, but we would potentially have to check all path prefixes, because, in its
    // current implementation, we only keep the prefix path in the set when the subtree (i.e. all
    // suffixes of that prefix) are prunedPaths. In other words, it is slightly more efficient to
    // return false here
    return false;
  }

  @Override
  public boolean isFullyPruned() {
    return this.prunedPaths.getRoot() != null &&
        this.prunedPaths.getRoot().isFlagSet();
  }

  @Override
  public void performPruning(gov.nasa.jpf.vm.Path path, ChoiceGenerator<?> cg) {
    int lastChoice = cg.getProcessedNumberOfChoices() - 1;

    //If backtracking is not used, we can optimize how pruning information is kept:
    // We only need to keep parent, because all subtrees (children) are prunedPaths.
    // *However*, when backtracking is used we can end up in a state where parents are prunedPaths
    // but we are currently asking for whether some other cg in a subtree of a prunedPaths node has
    // non-prunedPaths children. In that case, if we removed this information using the code below,
    // the backtracker would incorrectly select a choice that actually was prunedPaths.
//      for(Path child : children) {
//        prunedPaths.remove(child);
//      }

    //GUARANTEE: We should not use lastchoice here
    prunedPaths.setFlag(path, true);
    //For very long paths, this could be a bottleneck. Basically we are adding an element to the
    // trie, and getting it again subsequently..
    Trie.TrieNode lastNode = prunedPaths.getNode(path);

    //propagate pruning backwards
    Trie.TrieNode currentNode = lastNode.getParent();
    while(currentNode != null) {
      Trie.TrieNode[] nxt = currentNode.getNext();
      for(int choice = 0; choice < nxt.length; choice++) {
        if(nxt[choice] == null ||
            !nxt[choice].isFlagSet()) {
          //we found a node that had a child that was not pruned, i.e. we will not proceed
          // propagating pruning information
          return;
        }
      }
      //all siblings were pruned, so we also prune the parent by setting its data field to true
      currentNode.setFlag(true);
      currentNode = currentNode.getParent();
    }
  }
}
