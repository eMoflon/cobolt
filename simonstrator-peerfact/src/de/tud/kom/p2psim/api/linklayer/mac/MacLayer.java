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

package de.tud.kom.p2psim.api.linklayer.mac;

import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.energy.EnergyCommunicationComponent;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.linklayer.LinkLayerMessage;
import de.tud.kom.p2psim.api.linklayer.LinkMessageListener;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * The MAC (Medium Access Control) is a central part of the LinkLayer. In
 * PeerfactSim.KOM a LinkLayer can be configured with multiple MAC-Layers, each
 * taking care of a different physical medium.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 20.02.2012
 */
public interface MacLayer extends SimHostComponent, EventHandler {

	/**
	 * Returns our own Address in this MAC/PHY-Layer
	 * 
	 * @return
	 */
	public MacAddress getMacAddress();
	
	/**
	 * The NetID used via this mac (convenience method)
	 * 
	 * @return
	 */
	public NetID getNetId();

	/**
	 * The {@link TopologyView} this MAC operates on
	 * 
	 * @return
	 */
	public TopologyView getTopologyView();

	/**
	 * This component is used to account for energy consumption on the MacLayer
	 * 
	 * @return
	 */
	public EnergyCommunicationComponent getEnergyComponent();

	/**
	 * The PHY this MAC operates on
	 * 
	 * @return
	 */
	public PhyType getPhyType();

	/**
	 * Tell this MAC to go online.
	 */
	public void goOnline();

	/**
	 * Tell this MAC to go offline. Messages in the outgoing queue will be
	 * dropped. The MAC will no longer receive Messages.
	 */
	public void goOffline();

	/**
	 * Is this MAC currently online?
	 * 
	 * @return
	 */
	public boolean isOnline();

	/**
	 * Bandwidth of this MAC (this is the connection BW, i.e. the maximum BW if
	 * no queue is assumed). This will not change over time.
	 * 
	 * @return
	 */
	public BandwidthImpl getMaxBandwidth();

	/**
	 * This Bandwidth takes into account the time a message spent in the queue -
	 * this is the bandwidth a netlayer would see for its packets. This will
	 * change over time (which will be reflected in updates of the BW-Object).
	 * It is the bandwidth that is currently free (i.e. available to the user).
	 * 
	 * @return
	 */
	public BandwidthImpl getCurrentBandwidth();

	/**
	 * Send the Message through the MAC. This might involve further partitioning
	 * of the message as well as a number of retransmits. Finally, if the
	 * message made it through the Link, we issue a receive-Event on the
	 * receiving hosts.
	 * 
	 * @param receiver
	 * @param message
	 */
	public void send(MacAddress receiver, LinkLayerMessage message);

	/**
	 * The {@link LinkLayer} will register itself as a LinkMessageListener with
	 * each MAC
	 * 
	 * @param listener
	 */
	public void setMessageListener(LinkMessageListener listener);

}
