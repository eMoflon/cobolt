/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */



package de.tud.kom.p2psim.impl.transport;

import de.tud.kom.p2psim.api.transport.TransMessage;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tudarmstadt.maki.simonstrator.api.Message;

/**
 * The base class of all transport layer messages.
 */
public abstract class AbstractTransMessage implements TransMessage {

	private final TransProtocol protocol;

	private int commId;

	private final Message payload;

	private final int srcPort;

	private final int dstPort;

	private boolean isReply;

	private long payloadSizeCache = -1;

	/**
	 * Use this constructor to create a new TransMessage
	 * 
	 * @param protocol
	 * @param payload
	 * @param senderPort
	 * @param receiverPort
	 * @param commId
	 */
	public AbstractTransMessage(TransProtocol protocol, Message payload,
			int senderPort, int receiverPort, int commId, boolean isReply) {
		this.protocol = protocol;
		this.payload = payload;
		this.srcPort = senderPort;
		this.dstPort = receiverPort;
		this.commId = commId;
		this.isReply = isReply;
	}

    @Override
	public long getSize() {
		if (payloadSizeCache == -1) {
			payloadSizeCache = payload.getSize();
			if (payloadSizeCache <= 0) {
				throw new AssertionError(
						"You forgot to implement getSize in your Message!");
			}
		}
		return protocol.getHeaderSize() + payloadSizeCache;
	}

	@Override
	public Message getPayload() {
		return payload;
	}

	@Override
	public int getCommId() {
		return this.commId;
	}

	public void setCommId(int commId) {
		this.commId = commId;
	}

	@Override
	public int getSenderPort() {
		return this.srcPort;
	}

	@Override
	public int getReceiverPort() {
		return this.dstPort;
	}

	@Override
	public TransProtocol getProtocol() {
		return protocol;
	}

	@Override
	public boolean isReply() {
		return this.isReply;
	}

}
