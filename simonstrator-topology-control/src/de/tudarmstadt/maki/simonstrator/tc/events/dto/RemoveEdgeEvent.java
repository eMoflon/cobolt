package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

public class RemoveEdgeEvent extends EdgeEvent {
	public static final String TYPE_ID = "remove-edge";

	public RemoveEdgeEvent(final IEdge edge) {
		super(TYPE_ID, edge);
	}

	@Override
	public String toString() {
		return String.format("#%d:-e %s", this.getNumber(), this.getEdgeId());
	}
}