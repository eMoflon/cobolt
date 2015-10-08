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
import de.tudarmstadt.maki.modeling.jvlc.listener.AttributeValueSynchronizingContentAdapter;
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

	private IncrementalKTC algorithm;
	private Topology topology;
	private Graph graph;
	private final List<ILinkActivationListener> linkActivationListeners;
	private final Map<INodeID, KTCNode> nodeMappingSim2Jvlc;
	private final Map<KTCNode, INodeID> nodeMappingJvlc2Sim;
	private final Map<IEdge, KTCLink> edgeMappingSim2Jvlc;
	private final Map<KTCLink, IEdge> edgeMappingJvlc2Sim;

	private JVLCFacade(final IncrementalKTC algorithm) {
		this.linkActivationListeners = new ArrayList<>();
		this.nodeMappingSim2Jvlc = new HashMap<>();
		this.nodeMappingJvlc2Sim = new HashMap<>();
		this.edgeMappingSim2Jvlc = new HashMap<>();
		this.edgeMappingJvlc2Sim = new HashMap<>();
		this.algorithm = algorithm;
		this.intializeGraph();
	}

	public static JVLCFacade createFacadeForIncrementalDistanceKTC() {
		final JVLCFacade facade = new JVLCFacade(JvlcFactory.eINSTANCE.createIncrementalDistanceKTC());
		return facade;
	}

	public static JVLCFacade createFacadeForIncrementalEnergyKTC() {
		final JVLCFacade facade = new JVLCFacade(JvlcFactory.eINSTANCE.createIncrementalEnergyKTC());
		return facade;
	}

	/**
	 * Returns the graph of this facade.
	 */
	public Topology getTopology() {
		return this.topology;
	}

	/**
	 * Returns the algorithm instance. You should not use the returned instance to run the algorithm.
	 * Use {@link #run(double)} instead.
	 *
	 * @return the algorithm of this facade
	 */
	public IncrementalKTC getAlgorithm() {
		return this.algorithm;
	}

	public void loadAndSetTopologyFromFile(final String inputFilename) throws FileNotFoundException {
		loadAndSetTopologyFromFile(new File(inputFilename));
	}

	public void loadAndSetTopologyFromFile(final File inputFile) throws FileNotFoundException {
		final JvlcTopologyFromTextFileReader reader = new JvlcTopologyFromTextFileReader();
		this.topology = reader.read(new FileInputStream(inputFile));
		this.registerListeners(this.topology);
	}

	private void registerListeners(final Topology graph) {
		graph.eAdapters().add(new AttributeValueSynchronizingContentAdapter());
		graph.eAdapters().add(new LinkActivationContentAdapter());
	}

	@Override
	public void run(final TopologyControlAlgorithmID algorithm, final TopologyControlAlgorithmParamters parameters) {
		this.configureAlgorithm(algorithm);
		this.algorithm.setK((Double) parameters.get(KTCConstants.K));
		this.algorithm.run(getTopology());
	}

	public void run(final double k) {
		this.algorithm.setK(k);
		this.algorithm.run(getTopology());
	}

	private void configureAlgorithm(final TopologyControlAlgorithmID algorithmId) {
		switch (algorithmId) {
		case ID_KTC:
			this.algorithm = JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
			break;
		case IE_KTC:
			this.algorithm = JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
			break;
		default:
			throw new IllegalArgumentException("Unsupported algorithm: " + algorithmId);
		}
	}

	@Override
	public Graph intializeGraph() {
		this.topology = JvlcFactory.eINSTANCE.createTopology();
		registerListeners(topology);
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
	public <T> void updateAttribute(final IElement element, final GraphElementProperty<T> property) {
		if (element instanceof IEdge) {
			final IEdge simEdge = (IEdge) element;
			final KTCLink ktcLink = this.edgeMappingSim2Jvlc.get(simEdge);
			if (KTCConstants.DISTANCE.equals(property)) {
				ktcLink.setDistance((Double) element.getProperty(property));
			} else if (KTCConstants.REQUIRED_TRANSMISSION_POWER.equals(property)) {
				ktcLink.setRequiredTransmissionPower((Double) element.getProperty(property));
			}
		} else if (element instanceof INode) {
			final INode simNode = (INode) element;
			final KTCNode ktcNode = this.nodeMappingSim2Jvlc.get(simNode.getId());
			if (KTCConstants.REMAINING_ENERGY.equals(property)) {
				ktcNode.setRemainingEnergy((Double) element.getProperty(property));
			}
		} else {
			throw new IllegalArgumentException("Unknown elment type: " + element.toString());
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
