package de.tudarmstadt.maki.modeling.jvlc.facade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.eclipse.emf.ecore.EAttribute;

import de.tudarmstadt.maki.modeling.graphmodel.Edge;
import de.tudarmstadt.maki.modeling.graphmodel.Node;
import de.tudarmstadt.maki.modeling.graphmodel.listener.GraphContentAdapter;
import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.Topology;
import de.tudarmstadt.maki.modeling.jvlc.io.JvlcTopologyFromTextFileReader;
import de.tudarmstadt.maki.modeling.jvlc.listener.AttributeValueSynchronizingContentAdapter;

public class JVLCFacade {

	private final IncrementalKTC algorithm;
	private Topology topology;

	private JVLCFacade(final IncrementalKTC algorithm) {
		this.algorithm = algorithm;
		this.topology = createTopology();
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

	/**
	 * Execute the configured kTC derivate using the given k parameter.
	 * @param k the tuning parameter k
	 */
	public void run(final double k) {
		this.algorithm.setK(k);
		this.algorithm.run(getTopology());
	}

	public void loadAndSetTopologyFromFile(final String inputFilename) throws FileNotFoundException {
		loadAndSetTopologyFromFile(new File(inputFilename));
	}

	public void loadAndSetTopologyFromFile(final File inputFile) throws FileNotFoundException {
		final JvlcTopologyFromTextFileReader reader = new JvlcTopologyFromTextFileReader();
		this.topology = reader.read(new FileInputStream(inputFile));
		this.registerListeners(this.topology);
	}

	/*
	 * Create the graph and configure the appropriate content adapters
	 */
	private Topology createTopology() {
		final Topology graph = JvlcFactory.eINSTANCE.createTopology();
		registerListeners(graph);
		return graph;
	}

	private void registerListeners(final Topology graph) {
		graph.eAdapters().add(new AttributeValueSynchronizingContentAdapter());
		graph.eAdapters().add(new IncrementalKTCContentAdapter());
	}

	private class IncrementalKTCContentAdapter extends GraphContentAdapter {

		@Override
		protected void nodeAdded(final Node newNode) {
			super.nodeAdded(newNode);
		}

		@Override
		protected void nodeRemoved(final Node removedNode) {
			super.nodeRemoved(removedNode);
		}

		@Override
		protected void edgeAdded(final Edge newEdge) {
			super.edgeAdded(newEdge);
		}

		@Override
		protected void edgeRemoved(final Edge oldEdge) {
			super.edgeRemoved(oldEdge);
		}

		@Override
		protected void edgeAttributeChanged(final Edge edge, final EAttribute attribute, final Object oldValue) {
			super.edgeAttributeChanged(edge, attribute, oldValue);
		}

		@Override
		protected void nodeAttributeChanged(final Node node, final EAttribute attribute, final Object oldValue) {
			super.nodeAttributeChanged(node, attribute, oldValue);
		}

	}

}
