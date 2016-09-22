package edu.cmu.sv.isstac.sampling.visualization;

import com.mxgraph.layout.hierarchical.mxHierarchicalLayout;
import com.mxgraph.model.mxCell;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import com.mxgraph.view.mxGraphView;

import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.*;

import edu.cmu.sv.isstac.sampling.analysis.MCTSEventObserver;
import edu.cmu.sv.isstac.sampling.analysis.SamplingResult;
import edu.cmu.sv.isstac.sampling.structure.FinalNode;
import edu.cmu.sv.isstac.sampling.structure.Node;
import edu.cmu.sv.isstac.sampling.structure.PCNode;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.symbc.numeric.Constraint;
import gov.nasa.jpf.symbc.numeric.PathCondition;

/**
 * @author Kasper Luckow
 */
public class SymTreeVisualizer implements MCTSEventObserver {
  private Object parent;

  private mxHierarchicalLayout layout;
  private mxGraphComponent graphComponent;
  private mxGraph graph;
  private Map<String, mxCell> cgToVertex = new HashMap<>();

  private final int VERT_SIZE = 1024;
  private final int HORIZ_SIZE = 1024;

  int sampleNum = 0;

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

  private static List<Node> getNodePath(Node node) {
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
    mxCell prevVertex = null;

    graph.getModel().beginUpdate();
    try {
      for(Node node : getNodePath(lastNode)) {
        //We skip nondeterministic nodes for now
        if(node instanceof PCNode ||
            node instanceof FinalNode) {

          PathCondition pc = node.getPathCondition();
          mxCell curr = this.cgToVertex.get(pc.toString());
          if (curr == null) {
            String contents = getNodeContents(node);
            String style = getStyle(node);
            curr = (mxCell)graph.insertVertex(parent, pc.toString(), contents, 10, 20, 80, 30,
                style);
            this.cgToVertex.put(pc.toString(), curr);
            if (prevVertex != null) {
              graph.insertEdge(parent, null, null, prevVertex, curr);
            }
          } else {
            String contents = getNodeContents(node);
            curr.setValue(contents);
          }
          graph.updateCellSize(curr);
          prevVertex = curr;
        }
      }
      layout.execute(graph.getDefaultParent());
    } finally {
      //Buffering a bit here...
      //if(samples % 5 == 0) {
        mxGraphView view = graphComponent.getGraph().getView();
        int compLen = graphComponent.getWidth();
        int viewLen = (int) view.getGraphBounds().getWidth();
        if (viewLen > 0) {
          view.setScale((double) compLen / viewLen * view.getScale());
        }
      //}
      graph.getModel().endUpdate();
    }
    samples++;
  }

  private static String getStyle(Node n) {
    //TODO: make awesome styles here---e.g., implement heatmap based on reward/visitednum
    if(n instanceof PCNode) {
      return "";
    } else if(n instanceof FinalNode) {
      return "";
    }
    return "";
  }

  private static String getNodeContents(Node node) {
    StringBuilder sb = new StringBuilder();
    PathCondition pc = node.getPathCondition();
    String constraintStr = getConstraintRepresentation(pc);
    sb.append(constraintStr).append('\n')
        .append("Reward: ").append(node.getReward().getSucc()).append('\n')
        .append("Visited: ").append(node.getVisitedNum());
    return sb.toString();
  }

  private static String getConstraintRepresentation(PathCondition pc) {
    Constraint header = pc.header;
    if(header != null) {
      return header.getLeft().stringPC() + header.getComparator().toString() + header.getRight()
          .toString();
    } else {
      return "true";
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

  @Override
  public void analysisStarted(Search search) {
    // Do nothing
  }
}
