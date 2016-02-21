package de.tudarmstadt.maki.modeling.jvlc.listener;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.modeling.graphmodel.Graph;
import de.tudarmstadt.maki.modeling.jvlc.AttributeNames;
import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.KTCNode;
import de.tudarmstadt.maki.modeling.jvlc.LinkState;
import de.tudarmstadt.maki.modeling.jvlc.graph.TestGraphGenerator;

public class AttributeValueSynchronizationContentAdapterTest {

	private static final double EPS_ZERO = 0.0;

	@Test
	public void testForwardSynchronization() throws Exception {
		final Graph testGraph1 = TestGraphGenerator.produceTestTopology1();
		final KTCNode node = (KTCNode) testGraph1.getNodes().get(0);
		final KTCLink link = (KTCLink) testGraph1.getEdges().get(0);
		link.setState(LinkState.ACTIVE);
		link.setDistance(1.0);
		link.setRequiredTransmissionPower(0.1);
		node.setRemainingEnergy(5.3);

		testGraph1.eAdapters().add(new AttributeValueSynchronizingContentAdapter());

		node.setDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY, 10.11);
		Assert.assertEquals(10.11, node.getRemainingEnergy(), EPS_ZERO);

		link.setObjectAttribute(AttributeNames.ATTR_STATE, LinkState.INACTIVE);
		Assert.assertEquals(LinkState.INACTIVE, link.getState());

		link.setDoubleAttribute(AttributeNames.ATTR_DISTANCE, 3.141);
		Assert.assertEquals(3.141, link.getDistance(), EPS_ZERO);

		link.setDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER, 0.75);
		Assert.assertEquals(0.75, link.getRequiredTransmissionPower(), EPS_ZERO);
	}

	@Test
	public void testBackwardSynchronization() throws Exception {

		final Graph testGraph1 = TestGraphGenerator.produceTestTopology1();
		testGraph1.eAdapters().add(new AttributeValueSynchronizingContentAdapter());
		final KTCNode node = (KTCNode) testGraph1.getNodes().get(0);
		final KTCLink link = (KTCLink) testGraph1.getEdges().get(0);

		node.setRemainingEnergy(10.0);
		Assert.assertEquals(10.0, node.getDoubleAttribute(AttributeNames.ATTR_REMAINING_ENERGY), EPS_ZERO);

		link.setState(LinkState.ACTIVE);
		Assert.assertSame(LinkState.ACTIVE, link.getObjectAttribute(AttributeNames.ATTR_STATE));

		link.setDistance(13.12);
		Assert.assertEquals(13.12, link.getDoubleAttribute(AttributeNames.ATTR_DISTANCE), EPS_ZERO);

		link.setRequiredTransmissionPower(2.59);
		Assert.assertEquals(2.59, link.getDoubleAttribute(AttributeNames.ATTR_REQUIRED_TRANSMISSION_POWER), EPS_ZERO);
	}
}
