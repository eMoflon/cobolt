/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.linklayer.mac;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacEventInformation;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.linklayer.DefaultLinkMessageEvent;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This MAC maintains the outgoing queue and sends subsequent messages one after
 * the other (this does not imply that this MAC handles collisions on a
 * broadcast medium). It just mimics the behavior of the ModularNetLayer with a
 * TrafficControl-Strategy enabled.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.03.2012
 */
public class EnqueuingMac extends AbstractMacLayer {

	private boolean blocked = false;

	/**
	 * The timestamp on which an ingoing transmission could start again (this
	 * does only take upload time into account and not link latency)
	 */
	private long nextRcvTime = 0;

	/**
	 * This will simulate the incoming queue at the receiver as well. This does
	 * only make sense in topologyViews that consider real link layers (e.g.,
	 * Wi-Fi). For all Internet-scale scenarios, where we use links as a concept
	 * for a connection between end hosts and not in the sense of a real link
	 * layer, this HAS TO BE false.
	 */
	private boolean simulateReceiverQueue = true;

	/**
	 * A MAC that provides an outgoing queue (messages are sent one after the
	 * other). This may lead to packet drop if the queue is bound or if a
	 * timeout is configured and the message has been in the queue for too long.
	 * 
	 * @param ownMacAddress
	 * @param phy
	 * @param maxQueueLength
	 * @param maxTimeInQueue
	 * @param maxRetransmissions
	 * @param bandwidth
	 *            the maximum BW of this MAC (i.e. the BW that would be achieved
	 *            if messages are not queued)
	 */
	public EnqueuingMac(SimHost host, MacAddress ownMacAddress, PhyType phy,
			int maxQueueLength, long maxTimeInQueue, int maxRetransmissions,
			BandwidthImpl bandwidth) {
		super(host, ownMacAddress, phy, maxQueueLength, maxTimeInQueue,
				maxRetransmissions, bandwidth);
	}

	@Override
	public void initialize() throws ConfigurationException {
		super.initialize();
		this.simulateReceiverQueue = getTopologyView().hasRealLinkLayer();
	}

	@Override
	protected void handleNewQueueEntry() {
		if (!blocked) {
			blocked = true;
			sendNextMessage();
		}
	}

	@Override
	protected void handleReceivedMessage(MacEventInformation eventInfo) {
		/*
		 * There are not CTRL-Messages in this MAC, we just deliver the message
		 */
		notifyLinkLayer(new DefaultLinkMessageEvent(
				(LinkLayerMessage) eventInfo.getMessage(), getPhyType(),
				eventInfo.getSender(),
				eventInfo.isBroadcast()));
	}

	/**
	 * Call this to dispatch the next message out of the queue. If the queue is
	 * empty, the host will not be blocked anymore.
	 */
	protected void sendNextMessage() {
		assert blocked;
		QueueEntry toSend = getQueueHead();
		if (toSend == null) {
			blocked = false;
			// do nothing.
		} else {
			DefaultMacEventInformation eventInfo = new DefaultMacEventInformation(
					toSend.getMessage(), this.getMacAddress(),
					toSend.getReceiver(), Time.getCurrentTime()
							- toSend.getTimeEntered());
			long timeToSend = 0;
			long rcvTimeDelay = 0;
			if (toSend.getReceiver().isBroadcast()) {
				timeToSend = sendBroadcast(eventInfo);
			} else {
				timeToSend = sendUnicast(eventInfo);
				MacLayer receiverMac = getTopologyView().getMac(
						eventInfo.getReceiver());
				if (simulateReceiverQueue
						&& receiverMac instanceof EnqueuingMac) {
					rcvTimeDelay = ((EnqueuingMac) receiverMac)
							.getNextRcvDelay();
					((EnqueuingMac) receiverMac).incNextRcvTime(timeToSend);
				}
			}
			/*
			 * Notify of nextMsg send
			 */
			Event.scheduleWithDelay(timeToSend + rcvTimeDelay, this, this,
					MESSAGE_AT_SUBNET);
		}
	}

	public void incNextRcvTime(long incRcvTime) {
		if (nextRcvTime <= Time.getCurrentTime()) {
			nextRcvTime = Time.getCurrentTime() + incRcvTime;
		} else {
			nextRcvTime += incRcvTime;
		}
	}

	public long getNextRcvDelay() {
		return Math.max(0, nextRcvTime - Time.getCurrentTime());
	}

	@Override
	protected void handleEvent(Object data, int type) {
		if (type == MESSAGE_AT_SUBNET) {
			sendNextMessage();
		}
	}

}
