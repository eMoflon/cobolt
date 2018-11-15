package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol;

import org.junit.Assert;
import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.testing.TopologyControlTestHelper;

public class TopologyControlComponentTest {
	@Test
	public void testArcusTangens() throws Exception {
		// The vector (1,1) corresponds to 45° = PI/4
		Assert.assertEquals(Math.PI / 4, Math.atan2(1, 1), TopologyControlTestHelper.EPS_0);
		Assert.assertEquals(45.0, Math.atan2(1, 1) * 180 / Math.PI, TopologyControlTestHelper.EPS_0);
	}
}
