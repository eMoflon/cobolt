package de.tudarmstadt.maki.modeling.jvlc.constraints;

import org.junit.Before;

import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.modeling.jvlc.PlainKTCInactiveLinkConstraint;

/**
 * Unit tests for {@link DistanceKTCInactiveLinkConstraint}
 */
public class PlainKTCConstraintTest {

	@SuppressWarnings("unused")
	private PlainKTCInactiveLinkConstraint constraint;

	@Before
	public void setUp() {
		this.constraint = JvlcFactory.eINSTANCE.createPlainKTCInactiveLinkConstraint();
	}

}
