package de.tudarmstadt.maki.tc.cbctc.algorithms.constraints;

import org.junit.Before;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsFactory;
import de.tudarmstadt.maki.tc.cbctc.algorithms.PlainKTCInactiveEdgeConstraint;

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
