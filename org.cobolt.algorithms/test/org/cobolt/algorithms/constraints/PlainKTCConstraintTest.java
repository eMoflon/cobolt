package org.cobolt.algorithms.constraints;

import org.cobolt.algorithms.AlgorithmsFactory;
import org.cobolt.algorithms.PlainKTCInactiveEdgeConstraint;
import org.junit.Before;

/**
 * Unit tests for {@link DistanceKTCInactiveLinkConstraint}
 */
public class PlainKTCConstraintTest {

	@SuppressWarnings("unused")
	private PlainKTCInactiveEdgeConstraint constraint;

	@Before
	public void setUp() {
		this.constraint = AlgorithmsFactory.eINSTANCE.createPlainKTCInactiveEdgeConstraint();
	}

}
