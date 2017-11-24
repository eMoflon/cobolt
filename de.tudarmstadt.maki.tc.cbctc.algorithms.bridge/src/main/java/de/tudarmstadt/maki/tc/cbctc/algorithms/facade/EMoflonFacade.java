package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.util.ArrayList;
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
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.algorithms.LStarKTC;
import de.tudarmstadt.maki.tc.cbctc.algorithms.NodePreprocessor;
import de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlOperationMode;
import de.tudarmstadt.maki.tc.cbctc.algorithms.YaoGraphAlgorithm;
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
 * Implementation of {@link ITopologyControlFacade} that integrates with eMoflon (www.emoflon.org)
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class EMoflonFacade extends TopologyControlFacade_ImplBase
{

   public static final Double DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES = Double.NaN;

   public static final int DEFAULT_VALUE_FOR_UNDEFINED_HOP_COUNT = -1;

   private final Topology topology;

   private AbstractTopologyControlAlgorithm algorithm;

   private final Map<INodeID, Node> simonstratorNodeToModelNode;

   private final Map<Node, INodeID> modelNodeToSimonstratorNode;

   private final Map<EdgeID, Edge> simonstratorEdgeToModelLink;

   private final Map<Edge, EdgeID> modelLinkToSimonstratorLink;

   private final Map<NodePair, Edge> nodePairToModelLink;

   private int constraintViolationCounter;

   private EdgeStateBasedConnectivityConstraint physicalConnectivityConstraint;

   private EdgeStateBasedConnectivityConstraint weakConnectivityConstraint;

   private TopologyConstraint noUnclassifiedLinksConstraint;

   private NodePreprocessor nodePreprocessor;

   public EMoflonFacade()
   {
      this.simonstratorNodeToModelNode = new HashMap<>();
      this.modelNodeToSimonstratorNode = new HashMap<>();
      this.simonstratorEdgeToModelLink = new HashMap<>();
      this.modelLinkToSimonstratorLink = new HashMap<>();
      this.nodePairToModelLink = new HashMap<>();
      this.topology = ModelFactory.eINSTANCE.createTopology();
      this.constraintViolationCounter = 0;

      this.physicalConnectivityConstraint = EMoflonFacadeConstraintsHelper.createPhysicalConnectivityConstraint();

      this.weakConnectivityConstraint = EMoflonFacadeConstraintsHelper.createWeakConnectivityConstraint();

      this.noUnclassifiedLinksConstraint = ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint();
   }

   public void setNodePreprocessor(NodePreprocessor nodePreprocessor)
   {
      this.nodePreprocessor = nodePreprocessor;
   }

   @Override
   public boolean supportsOperationMode(final TopologyControlAlgorithmID algorithmId,
         final de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode mode)
   {
      final AbstractTopologyControlAlgorithm algorithm = EMoflonFacadeAlgorithmHelper.createAlgorithmForID(algorithmId);
      return algorithm.supportsOperationMode(mapOperationMode(mode));
   }

   @Override
   public void configureAlgorithm(final TopologyControlAlgorithmID algorithmId)
   {
      super.configureAlgorithm(algorithmId);
      
      if (this.operationMode == de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode.NOT_SET)
         throw new IllegalStateException("Please specify an operation mode before configuring the algorithm");

      final AbstractTopologyControlAlgorithm algorithm = EMoflonFacadeAlgorithmHelper.createAlgorithmForID(algorithmId);
      //TODO@rkluge: Re-enable assertion as soon as TC architecture has been refactored
      //      if (!algorithm.supportsOperationMode(mapOperationMode(this.operationMode)))
      //         throw new IllegalArgumentException(
      //               String.format("The configured algorithm '%s' does not support operation mode '%s'", algorithmId, this.operationMode));

      algorithm.setOperationMode(mapOperationMode(this.operationMode));
      this.algorithm = algorithm;
      this.registerEMFListeners();

      this.algorithm.setNodePreprocessor(this.nodePreprocessor);
      this.unclassifyAllLinks();
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
         throw new IllegalArgumentException(String.format("Unsupported mode: '%s'", operationMode));
      }
   }

   @Override
   public void run()
   {
      this.run(new TopologyControlAlgorithmParamters());
   }

   @Override
   public void run(final TopologyControlAlgorithmParamters parameters)
   {
      if (this.algorithm instanceof AbstractKTC)
      {
         final Double k = parameters.getDouble(UnderlayTopologyControlAlgorithms.KTC_PARAM_K);
         if (k == null)
            throw new IllegalArgumentException(
                  String.format("Missing mandatory parameter '%s' for %s", UnderlayTopologyControlAlgorithms.KTC_PARAM_K, this.getConfiguredAlgorithm()));
         AbstractKTC.class.cast(this.algorithm).setK(k);
      }

      if (this.algorithm instanceof LStarKTC)
      {
         final Double a = parameters.getDouble(UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A);
         if (a == null)
            throw new IllegalArgumentException(
                  String.format("Missing mandatory parameter '%s' for %s", UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A, this.getConfiguredAlgorithm()));
         LStarKTC.class.cast(this.algorithm).setA(a);
      }

      if (this.algorithm instanceof YaoGraphAlgorithm)
      {
         //TODO@rkluge: Fix getInt
         final Double coneCount = parameters.getDouble(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT);
         if (coneCount == null)
            throw new IllegalArgumentException(
                  String.format("Missing mandatory parameter '%s' for %s", UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT, this.getConfiguredAlgorithm()));
         YaoGraphAlgorithm.class.cast(this.algorithm).setConeCount(coneCount.intValue());
      }

      this.algorithm.initializeConstraints();

      this.algorithm.runOnTopology(this.topology);
   }

   @Override
   public INode addNode(final INode prototype)
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

      final Edge modelEdge = createLinkFromPrototype(prototype);

      this.establishLinkMapping(newEdge, modelEdge);

      connectWithReverseEdge(modelEdge);

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

   /**
    * Creates a model node from the given Simonstrator node
    */
   private Node createNodeFromPrototype(INode prototype)
   {
      final Node modelNode = ModelFactory.eINSTANCE.createNode();
      topology.getNodes().add(modelNode);
      modelNode.setId(prototype.getId().valueAsString());
      modelNode.setEnergyLevel(getRemainingEnergySafe(prototype));
      modelNode.setHopCount(getHopCountPropertySafe(prototype));
      modelNode.setX(getPropertySafe(prototype, UnderlayTopologyProperties.LONGITUDE));
      modelNode.setY(getPropertySafe(prototype, UnderlayTopologyProperties.LATITUDE));
      modelNode.setBatteryCapacity(getPropertySafe(prototype, SiSTypes.ENERGY_BATTERY_CAPACITY));
      modelNode.setLocalViewHorizon(getPropertySafe(prototype, UnderlayTopologyProperties.LOCAL_VIEW_HORIZON, 1));
      return modelNode;
   }

   /**
    * Creates a model edge from the given Simonstrator edge
    */
   private Edge createLinkFromPrototype(final IEdge prototype)
   {
      final Edge modelLink = ModelFactory.eINSTANCE.createEdge();
      topology.getEdges().add(modelLink);
      modelLink.setId(prototype.getId().valueAsString());
      modelLink.setSource(getModelNodeForSimonstratorNode(prototype.fromId()));
      modelLink.setTarget(getModelNodeForSimonstratorNode(prototype.toId()));
      modelLink.setState(EMoflonFacadeAttributeHelper.getEdgeStateSafe(prototype));
      modelLink.setAngle(getPropertySafe(prototype, UnderlayTopologyProperties.ANGLE));
      modelLink.setDistance(getPropertySafe(prototype, UnderlayTopologyProperties.DISTANCE));
      modelLink.setWeight(getPropertySafe(prototype, UnderlayTopologyProperties.WEIGHT));
      modelLink.setExpectedLifetime(getPropertySafe(prototype, UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE));
      modelLink.setTransmissionPower(getPropertySafe(prototype, UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER));
      // modelLink.setReverseEdge(...); is missing because reverse edges are linked elsewhere #connectOppositeEdges
      return modelLink;
   }

   private double getRemainingEnergySafe(final INode prototype)
   {
      return getPropertySafe(prototype, UnderlayTopologyProperties.REMAINING_ENERGY, DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES);
   }

   private int getHopCountPropertySafe(final INode prototype)
   {
      return getPropertySafe(prototype, UnderlayTopologyProperties.HOP_COUNT, DEFAULT_VALUE_FOR_UNDEFINED_HOP_COUNT);
   }

   private double getPropertySafe(IElement prototype, SiSType<Double> property)
   {
      return getPropertySafe(prototype, property, DEFAULT_VALUE_FOR_UNDEFINED_ATTRIBUTES);
   }

   private <T> T getPropertySafe(IElement prototype, SiSType<T> property, T defaultValueForUndefinedAttributes)
   {
      final T value = prototype.getProperty(property);
      if (value != null)
         return value;
      else
         return defaultValueForUndefinedAttributes;
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
         Monitor.log(getClass(), Level.ERROR, "%3d constraint violations detected for %6s: %s", violations.size(), this.getConfiguredAlgorithm(),
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

   private void connectWithReverseEdge(final Edge modelLink)
   {
      final Edge reverseEdge = findReverseModelEdge(modelLink);
      if (reverseEdge != null)
      {
         modelLink.setReverseEdge(reverseEdge);
         reverseEdge.setReverseEdge(modelLink);
      }
   }

   private Edge findReverseModelEdge(Edge modelLink)
   {
      final NodePair nodePairForReverse = new NodePair(modelLink.getTarget(), modelLink.getSource());
      if (this.nodePairToModelLink.containsKey(nodePairForReverse))
      {
         return this.nodePairToModelLink.get(nodePairForReverse);
      }
      return null;
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
      this.nodePairToModelLink.put(createNodePair(modelLink), modelLink);
   }

   private NodePair createNodePair(Edge modelLink)
   {
      return new NodePair(modelLink.getSource(), modelLink.getTarget());
   }

   private void removeLinkMapping(final EdgeID simonstratorEdgeId, final Edge modelLink)
   {
      this.modelLinkToSimonstratorLink.remove(modelLink);
      this.simonstratorEdgeToModelLink.remove(simonstratorEdgeId);
      this.nodePairToModelLink.remove(createNodePair(modelLink));
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
   
   /**
    * Pair of {@link Node}s
    * @author Roland Kluge - Initial implementation
    */
   private static class NodePair
   {
      final Node node1;

      final Node node2;

      public NodePair(Node node1, Node node2)
      {
         this.node1 = node1;
         this.node2 = node2;
      }
      
      @Override
      public String toString()
      {
         return this.node1.getId() + "+" + this.node2.getId();
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((node1 == null) ? 0 : node1.hashCode());
         result = prime * result + ((node2 == null) ? 0 : node2.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         NodePair other = (NodePair) obj;
         if (node1 == null)
         {
            if (other.node1 != null)
               return false;
         } else if (!node1.equals(other.node1))
            return false;
         if (node2 == null)
         {
            if (other.node2 != null)
               return false;
         } else if (!node2.equals(other.node2))
            return false;
         return true;
      }
   }
}
