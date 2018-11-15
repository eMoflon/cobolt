package de.tudarmstadt.maki.simonstrator.tc.facade;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.EdgeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;

public class ITopologyControlFacadeTest {

	/**
	 * This test showcases how to use the facade for calling the incremental kTC
	 * variants developed for the JVLC paper.
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateJvlcFacade() throws Exception {
		try {
			Class.forName("org.eclipse.emf.ecore.EObject");
			Class.forName("org.cobolt.algorithms.facade.EMoflonFacade");
		} catch (final Exception e) {
			Assume.assumeNoException(e);
		}

		final double d = 2.0;
		final ITopologyControlFacade facade = TopologyControlFacadeFactory
				.create("de.tudarmstadt.maki.modeling.jvlc.facade.JVLCFacade");
		facade.configureAlgorithm(UnderlayTopologyControlAlgorithms.D_KTC);
		final CountingLinkStateListener listener = new CountingLinkStateListener(new NullTopologyControlFacade());
		facade.addLinkStateListener(listener);
		final Graph graph = facade.getGraph();
		addNodeWithDistance(facade, INodeID.get(1), 100);
		addNodeWithDistance(facade, INodeID.get(2), 100);
		addNodeWithDistance(facade, INodeID.get(3), 100);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(1), INodeID.get(2), 2, 4);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(2), INodeID.get(1), 2, 4);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(2), INodeID.get(3), 2, 4);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(3), INodeID.get(2), 2, 4);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(1), INodeID.get(3), 4, 16);
		addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(3), INodeID.get(1), 4, 16);

		Assert.assertEquals(3, graph.getNodeCount());
		Assert.assertEquals(6, graph.getEdgeCount());

		// Adding the reverse edge is also possible but no new ede will be
		// created
		// The parameters will be ignored
		final IEdge e21 = addEdgeWithDistanceAndRequiredEnergy(facade, INodeID.get(2), INodeID.get(1), 3, 9);
		Assert.assertEquals(e21.getProperty(UnderlayTopologyProperties.WEIGHT), 2.0, 0.0);

		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, d));

		Assert.assertEquals(4, listener.getActivationCount());
		Assert.assertEquals(2, listener.getInactivationCount());

		e21.setProperty(UnderlayTopologyProperties.WEIGHT, 6.0);
		e21.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER, 36.0);
		facade.updateEdgeAttribute(e21, UnderlayTopologyProperties.WEIGHT);

		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, 2.0));

		facade.removeNode(INodeID.get(1));
		Assert.assertEquals(2, graph.getNodeCount());
		Assert.assertEquals(2, graph.getEdgeCount());

		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, 2.0));

		facade.removeEdge(graph.getEdge(INodeID.get(2), INodeID.get(3)));
		facade.removeEdge(graph.getEdge(INodeID.get(3), INodeID.get(2)));
		Assert.assertEquals(2, graph.getNodeCount());
		Assert.assertEquals(0, graph.getEdgeCount());

		facade.run(TopologyControlAlgorithmParamters.create(UnderlayTopologyControlAlgorithms.KTC_PARAM_K, 2.0));

	}

	private static IEdge addEdgeWithDistanceAndRequiredEnergy(ITopologyControlFacade facade, INodeID srcId, INodeID targetId,
			double distance, double requiredEnergy) {
		IEdge prototype = new DirectedEdge(srcId, targetId, EdgeID.get(srcId, targetId));
		prototype.setProperty(UnderlayTopologyProperties.WEIGHT, distance);
		prototype.setProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER, distance);
		return facade.addEdge(prototype);
	}

	private static INode addNodeWithDistance(ITopologyControlFacade facade, INodeID iNodeID, double remainingEnergy) {
		INode prototype = new Node(iNodeID);
		prototype.setProperty(UnderlayTopologyProperties.REMAINING_ENERGY, remainingEnergy);
		return facade.addNode(prototype);
	}
}
