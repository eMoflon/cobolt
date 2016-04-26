package de.tudarmstadt.maki.modeling.jvlc.constraints;

import org.junit.Assert;

import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.impl.KTCConstraintViolationEnumeratorImpl;

/**
 * This constraint checker reports the first constraint violation via {@link Assert#fail(String)}.
 */
public class AssertConstraintViolationEnumerator extends KTCConstraintViolationEnumeratorImpl {

	private static final AssertConstraintViolationEnumerator INSTANCE = new AssertConstraintViolationEnumerator();

	public static AssertConstraintViolationEnumerator getInstance() {
		return INSTANCE;
	}

	@Override
	public void reportViolation(final KTCLink link1, final KTCLink link2, final KTCLink link3) {
		super.reportViolation(link1, link2, link3);
		Assert.fail("Violation found:\n" + "link1 = " + link1 + "\n" //
				+ "link2 = " + link2 + "\n" //
				+ "link3 = " + link3);
	}
}
