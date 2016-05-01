package de.tudarmstadt.maki.modeling.jvlc.facade;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.EList;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.EdgeState;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.ConstraintViolation;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.ConstraintViolationReport;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.GraphConstraint;
import de.tudarmstadt.maki.modeling.jvlc.DistanceKTCConstraint;
import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.algorithm.AlgorithmHelper;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

public class JVLCFacade extends TopologyControlFacade_ImplBase {

	private final Topology topology;
	private IncrementalKTC algorithm;
	private final Map<INodeID, KTCNode> simonstratorNodeToModelNode;
	private final Map<KTCNode, INodeID> modelNodeToSimonstratorNode;
	private final Map<EdgeID, KTCLink> simonstratorEdgeToModelLink;
	private final Map<KTCLink, EdgeID> modelLinkToSimonstratorLink;
	private TopologyControlAlgorithmID algorithmID;
	private int constraintViolationCounter;
	private EdgeStateBasedConnectivityConstraint physicalConnectivityConstraint;
	private EdgeStateBasedConnectivityConstraint activeLinkConnectivityConstraint;
	private EdgeStateBasedConnectivityConstraint weakConnectivityConstraint;
	private GraphConstraint noUnclassifiedLinksConstraint;
	private DistanceKTCConstraint ktcConstraint;

	public JVLCFacade() {
		this.simonstratorNodeToModelNode = new HashMap<>();
		this.modelNodeToSimonstratorNode = new HashMap<>();
		this.simonstratorEdgeToModelLink = new HashMap<>();
		this.modelLinkToSimonstratorLink = new HashMap<>();
		this.topology = JvlcFactory.eINSTANCE.createTopology();
		this.constraintViolationCounter = 0;

		physicalConnectivityConstraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
		physicalConnectivityConstraint.getStates().add(EdgeState.ACTIVE);
		physicalConnectivityConstraint.getStates().add(EdgeState.INACTIVE);
		physicalConnectivityConstraint.getStates().add(EdgeState.UNCLASSIFIED);

		weakConnectivityConstraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
		weakConnectivityConstraint.getStates().add(EdgeState.ACTIVE);
		weakConnectivityConstraint.getStates().add(EdgeState.UNCLASSIFIED);

		activeLinkConnectivityConstraint = ConstraintsFactory.eINSTANCE.createEdgeStateBasedConnectivityConstraint();
		activeLinkConnectivityConstraint.getStates().add(EdgeState.ACTIVE);

		noUnclassifiedLinksConstraint = ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint();

		ktcConstraint = JvlcFactory.eINSTANCE.createDistanceKTCConstraint();
	}

	@Override
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		this.algorithm = AlgorithmHelper.createAlgorithmForID(algorithmID);
		this.algorithmID = algorithmID;
		this.registerEMFListeners();
	}

	@Override
	public Collection<String> getExpectedParameters() {
		return Arrays.asList(KTCConstants.K);
	}

	@Override
	public void run(final TopologyControlAlgorithmParamters parameters) {
		final Double k = (Double) parameters.get(KTCConstants.K);
		this.algorithm.setK(k);
		this.ktcConstraint.setK(k);
		this.algorithm.runOnTopology(this.topology);
	}

	/**
	 * Convenience method that is tailored to kTC.
	 */
	public void run(final double k) {
		this.run(TopologyControlAlgorithmParamters.create(KTCConstants.K, k));
	}

	@Override
	public INode addNode(INode prototype) {

		final INode simNode = super.addNode(prototype);

		final Double remainingEnergy;
		if (simNode.getProperty(KTCConstants.REMAINING_ENERGY) != null) {
			remainingEnergy = simNode.getProperty(KTCConstants.REMAINING_ENERGY);
		} else {
			remainingEnergy = Double.NaN;
		}

		final KTCNode ktcNode = this.topology.addKTCNode(simNode.getId().valueAsString(), remainingEnergy);

		this.algorithm.handleNodeAddition(ktcNode);

		this.establishNodeMapping(simNode, ktcNode);

		this.firePostNodeAdded(simNode);

		return simNode;
	}

	@Override
	public void removeNode(final INodeID nodeId) {
		if (this.simonstratorNodeToModelNode.containsKey(nodeId)) {
			for (final IEdge outgoingEdge : new ArrayList<>(this.simonstratorGraph.getOutgoingEdges(nodeId))) {
				removeEdge(outgoingEdge);
			}

			for (final IEdge incomingEdge : new ArrayList<>(this.simonstratorGraph.getIncomingEdges(nodeId))) {
				removeEdge(incomingEdge);
			}

			final INode removedNodeId = this.simonstratorGraph.getNode(nodeId);
			final KTCNode ktcNode = getModelNodeForSimonstratorNode(nodeId);

			firePreRemovedNode(removedNodeId);

			removeNodeMapping(nodeId, ktcNode);

			removeKTCNode(ktcNode);

			super.removeNode(nodeId);
		}
	}

	@Override
	public <T> void updateNodeAttribute(final INode simNode, final GraphElementProperty<T> property) {
		if (this.algorithmID.requiresUpdatesOfProperty(property)) {

			final KTCNode ktcNode = getModelNodeForSimonstratorNode(simNode.getId());

			this.updateNodeAttribute(ktcNode, property, simNode.getProperty(property));

			this.firePostNodeAttributeUpdated(simNode, property);

		}
	}

	@Override
	public IEdge addEdge(IEdge prototype) {
		final IEdge newEdge;
		if (!this.simonstratorGraph.containsEdge(prototype)) {
			newEdge = super.addEdge(prototype);

			Double distance = prototype.getProperty(KTCConstants.DISTANCE);
			if (distance == null)
				distance = Double.NaN;

			Double requiredTransmissionPower = prototype.getProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER);

			final KTCLink modelLink = this.topology.addKTCLink(prototype.getId().valueAsString(),
					getModelNodeForSimonstratorNode(prototype.fromId()),
					getModelNodeForSimonstratorNode(prototype.toId()), distance, requiredTransmissionPower);

			establishLinkMapping(newEdge, modelLink);

			firePostEdgeAdded(prototype);

		} else {
			newEdge = this.simonstratorGraph.getEdge(prototype.getId());
		}

		return newEdge;
	}

	@Override
	public void removeEdge(final IEdge simEdge) {
		if (this.simonstratorEdgeToModelLink.containsKey(simEdge)) {

			final KTCLink ktcLink = getModelLinkForSimonstratorEdge(simEdge);

			this.firePreEdgeRemoved(simEdge);

			this.removeLinkMapping(simEdge, ktcLink);

			this.removeKTCLink(ktcLink);

			super.removeEdge(simEdge);
		}
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge simEdge, final GraphElementProperty<T> property) {
		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			final KTCLink ktcLink = getModelLinkForSimonstratorEdge(simEdge);
			final T value = simEdge.getProperty(property);

			this.updateLinkAttribute(ktcLink, property, value);
			this.firePostEdgeAttributeUpdated(simEdge, property);
		}
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
		ktcConstraint.checkOnGraph(topology, report);

		ConstraintViolationReport tempReport = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		physicalConnectivityConstraint.checkOnGraph(this.topology, tempReport);
		if (tempReport.getViolations().size() == 0) {
			weakConnectivityConstraint.checkOnGraph(this.topology, report);
		}

		reportConstraintViolations(report);
	}

	@Override
	public void checkConstraintsAfterTopologyControl() {
		ConstraintViolationReport report = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();
		ConstraintViolationReport tempReport = ConstraintsFactory.eINSTANCE.createConstraintViolationReport();

		ktcConstraint.checkOnGraph(topology, report);
		noUnclassifiedLinksConstraint.checkOnGraph(topology, report);
		physicalConnectivityConstraint.checkOnGraph(this.topology, tempReport);
		if (tempReport.getViolations().size() == 0) {
			activeLinkConnectivityConstraint.checkOnGraph(this.topology, report);
		}

		reportConstraintViolations(report);
	}

	public KTCLink addSymmetricKTCLink(final String forwardEdgeId, final String backwardEdgeId,
			final KTCNode sourceNode, final KTCNode targetNode, final double distance,
			final double requiredTransmissionPower) {
		final KTCLink ktcLink = this.topology.addUndirectedKTCLink(forwardEdgeId, backwardEdgeId, sourceNode,
				targetNode, distance, requiredTransmissionPower);

		this.algorithm.handleLinkAddition(ktcLink);

		return ktcLink;
	}

	public <T> void updateNodeAttribute(final KTCNode ktcNode, final GraphElementProperty<T> property, final T value) {
		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			if (KTCConstants.REMAINING_ENERGY.equals(property)) {
				ktcNode.setRemainingEnergy((Double) value);
				this.algorithm.handleNodeAttributeModification(ktcNode);
			}
		}
	}

	/**
	 * Calls {@link #updateLinkAttribute(KTCLink, GraphElementProperty, Object)}
	 * for the given link and its reverse link, setting the same value for the
	 * given property on both links.
	 */
	public <T> void updateLinkAttributeSymmetric(final KTCLink ktcLink, final GraphElementProperty<T> property,
			final T value) {
		updateLinkAttribute(ktcLink, property, value);
		updateLinkAttribute((KTCLink) ktcLink.getReverseEdge(), property, value);
	}

	/**
	 * Sets the property of the given link to the given value.
	 */
	public <T> void updateLinkAttribute(final KTCLink ktcLink, final GraphElementProperty<T> property, final T value) {
		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			if (ktcLink == null) {
				throw new NullPointerException();
			}
			if (KTCConstants.DISTANCE.equals(property)) {
				ktcLink.setDistance((Double) value);
				this.algorithm.handleLinkAttributeModification(ktcLink);
			} else if (KTCConstants.REQUIRED_TRANSMISSION_POWER.equals(property)) {
				ktcLink.setRequiredTransmissionPower((Double) value);
				this.algorithm.handleLinkAttributeModification(ktcLink);
			} else if (KTCConstants.EDGE_STATE.equals(property)) {
				ktcLink.setState(de.tudarmstadt.maki.modeling.graphmodel.EdgeState.UNCLASSIFIED);
				this.algorithm.handleLinkAttributeModification(ktcLink);
			}
		}
	}

	public void removeKTCNode(final KTCNode ktcNode) {
		this.algorithm.handleNodeDeletion(ktcNode);
		this.topology.removeNode(ktcNode);
	}

	public void removeKTCLink(final KTCLink ktcLink) {
		this.algorithm.handleLinkDeletion(ktcLink);

		final Edge reverseEdge = ktcLink.getReverseEdge();
		this.topology.removeEdge(ktcLink);
		this.topology.removeEdge(reverseEdge);
	}

	private void reportConstraintViolations(ConstraintViolationReport report) {
		final EList<ConstraintViolation> violations = report.getViolations();
		if (!violations.isEmpty()) {
			Monitor.log(getClass(), Level.ERROR, "%d constraint violations detected: %s", violations.size(),
					violations);
			this.constraintViolationCounter += violations.size();
		} else {
			Monitor.log(getClass(), Level.DEBUG, "No constraint violations found");
		}
	}

	private KTCNode getModelNodeForSimonstratorNode(final INodeID nodeId) {
		return this.simonstratorNodeToModelNode.get(nodeId);
	}

	private KTCLink getModelLinkForSimonstratorEdge(final IEdge simEdge) {
		return this.simonstratorEdgeToModelLink.get(simEdge.getId());
	}

	public IEdge getSimonstratorLinkForTopologyModelLink(final KTCLink edge) {
		return getGraph().getEdge(modelLinkToSimonstratorLink.get(edge));
	}

	public INodeID getSimonstratorNodeForTopologyModelNode(final Node node) {
		return modelNodeToSimonstratorNode.get(node);
	}

	private void establishNodeMapping(INode simonstratorNode, final KTCNode modelNode) {
		this.simonstratorNodeToModelNode.put(simonstratorNode.getId(), modelNode);
		this.modelNodeToSimonstratorNode.put(modelNode, simonstratorNode.getId());
	}

	private void establishLinkMapping(final IEdge simonstratorEdge, final KTCLink modelLink) {
		this.simonstratorEdgeToModelLink.put(simonstratorEdge.getId(), modelLink);
		this.modelLinkToSimonstratorLink.put(modelLink, simonstratorEdge.getId());
	}

	private void removeLinkMapping(final IEdge simonstratorEdge, final KTCLink modelLink) {
		this.modelLinkToSimonstratorLink.remove(modelLink.getId());
		this.simonstratorEdgeToModelLink.remove(simonstratorEdge);
	}

	private void removeNodeMapping(final INodeID simonstratorNodeId, final KTCNode modelNode) {
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

		final KTCLink fwdModelLink = getModelLinkForSimonstratorEdge(fwdEdgePrototype);
		final KTCLink bwdModelLink = getModelLinkForSimonstratorEdge(bwdEdgePrototype);

		fwdModelLink.setReverseEdge(bwdModelLink);
		bwdModelLink.setReverseEdge(fwdModelLink);
	}

}
