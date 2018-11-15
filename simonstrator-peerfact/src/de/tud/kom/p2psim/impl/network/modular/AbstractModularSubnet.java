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

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.impl.network.AbstractSubnet;
import de.tud.kom.p2psim.impl.network.modular.livemon.NetLayerLiveMonitoring;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tud.kom.p2psim.impl.util.BackToXMLWritable;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * Base class for Subnets within the modular Net-Layer. This allows for
 * different subnet implementations if a subnet should not make the "big cloud"
 * assumption but instead rely on some kind of topology and routing.
 * 
 * @author Leo Nobach, modified by Bjoern Richerzhagen (v 1.1)
 * @version 1.1, 04/13/2011
 */
public abstract class AbstractModularSubnet extends AbstractSubnet implements
		BackToXMLWritable {

	//private static Logger log = SimLogger.getLogger(AbstractModularSubnet.class);

	/**
	 * Used strategies
	 */
	private IStrategies strategies;

	/**
	 * Whether to deliver messages in the right order or not
	 */
	private boolean inOrderDelivery = false;

	/**
	 * List of all NetLayers
	 */
	private Map<NetID, ModularNetLayer> allNetLayers = new HashMap<NetID, ModularNetLayer>();

	/**
	 * Needed for creation of a subnet from config.xml, strategies should be
	 * provided via setStrategies
	 */
	public AbstractModularSubnet() {
		// nothing to do here
	}

	/**
	 * Create a new Subnet with the provided strategies
	 * 
	 * @param strategies
	 */
	public AbstractModularSubnet(IStrategies strategies) {
		this();
		setStrategies(strategies);
	}

	/**
	 * Convenience function for a check of PLoss-Strategy and assignment of
	 * Log-Messages and Monitor-Events
	 * 
	 * @return
	 */
	protected boolean shallBeDropped(ModularNetMessage msg,
			ModularNetLayer nlSender, ModularNetLayer nlReceiver) {

		boolean shallBeDropped = getStrategies().getPLossStrategy().shallDrop(
				msg, nlSender, nlReceiver, getDB());

		if (shallBeDropped) {
			if (msg.getPayload() != null
					&& msg.getPayload() instanceof AbstractTransMessage) {
				int assignedMsgId = determineTransMsgNumber(msg);
				((AbstractTransMessage) msg.getPayload())
						.setCommId(assignedMsgId);
			}
			//log.debug("Dropping message " + msg
			//		+ ", because of the packet loss strategy that is used.");
			NetLayerLiveMonitoring.getSubnetMsgDrop().droppedMessage();
			try {
				Monitor.get(NetlayerAnalyzer.class).netMsgEvent(msg,
						nlSender.getHost(), Reason.DROP);
			} catch (AnalyzerNotAvailableException e) {
				// nothing to do
			}
			return true;
		} else {
			NetLayerLiveMonitoring.getSubnetMsgDrop().noDropMessage();
			return false;
		}
	}

	/**
	 * Calculate the Receive-Time and delay+jitter
	 * 
	 * @param msg
	 * @param nlSender
	 * @param nlReceiver
	 * @return
	 */
	protected long getRcvTime(ModularNetMessage msg, ModularNetLayer nlSender,
			ModularNetLayer nlReceiver) {
		long delay = getStrategies().getLatencyStrategy()
				.getMessagePropagationDelay(msg, nlSender, nlReceiver, getDB());
		long jitter = getStrategies().getJitterStrategy().getJitter(delay, msg,
				nlSender, nlReceiver, getDB());
		return getRcvTime(msg, delay, jitter, nlReceiver);
	}

	/**
	 * Provide Delay and Jitter and get the receive-Time
	 * 
	 * @param msg
	 * @param delay
	 * @param jitter
	 * @param nlReceiver
	 * @return
	 */
	protected long getRcvTime(ModularNetMessage msg, long delay, long jitter,
			ModularNetLayer nlReceiver) {
		long rcvTime;
		if (getUseInOrderDelivery()) {
			rcvTime = Math.max(nlReceiver.getLastSchRcvTime() + 1,
					Time.getCurrentTime() + delay + jitter);
			nlReceiver.setLastSchRcvTime(rcvTime);
		} else {
			rcvTime = Time.getCurrentTime() + delay + jitter;
		}

		//log.debug("Delaying message " + msg + ": delay=" + delay
		//		+ ", jitter=" + jitter);

		return rcvTime;
	}

	/**
	 * On registration a netLayer automatically is online without calling
	 * "goOnline" (possibly a bug). Fixed: added call to goOnline in
	 * ModularNetLayer
	 */
	@Override
	public void registerNetLayer(NetLayer net) {
		allNetLayers.put(net.getNetID(), (ModularNetLayer) net);
	}

	/**
	 * Load a set of strategies for this subnet
	 * 
	 * @param strategies
	 */
	public void setStrategies(IStrategies strategies) {
		this.strategies = strategies;
	}

	/**
	 * Called if a NetLayer goes online.
	 * 
	 * @param net
	 */
	protected abstract void netLayerWentOnline(NetLayer net);

	/**
	 * Called if a NetLayer goes offline.
	 * 
	 * @param net
	 */
	protected abstract void netLayerWentOffline(NetLayer net);

	/**
	 * get the NetLayer of a given NetID
	 * 
	 * @param id
	 * @return The NetLayer or null, if there is no Host with the given ID
	 */
	public ModularNetLayer getNetLayer(NetID id) {
		ModularNetLayer nl = allNetLayers.get(id);
		return nl;
	}

	/**
	 * Get used Strategies
	 * 
	 * @return
	 */
	public IStrategies getStrategies() {
		return strategies;
	}

	/**
	 * Deliver messages in Order, if true
	 * 
	 * @param useInOrderDelivery
	 */
	public void setUseInOrderDelivery(boolean useInOrderDelivery) {
		this.inOrderDelivery = useInOrderDelivery;
	}

	/**
	 * Are messages delivered in order?
	 * 
	 * @return
	 */
	public boolean getUseInOrderDelivery() {
		return this.inOrderDelivery;
	}

}
