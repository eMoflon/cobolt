package de.tudarmstadt.maki.modeling.jvlc.constraints;

import org.junit.Before;

import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsFactory;
import de.tudarmstadt.maki.tc.cbctc.algorithms.PlainKTCInactiveLinkConstraint;

/**
 * Unit tests for {@link DistanceKTCInactiveLinkConstraint}
 */
public class PlainKTCConstraintTest {

	@SuppressWarnings("unused")
	private PlainKTCInactiveLinkConstraint constraint;

	@Before
	public void setUp() {
		this.constraint = AlgorithmsFactory.eINSTANCE.createPlainKTCInactiveLinkConstraint();
	}

}
