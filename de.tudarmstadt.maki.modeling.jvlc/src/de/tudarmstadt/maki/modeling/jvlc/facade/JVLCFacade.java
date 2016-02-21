package de.tudarmstadt.maki.modeling.jvlc.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.AttributeNames;
import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.JvlcPackage;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.io.JvlcTopologyFromTextFileReader;
import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.facade.IContextEventListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.ktc.EdgeState;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

public class JVLCFacade implements ITopologyControlFacade {

	private final Topology topology;
	private IncrementalKTC algorithm;
	private final Graph graph;
	private final List<ILinkStateListener> linkActivationListeners;
	private final List<IContextEventListener> contextEventListeners;
	private final Map<INodeID, KTCNode> nodeMappingSim2Jvlc;
	private final Map<KTCNode, INodeID> nodeMappingJvlc2Sim;
	private final Map<IEdge, KTCLink> edgeMappingSim2Jvlc;
	private final Map<KTCLink, IEdge> edgeMappingJvlc2Sim;
	private TopologyControlAlgorithmID algorithmID;
	private boolean isInsideContextEventSequence;
	private final List<Runnable> deferredContextEvents;

	public static IncrementalKTC getAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {

		if (KTCConstants.ID_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		else if (KTCConstants.IE_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		else if (KTCConstants.NULL_TC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createNullkTC();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);

	}

	/**
	 * Default constructor.
	 */
	public JVLCFacade() {
		this.linkActivationListeners = new ArrayList<>();
		this.contextEventListeners = new ArrayList<>();
		this.nodeMappingSim2Jvlc = new HashMap<>();
		this.nodeMappingJvlc2Sim = new HashMap<>();
		this.edgeMappingSim2Jvlc = new HashMap<>();
		this.edgeMappingJvlc2Sim = new HashMap<>();
		this.topology = JvlcFactory.eINSTANCE.createTopology();
		this.graph = Graphs.createGraph();
		this.isInsideContextEventSequence = false;
		this.deferredContextEvents = new ArrayList<>();
	}

	@Override
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		this.algorithm = getAlgorithmForID(algorithmID);
		this.algorithmID = algorithmID;
		this.registerEWFListeners();
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

	private void registerEWFListeners() {
		topology.eAdapters().clear();
		topology.eAdapters().add(new LinkActivationContentAdapter());
	}

	@Override
	public void run(final TopologyControlAlgorithmParamters parameters) {
		algorithm.setK((Double) parameters.get(KTCConstants.K));
		algorithm.runOnTopology(this.topology);
	}

	/**
	 * Convenience method that is tailored to kTC.
	 */
	public void run(final double k) {
		this.run(TopologyControlAlgorithmParamters.create(KTCConstants.K, k));
	}

	@Override
	public Graph getGraph() {
		return this.graph;
	}

	@Override
	public INode addNode(INode prototype) {
		final INode simNode = this.graph.createNode(prototype.getId());
		simNode.addPropertiesFrom(prototype);
		this.graph.addNode(simNode);

		final KTCNode ktcNode = this.addKTCNode(simNode);
		if (!isInsideContextEventSequence) {
			this.algorithm.handleNodeAddition(ktcNode);
		} else {
			this.deferredContextEvents.add(new Runnable() {
				@Override
				public void run() {
					JVLCFacade.this.algorithm.handleNodeAddition(ktcNode);
				}
			});
		}

		this.nodeMappingSim2Jvlc.put(simNode.getId(), ktcNode);
		this.nodeMappingJvlc2Sim.put(ktcNode, simNode.getId());

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postNodeAdded(simNode);
		}

		return simNode;
	}

	private KTCNode addKTCNode(INode simNode) {
		// TODO@rkluge: Implement me
		return null;
	}

	@Override
	public INode addNode(final INodeID id, final double remainingEnergy) {

		final INode simNode = this.graph.createNode(id);
		simNode.setProperty(KTCConstants.REMAINING_ENERGY, remainingEnergy);
		this.graph.addNode(simNode);

		final KTCNode ktcNode = this.topology.addKTCNode(id.valueAsString(), remainingEnergy);
		ktcNode.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, remainingEnergy);
		if (!isInsideContextEventSequence) {
			this.algorithm.handleNodeAddition(ktcNode);
		} else {
			this.deferredContextEvents.add(new Runnable() {
				@Override
				public void run() {
					JVLCFacade.this.algorithm.handleNodeAddition(ktcNode);
				}
			});
		}

		this.nodeMappingSim2Jvlc.put(simNode.getId(), ktcNode);
		this.nodeMappingJvlc2Sim.put(ktcNode, simNode.getId());

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postNodeAdded(simNode);
		}

		return simNode;
	}

	@Override
	public void removeNode(final INodeID nodeId) {
		if (this.nodeMappingSim2Jvlc.containsKey(nodeId)) {
			for (final IEdge outgoingEdge : new ArrayList<>(this.graph.getOutgoingEdges(nodeId))) {
				removeEdge(outgoingEdge);
			}
			for (final IEdge incomingEdge : new ArrayList<>(this.graph.getIncomingEdges(nodeId))) {
				removeEdge(incomingEdge);
			}

			for (final IContextEventListener contextEventListener : this.contextEventListeners) {
				contextEventListener.preNodeRemoved(this.graph.getNode(nodeId));
			}
			final KTCNode ktcNode = this.nodeMappingSim2Jvlc.get(nodeId);
			this.nodeMappingJvlc2Sim.remove(ktcNode);
			this.nodeMappingSim2Jvlc.remove(nodeId);
			this.graph.removeNode(nodeId);
			removeKTCNode(ktcNode);
		}
	}

	@Override
	public <T> void updateNodeAttribute(final INode simNode, final GraphElementProperty<T> property) {
		final KTCNode ktcNode = this.nodeMappingSim2Jvlc.get(simNode.getId());
		this.updateNodeAttribute(ktcNode, property, simNode.getProperty(property));

		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			for (final IContextEventListener contextEventListener : this.contextEventListeners) {
				contextEventListener.postNodeAttributeUpdated(simNode, property);
			}
		}
	}

	public IEdge addEdge(final EdgeID forwardEdgeId, final EdgeID backwardEdgeId, final INodeID source,
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

		if (!this.graph.containsEdge(forwardEdge)) {
			this.graph.addEdge(forwardEdge);
			this.graph.addEdge(backwardEdge);

			final KTCLink forwardKtcLink = addSymmetricKTCLink(forwardEdge.getId().valueAsString(),
					backwardEdge.getId().valueAsString(), this.nodeMappingSim2Jvlc.get(source),
					this.nodeMappingSim2Jvlc.get(target), distance, requiredTransmissionPower);
			final KTCLink backwardKtcLink = (KTCLink) forwardKtcLink.getReverseEdge();

			this.edgeMappingSim2Jvlc.put(forwardEdge, forwardKtcLink);
			this.edgeMappingJvlc2Sim.put(forwardKtcLink, forwardEdge);
			this.edgeMappingSim2Jvlc.put(backwardEdge, backwardKtcLink);
			this.edgeMappingJvlc2Sim.put(backwardKtcLink, backwardEdge);

			for (final IContextEventListener contextEventListener : this.contextEventListeners) {
				contextEventListener.postEdgeAdded(forwardEdge);
				contextEventListener.postEdgeAdded(backwardEdge);
			}

			return forwardEdge;
		} else {
			return graph.getEdge(forwardEdge.getId());
		}
	}

	@Override
	public IEdge addEdge(IEdge prototype) {

		final IEdge forwardEdge = Graphs.createDirectedEdge(prototype.getId(), prototype.fromId(), prototype.toId());
		forwardEdge.addPropertiesFrom(prototype);

		this.graph.addEdge(forwardEdge);

		final KTCLink forwardKtcLink = addKTCLink(forwardEdge);

		this.edgeMappingSim2Jvlc.put(forwardEdge, forwardKtcLink);
		this.edgeMappingJvlc2Sim.put(forwardKtcLink, forwardEdge);

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postEdgeAdded(forwardEdge);
		}

		return forwardEdge;
	}

	@Override
	public IEdge addEdge(final INodeID source, final INodeID target, final double distance,
			final double requiredTransmissionPower) {
		return addEdge(EdgeID.get(source, target), EdgeID.get(target, source), source, target, distance,
				requiredTransmissionPower);
	}

	@Override
	public void removeEdge(final IEdge simEdge) {
		if (this.edgeMappingSim2Jvlc.containsKey(simEdge)) {

			final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
			final KTCLink reverseKTCLink = (KTCLink) ktcLink.getReverseEdge();
			final IEdge reverseSimEdge = this.edgeMappingJvlc2Sim.get(reverseKTCLink);
			for (final IContextEventListener contextEventListener : this.contextEventListeners) {
				contextEventListener.preEdgeRemoved(simEdge);
				contextEventListener.preEdgeRemoved(reverseSimEdge);
			}
			this.edgeMappingJvlc2Sim.remove(ktcLink);
			this.edgeMappingJvlc2Sim.remove(reverseKTCLink);
			this.edgeMappingSim2Jvlc.remove(simEdge);
			this.edgeMappingSim2Jvlc.remove(reverseSimEdge);
			this.graph.removeEdge(simEdge);
			this.graph.removeEdge(reverseSimEdge);
			removeKTCLink(ktcLink);
		}
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge simEdge, final GraphElementProperty<T> property) {
		final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
		final T value = simEdge.getProperty(property);
		updateLinkAttribute(ktcLink, property, value);

		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			for (final IContextEventListener contextEventListener : this.contextEventListeners) {
				contextEventListener.postEdgeAttributeUpdated(simEdge, property);
			}
		}
	}

	@Override
	public void addLinkStateListener(final ILinkStateListener listener) {
		this.linkActivationListeners.add(listener);
	}

	@Override
	public void removeLinkStateListener(final ILinkStateListener listener) {
		this.linkActivationListeners.remove(listener);
	}

	@Override
	public void addContextEventListener(final IContextEventListener listener) {
		this.contextEventListeners.add(listener);
	}

	@Override
	public void removeContextEventListener(final IContextEventListener listener) {
		this.contextEventListeners.remove(listener);
	}

	@Override
	public void beginContextEventSequence() {
		// TODO@rkluge This does not work properly because the affected elements
		// are already removed
		this.isInsideContextEventSequence = false;
	}

	@Override
	public void endContextEventSequence() {
		this.isInsideContextEventSequence = false;
		for (final Runnable runnable : this.deferredContextEvents) {
			runnable.run();
		}
		this.deferredContextEvents.clear();
	}

	private KTCLink addKTCLink(IEdge forwardEdge) {
		// TODO@rkluge: Implement me
		return null;
	}

	public KTCLink addSymmetricKTCLink(final String forwardEdgeId, final String backwardEdgeId,
			final KTCNode sourceNode, final KTCNode targetNode, final double distance,
			final double requiredTransmissionPower) {
		final KTCLink ktcLink = this.topology.addUndirectedKTCLink(forwardEdgeId, backwardEdgeId, sourceNode,
				targetNode, distance, requiredTransmissionPower);
		ktcLink.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, distance);
		ktcLink.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		ktcLink.getReverseEdge().setDoubleAttribute(AttributeNames.ATTR_DISTANCE, distance);
		ktcLink.getReverseEdge().setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER,
				requiredTransmissionPower);

		if (!isInsideContextEventSequence) {
			this.algorithm.handleLinkAddition(ktcLink);
		} else {
			this.deferredContextEvents.add(new Runnable() {
				@Override
				public void run() {
					JVLCFacade.this.algorithm.handleLinkAddition(ktcLink);
				}
			});
		}

		return ktcLink;
	}

	public <T> void updateNodeAttribute(final KTCNode ktcNode, final GraphElementProperty<T> property, final T value) {
		if (this.algorithmID.requiresUpdatesOfProperty(property)) {
			if (KTCConstants.REMAINING_ENERGY.equals(property)) {
				ktcNode.setRemainingEnergy((Double) value);
				ktcNode.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, (Double) value);
				if (!isInsideContextEventSequence) {
					this.algorithm.handleNodeAttributeModification(ktcNode);
				} else {
					this.deferredContextEvents.add(new Runnable() {
						@Override
						public void run() {
							JVLCFacade.this.algorithm.handleNodeAttributeModification(ktcNode);
						}
					});
				}
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
				ktcLink.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, (Double) value);
				if (!isInsideContextEventSequence) {
					this.algorithm.handleLinkAttributeModification(ktcLink);
				} else {
					this.deferredContextEvents.add(new Runnable() {
						@Override
						public void run() {
							JVLCFacade.this.algorithm.handleLinkAttributeModification(ktcLink);
						}
					});
				}
			} else if (KTCConstants.REQUIRED_TRANSMISSION_POWER.equals(property)) {
				ktcLink.setRequiredTransmissionPower((Double) value);
				ktcLink.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, (Double) value);
				if (!isInsideContextEventSequence) {
					this.algorithm.handleLinkAttributeModification(ktcLink);
				} else {
					this.deferredContextEvents.add(new Runnable() {

						@Override
						public void run() {
							JVLCFacade.this.algorithm.handleLinkAttributeModification(ktcLink);
						}

					});
				}
			}
		}
	}

	public void removeKTCNode(final KTCNode ktcNode) {
		if (!isInsideContextEventSequence) {
			this.algorithm.handleNodeDeletion(ktcNode);
		} else {
			this.deferredContextEvents.add(new Runnable() {
				@Override
				public void run() {
					JVLCFacade.this.algorithm.handleNodeDeletion(ktcNode);
				}
			});
		}
		this.topology.removeNode(ktcNode);
	}

	public void removeKTCLink(final KTCLink ktcLink) {
		if (!isInsideContextEventSequence) {
			this.algorithm.handleLinkDeletion(ktcLink);
		} else {
			this.deferredContextEvents.add(new Runnable() {
				@Override
				public void run() {
					JVLCFacade.this.algorithm.handleLinkDeletion(ktcLink);
				}
			});
		}

		final Edge reverseEdge = ktcLink.getReverseEdge();
		this.topology.removeEdge(ktcLink);
		this.topology.removeEdge(reverseEdge);
	}

	/**
	 * This content adapter listens for link state modifications and notifies
	 * the registered {@link ILinkStateListener}s.
	 */
	private class LinkActivationContentAdapter extends GraphContentAdapter {
		@Override
		protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
			super.edgeAttributeChanged(edge, attribute, oldValue);

			final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
			// We may be in the initialization phase - no events should be
			// triggered here.
			if (simEdge == null) {
				return;
			}

			switch (attribute.getFeatureID()) {
			case JvlcPackage.KTC_LINK__STATE:
				for (final ILinkStateListener listener : linkActivationListeners) {
					if (LinkState.ACTIVE.equals(edge.eGet(attribute))) {
						simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.ACTIVE);
						listener.linkActivated(simEdge);
					} else if (LinkState.INACTIVE.equals(edge.eGet(attribute))) {
						simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.INACTIVE);
						listener.linkInactivated(simEdge);
					} else if (LinkState.UNCLASSIFIED.equals(edge.eGet(attribute))) {
						simEdge.setProperty(KTCConstants.EDGE_STATE, EdgeState.UNCLASSIFIED);
						listener.linkUnclassified(simEdge);
					}
				}
				break;
			}
		}
	}

}
