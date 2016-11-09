package de.tudarmstadt.maki.tc.cbctc.model.listener;

import java.util.logging.Logger;

import de.tudarmstadt.maki.tc.cbctc.model.Edge;
import de.tudarmstadt.maki.tc.cbctc.model.Node;

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
