package org.cobolt.model.listener;

import java.util.logging.Logger;

import org.cobolt.model.Edge;
import org.cobolt.model.Node;

/**
 * An implementation of {@link GraphContentAdapter} that logs all events to its
 * logger (level=INFO)
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public class LoggingGraphContentAdapter extends GraphContentAdapter {

	private static final Logger logger = Logger.getLogger(LoggingGraphContentAdapter.class.getName());

	@Override
	protected void nodeAdded(final Node newNode) {
		logger.info("Node added: " + newNode);
	}

	@Override
	protected void nodeRemoved(final Node removedNode) {
		logger.info("Node removed: " + removedNode);
	}

	@Override
	protected void edgeRemoved(final Edge oldEdge) {
		logger.info("Edge removed: " + oldEdge);
	}

	@Override
	protected void edgeAdded(final Edge newEdge) {
		logger.info("Edge added: " + newEdge);
	}
}
