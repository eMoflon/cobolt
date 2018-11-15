package de.tudarmstadt.maki.simonstrator.tc.facade;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Implementations of this interface receive notifications about context events (such as node/edge addition/removal).
 */
public interface IContextEventListener {

	void postNodeAdded(INode node);

	void preNodeRemoved(INode node);

	void postEdgeAdded(IEdge edge);

	void preEdgeRemoved(IEdge edge);

	<T> void postNodeAttributeUpdated(INode node, SiSType<T> property);

	<T> void postEdgeAttributeUpdated(IEdge edge, SiSType<T> property);
}
