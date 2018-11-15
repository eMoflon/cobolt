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

package de.tud.kom.p2psim.impl.network.modular;

import java.util.Collection;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.api.network.NetMessageListener;
import de.tud.kom.p2psim.api.network.NetMsgEvent;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.transport.TransProtocol;
import de.tud.kom.p2psim.impl.network.AbstractNetLayer;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.City;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Country;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Region;
import de.tud.kom.p2psim.impl.network.modular.livemon.NetLayerLiveMonitoring;
import de.tud.kom.p2psim.impl.network.modular.st.FragmentingStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.JitterStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PLossStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PacketSizingStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.TrafficControlStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.TrafficControlStrategy.IReceiveContext;
import de.tud.kom.p2psim.impl.network.modular.st.TrafficControlStrategy.ISendContext;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.Bandwidth;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * <p>
 * The Modular Network layer aims at being most flexible by allowing every
 * aspect of a network layer to be modeled on its own - as a module. Like
 * "building blocks", these modules can be customized in the simulator
 * configuration file and put together to form the whole network layer.
 * </p>
 * 
 * <p>
 * The Modular Network Layer currently supports modules of the type
 * <ul>
 * <li>Fragmenting
 * <li>Jitter
 * <li>Latency
 * <li>Packet Sizing
 * <li>Packet Loss
 * <li>Positioning
 * <li>Traffic Control
 * </ul>
 * </p>
 * 
 * <p>
 * For information how to configure a modular network layer, please consult
 * ModularNetLayerFactory.
 * </p>
 * <p>
 * To understand particular module types and to write a module yourself, please
 * consult its abstract strategy class.
 * </p>
 * 
 * @see FragmentingStrategy
 * @see JitterStrategy
 * @see LatencyStrategy
 * @see PacketSizingStrategy
 * @see PLossStrategy
 * @see PositioningStrategy
 * @see TrafficControlStrategy
 * @see ModularNetLayerFactory
 * @author Leo Nobach
 * 
 */
public class ModularNetLayer extends AbstractNetLayer {

	private AbstractModularSubnet subnet;

	/**
	 * Creates a new Modular Network layer. This should be only called from the
	 * ModularNetLayerFactory
	 * 
	 * @param subnet
	 * @param maxBW
	 * @param hostMeta
	 * @param position
	 * @param id
	 * @param device
	 *            Device-Type of this Host
	 */
	ModularNetLayer(SimHost host, AbstractModularSubnet subnet,
			BandwidthImpl maxBW, NetMeasurementDB.Host hostMeta,
			Location position,
			IPv4NetID id) {
		super(host, id, maxBW, position, hostMeta);
		this.subnet = subnet;
	}

	@Override
	public void initialize() {
		subnet.registerNetLayer(this);
		goOnline();
		super.initialize();
	}

	public String toString() {

		if (hostMeta != null) {

			City city = hostMeta.getCity();
			Region region = city.getRegion();
			Country country = region.getCountry();

			return "ModNetLayer(" + getNetID() + ", " + city + ", " + region
					+ ", "
					+ country + ")";

		}

		return "ModNetLayer(" + getLocalInetAddress() + " (no location info))";
	}

	@Override
	protected boolean isSupported(TransProtocol protocol) {
		return protocol.equals(TransProtocol.UDP);
	}

	Object trafCtrlMetadata = null;

	/**
	 * Convenience method for send()
	 * 
	 * @param netMsg
	 * @param receiver
	 */
	private void sendNetMessage(NetMessage netMsg, NetID receiver) {
		if (this.isOnline()) {
			NetLayerLiveMonitoring.getOfflineMsgDrop().noDropMessage();
			subnet.getStrategies()
					.getTrafficControlStrategy()
					.onSendRequest(this.new SendContextImpl(), netMsg, receiver);
		}
	}

	/**
	 * Convenience method for send()
	 * 
	 * @param msg
	 * @param receiver
	 */
	private void dropOfflineNetMessage(Message msg, NetID receiver) {
		NetMessage netMsg = new IPv4Message(msg, receiver,
				this.getLocalInetAddress());
		//log.debug("Dropping message " + msg + ", because sender " + this
		//		+ " is offline.");
		NetLayerLiveMonitoring.getOfflineMsgDrop().droppedMessage();
		if (hasAnalyzer) {
			netAnalyzerProxy.netMsgEvent(netMsg, getHost(), Reason.DROP);
		}
	}

	@Override
	public void send(Message msg, NetID receiver, NetProtocol protocol) {
		if (msg instanceof AbstractTransMessage) {
			if (protocol != NetProtocol.IPv4)
				throw new AssertionError(
						"Currently, the simulator only supports IPv4. "
								+ msg.getClass().getSimpleName());

			if (this.isOnline()) {
				TransProtocol usedTransProtocol = ((AbstractTransMessage) msg)
						.getProtocol();
				if (this.isSupported(usedTransProtocol)) {
					NetMessage netMsg = new ModularNetMessage(msg, receiver,
							getNetID(), subnet.getStrategies(),
							NetProtocol.IPv4);
					// IPv6 currently not supported
					sendNetMessage(netMsg, receiver);

					if (((AbstractTransMessage) msg).getCommId() == -1) {
						int assignedMsgId = subnet.determineTransMsgNumber(msg);
						((AbstractTransMessage) msg).setCommId(assignedMsgId);
					}
				} else
					throw new IllegalArgumentException("Transport protocol "
							+ usedTransProtocol
							+ " not supported by this NetLayer implementation.");
			} else {
				int assignedMsgId = subnet.determineTransMsgNumber(msg);
				((AbstractTransMessage) msg).setCommId(assignedMsgId);
				dropOfflineNetMessage(msg, receiver);
			}
		} else {
			throw new AssertionError(
					"Can only send messages of class AbstractTransMessage or "
							+ "subclasses of it through the network layer, but the message class was "
							+ msg.getClass().getSimpleName());
		}
	}

	class SendContextImpl implements ISendContext {

		@Override
		public Object getTrafCtrlMetadata() {
			return trafCtrlMetadata;
		}

		@Override
		public void setTrafCtrlMetadata(Object trafCtrlMetadata) {
			ModularNetLayer.this.trafCtrlMetadata = trafCtrlMetadata;
		}

		@Override
		public void sendSubnet(NetMessage netMsg) {
			NetLayerLiveMonitoring.getTrafCtrlMsgDrop().noDropMessage();
			if (hasAnalyzer) {
				netAnalyzerProxy.netMsgEvent(netMsg, getHost(), Reason.SEND);
			}
			ModularNetLayer.this.getSubnet().send(netMsg);
		}

		@Override
		public void dropMessage(NetMessage netMsg) {
			//log.debug("Dropping message " + netMsg + ", because the sender's ("
			//		+ ModularNetLayer.this
			//		+ ") traffic control mechanism has decided it.");
			NetLayerLiveMonitoring.droppedMessageTrafCtrl(netMsg);
			if (hasAnalyzer) {
				netAnalyzerProxy.netMsgEvent(netMsg, getHost(), Reason.DROP);
			}
		}

		@Override
		public Bandwidth getMaxBW() {
			return ModularNetLayer.this.getMaxBandwidth();
		}

	}

	// TODO
	@Override
	@Deprecated
	public void receive(NetMessage message) {
		throw new IllegalStateException(
				"The method receive(NetMessage) is deprecated in the Modular Network Layer.");
	}

	/**
	 * Receiving a Message without additional information on the Subnet-Context.
	 * This is only used by the SimpleModularSubnet, as it has no Topology or
	 * Channels.
	 * 
	 * @param message
	 * @param netLayerOfSender
	 */
	public void receive(ModularNetMessage message,
			ModularNetLayer netLayerOfSender) {
		ReceiveContextImpl ctx = new ReceiveContextImpl(
				netLayerOfSender.getMaxBandwidth(), getMaxBandwidth());
		subnet.getStrategies().getTrafficControlStrategy()
				.onReceive(ctx, message);
	}

	class ReceiveContextImpl implements IReceiveContext {

		private Bandwidth senderBW;

		private Bandwidth receiverBW;

		public ReceiveContextImpl(Bandwidth senderBW, Bandwidth receiverBW) {
			this.senderBW = senderBW;
			this.receiverBW = receiverBW;
		}

		@Override
		public Object getTrafCtrlMetadata() {
			return trafCtrlMetadata;
		}

		@Override
		public void setTrafCtrlMetadata(Object trafCtrlMetadata) {
			ModularNetLayer.this.trafCtrlMetadata = trafCtrlMetadata;
		}

		@Override
		public void arrive(NetMessage message) {
			NetLayerLiveMonitoring.getTrafCtrlMsgDrop().noDropMessage();
			if (ModularNetLayer.this.isOnline()) {
				NetLayerLiveMonitoring.getOfflineMsgDrop().noDropMessage();
				NetLayerLiveMonitoring.getRoutingMsgDrop().noDropMessage();
				NetID myID = getNetID();
				if (hasAnalyzer) {
					netAnalyzerProxy.netMsgEvent(message, getHost(),
							Reason.RECEIVE);
				}
				NetMsgEvent event = new NetMsgEvent(message,
						ModularNetLayer.this);
				Collection<NetMessageListener> msgListeners = getNetMsgListeners();
				if (msgListeners == null || msgListeners.isEmpty()) {
					if (hasAnalyzer) {
						netAnalyzerProxy.netMsgEvent(message, getHost(),
								Reason.RECEIVE);
					}
					Monitor.log(ModularNetLayer.class, Level.WARN,
							"Cannot deliver message "
							+ message.getPayload() + " at netID=" + myID
							+ " as no message msgListeners registered");
				} else {
					for (NetMessageListener listener : msgListeners) {
						listener.messageArrived(event);
					}
				}
			} else {
				//log.debug("Dropping message " + message + ", because receiver "
				//		+ this + " is offline.");
				NetLayerLiveMonitoring.getOfflineMsgDrop().droppedMessage();
				if (hasAnalyzer) {
					netAnalyzerProxy.netMsgEvent(message, getHost(),
							Reason.DROP);
				}
			}
		}

		@Override
		public void dropMessage(NetMessage netMsg) {
			NetLayerLiveMonitoring.getTrafCtrlMsgDrop().droppedMessage();
			//log.debug("Dropping message " + netMsg
			//		+ ", because the receiver's (" + ModularNetLayer.this
			//		+ ") traffic control mechanism has decided it.");
			if (hasAnalyzer) {
				netAnalyzerProxy.netMsgEvent(netMsg, getHost(), Reason.DROP);
			}
		}

		@Override
		public Bandwidth getMaxBW() {
			return receiverBW;
		}

		@Override
		public Bandwidth getBandwidthOfSender() {
			return senderBW;
		}

	}

	/**
	 * Get the subnet this NetLayer operates on
	 * 
	 * @return
	 */
	public AbstractModularSubnet getSubnet() {
		return subnet;
	}

	private long lastSchRcvTime = -1;

	protected long getLastSchRcvTime() {
		return lastSchRcvTime;
	}

	public void setLastSchRcvTime(long lastSchRcvTime) {
		this.lastSchRcvTime = lastSchRcvTime;
	}

	@Override
	public void goOffline() {
		super.goOffline();
		getSubnet().netLayerWentOffline(this);
	}

	@Override
	public void goOnline() {
		super.goOnline();
		getSubnet().netLayerWentOnline(this);
	}

}
