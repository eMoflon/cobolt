package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;

public abstract class EdgeEvent extends Event {
	private final String edgeId;
	private final String sourceId;
	private final String targetId;

	public EdgeEvent(final String type, IEdge edge) {
		super(type);
		this.edgeId = edge.getId().valueAsString();
		this.sourceId = edge.fromId().toString();
		this.targetId = edge.toId().toString();
	}

	public String getEdgeId() {
		return edgeId;
	}

	public String getSourceId() {
		return sourceId;
	}

	public String getTargetId() {
		return targetId;
	}

	@Override
	public String toString() {
		return super.toString() + " id: " + getEdgeId();
	}
}