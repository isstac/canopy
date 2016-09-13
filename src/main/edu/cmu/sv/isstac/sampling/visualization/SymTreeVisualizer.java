package edu.cmu.sv.isstac.sampling.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.*;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.util.ObjectConverter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class SymTreeVisualizer extends PropertyListenerAdapter {
  Object parent;
  mxGraph graph;

  private Map<String, Object> cgToVertex = new HashMap<>();
  JFrame frame;
  mxHierarchicalLayout layout;
  mxGraphComponent graphComponent;
  public SymTreeVisualizer() {
    frame = new JFrame("Sym tree visualizer");

    graph = new mxGraph();
    layout = new mxHierarchicalLayout(graph);
    parent = graph.getDefaultParent();
    graphComponent = new mxGraphComponent(graph);
    frame.getContentPane().add(graphComponent);

    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(1024, 1024);
    graphComponent.getGraphControl().addMouseWheelListener(new MouseWheelListener() {
      @Override
      public void mouseWheelMoved(MouseWheelEvent e) {
        if(e.getWheelRotation() < 0) {
          graphComponent.zoomIn();
        }
        else {
          graphComponent.zoomOut();
        }
      }
    });

    frame.setVisible(true);
  }
  int count = 0;


  /**
   * Compute reward for "failure"
   */
  @Override
  public void exceptionThrown(VM vm, ThreadInfo currentThread, ElementInfo thrownException) {
    visualize(vm.getSearch());
  }

  @Override
  public void searchConstraintHit(Search search) {
    visualize(search);
  }

  @Override
  public void stateAdvanced(Search search) {
    if(search.isEndState()) {
      visualize(search);
    }
  }

  private void visualize(Search search) {
    PCChoiceGenerator[] pcs = search.getVM().getChoiceGeneratorsOfType(PCChoiceGenerator.class);

    Object prevVertex = this.cgToVertex.get("root");
    if(prevVertex == null) {
      prevVertex = graph.insertVertex(parent, null, "root", 10, 20, 80,
          30);
      this.cgToVertex.put("root", prevVertex);
    }

    graph.getModel().beginUpdate();
    try {
      for(int i = 0; i < pcs.length; i++) {
        PCChoiceGenerator pc = pcs[i];
        Object curr = this.cgToVertex.get(pc.getCurrentPC().toString());
        if (curr == null) {
          curr = graph.insertVertex(parent, null, pc.getCurrentPC().toString(), 10, 20, 80,
              30);
          this.cgToVertex.put(pc.getCurrentPC().toString(), curr);
          if (prevVertex != null) {
            graph.insertEdge(parent, null, "", prevVertex, curr);
          }
        }
        prevVertex = curr;
      }
      layout.execute(graph.getDefaultParent());


    } finally {
      graph.getModel().endUpdate();

    }
  }
}
