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


package de.tud.kom.p2psim.impl.network.modular.st;

import de.tud.kom.p2psim.api.network.NetMessage;
import de.tudarmstadt.maki.simonstrator.api.component.network.Bandwidth;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * If we would simply delay every message that is being sent, every peer 
 * would be able to send an unlimited amount of data in any time interval,
 * not caring about bandwidth that is available. This is inappropriate for
 * a realistic network layer behavior.
 * 
 * In the network layers of PeerfactSim, a bandwidth is assigned to every host.
 * We can use this bandwidth to control the incoming and outgoing traffic of 
 * every peer, using different strategies.
 * 
 * Note that the send context and the receive context of a network layer
 * access the same traffic control metadata object.
 * 
 * @author Leo Nobach
 *
 */
public interface TrafficControlStrategy extends ModNetLayerStrategy {

	/**
	 * Called whenever the sender is online and attempts to send a packet to someone.
	 * Note that at the end of this method, the network message must either be
	 * sent through the subnet, dropped (use ctx for that), or the sending or dropping action must be 
	 * scheduled at the simulator in the future.
	 * @param modularNetLayer 
	 * 
	 * @param ctx, the sending context of the receiver's network layer
	 * @param msg, the network message that has to be sent.
	 * @param receiver, the receiver of the current network message.
	 * @param protocol, the network protocol that is used.
	 */
	public void onSendRequest(ISendContext ctx, NetMessage msg, NetID receiver);
	
	public interface ISendContext extends IContext {
		
		/**
		 * Sends the message through the given subnet. Note that the message will reside the time given in
		 * the current latency model, and additionally the packet might wait in the receiver's traffic control
		 * given by this strategy.
		 * @param netMsg
		 */
		public void sendSubnet(NetMessage netMsg);
		
		/**
		 * Drops the current message caused by the current traffic control strategy (not only the packet loss strategy decides
		 * about packets that will be dropped).
		 * @param netMsg
		 */
		public void dropMessage(NetMessage netMsg);
		
	}
	
	/**
	 * Called whenever the receiver is online and attempts to receive a packet from the subnet.
	 * Note that at the end of this method, the network message must either arrive, 
	 * be dropped (use ctx for that), or the arriving or dropping action must be scheduled at the simulator in the future.
	 * @param modularNetLayer 
	 * 
	 * @param ctx, the sending context of the receiver's network layer
	 * @param msg, the network message that has to be sent.
	 * @param receiver, the receiver of the current network message.
	 * @param protocol, the network protocol that is used.
	 */
	public void onReceive(IReceiveContext ctx, NetMessage message);
	
	public interface IReceiveContext extends IContext {
		
		/**
		 * Lets the message arrive at the given receiver network layer.
		 * given by this strategy.
		 * @param netMsg
		 */
		public void arrive(NetMessage netMsg);
		
		/**
		 * Drops the current message caused by the current traffic control strategy (not only the packet loss strategy decides
		 * about packets that will be dropped).
		 * @param netMsg
		 */
		public void dropMessage(NetMessage netMsg);
		
		/**
		 * Returns the bandwidth of the sender that has sent the packet, this is sometimes useful by the receiver traffic control.
		 * @return
		 */
		public Bandwidth getBandwidthOfSender();
		
	}
	
	public interface IContext {
		/**
		 * Returns the traffic control metadata from the network layer of the current receiver(IReceiveContext) or sender(ISendContext). Note that the 
		 * metadata object may be null if never set before.
		 * @return
		 */
		public Object getTrafCtrlMetadata();
		
		/**
		 * Sets the traffic control metadata on the network layer of the current receiver(IReceiveContext) or sender(ISendContext) to the given object o.
		 * @param trafCtrlMetadata
		 */
		public void setTrafCtrlMetadata(Object trafCtrlMetadata);
		
		/**
		 * Returns the current maximum bandwidth of the context
		 * @return
		 */
		public Bandwidth getMaxBW();
	}
	
}
