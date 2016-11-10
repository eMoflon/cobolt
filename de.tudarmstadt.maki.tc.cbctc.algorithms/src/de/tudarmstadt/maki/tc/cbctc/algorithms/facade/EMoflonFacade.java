package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.NodeProperty;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyProperties;
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
import de.tudarmstadt.maki.tc.cbctc.model.constraints.GraphConstraint;
import de.tudarmstadt.maki.tc.cbctc.model.utils.TopologyUtils;

/**
 * Deferred:
 * 
 * TODO@rkluge: Create screenshots (PNG/SVG) from topology visualization
 * 
 * TODO@rkluge: Kill Topology#addEdge
 * {@link Topology#addEdge(String, Node, Node, double, double, EdgeState)}
 * 
 * TODO@rkluge More modular configuration of facades etc.
 * 
 * TODO@rkluge: Create a basic example of how to implement the facade interface
 * 
 * TODO@rkluge XTC impl.
 * 
 * TODO@rkluge Yao impl.
 * 
 * TODO@rkluge: Extract hop count from routing
 * 
 * TODO@rkluge- l-kTC impl.
 * 
 * TODO@rkluge- GG impl.
 */
public class EMoflonFacade extends TopologyControlFacade_ImplBase {

	private static final List<TopologyControlOperationMode> SUPPORTED_OPERATION_MODES = Arrays
			.asList(TopologyControlOperationMode.BATCH, TopologyControlOperationMode.INCREMENTAL);
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
	private GraphConstraint noUnclassifiedLinksConstraint;

	public EMoflonFacade() {
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
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		if (this.operationMode == de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode.NOT_SET)
			throw new IllegalArgumentException(
					"Need to specify an operation mode from the following set: " + SUPPORTED_OPERATION_MODES);

		this.algorithm = AlgorithmHelper.createAlgorithmForID(algorithmID);
		this.algorithm.setOperationMode(mapOperationMode(this.operationMode));
		this.algorithmID = algorithmID;
		this.registerEMFListeners();
	}

	private TopologyControlOperationMode mapOperationMode(
			de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode operationMode) {
		switch (operationMode) {
		case BATCH:
			return TopologyControlOperationMode.BATCH;
		case INCREMENTAL:
			return TopologyControlOperationMode.INCREMENTAL;
		default:
			throw new IllegalArgumentException("Unsupported mode: " + operationMode);
		}
	}

	@Override
	public Collection<String> getExpectedParameters() {
		return Arrays.asList(UnderlayTopologyControlAlgorithms.KTC_PARAMETER_K);
	}

	@Override
	public void run(final TopologyControlAlgorithmParamters parameters) {
		final Double k = (Double) parameters.get(UnderlayTopologyControlAlgorithms.KTC_PARAMETER_K);
		if (this.algorithm instanceof AbstractKTC) {
			((AbstractKTC) this.algorithm).setK(k);
		}
		this.algorithm.initializeConstraints();

		this.algorithm.runOnTopology(this.topology);
	}

	/**
	 * Convenience method that is tailored to kTC.
	 */
	public void run(final double k) {
		this.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAMETER_K, k));
	}

	@Override
	public INode addNode(INode prototype) {
		if (isNodeIdKnown(prototype))
			throw new IllegalStateException(String.format("Node ID has already been added. Existing: %s. New: %s",
					this.simonstratorGraph.getNode(prototype.getId()), prototype));

		final INode simNode = super.addNode(prototype);

		final Node ktcNode = createNodeFromPrototype(prototype);

		this.algorithm.handleNodeAddition(ktcNode);

		this.establishNodeMapping(simNode, ktcNode);

		this.firePostNodeAdded(simNode);

		return simNode;
	}

	@Override
	public void removeNode(final INodeID nodeId) {
		if (!isNodeIdKnown(nodeId))
			throw new IllegalStateException(String.format("Try to remove non-existing node: %s", nodeId));

		for (final IEdge outgoingEdge : new ArrayList<>(this.simonstratorGraph.getOutgoingEdges(nodeId))) {
			removeEdge(outgoingEdge);
		}

		for (final IEdge incomingEdge : new ArrayList<>(this.simonstratorGraph.getIncomingEdges(nodeId))) {
			removeEdge(incomingEdge);
		}

		final INode removedNodeId = this.simonstratorGraph.getNode(nodeId);
		final Node ktcNode = getModelNodeForSimonstratorNode(nodeId);

		firePreRemovedNode(removedNodeId);

		removeNodeMapping(nodeId, ktcNode);

		removeNode(ktcNode);

		super.removeNode(nodeId);
	}

	@Override
	public <T> void updateNodeAttribute(final INode simNode, final NodeProperty<T> property) {
		if (!isNodeIdKnown(simNode))
			throw new IllegalStateException(String.format("Try to update non-existing node: %s", simNode));

		super.updateNodeAttribute(simNode, property);

		final Node ktcNode = getModelNodeForSimonstratorNode(simNode.getId());

		this.updateModelNodeAttribute(ktcNode, property, simNode.getProperty(property));

		this.firePostNodeAttributeUpdated(simNode, property);
	}

	@Override
	public IEdge addEdge(IEdge prototype) {
		if (isEdgeIdKnown(prototype))
			throw new IllegalStateException(String.format("Edge ID has already been added. Existing: %s. New: %s",
					this.simonstratorGraph.getEdge(prototype.getId()), prototype));

		final IEdge newEdge = super.addEdge(prototype);

		final Edge modelLink = createLinkFromPrototype(prototype);

		this.establishLinkMapping(newEdge, modelLink);

		this.firePostEdgeAdded(prototype);

		return newEdge;
	}

	@Override
	public void removeEdge(final IEdge simEdge) {
		if (!isEdgeIdKnown(simEdge))
			throw new IllegalStateException(String.format("Try to remove non-existing edge: %s", simEdge));

		final Edge ktcLink = getModelLinkForSimonstratorEdge(simEdge);

		this.firePreEdgeRemoved(simEdge);

		this.removeLinkMapping(simEdge.getId(), ktcLink);

		this.removeEdge(ktcLink);

		super.removeEdge(simEdge);
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge simEdge, final EdgeProperty<T> property) {
		if (!isEdgeIdKnown(simEdge))
			throw new IllegalStateException(String.format("Try to update non-existing edge: %s", simEdge));

		super.updateEdgeAttribute(simEdge, property);

		final Edge ktcLink = getModelLinkForSimonstratorEdge(simEdge);
		final T value = simEdge.getProperty(property);

		this.updateModelLinkAttribute(ktcLink, property, value);

		this.firePostEdgeAttributeUpdated(simEdge, property);
	}

	@Override
	public void resetConstraintViolationCounter() {
		this.constraintViolationCounter = 0;
	}

	@Override
	public int getConstraintViolationCount() {
		return this.constraintViolationCounter;
	}

	@Override
	public void checkConstraintsAfterContextEvent() {

		ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		if (algorithm.getOperationMode() == TopologyControlOperationMode.INCREMENTAL) {
			for (final GraphConstraint constraint : this.algorithm.getAlgorithmSpecificConstraints()) {
				constraint.checkOnTopology(topology, report);
			}

			if (isTopologyPhysicallyConnected()) {
				weakConnectivityConstraint.checkOnTopology(this.topology, report);
			}

			reportConstraintViolations(report);
		}
	}

	@Override
	public void checkConstraintsAfterTopologyControl() {
		ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();

		for (final GraphConstraint constraint : this.algorithm.getAlgorithmSpecificConstraints()) {
			constraint.checkOnTopology(topology, report);
		}

		noUnclassifiedLinksConstraint.checkOnTopology(topology, report);
		if (isTopologyPhysicallyConnected()) {
			weakConnectivityConstraint.checkOnTopology(this.topology, report);
		}

		reportConstraintViolations(report);
	}

	private static EdgeStateBasedConnectivityConstraint createPhysicalConnectivityConstraint() {
		EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE
				.createEdgeStateBasedConnectivityConstraint();
		constraint.getStates().add(EdgeState.ACTIVE);
		constraint.getStates().add(EdgeState.INACTIVE);
		constraint.getStates().add(EdgeState.UNCLASSIFIED);
		return constraint;
	}

	private static EdgeStateBasedConnectivityConstraint createWeakConnectivityConstraint() {
		EdgeStateBasedConnectivityConstraint constraint = ConstraintsFactory.eINSTANCE
				.createEdgeStateBasedConnectivityConstraint();
		constraint.getStates().add(EdgeState.ACTIVE);
		constraint.getStates().add(EdgeState.UNCLASSIFIED);
		return constraint;
	}

	private Node createNodeFromPrototype(INode prototype) {
		final Node ktcNode = ModelFactory.eINSTANCE.createNode();
		topology.getNodes().add(ktcNode);
		ktcNode.setId(prototype.getId().valueAsString());
		ktcNode.setEnergyLevel(getNodePropertySafe(prototype, UnderlayTopologyProperties.REMAINING_ENERGY));
		return ktcNode;
	}

	private Edge createLinkFromPrototype(IEdge prototype) {
		final Edge modelLink = ModelFactory.eINSTANCE.createEdge();
		topology.getEdges().add(modelLink);
		modelLink.setId(prototype.getId().valueAsString());
		modelLink.setSource(getModelNodeForSimonstratorNode(prototype.fromId()));
		modelLink.setTarget(getModelNodeForSimonstratorNode(prototype.toId()));
		modelLink.setState(EdgeState.UNCLASSIFIED);
		modelLink.setAngle(getEdgePropertySafe(prototype, UnderlayTopologyProperties.ANGLE));
		modelLink.setDistance(getEdgePropertySafe(prototype, UnderlayTopologyProperties.DISTANCE));
		modelLink.setWeight(getEdgePropertySafe(prototype, UnderlayTopologyProperties.WEIGHT));
		modelLink.setExpectedLifetime(
				getEdgePropertySafe(prototype, UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE));
		return modelLink;
	}

	private double getNodePropertySafe(INode prototype, NodeProperty<Double> property) {
		final Double value = prototype.getProperty(property);
		if (value != null)
			return value;
		else
			return Double.NaN;
	}

	private double getEdgePropertySafe(IEdge prototype, EdgeProperty<Double> property) {
		final Double value = prototype.getProperty(property);
		if (value != null)
			return value;
		else
			return Double.NaN;
	}

	private boolean isNodeIdKnown(final INode prototype) {
		return this.isNodeIdKnown(prototype.getId());
	}

	private boolean isNodeIdKnown(final INodeID nodeId) {
		return this.simonstratorNodeToModelNode.containsKey(nodeId);
	}

	private boolean isEdgeIdKnown(final IEdge prototype) {
		EdgeID id = prototype.getId();
		return isEdgeIdKnown(id);
	}

	private boolean isEdgeIdKnown(final EdgeID id) {
		return this.simonstratorEdgeToModelLink.containsKey(id);
	}

	private boolean isTopologyPhysicallyConnected() {
		ConstraintViolationReport tempReport = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		physicalConnectivityConstraint.checkOnTopology(this.topology, tempReport);
		boolean isPhysicallyConnected = tempReport.getViolations().size() == 0;
		return isPhysicallyConnected;
	}

	public Edge addSymmetricEdge(final String forwardEdgeId, final String backwardEdgeId,
			final Node sourceNode, final Node targetNode, final double distance,
			final double requiredTransmissionPower) {
		final Edge ktcLink = TopologyUtils.addUndirectedEdge(this.topology, forwardEdgeId, backwardEdgeId, sourceNode,
				targetNode, distance, requiredTransmissionPower);

		this.algorithm.handleLinkAddition(ktcLink);

		return ktcLink;
	}

	public <T> void updateModelNodeAttribute(final Node ktcNode, final GraphElementProperty<T> property,
			final T value) {

		if (UnderlayTopologyProperties.REMAINING_ENERGY.equals(property)) {
			double oldEnergyLevel = ktcNode.getEnergyLevel();
			ktcNode.setEnergyLevel((Double) value);
			this.algorithm.handleNodeEnergyLevelModification(ktcNode, oldEnergyLevel);
		}
	}

	/**
	 * Calls
	 * {@link #updateModelLinkAttribute(Edge, GraphElementProperty, Object)}
	 * for the given link and its reverse link, setting the same value for the
	 * given property on both links.
	 */
	public <T> void updateModelLinkAttributeSymmetric(final Edge ktcLink, final GraphElementProperty<T> property,
			final T value) {
		updateModelLinkAttribute(ktcLink, property, value);
		updateModelLinkAttribute(ktcLink.getReverseEdge(), property, value);
	}

	/**
	 * Sets the property of the given link to the given value.
	 * 
	 * <p>
	 * This method also handles the notification of the CE handlers.
	 * </p>
	 */
	public <T> void updateModelLinkAttribute(final Edge ktcLink, final GraphElementProperty<T> property,
			final T value) {
		if (ktcLink == null) {
			throw new NullPointerException();
		}

		if (UnderlayTopologyProperties.WEIGHT.equals(property)) {
			final double oldWeight = ktcLink.getWeight();
			ktcLink.setWeight((Double) value);
			this.algorithm.handleLinkWeightModification(ktcLink, oldWeight);
		} else if (UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE.equals(property)) {
			double oldExpectedLifetime = ktcLink.getExpectedLifetime();
			ktcLink.setExpectedLifetime((Double) value);
			this.algorithm.handleLinkExpectedLifetimeModification(ktcLink, oldExpectedLifetime);
		} else if (UnderlayTopologyProperties.EDGE_STATE.equals(property)) {
			ktcLink.setState(de.tudarmstadt.maki.tc.cbctc.model.EdgeState.UNCLASSIFIED);
			this.algorithm.handleLinkUnclassification(ktcLink);
		}
	}

	public void removeNode(final Node ktcNode) {
		this.algorithm.handleNodeDeletion(ktcNode);
		this.topology.removeNode(ktcNode);
	}

	public void removeEdge(final Edge ktcLink) {
		this.algorithm.handleLinkDeletion(ktcLink);

		this.topology.removeEdge(ktcLink);
	}

	private void reportConstraintViolations(ConstraintViolationReport report) {
		final EList<ConstraintViolation> violations = report.getViolations();
		final int violationCount = violations.size();
		if (!violations.isEmpty()) {
			Monitor.log(getClass(), Level.ERROR, "%3d constraint violations detected for %6s: %s", violations.size(),
					this.algorithmID, formatHistogramOfViolations(report));
			this.constraintViolationCounter += violationCount;
		} else {
			Monitor.log(getClass(), Level.DEBUG, "No constraint violations found");
		}
	}

	private String formatHistogramOfViolations(ConstraintViolationReport report) {
		Map<String, Integer> histogramm = new HashMap<>();
		Map<String, List<ConstraintViolation>> bytype = new HashMap<>();
		for (final ConstraintViolation violation : report.getViolations()) {
			final String simpleName = violation.getViolatedConstraint().getClass().getSimpleName();
			if (!histogramm.containsKey(simpleName)) {
				histogramm.put(simpleName, 0);
				bytype.put(simpleName, new ArrayList<>());
			}
			histogramm.put(simpleName, histogramm.get(simpleName) + 1);
			bytype.get(simpleName).add(violation);
		}
		final StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (final String key : histogramm.keySet()) {
			sb.append(String.format("%s : %d\n", key, histogramm.get(key)));
			for (final ConstraintViolation violation : bytype.get(key)) {
				sb.append("\n\t[");
				sb.append(violation.getAffectedEdges().stream().map(EMoflonFacade::formatEdge)
						.collect(Collectors.joining(",")));
				sb.append("]\n");
			}
			sb.append("\n");
		}
		sb.append("]");

		return sb.toString();
	}

	private static String formatEdge(final Edge edge) {
		final Edge link = edge;
		return String.format("%s (s=%s, w=%.3f, L1=%.3f)", link.getId(), link.getState().toString().charAt(0),
				link.getWeight(), link.getExpectedLifetime());
	}

	private Node getModelNodeForSimonstratorNode(final INodeID nodeId) {
		return this.simonstratorNodeToModelNode.get(nodeId);
	}

	private Edge getModelLinkForSimonstratorEdge(final IEdge simEdge) {
		return this.simonstratorEdgeToModelLink.get(simEdge.getId());
	}

	public IEdge getSimonstratorLinkForTopologyModelLink(final Edge edge) {
		return getGraph().getEdge(modelLinkToSimonstratorLink.get(edge));
	}

	public INodeID getSimonstratorNodeForTopologyModelNode(final Node node) {
		return modelNodeToSimonstratorNode.get(node);
	}

	private void establishNodeMapping(INode simonstratorNode, final Node modelNode) {
		this.simonstratorNodeToModelNode.put(simonstratorNode.getId(), modelNode);
		this.modelNodeToSimonstratorNode.put(modelNode, simonstratorNode.getId());
	}

	private void establishLinkMapping(final IEdge simonstratorEdge, final Edge modelLink) {
		this.simonstratorEdgeToModelLink.put(simonstratorEdge.getId(), modelLink);
		this.modelLinkToSimonstratorLink.put(modelLink, simonstratorEdge.getId());
	}

	private void removeLinkMapping(final EdgeID simonstratorEdgeId, final Edge modelLink) {
		this.modelLinkToSimonstratorLink.remove(modelLink);
		this.simonstratorEdgeToModelLink.remove(simonstratorEdgeId);
	}

	private void removeNodeMapping(final INodeID simonstratorNodeId, final Node modelNode) {
		this.modelNodeToSimonstratorNode.remove(modelNode);
		this.simonstratorNodeToModelNode.remove(simonstratorNodeId);
	}

	/**
	 * Returns the graph of this facade.
	 */
	public Topology getTopology() {
		return this.topology;
	}

	private void registerEMFListeners() {
		topology.eAdapters().clear();
		topology.eAdapters().add(new LinkActivationContentAdapter(this));
	}

	@Override
	public void connectOppositeEdges(IEdge fwdEdgePrototype, IEdge bwdEdgePrototype) {
		super.connectOppositeEdges(fwdEdgePrototype, bwdEdgePrototype);

		final Edge fwdModelLink = getModelLinkForSimonstratorEdge(fwdEdgePrototype);
		final Edge bwdModelLink = getModelLinkForSimonstratorEdge(bwdEdgePrototype);

		fwdModelLink.setReverseEdge(bwdModelLink);
		bwdModelLink.setReverseEdge(fwdModelLink);
	}

	@Override
	public void unclassifyAllLinks() {
		super.unclassifyAllLinks();
		for (final Edge edge : this.getTopology().getEdges()) {
			edge.setState(EdgeState.UNCLASSIFIED);
		}
	}

	public static String formatEdgeStateReport(final Topology graph) {
		final StringBuilder builder = new StringBuilder();
		final List<String> edgeIds = new ArrayList<>();
		final Set<String> processedIds = new HashSet<>();
		for (final Edge edge : graph.getEdges()) {
			edgeIds.add(edge.getId());
		}
		final Map<EdgeState, Integer> stateCounts = new HashMap<>();
		stateCounts.put(EdgeState.ACTIVE, 0);
		stateCounts.put(EdgeState.INACTIVE, 0);
		stateCounts.put(EdgeState.UNCLASSIFIED, 0);
		Collections.sort(edgeIds);

		for (final String id : edgeIds) {
			if (!processedIds.contains(id)) {
				final Edge link = graph.getEdgeById(id);
				EdgeState linkState = link.getState();
				builder.append(String.format("%6s [%.3f]", link.getId() + " : " + linkState.toString().substring(0, 1),
						link.getWeight()));
				processedIds.add(link.getId());
				stateCounts.put(linkState, stateCounts.get(linkState) + 1);

				if (link.getReverseEdge() != null) {
					Edge revLink = link.getReverseEdge();
					EdgeState revLinkState = revLink.getState();
					builder.append(String.format("%6s [%.3f]",
							revLink.getId() + " : " + revLinkState.toString().substring(0, 1), revLink.getWeight()));
					processedIds.add(revLink.getId());
					stateCounts.put(revLinkState, stateCounts.get(revLinkState) + 1);
				}

				builder.append("\n");
			}
		}

		builder.insert(0,
				String.format("#A : %d || #I : %d || #U : %d\n || Sum : %d", //
						stateCounts.get(EdgeState.ACTIVE), //
						stateCounts.get(EdgeState.INACTIVE), //
						stateCounts.get(EdgeState.UNCLASSIFIED), //
						stateCounts.get(EdgeState.ACTIVE) + stateCounts.get(EdgeState.INACTIVE)
								+ stateCounts.get(EdgeState.UNCLASSIFIED)//
				));

		return builder.toString().trim();

	}
}