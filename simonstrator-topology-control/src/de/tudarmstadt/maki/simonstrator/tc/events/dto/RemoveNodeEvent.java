package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;

public class RemoveNodeEvent extends NodeEvent {
	public static final String TYPE_ID = "remove-node";

	public RemoveNodeEvent(final INode node) {
		super(TYPE_ID, node);
	}

	@Override
	public String toString() {
		return String.format("#%d:-n %s", this.getNumber(), this.getNodeId());
	}
}