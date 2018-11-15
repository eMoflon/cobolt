package de.tudarmstadt.maki.simonstrator.tc.facade;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

/**
 * Specifies an interface for listeners of link state
 */
public interface ILinkStateListener {

	/**
	 * The given edge has been set to ACTIVE.
	 */
	void linkActivated(final IEdge edge);

	/**
	 * The given edge has been set to INACTIVE
	 */
	void linkInactivated(final IEdge edge);

	/**
	 * The given edge has been set to UNCLASSIFIED
	 */
	void linkUnclassified(IEdge edge);
}
