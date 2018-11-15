package de.tudarmstadt.maki.simonstrator.tc.patternMatching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.tudarmstadt.maki.simonstrator.tc.patternMatching.constraint.ComparisonOperator;

public class ComparisonOperatorTest {

	@Test
	public void testEquals() throws Exception {
		assertTrue(ComparisonOperator.EQUAL.evaluate(1.0, 1.0));
		assertFalse(ComparisonOperator.EQUAL.evaluate(1.1, 1.0));
	}
}
