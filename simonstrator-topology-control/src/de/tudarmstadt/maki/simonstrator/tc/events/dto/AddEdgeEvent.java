package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

public class AddEdgeEvent extends EdgeEvent {
	public static final String TYPE_ID = "add-edge";

	public AddEdgeEvent(IEdge edge) {
		super(TYPE_ID, edge);
	}

	@Override
	public String toString() {
		return String.format("#%d:+e %s", this.getNumber(), this.getEdgeId());
	}
}