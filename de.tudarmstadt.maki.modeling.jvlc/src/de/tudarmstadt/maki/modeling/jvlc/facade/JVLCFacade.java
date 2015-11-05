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
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.GraphElementProperty;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.tc.facade.IContextEventListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkStateListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
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
	private boolean isInsideContextEventSequence;

	public static IncrementalKTC getAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {
		switch (algorithmId) {
		case ID_KTC:
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		case IE_KTC:
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		case NULL_TC:
			return JvlcFactory.eINSTANCE.createNullkTC();
		default:
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
		}
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
	}

	@Override
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		this.algorithm = getAlgorithmForID(algorithmID);
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
		reader.read(this.topology, new FileInputStream(inputFile));
	}

	private void registerEWFListeners() {
		// TODO@rkluge: Sync manually - this will take too much time otherwise
		// graph.eAdapters().add(new AttributeValueSynchronizingContentAdapter());
		topology.eAdapters().clear();
		topology.eAdapters().add(new LinkActivationContentAdapter());
	}

	@Override
	public void run(final TopologyControlAlgorithmParamters parameters) {
		algorithm.setK((Double) parameters.get(KTCConstants.K));
		algorithm.run(this.topology);
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
	public INode addNode(final INodeID id, final double remainingEnergy) {

		final INode simNode = this.graph.createNode(id);
		simNode.setProperty(KTCConstants.REMAINING_ENERGY, remainingEnergy);

		final KTCNode ktcNode = this.topology.addKTCNode(id.valueAsString(), remainingEnergy);
		ktcNode.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, remainingEnergy);
		this.algorithm.handleNodeAddition(ktcNode);

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

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postNodeAttributeUpdated(simNode, property);
		}
	}

	@Override
	public IEdge addEdge(final INodeID source, final INodeID target, final double distance, final double requiredTransmissionPower) {
		final IEdge forwardEdge = Graphs.createDirectedEdge(source, target);
		final IEdge backwardEdge = Graphs.createDirectedEdge(target, source);
		forwardEdge.setProperty(KTCConstants.DISTANCE, distance);
		forwardEdge.setProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		backwardEdge.setProperty(KTCConstants.DISTANCE, distance);
		backwardEdge.setProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		if (!this.graph.containsEdge(forwardEdge)) {
			this.graph.addEdge(forwardEdge);
			this.graph.addEdge(backwardEdge);

			final KTCLink forwardKtcLink = addSymmetricKTCLink(forwardEdge.getId().valueAsString(), backwardEdge.getId().valueAsString(),
					this.nodeMappingSim2Jvlc.get(source), this.nodeMappingSim2Jvlc.get(target), distance, requiredTransmissionPower);
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
	public void removeEdge(final IEdge simEdge) {
		if (this.edgeMappingSim2Jvlc.containsKey(simEdge)) {
			final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
			final KTCLink reverseKTCLink = (KTCLink) ktcLink.getReverseEdge();
			final IEdge reverseSimEdge = this.edgeMappingJvlc2Sim.get(reverseKTCLink);
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

		for (final IContextEventListener contextEventListener : this.contextEventListeners) {
			contextEventListener.postEdgeAttributeUpdated(simEdge, property);
		}
	}

	@Override
	public void addLinkActivationListener(final ILinkStateListener listener) {
		this.linkActivationListeners.add(listener);
	}

	@Override
	public void addContextEventListener(final IContextEventListener listener) {
		this.contextEventListeners.add(listener);
	}

	@Override
	public void beginContextEventSequence() {
		this.isInsideContextEventSequence = true;
	}

	@Override
	public void endContextEventSequence() {
		this.isInsideContextEventSequence = false;
	}

	public KTCLink addSymmetricKTCLink(final String forwardEdgeId, final String backwardEdgeId, final KTCNode sourceNode, final KTCNode targetNode,
			final double distance, final double requiredTransmissionPower) {
		final KTCLink ktcLink = this.topology.addUndirectedKTCLink(forwardEdgeId, backwardEdgeId, sourceNode, targetNode, distance,
				requiredTransmissionPower);
		ktcLink.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, distance);
		ktcLink.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		ktcLink.getReverseEdge().setDoubleAttribute(AttributeNames.ATTR_DISTANCE, distance);
		ktcLink.getReverseEdge().setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);

		this.algorithm.handleLinkAddition(ktcLink);

		return ktcLink;
	}

	public <T> void updateNodeAttribute(final KTCNode ktcNode, final SiSType<T> property, final T value) {
		if (KTCConstants.REMAINING_ENERGY.equals(property)) {
			ktcNode.setRemainingEnergy((Double) value);
			ktcNode.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, (Double) value);
			this.algorithm.handleNodeAttributeModification(ktcNode);
		}
	}

	public <T> void updateLinkAttributeSymmetric(final KTCLink ktcLink, final SiSType<T> property, final T value) {
		updateLinkAttribute(ktcLink, property, value);
		updateLinkAttribute((KTCLink) ktcLink.getReverseEdge(), property, value);
	}

	public <T> void updateLinkAttribute(final KTCLink ktcLink, final SiSType<T> property, final T value) {
		if (ktcLink == null) {
			throw new NullPointerException();
		}
		if (KTCConstants.DISTANCE.equals(property)) {
			ktcLink.setDistance((Double) value);
			ktcLink.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, (Double) value);
			this.algorithm.handleLinkAttributeModification(ktcLink);
		} else if (KTCConstants.REQUIRED_TRANSMISSION_POWER.equals(property)) {
			ktcLink.setRequiredTransmissionPower((Double) value);
			ktcLink.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, (Double) value);
			this.algorithm.handleLinkAttributeModification(ktcLink);
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

	private class LinkActivationContentAdapter extends GraphContentAdapter {
		@Override
		protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
			super.edgeAttributeChanged(edge, attribute, oldValue);

			switch (attribute.getFeatureID()) {
			case JvlcPackage.KTC_LINK__STATE:
				for (final ILinkStateListener listener : linkActivationListeners) {
					if (LinkState.ACTIVE.equals(edge.eGet(attribute))) {
						final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
						listener.linkActivated(simEdge);
					} else if (LinkState.INACTIVE.equals(edge.eGet(attribute))) {
						final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
						listener.linkInactivated(simEdge);
					} else if (LinkState.UNCLASSIFIED.equals(edge.eGet(attribute))) {
						final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
						listener.linkUnclassified(simEdge);
					}
				}
				break;
			}
		}
	}

}
