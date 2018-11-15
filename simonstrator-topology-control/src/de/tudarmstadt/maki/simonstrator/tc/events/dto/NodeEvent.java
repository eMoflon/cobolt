package de.tudarmstadt.maki.simonstrator.tc.events.dto;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;

public abstract class NodeEvent extends Event {
	private final String nodeId;

	public NodeEvent(final String type, final INode node) {
		super(type);
		this.nodeId = node.getId().toString();
	}

	public String getNodeId() {
		return nodeId;
	}

	@Override
	public String toString() {
		return super.toString() + " id=" + getNodeId();
	}
}