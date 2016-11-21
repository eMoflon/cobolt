package de.tudarmstadt.maki.tc.cbctc.algorithms.facade;

import static de.tudarmstadt.maki.tc.cbctc.algorithms.TopologyControlAlgorithmsTestUtils.getPathToEnergyTestGraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyProperties;
import de.tudarmstadt.maki.tc.cbctc.model.Topology;
import de.tudarmstadt.maki.tc.cbctc.model.TopologyTestUtils;
import de.tudarmstadt.maki.tc.cbctc.model.derivedfeatures.EdgeWeightProviders;

/**
 * Unit tests for {@link EMoflonFacade}, using {@link UnderlayTopologyControlAlgorithms#E_KTC}.
 */
public class EMoflonFacadeTestForEnergyKTC extends AbstractEMoflonFacadeTest {

	@Override
	protected TopologyControlAlgorithmID getAlgorithmID()
	{
	   return UnderlayTopologyControlAlgorithms.E_KTC;
	}
	
	@Test
	public void testWithTestgraphE1() throws Exception {
		readTestCase(1);

		this.facade.run(1.5);

		this.facade.checkConstraintsAfterTopologyControl();
	}

	@Test
	public void testWithTestgraphE1_OneContextEvent() throws Exception {
		readTestCase(1);

		this.facade.run(1.5);
		this.facade.checkConstraintsAfterTopologyControl();

		final Graph graph = this.facade.getGraph();
		final Topology topology = this.facade.getTopology();

		final INode n3 = graph.getNode(INodeID.get("n3"));
		Assert.assertEquals(60, n3.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY), TopologyTestUtils.EPS_0);
		n3.setProperty(UnderlayTopologyProperties.REMAINING_ENERGY, 15.0);
		this.facade.updateNodeAttribute(n3, UnderlayTopologyProperties.REMAINING_ENERGY);
		for (final IEdge edge : graph.getOutgoingEdges(n3.getId())) {
			final double newExpectedLifetime = n3.getProperty(UnderlayTopologyProperties.REMAINING_ENERGY)
					/ edge.getProperty(UnderlayTopologyProperties.REQUIRED_TRANSMISSION_POWER);
			edge.setProperty(UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE, newExpectedLifetime);
			this.facade.updateEdgeAttribute(edge, UnderlayTopologyProperties.EXPECTED_LIFETIME_PER_EDGE);
		}
		Assert.assertEquals(15, topology.getNodeById("n3").getEnergyLevel(), TopologyTestUtils.EPS_0);

		TopologyTestUtils.assertUnclassified(topology.getEdgeById("e31"));
		TopologyTestUtils.assertUnclassified(topology.getEdgeById("e32"));

		this.facade.run(1.5);
		this.facade.checkConstraintsAfterTopologyControl();
	}

	/**
	 * This test illustrates that in a triangle that contains two equally long
	 * 'longest' links (in terms of remaining lifetime), only the link with the
	 * larger ID ('e13' in this case) is inactivated.
	 */
	@Test
	public void testTriangleWithEquisecles() throws Exception {
		readTestCase(2);
		facade.run(1.1);

		TopologyTestUtils.assertActiveSymmetric(facade.getTopology());
	}

	private void readTestCase(int id) throws FileNotFoundException {
		reader.read(this.facade, new FileInputStream(new File(getPathToEnergyTestGraph(id))));
		EdgeWeightProviders.apply(this.facade.getTopology(), EdgeWeightProviders.EXPECTED_REMAINING_LIFETIME_PROVIDER);
	}
}
