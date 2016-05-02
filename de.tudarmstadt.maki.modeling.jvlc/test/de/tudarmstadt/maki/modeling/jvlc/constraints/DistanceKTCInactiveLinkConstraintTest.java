package de.tudarmstadt.maki.modeling.jvlc.constraints;

import org.junit.Before;

import de.tudarmstadt.maki.modeling.jvlc.DistanceKTCInactiveLinkConstraint;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;

/**
 * Unit tests for {@link DistanceKTCInactiveLinkConstraint}
 */
public class DistanceKTCInactiveLinkConstraintTest {

	private DistanceKTCInactiveLinkConstraint constraint;

	@Before
	public void setUp() {
		this.constraint = JvlcFactory.eINSTANCE.createDistanceKTCInactiveLinkConstraint();
	}

}
