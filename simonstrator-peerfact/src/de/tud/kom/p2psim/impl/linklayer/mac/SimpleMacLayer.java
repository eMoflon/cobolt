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
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.linklayer.DefaultLinkMessageEvent;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * A very simple MAC-layer that just delivers messages. Mainly for functional
 * testing of components or if you are not interested in Traffic-Control and/or
 * scheduling behavior but want to use advanced features such as topology
 * support and/or movement.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class SimpleMacLayer extends AbstractMacLayer {

	/**
	 * 
	 * @param ownMacAddress
	 * @param phy
	 *            PHY to work on
	 * @param maxRetransmissions
	 *            before the message is dropped on the LinkLayer
	 * @param bandwidth
	 *            the maximum BW of this MAC (i.e. the BW that would be achieved
	 *            if messages are not queued)
	 */
	public SimpleMacLayer(SimHost host, MacAddress ownMacAddress, PhyType phy,
			int maxRetransmissions, BandwidthImpl bandwidth) {
		super(host, ownMacAddress, phy, maxRetransmissions, bandwidth);
	}

	@Override
	protected void handleNewQueueEntry() {
		/*
		 * We do not enqueue, we just send.
		 */
		QueueEntry toSend = getQueueHead();
		DefaultMacEventInformation eventInfo = new DefaultMacEventInformation(
				toSend.getMessage(), getMacAddress(), toSend.getReceiver(),
				Time.getCurrentTime() - toSend.getTimeEntered());
		if (toSend.getReceiver().isBroadcast()) {
			sendBroadcast(eventInfo);
		} else {
			sendUnicast(eventInfo);
		}
	}

	@Override
	protected void handleReceivedMessage(MacEventInformation eventInfo) {
		// just deliver
		notifyLinkLayer(new DefaultLinkMessageEvent(
				(LinkLayerMessage) eventInfo.getMessage(),
				getPhyType(), eventInfo.getSender(),
				eventInfo.isBroadcast()));
	}
}
