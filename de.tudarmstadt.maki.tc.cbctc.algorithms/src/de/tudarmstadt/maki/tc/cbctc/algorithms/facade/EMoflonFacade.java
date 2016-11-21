package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlOperationMode;
import de.tudarmstadt.maki.tc.cbctc.algorithms.algorithm.AlgorithmHelper;
import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.EdgeState;
import de.tudarmstadt.maki.tc.cbctc.model.ModelFactory;
import de.tudarmstadt.maki.tc.cbctc.model.Node;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintViolation;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintViolationReport;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.constraints.TopologyConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

/**
 * Deferred:
 * 
 * TODO@rkluge: Create screenshots (PNG/SVG) from topology visualization
 * 
 * TODO@rkluge Yao impl.
 * 
 * TODO@rkluge- l-kTC impl.
 */
public class EMoflonFacade extends TopologyControlFacade_ImplBase
{

   public static final double DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES = Double.NaN;

   private static final List<TopologyControlOperationMode> SUPPORTED_OPERATION_MODES = Arrays.asList(TopologyControlOperationMode.BATCH,
         TopologyControlOperationMode.INCREMENTAL);

   private final Topology topology;

   private AbstractTopologyControlAlgorithm algorithm;

   private final Map<INodeID, Node> simonstratorNodeToModelNode;

   private final Map<Node, INodeID> modelNodeToSimonstratorNode;

   private final Map<EdgeID, Edge> simonstratorEdgeToModelLink;

   private final Map<Edge, EdgeID> modelLinkToSimonstratorLink;

   private TopologyControlAlgorithmID algorithmID;

   private int constraintViolationCounter;

   private EdgeStateBasedConnectivityConstraint physicalConnectivityConstraint;

   private EdgeStateBasedConnectivityConstraint weakConnectivityConstraint;

   private TopologyConstraint noUnclassifiedLinksConstraint;

   public EMoflonFacade()
   {
      this.simonstratorNodeToModelNode = new HashMap<>();
      this.modelNodeToSimonstratorNode = new HashMap<>();
      this.simonstratorEdgeToModelLink = new HashMap<>();
      this.modelLinkToSimonstratorLink = new HashMap<>();
      this.topology = ModelFactory.eINSTANCE.createTopology();
      this.constraintViolationCounter = 0;

      this.physicalConnectivityConstraint = createPhysicalConnectivityConstraint();

      this.weakConnectivityConstraint = createWeakConnectivityConstraint();

      this.noUnclassifiedLinksConstraint = ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint();
   }

   @Override
   public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID)
   {
      if (this.operationMode == de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode.NOT_SET)
         throw new IllegalArgumentException("Need to specify an operation mode from the following set: " + SUPPORTED_OPERATION_MODES);

      this.algorithm = AlgorithmHelper.createAlgorithmForID(algorithmID);
      this.algorithm.setOperationMode(mapOperationMode(this.operationMode));
      this.algorithmID = algorithmID;
      this.registerEMFListeners();
   }

   private TopologyControlOperationMode mapOperationMode(de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode operationMode)
   {
      switch (operationMode)
      {
      case BATCH:
         return TopologyControlOperationMode.BATCH;
      case INCREMENTAL:
         return TopologyControlOperationMode.INCREMENTAL;
      default:
         throw new IllegalArgumentException("Unsupported mode: " + operationMode);
      }
   }

   @Override
   public Collection<String> getExpectedParameters()
   {
      return Arrays.asList(UnderlayTopologyControlAlgorithms.KTC_PARAMETER_K);
   }

   @Override
   public void run()
   {
      this.run(new TopologyControlAlgorithmParamters());
   }
   
   @Override
   public void run(final TopologyControlAlgorithmParamters parameters)
   {
      final Double k = (Double) parameters.get(UnderlayTopologyControlAlgorithms.KTC_PARAMETER_K);
      if (this.algorithm instanceof AbstractKTC)
      {
         ((AbstractKTC) this.algorithm).setK(k);
      }
      this.algorithm.initializeConstraints();

      this.algorithm.runOnTopology(this.topology);
   }

   @Override
   public INode addNode(INode prototype)
   {
      if (isNodeIdKnown(prototype))
         throw new IllegalStateException(
               String.format("Node ID has already been added. Existing: %s. New: %s", this.simonstratorGraph.getNode(prototype.getId()), prototype));

      final INode simNode = super.addNode(prototype);

      final Node modelNode = createNodeFromPrototype(prototype);

      this.algorithm.handleNodeAddition(modelNode);

      this.establishNodeMapping(simNode, modelNode);

      this.firePostNodeAdded(simNode);

      return simNode;
   }

   @Override
   public void removeNode(final INodeID nodeId)
   {
      if (!isNodeIdKnown(nodeId))
         throw new IllegalStateException(String.format("Try to remove non-existing node: %s", nodeId));

      for (final IEdge outgoingEdge : new ArrayList<>(this.simonstratorGraph.getOutgoingEdges(nodeId)))
      {
         removeEdge(outgoingEdge);
      }

      for (final IEdge incomingEdge : new ArrayList<>(this.simonstratorGraph.getIncomingEdges(nodeId)))
      {
         removeEdge(incomingEdge);
      }

      final INode removedNodeId = this.simonstratorGraph.getNode(nodeId);
      final Node modelNode = getModelNodeForSimonstratorNode(nodeId);

      firePreRemovedNode(removedNodeId);

      removeNodeMapping(nodeId, modelNode);

      removeNode(modelNode);

      super.removeNode(nodeId);
   }

   @Override
   public <T> void updateNodeAttribute(final INode simNode, final SiSType<T> property)
   {
      if (!isNodeIdKnown(simNode))
         throw new IllegalStateException(String.format("Try to update non-existing node: %s", simNode));

      super.updateNodeAttribute(simNode, property);

      final Node modelNode = getModelNodeForSimonstratorNode(simNode.getId());

      this.updateModelNodeAttribute(modelNode, property, simNode.getProperty(property));

      this.firePostNodeAttributeUpdated(simNode, property);
   }

   @Override
   public IEdge addEdge(IEdge prototype)
   {
      if (isEdgeIdKnown(prototype))
         throw new IllegalStateException(
               String.format("Edge ID has already been added. Existing: %s. New: %s", this.simonstratorGraph.getEdge(prototype.getId()), prototype));

      final IEdge newEdge = super.addEdge(prototype);

      final Edge modelLink = createLinkFromPrototype(prototype);

      this.establishLinkMapping(newEdge, modelLink);

      this.firePostEdgeAdded(prototype);

      return newEdge;
   }

   @Override
   public void removeEdge(final IEdge simEdge)
   {
      if (!isEdgeIdKnown(simEdge))
         throw new IllegalStateException(String.format("Try to remove non-existing edge: %s", simEdge));

      final Edge modelEdge = getModelLinkForSimonstratorEdge(simEdge);

      this.firePreEdgeRemoved(simEdge);

      this.removeLinkMapping(simEdge.getId(), modelEdge);

      this.removeEdge(modelEdge);

      super.removeEdge(simEdge);
   }

   @Override
   public <T> void updateEdgeAttribute(final IEdge simEdge, final SiSType<T> property)
   {
      if (!isEdgeIdKnown(simEdge))
         throw new IllegalStateException(String.format("Try to update non-existing edge: %s", simEdge));

      super.updateEdgeAttribute(simEdge, property);

      final Edge modelEdge = getModelLinkForSimonstratorEdge(simEdge);
      final T value = simEdge.getProperty(property);

      this.updateModelLinkAttribute(modelEdge, property, value);

      this.firePostEdgeAttributeUpdated(simEdge, property);
   }

   @Override
   public void resetConstraintViolationCounter()
   {
      this.constraintViolationCounter = 0;
   }

   @Override
   public int getConstraintViolationCount()
   {
      return this.constraintViolationCounter;
   }

   @Override
   public void checkConstraintsAfterContextEvent()
   {

      ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
      if (algorithm.getOperationMode() == TopologyControlOperationMode.INCREMENTAL)
      {
         for (final TopologyConstraint constraint : this.algorithm.getAlgorithmSpecificConstraints())
         {
            constraint.checkOnTopology(topology, report);
         }

         if (isTopologyPhysicallyConnected())
         {
            weakConnectivityConstraint.checkOnTopology(this.topology, report);
         }

         reportConstraintViolations(report);
      }
   }

   @Override
   public void checkConstraintsAfterTopologyControl()
   {
      ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();

      for (final TopologyConstraint constraint : this.algorithm.getAlgorithmSpecificConstraints())
      {
         constraint.checkOnTopology(topology, report);
      }

      noUnclassifiedLinksConstraint.checkOnTopology(topology, report);
      if (isTopologyPhysicallyConnected())
      {
         weakConnectivityConstraint.checkOnTopology(this.topology, report);
      }

      reportConstraintViolations(report);
   }

   @Override
   public void connectOppositeEdges(IEdge fwdEdgePrototype, IEdge bwdEdgePrototype)
   {
      super.connectOppositeEdges(fwdEdgePrototype, bwdEdgePrototype);

      final Edge fwdModelLink = getModelLinkForSimonstratorEdge(fwdEdgePrototype);
      final Edge bwdModelLink = getModelLinkForSimonstratorEdge(bwdEdgePrototype);

      fwdModelLink.setReverseEdge(bwdModelLink);
      bwdModelLink.setReverseEdge(fwdModelLink);
   }

   @Override
   public void unclassifyAllLinks()
   {
      super.unclassifyAllLinks();
      for (final Edge edge : this.getTopology().getEdges())
      {
         edge.setState(EdgeState.UNCLASSIFIED);
      }
   }

   private static EdgeStateBasedConnectivityConstraint createPhysicalConnectivityConstraint()
   {
      EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
      constraint.getStates().add(EdgeState.ACTIVE);
      constraint.getStates().add(EdgeState.INACTIVE);
      constraint.getStates().add(EdgeState.UNCLASSIFIED);
      return constraint;
   }

   private static EdgeStateBasedConnectivityConstraint createWeakConnectivityConstraint()
   {
      EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
      constraint.getStates().add(EdgeState.ACTIVE);
      constraint.getStates().add(EdgeState.UNCLASSIFIED);
      return constraint;
   }

   private Node createNodeFromPrototype(INode prototype)
   {
      final Node modelNode = ModelFactory.eINSTANCE.createNode();
      topology.getNodes().add(modelNode);
      modelNode.setId(prototype.getId().valueAsString());
      modelNode.setEnergyLevel(getNodePropertySafe(prototype, UnderlayTopologyProperties.REMAINING_ENERGY));
      return modelNode;
   }

   private Edge createLinkFromPrototype(IEdge prototype)
   {
      final Edge modelLink = ModelFactory.eINSTANCE.createEdge();
      topology.getEdges().add(modelLink);
      modelLink.setId(prototype.getId().valueAsString());
      modelLink.setSource(getModelNodeForSimonstratorNode(prototype.fromId()));
      modelLink.setTarget(getModelNodeForSimonstratorNode(prototype.toId()));
      modelLink.setState(EdgeState.UNCLASSIFIED);
      modelLink.setAngle(getEdgePropertySafe(prototype, UnderlayTopologyProperties.ANGLE));
      modelLink.setDistance(getEdgePropertySafe(prototype, UnderlayTopologyProperties.DISTANCE));
      modelLink.setWeight(getEdgePropertySafe(prototype, UnderlayTopologyProperties.WEIGHT));
      modelLink.setExpectedLifetime(getEdgePropertySafe(prototype, UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE));
      return modelLink;
   }

   private double getNodePropertySafe(INode prototype, SiSType<Double> property)
   {
      final Double value = prototype.getProperty(property);
      if (value != null)
         return value;
      else
         return DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES;
   }

   private double getEdgePropertySafe(IEdge prototype, SiSType<Double> property)
   {
      final Double value = prototype.getProperty(property);
      if (value != null)
         return value;
      else
         return DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES;
   }

   private boolean isNodeIdKnown(final INode prototype)
   {
      return this.isNodeIdKnown(prototype.getId());
   }

   private boolean isNodeIdKnown(final INodeID nodeId)
   {
      return this.simonstratorNodeToModelNode.containsKey(nodeId);
   }

   private boolean isEdgeIdKnown(final IEdge prototype)
   {
      EdgeID id = prototype.getId();
      return isEdgeIdKnown(id);
   }

   private boolean isEdgeIdKnown(final EdgeID id)
   {
      return this.simonstratorEdgeToModelLink.containsKey(id);
   }

   private boolean isTopologyPhysicallyConnected()
   {
      ConstraintViolationReport tempReport = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
      physicalConnectivityConstraint.checkOnTopology(this.topology, tempReport);
      boolean isPhysicallyConnected = tempReport.getViolations().size() == 0;
      return isPhysicallyConnected;
   }

   public Edge addSymmetricEdge(final String forwardEdgeId, final String backwardEdgeId, final Node sourceNode, final Node targetNode, final double distance,
         final double requiredTransmissionPower)
   {
      final Edge modelEdge = TopologyUtils.addUndirectedEdge(this.topology, forwardEdgeId, backwardEdgeId, sourceNode, targetNode, distance,
            requiredTransmissionPower);

      this.algorithm.handleLinkAddition(modelEdge);

      return modelEdge;
   }

   public <T> void updateModelNodeAttribute(final Node modelNode, final SiSType<T> property, final T value)
   {

      if (UnderlayTopologyProperties.REMAINING_ENERGY.equals(property))
      {
         double oldEnergyLevel = modelNode.getEnergyLevel();
         modelNode.setEnergyLevel((Double) value);
         this.algorithm.handleNodeEnergyLevelModification(modelNode, oldEnergyLevel);
      }
   }

   /**
    * Calls
    * {@link #updateModelLinkAttribute(Edge, GraphElementProperty, Object)}
    * for the given link and its reverse link, setting the same value for the
    * given property on both links.
    */
   public <T> void updateModelLinkAttributeSymmetric(final Edge modelEdge, final SiSType<T> property, final T value)
   {
      updateModelLinkAttribute(modelEdge, property, value);
      updateModelLinkAttribute(modelEdge.getReverseEdge(), property, value);
   }

   /**
    * Sets the property of the given link to the given value.
    * 
    * <p>
    * This method also handles the notification of the CE handlers.
    * </p>
    */
   public <T> void updateModelLinkAttribute(final Edge modelEdge, final SiSType<T> property, final T value)
   {
      if (modelEdge == null)
      {
         throw new NullPointerException();
      }

      if (UnderlayTopologyProperties.WEIGHT.equals(property))
      {
         final double oldWeight = modelEdge.getWeight();
         modelEdge.setWeight((Double) value);
         this.algorithm.handleLinkWeightModification(modelEdge, oldWeight);
      } else if (UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE.equals(property))
      {
         double oldExpectedLifetime = modelEdge.getExpectedLifetime();
         modelEdge.setExpectedLifetime((Double) value);
         this.algorithm.handleLinkExpectedLifetimeModification(modelEdge, oldExpectedLifetime);
      } else if (UnderlayTopologyProperties.EDGE_STATE.equals(property))
      {
         modelEdge.setState(de.tudarmstadt.maki.tc.cbctc.model.EdgeState.UNCLASSIFIED);
         this.algorithm.handleLinkUnclassification(modelEdge);
      }
   }

   public void removeNode(final Node modelNode)
   {
      this.algorithm.handleNodeDeletion(modelNode);
      this.topology.removeNode(modelNode);
   }

   public void removeEdge(final Edge modelEdge)
   {
      this.algorithm.handleLinkDeletion(modelEdge);

      this.topology.removeEdge(modelEdge);
   }

   private void reportConstraintViolations(ConstraintViolationReport report)
   {
      final EList<ConstraintViolation> violations = report.getViolations();
      final int violationCount = violations.size();
      if (!violations.isEmpty())
      {
         Monitor.log(getClass(), Level.ERROR, "%3d constraint violations detected for %6s: %s", violations.size(), this.algorithmID,
               formatHistogramOfViolations(report));
         this.constraintViolationCounter += violationCount;
      } else
      {
         Monitor.log(getClass(), Level.DEBUG, "No constraint violations found");
      }
   }

   private String formatHistogramOfViolations(ConstraintViolationReport report)
   {
      Map<String, Integer> histogramm = new HashMap<>();
      Map<String, List<ConstraintViolation>> bytype = new HashMap<>();
      for (final ConstraintViolation violation : report.getViolations())
      {
         final String simpleName = violation.getViolatedConstraint().getClass().getSimpleName();
         if (!histogramm.containsKey(simpleName))
         {
            histogramm.put(simpleName, 0);
            bytype.put(simpleName, new ArrayList<>());
         }
         histogramm.put(simpleName, histogramm.get(simpleName) + 1);
         bytype.get(simpleName).add(violation);
      }
      final StringBuilder sb = new StringBuilder();
      sb.append("[");
      for (final String key : histogramm.keySet())
      {
         sb.append(String.format("%s : %d\n", key, histogramm.get(key)));
         for (final ConstraintViolation violation : bytype.get(key))
         {
            sb.append("\n\t[");
            sb.append(violation.getAffectedEdges().stream().map(EMoflonFacade::formatEdge).collect(Collectors.joining(",")));
            sb.append("]\n");
         }
         sb.append("\n");
      }
      sb.append("]");

      return sb.toString();
   }

   private static String formatEdge(final Edge edge)
   {
      final Edge link = edge;
      return String.format("%s (s=%s, w=%.3f, L1=%.3f)", link.getId(), link.getState().toString().charAt(0), link.getWeight(), link.getExpectedLifetime());
   }

   private Node getModelNodeForSimonstratorNode(final INodeID nodeId)
   {
      return this.simonstratorNodeToModelNode.get(nodeId);
   }

   private Edge getModelLinkForSimonstratorEdge(final IEdge simEdge)
   {
      return this.simonstratorEdgeToModelLink.get(simEdge.getId());
   }

   public IEdge getSimonstratorLinkForTopologyModelLink(final Edge edge)
   {
      return getGraph().getEdge(modelLinkToSimonstratorLink.get(edge));
   }

   public INodeID getSimonstratorNodeForTopologyModelNode(final Node node)
   {
      return modelNodeToSimonstratorNode.get(node);
   }

   private void establishNodeMapping(INode simonstratorNode, final Node modelNode)
   {
      this.simonstratorNodeToModelNode.put(simonstratorNode.getId(), modelNode);
      this.modelNodeToSimonstratorNode.put(modelNode, simonstratorNode.getId());
   }

   private void establishLinkMapping(final IEdge simonstratorEdge, final Edge modelLink)
   {
      this.simonstratorEdgeToModelLink.put(simonstratorEdge.getId(), modelLink);
      this.modelLinkToSimonstratorLink.put(modelLink, simonstratorEdge.getId());
   }

   private void removeLinkMapping(final EdgeID simonstratorEdgeId, final Edge modelLink)
   {
      this.modelLinkToSimonstratorLink.remove(modelLink);
      this.simonstratorEdgeToModelLink.remove(simonstratorEdgeId);
   }

   private void removeNodeMapping(final INodeID simonstratorNodeId, final Node modelNode)
   {
      this.modelNodeToSimonstratorNode.remove(modelNode);
      this.simonstratorNodeToModelNode.remove(simonstratorNodeId);
   }

   /**
    * Returns the graph of this facade.
    */
   public Topology getTopology()
   {
      return this.topology;
   }

   /**
    * Ensures that the {@link LinkActivationContentAdapter} is installed
    */
   private void registerEMFListeners()
   {
      topology.eAdapters().clear();
      topology.eAdapters().add(new LinkActivationContentAdapter(this));
   }
}
