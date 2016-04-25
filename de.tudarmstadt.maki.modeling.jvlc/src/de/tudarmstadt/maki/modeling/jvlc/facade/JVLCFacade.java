package de.tudarmstadt.maki.modeling.jvlc.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.constraints.CollectionConstraintViolationEnumerator;
import de.tudarmstadt.maki.modeling.jvlc.io.JvlcTopologyFromTextFileReader;
import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlFacade_ImplBase;
import de.tudarmstadt.maki.simonstrator.tc.ktc.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

public class JVLCFacade extends TopologyControlFacade_ImplBase {

	private final Topology topology;
	private IncrementalKTC algorithm;
	private final Map<INodeID, KTCNode> simonstratorNodeToModelNode;
	private final Map<KTCNode, INodeID> modelNodeToSimonstratorNode;
	private final Map<IEdge, KTCLink> simonstratorEdgeToModelLink;
	private final Map<KTCLink, IEdge> modelLinkToSimonstratorLink;
	private TopologyControlAlgorithmID algorithmID;
	private int constraintViolationCounter;

	public static IncrementalKTC createAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {

		if (KTCConstants.ID_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		else if (KTCConstants.IE_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		else if (KTCConstants.NULL_TC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createNullkTC();
		else if (KTCConstants.D_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createDistanceKTC();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);

	}

	/**
	 * Default constructor.
	 */
	public JVLCFacade() {
		this.simonstratorNodeToModelNode = new HashMap<>();
		this.modelNodeToSimonstratorNode = new HashMap<>();
		this.simonstratorEdgeToModelLink = new HashMap<>();
		this.modelLinkToSimonstratorLink = new HashMap<>();
		this.topology = JvlcFactory.eINSTANCE.createTopology();
		this.constraintViolationCounter = 0;
	}

	@Override
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		this.algorithm = createAlgorithmForID(algorithmID);
		this.algorithmID = algorithmID;
		this.registerEMFListeners();
	}

	@Override
	public void run(final TopologyControlAlgorithmParamters parameters) {
		algorithm.setK((Double) parameters.get(KTCConstants.K));
		algorithm.runOnTopology(this.topology);

		this.checkConstraintsAfterTopologyControl();
	}

	/**
	 * Convenience method that is tailored to kTC.
	 */
	public void run(final double k) {
		this.run(TopologyControlAlgorithmParamters.create(KTCConstants.K, k));
	}

	@Override
	public INode addNode(INode prototype) {
		// TODO@rkluge Implement me
		throw new UnsupportedOperationException("Not fully implemented yet");
		// final INode simNode = super.addNode(prototype);
		//
		// final KTCNode ktcNode = this.addKTCNode(simNode);
		// this.algorithm.handleNodeAddition(ktcNode);
		//
		// this.simonstratorNodeToModelNode.put(simNode.getId(), ktcNode);
		// this.modelNodeToSimonstratorNode.put(ktcNode, simNode.getId());
		//
		// for (final IContextEventListener contextEventListener :
		// this.contextEventListeners) {
		// contextEventListener.postNodeAdded(simNode);
		// }
		// this.checkConstraintsAfterContextEvent();
		//
		// return simNode;
	}

	@Override
	public INode addNode(final INodeID id, final double remainingEnergy) {

		final INode simNode = this.simonstratorGraph.createNode(id);
		simNode.setProperty(KTCConstants.REMAINING_ENERGY, remainingEnergy);
		this.simonstratorGraph.addNode(simNode);

		final KTCNode ktcNode = this.topology.addKTCNode(id.valueAsString(), remainingEnergy);
		this.algorithm.handleNodeAddition(ktcNode);

		this.simonstratorNodeToModelNode.put(simNode.getId(), ktcNode);
		this.modelNodeToSimonstratorNode.put(ktcNode, simNode.getId());

		this.firePostNodeAdded(simNode);

		this.checkConstraintsAfterContextEvent();

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
			firePreRemovedNode(removedNodeId);
			final KTCNode ktcNode = this.simonstratorNodeToModelNode.get(nodeId);
			this.modelNodeToSimonstratorNode.remove(ktcNode);
			this.simonstratorNodeToModelNode.remove(nodeId);
			this.simonstratorGraph.removeNode(nodeId);
			removeKTCNode(ktcNode);

			this.checkConstraintsAfterContextEvent();
		}
	}

	@Override
	public <T> void updateNodeAttribute(final INode simNode, final GraphElementProperty<T> property) {
		final KTCNode ktcNode = this.simonstratorNodeToModelNode.get(simNode.getId());
		this.updateNodeAttribute(ktcNode, property, simNode.getProperty(property));

		if (this.algorithmID.requiresUpdatesOfProperty(property)) {

			firePostNodeAttributeUpdated(simNode, property);

			this.checkConstraintsAfterContextEvent();
		}
	}

	@Override
	public IEdge addEdge(IEdge prototype) {

		// TODO@rkluge implement me
		throw new UnsupportedOperationException("Not implemented yet");
		// final IEdge forwardEdge =
		// Graphs.createDirectedEdge(prototype.getId(), prototype.fromId(),
		// prototype.toId());
		// forwardEdge.addPropertiesFrom(prototype);
		//
		// this.graph.addEdge(forwardEdge);
		//
		// final KTCLink forwardKtcLink = addKTCLink(forwardEdge);
		//
		// this.simonstratorEdgeToModelLink.put(forwardEdge, forwardKtcLink);
		// this.modelLinkToSimonstratorLink.put(forwardKtcLink, forwardEdge);
		//
		// for (final IContextEventListener contextEventListener :
		// this.contextEventListeners) {
		// contextEventListener.postEdgeAdded(forwardEdge);
		// }
		// this.checkConstraintsAfterContextEvent();
		//
		// return forwardEdge;
	}

	@Override
	public IEdge addEdge(final INodeID source, final INodeID target, final double distance,
			final double requiredTransmissionPower) {
		final IEdge newEdge = addBidirectionalEdge(EdgeID.get(source, target), EdgeID.get(target, source), source,
				target, distance, requiredTransmissionPower);

		this.checkConstraintsAfterContextEvent();

		return newEdge;
	}

	@Override
	public void removeEdge(final IEdge simEdge) {
		if (this.simonstratorEdgeToModelLink.containsKey(simEdge)) {

			final KTCLink ktcLink = this.simonstratorEdgeToModelLink.get(simEdge);
			final KTCLink reverseKTCLink = (KTCLink) ktcLink.getReverseEdge();
			final IEdge reverseSimEdge = this.modelLinkToSimonstratorLink.get(reverseKTCLink);

			firePreEdgeRemoved(reverseSimEdge);
			firePreEdgeRemoved(simEdge);

			this.modelLinkToSimonstratorLink.remove(ktcLink);
			this.modelLinkToSimonstratorLink.remove(reverseKTCLink);
			this.simonstratorEdgeToModelLink.remove(simEdge);
			this.simonstratorEdgeToModelLink.remove(reverseSimEdge);
			this.simonstratorGraph.removeEdge(simEdge);
			this.simonstratorGraph.removeEdge(reverseSimEdge);
			removeKTCLink(ktcLink);
		}
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge simEdge, final GraphElementProperty<T> property) {
		final KTCLink ktcLink = this.simonstratorEdgeToModelLink.get(simEdge);
		final T value = simEdge.getProperty(property);
		updateLinkAttribute(ktcLink, property, value);

		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			firePostEdgeAttributeUpdated(simEdge, property);
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
		CollectionConstraintViolationEnumerator violationEnumerator = new CollectionConstraintViolationEnumerator();
		violationEnumerator.checkPredicate(this.topology, algorithm);
		Collection<String> violationsList = violationEnumerator.getConstraintViolationList();
		if (!violationsList.isEmpty()) {
			Monitor.log(getClass(), Level.ERROR, "%d constraint violations detected: %s", violationsList.size(),
					violationsList);
			this.constraintViolationCounter += violationsList.size();
		} else {
			Monitor.log(getClass(), Level.DEBUG, "No constraint violations found");
		}
	}

	@Override
	public void checkConstraintsAfterTopologyControl() {
		final CollectionConstraintViolationEnumerator violationEnumerator = new CollectionConstraintViolationEnumerator();
		violationEnumerator.checkPredicate(this.topology, this.algorithm);
		violationEnumerator.checkThatNoUnclassifiedLinksExist(this.topology);
		violationEnumerator.checkConnectivityViaActiveLinks(this.topology);

		Collection<String> violationsList = violationEnumerator.getConstraintViolationList();
		if (!violationsList.isEmpty()) {
			Monitor.log(getClass(), Level.ERROR, "%d constraint violations detected: %s", violationsList.size(),
					violationsList);
			this.constraintViolationCounter += violationsList.size();
		} else {
			Monitor.log(getClass(), Level.DEBUG, "No constraint violations found");
		}
	}

	public IEdge addBidirectionalEdge(final EdgeID forwardEdgeId, final EdgeID backwardEdgeId, final INodeID source,
			final INodeID target, final double distance, final double requiredTransmissionPower) {
		final IEdge forwardEdge = Graphs.createDirectedEdge(forwardEdgeId, source, target);
		final IEdge backwardEdge = Graphs.createDirectedEdge(backwardEdgeId, target, source);

		forwardEdge.setProperty(KTCConstants.DISTANCE, distance);
		forwardEdge.setProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		forwardEdge.setProperty(GenericGraphElementProperties.REVERSE_EDGE, backwardEdge);
		forwardEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.UNCLASSIFIED);

		backwardEdge.setProperty(KTCConstants.DISTANCE, distance);
		backwardEdge.setProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		backwardEdge.setProperty(GenericGraphElementProperties.REVERSE_EDGE, forwardEdge);
		backwardEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.UNCLASSIFIED);

		if (!this.simonstratorGraph.containsEdge(forwardEdge)) {
			this.simonstratorGraph.addEdge(forwardEdge);
			this.simonstratorGraph.addEdge(backwardEdge);

			final KTCLink forwardKtcLink = addSymmetricKTCLink(forwardEdge.getId().valueAsString(),
					backwardEdge.getId().valueAsString(), this.simonstratorNodeToModelNode.get(source),
					this.simonstratorNodeToModelNode.get(target), distance, requiredTransmissionPower);
			final KTCLink backwardKtcLink = (KTCLink) forwardKtcLink.getReverseEdge();

			this.simonstratorEdgeToModelLink.put(forwardEdge, forwardKtcLink);
			this.modelLinkToSimonstratorLink.put(forwardKtcLink, forwardEdge);
			this.simonstratorEdgeToModelLink.put(backwardEdge, backwardKtcLink);
			this.modelLinkToSimonstratorLink.put(backwardKtcLink, backwardEdge);

			firePostEdgeAdded(forwardEdge);
			firePostEdgeAdded(backwardEdge);

			return forwardEdge;
		} else {
			return simonstratorGraph.getEdge(forwardEdge.getId());
		}
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
				ktcLink.setState(LinkState.UNCLASSIFIED);
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

	public IEdge getSimonstratorLinkForTopologyModelLink(final Edge edge) {
		return modelLinkToSimonstratorLink.get(edge);
	}

	public INodeID getSimonstratorNodeForTopologyModelNode(final Node node) {
		return modelNodeToSimonstratorNode.get(node);
	}

	/**
	 * Returns the graph of this facade.
	 */
	public Topology getTopology() {
		return this.topology;
	}

	public void loadAndSetTopologyFromFile(final String inputFilename) throws FileNotFoundException {
		loadAndSetTopologyFromFile(new File(inputFilename));
	}

	public void loadAndSetTopologyFromFile(final File inputFile) throws FileNotFoundException {
		if (!this.topology.getNodes().isEmpty()) {
			throw new IllegalStateException("This method may only be called if the stored topology is still empty");
		}
		final JvlcTopologyFromTextFileReader reader = new JvlcTopologyFromTextFileReader();
		reader.read(this, new FileInputStream(inputFile));
	}

	private void registerEMFListeners() {
		topology.eAdapters().clear();
		topology.eAdapters().add(new LinkActivationContentAdapter(this));
	}

}
