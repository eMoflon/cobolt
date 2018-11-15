package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link TopologyControlReconfigurationDecision}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlReconfigurationDecisionTest {
	@Test
	public void test_areUnequalWithNullCheck() throws Exception {
		final Object o1 = new Object();
		final Object o2 = new Object();
		Assert.assertFalse(TopologyControlReconfigurationDecision.areUnequalWithNullCheck(null, null));
		Assert.assertTrue(TopologyControlReconfigurationDecision.areUnequalWithNullCheck(null, o1));
		Assert.assertTrue(TopologyControlReconfigurationDecision.areUnequalWithNullCheck(o1, null));
		Assert.assertFalse(TopologyControlReconfigurationDecision.areUnequalWithNullCheck(o1, o1));
		Assert.assertTrue(TopologyControlReconfigurationDecision.areUnequalWithNullCheck(o1, o2));
	}
}
