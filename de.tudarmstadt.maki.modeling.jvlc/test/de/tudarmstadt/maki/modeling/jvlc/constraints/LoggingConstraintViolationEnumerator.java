package de.tudarmstadt.maki.modeling.jvlc.constraints;

import java.util.logging.Logger;

import de.tudarmstadt.maki.modeling.jvlc.KTCLink;
import de.tudarmstadt.maki.modeling.jvlc.impl.KTCConstraintViolationEnumeratorImpl;

/**
 * This constraint checker lists all found violations.
 */
public class LoggingConstraintViolationEnumerator extends KTCConstraintViolationEnumeratorImpl {

	private static final Logger logger = Logger.getLogger(LoggingConstraintViolationEnumerator.class.getName());
	private static final LoggingConstraintViolationEnumerator INSTANCE = new LoggingConstraintViolationEnumerator();

	public static LoggingConstraintViolationEnumerator getInstance() {
		return INSTANCE;
	}

	@Override
	public void reportViolation(final KTCLink link1, final KTCLink link2, final KTCLink link3) {
		super.reportViolation(link1, link2, link3);
		logger.warning("Violation found:\n" + "link1 = " + link1 + "\n" //
				+ "link2 = " + link2 + "\n" //
				+ "link3 = " + link3);
	}
}
