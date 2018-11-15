package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class TopologyControlEvaluationMessage implements Message {

	private static final long serialVersionUID = 8389152799174603539L;

	private final static long MESSAGE_SIZE_IN_BYTE = 1024;

	private final long creationTime;

	public TopologyControlEvaluationMessage() {
		this.creationTime = Time.getCurrentTime();
	}

	@Override
	public long getSize() {
		return MESSAGE_SIZE_IN_BYTE;
	}

	@Override
	public Message getPayload() {
		return null;
	}

	public long getCreationTime() {
		return this.creationTime;
	}

}