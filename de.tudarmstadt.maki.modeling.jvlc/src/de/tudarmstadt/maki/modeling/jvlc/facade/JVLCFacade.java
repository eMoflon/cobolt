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
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.facade.ILinkActivationListener;
import de.tudarmstadt.maki.simonstrator.tc.facade.ITopologyControlFacade;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmParamters;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

public class JVLCFacade implements ITopologyControlFacade {

	private Topology topology;
	private IncrementalKTC algorithm;
	private Graph graph;
	private final List<ILinkActivationListener> linkActivationListeners;
	private final Map<INodeID, KTCNode> nodeMappingSim2Jvlc;
	private final Map<KTCNode, INodeID> nodeMappingJvlc2Sim;
	private final Map<IEdge, KTCLink> edgeMappingSim2Jvlc;
	private final Map<KTCLink, IEdge> edgeMappingJvlc2Sim;

	public static IncrementalKTC getAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {
		switch (algorithmId) {
		case ID_KTC:
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		case IE_KTC:
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		default:
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
		}
	}

	/**
	 * Default constructor.
	 */
	public JVLCFacade() {
		this.linkActivationListeners = new ArrayList<>();
		this.nodeMappingSim2Jvlc = new HashMap<>();
		this.nodeMappingJvlc2Sim = new HashMap<>();
		this.edgeMappingSim2Jvlc = new HashMap<>();
		this.edgeMappingJvlc2Sim = new HashMap<>();
		this.intializeGraph();
	}

	@Override
	public void configureAlgorithm(final TopologyControlAlgorithmID algorithmID) {
		this.algorithm = getAlgorithmForID(algorithmID);
		this.registerListeners();
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

	private void registerListeners() {
		// TODO@rkluge: Sync manually - this will take too much time otherwise
		// graph.eAdapters().add(new AttributeValueSynchronizingContentAdapter());
		topology.eAdapters().clear();
		topology.eAdapters().add(new LinkActivationContentAdapter());
		// topology.eAdapters().add(new ContextEventHandlingAdapter(this.algorithm));
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
	public Graph intializeGraph() {
		this.topology = JvlcFactory.eINSTANCE.createTopology();
		this.graph = Graphs.createGraph();
		return this.graph;
	}

	@Override
	public INode addNode(final INodeID id, final double remainingEnergy) {
		final INode simNode = this.graph.createNode(id);
		final KTCNode ktcNode = this.topology.addKTCNode(id.valueAsString(), remainingEnergy);
		this.nodeMappingSim2Jvlc.put(simNode.getId(), ktcNode);
		this.nodeMappingJvlc2Sim.put(ktcNode, simNode.getId());
		return simNode;
	}

	@Override
	public IEdge addEdge(final INodeID source, final INodeID target, final double distance, final double requiredTransmissionPower) {
		final IEdge simEdge = this.graph.createEdge(source, target);
		simEdge.setProperty(KTCConstants.DISTANCE, distance);
		simEdge.setProperty(KTCConstants.REQUIRED_TRANSMISSION_POWER, requiredTransmissionPower);
		final KTCLink ktcLink = this.topology.addKTCLink(simEdge.getId().valueAsString(), this.nodeMappingSim2Jvlc.get(source),
				this.nodeMappingSim2Jvlc.get(target), distance, requiredTransmissionPower);
		this.edgeMappingSim2Jvlc.put(simEdge, ktcLink);
		this.edgeMappingJvlc2Sim.put(ktcLink, simEdge);
		return simEdge;
	}

	@Override
	public <T> void updateNodeAttribute(final INode simNode, final GraphElementProperty<T> property) {
		final KTCNode ktcNode = this.nodeMappingSim2Jvlc.get(simNode.getId());
		this.updateNodeAttribute(ktcNode, property, simNode.getProperty(property));

	}

	public <T> void updateNodeAttribute(final KTCNode ktcNode, final GraphElementProperty<T> property, final T value) {
		if (KTCConstants.REMAINING_ENERGY.equals(property)) {
			ktcNode.setRemainingEnergy((Double) value);
			this.algorithm.handleNodeAttributeModification(ktcNode);
		}
	}

	@Override
	public <T> void updateEdgeAttribute(final IEdge simEdge, final GraphElementProperty<T> property) {
		final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
		final Double value = (Double) simEdge.getProperty(property);
		updateLinkAttribute(ktcLink, property, value);
	}

	public <T> void updateLinkAttribute(final KTCLink ktcLink, final GraphElementProperty<T> property, final Double value) {
		if (KTCConstants.DISTANCE.equals(property)) {
			ktcLink.setDistance(value);
			this.algorithm.handleLinkAttributeModification(ktcLink);
		} else if (KTCConstants.REQUIRED_TRANSMISSION_POWER.equals(property)) {
			ktcLink.setRequiredTransmissionPower(value);
			this.algorithm.handleLinkAttributeModification(ktcLink);
		}
	}

	@Override
	public void removeElement(final IElement element) {
		if (element instanceof IEdge) {
			final IEdge simEdge = (IEdge) element;
			final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
			this.topology.removeEdge(ktcLink);
			this.edgeMappingJvlc2Sim.remove(ktcLink);
			this.edgeMappingSim2Jvlc.remove(simEdge);
		} else if (element instanceof INode) {
			final INode simNode = (INode) element;
			final KTCNode ktcNode = this.nodeMappingSim2Jvlc.get(simNode.getId());
			this.topology.removeNode(ktcNode);
			this.nodeMappingJvlc2Sim.remove(ktcNode);
			this.nodeMappingSim2Jvlc.remove(simNode.getId());
		} else {
			throw new IllegalArgumentException("Unknown elment type: " + element.toString());
		}
	}

	@Override
	public void registerLinkActivationListener(final ILinkActivationListener listener) {
		this.linkActivationListeners.add(listener);
	}

	private class LinkActivationContentAdapter extends GraphContentAdapter {
		@Override
		protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
			super.edgeAttributeChanged(edge, attribute, oldValue);

			switch (attribute.getFeatureID()) {
			case JvlcPackage.KTC_LINK__STATE:
				for (final ILinkActivationListener listener : linkActivationListeners) {
					if (LinkState.ACTIVE.equals(edge.eGet(attribute))) {
						final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
						listener.linkActivated(simEdge);
					} else if (LinkState.ACTIVE.equals(edge.eGet(attribute))) {
						final IEdge simEdge = edgeMappingJvlc2Sim.get(edge);
						listener.linkInactivated(simEdge);
					}
				}
				break;
			}
		}
	}

}
