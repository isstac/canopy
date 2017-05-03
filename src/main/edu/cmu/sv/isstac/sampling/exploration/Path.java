package edu.cmu.sv.isstac.sampling.exploration;

import java.util.Iterator;
import java.util.LinkedList;

import com.google.common.base.Objects;

import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.util.JPFUtil;
import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * @author Kasper Luckow
 *
 */
public class Path {
  private LinkedList<Integer> store;

  public Path(Path other) {
    this.store = new LinkedList<>();
    for(int o : other.store) {
      this.store.add(o);
    }
  }

  public Path() {
    this.store = new LinkedList<>();
  }

  public Path(ChoiceGenerator<?> cg) {
    this();
    if(cg != null) {
      for(ChoiceGenerator<?> c : cg.getAll()) {
        this.addChoice(c);
      }
    }
  }

  public Path(Node n) {
    this();
    Node node = n;
    while(node != null && node.getChoice() >= 0) {
      this.store.addFirst(node.getChoice());
      node = node.getParent();
    }
  }
  
  // same as copy constructor
  public Path copy() {
    return new Path(this);
  }
  
  public void addChoice(ChoiceGenerator<?> cg) {
    int choice = JPFUtil.getCurrentChoiceOfCG(cg);
    assert choice >= 0;
    addChoice(choice);
  }
  
  public void addChoice(int choice) {
    store.add(choice);
  }

  public int removeLast() {
    return store.removeLast();
  }
  
  @Override
  public int hashCode() {
    return Objects.hashCode(store);
  }
  
  @Override
  public boolean equals(Object other) {
    if(other == null) return false;
    if(getClass() != other.getClass()) return false;
    Path otherPath = (Path) other;
    return Objects.equal(this.store, otherPath.store);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    Iterator<Integer> iter = store.iterator();
    while(iter.hasNext()) {
      sb.append(iter.next());
      if(iter.hasNext())
        sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }
}
