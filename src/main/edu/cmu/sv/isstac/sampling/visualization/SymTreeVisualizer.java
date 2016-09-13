package edu.cmu.sv.isstac.sampling.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.swing.handler.mxPanningHandler;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import edu.cmu.sv.isstac.sampling.analysis.AnalysisEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.MCTSEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.PCNode;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.PCChoiceGenerator;
import gov.nasa.jpf.symbc.numeric.PathCondition;
import gov.nasa.jpf.util.ObjectConverter;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author Kasper Luckow
 */
public class SymTreeVisualizer implements MCTSEventObserver {
  private Object parent;

  private mxHierarchicalLayout layout;
  private mxGraphComponent graphComponent;
  private mxGraph graph;
  private Map<String, Object> cgToVertex = new HashMap<>();

  private final int VERT_SIZE = 1024;
  private final int HORIZ_SIZE = 1024;

  public SymTreeVisualizer() {
    JFrame frame = new JFrame("Sym tree visualizer");

    graph = new mxGraph();
    layout = new mxHierarchicalLayout(graph);
    parent = graph.getDefaultParent();

    //I find it weird that we need to override isPanningEvent
    //to make panning work correctly
    graphComponent = new mxGraphComponent(graph) {
      @Override
      public boolean isPanningEvent(MouseEvent event) {
        return true;
      }
    };
    //TODO: there is a weird bug when panning with mouse drag that inverts
    // panning when mouse exceeds boundary of frame. This is because
    // graphcomponent extends jcscrollpanel and auto scolling seems
    // to mess with the mouse drag :/
    graphComponent.setAutoscrolls(false);
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

    frame.getContentPane().add(graphComponent);
    frame.setSize(HORIZ_SIZE, VERT_SIZE);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
  }

  private static List<Node> getNodeList(Node node) {
    //Pretty lame
    LinkedList<Node> pcs = new LinkedList<>();
    for(Node tmp = node; tmp != null; tmp = tmp.getParent()) {
      pcs.addFirst(tmp);
    }
    return pcs;
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume,
                         SamplingResult.ResultContainer currentBestResult, Node lastNode) {
    PCChoiceGenerator[] pcs = searchState.getVM().getChoiceGeneratorsOfType(PCChoiceGenerator.class);
    String rootLbl = "true";
    Object prevVertex = this.cgToVertex.get(rootLbl);
    if(prevVertex == null) {
      prevVertex = graph.insertVertex(parent, rootLbl, null, 10, 20, 80, 30);
      this.cgToVertex.put(rootLbl, prevVertex);
    }

    List<Node> mctsPath = getNodeList(lastNode);

    graph.getModel().beginUpdate();
    try {
      for(Node node : mctsPath) {
        if(node instanceof PCNode) {
          PathCondition pc = ((PCNode)node).getPathCondition();
          Object curr = this.cgToVertex.get(pc.toString());
          if (curr == null) {
            curr = graph.insertVertex(parent, pc.toString(), "s", 10, 20, 80, 30);
            this.cgToVertex.put(pc.toString(), curr);
            if (prevVertex != null) {
              graph.insertEdge(parent, null, "", prevVertex, curr);
            }
          }
          prevVertex = curr;
        }
      }
      layout.execute(graph.getDefaultParent());
    } finally {
      graph.getModel().endUpdate();

    }
  }

  @Override
  public void sampleDone(Search searchState, long samples, long propagatedReward, long pathVolume, SamplingResult.ResultContainer currentBestResult) {
    // No thanks... ugly (see note in MCTSEventObserver)
  }

  @Override
  public void analysisDone(SamplingResult result) {
    // Do nothing
  }
}
