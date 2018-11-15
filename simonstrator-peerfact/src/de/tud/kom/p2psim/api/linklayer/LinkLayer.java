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

package de.tud.kom.p2psim.api.linklayer;

import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * The LinkLayer-Component which is part of a Host. Regarding the 802.x-Family
 * this is the LLC (Logical Link Control) that provides transparency regarding
 * the chosen MAC/PHY-Layer (WIFI, Ethernet...). Due to the fact that a NetLayer
 * has to specify the senders MAC-Address when transmitting a message, we know
 * which MAC-Layer to choose.
 * 
 * FIXME: currently, the {@link ConnectivityListener}s are not notified if a
 * host is offline due to the fact that all components where shut down directly
 * via the LinkLayer. If the NetLayer-Interfaces are used, this is not a
 * problem. Later we will extend the {@link ConnectivityEvent} to include
 * component-based information
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface LinkLayer extends SimHostComponent {

	/**
	 * The send-function needs the MAC-address of the destination as well as the
	 * PHY-Type to use. The LLC will then use the MAC-Layer associated with the
	 * phyType to dispatch the message.
	 * 
	 * @param phyType
	 *            specifies the outgoing link
	 * @param destination
	 * @param data
	 */
	public void send(PhyType phyType, MacAddress destination, Message data);

	/**
	 * ARP as a service of the LinkLayer
	 * 
	 * @param netID
	 * @param phy
	 * @return The MacAddress, or null, if no match is found
	 */
	public MacAddress addressResolution(NetID netID, PhyType phy);

	/**
	 * True, if the host is equipped with a MAC providing access to the PHY
	 * 
	 * @return
	 */
	public boolean hasPhy(PhyType phyType);

	/**
	 * Return a MAC for the PHY. Null, if hasPhy() returns false for this phy
	 * 
	 * @param phyType
	 * @return
	 */
	public MacLayer getMac(PhyType phyType);

	/**
	 * Add a MAC-Layer. The LLC is able to cope with a multitude of different
	 * MAC-Layers as specified in the 802.xx-Standard.
	 * 
	 * @param macLayer
	 */
	public void addMacLayer(MacLayer macLayer);

	/**
	 * Are we online in the given Phy?
	 * 
	 * @return
	 */
	public boolean isOnline(PhyType phy);

	/**
	 * On simulation startup all configured PHYs will go online.
	 * 
	 * @param phy
	 */
	public void goOnline(PhyType phy);

	/**
	 * This will cancel message transmissions and delete the queue
	 * 
	 * @param phy
	 */
	public void goOffline(PhyType phy);

	/**
	 * Add a listener (typically a NetLayer) to be informed as soon as a
	 * {@link LinkLayerMessage} arrives.
	 * 
	 * @param listener
	 */
	public void addLinkMessageListener(LinkMessageListener listener);

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public void removeLinkMessageListener(LinkMessageListener listener);

	/**
	 * Adds a listener to be informed as soon as a mac layer changes its
	 * online/offline state or all mac layers have gone offline or online.
	 * 
	 * @param listener
	 */
	public void addMacStateListener(MacStateListener listener);
	
	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public void removeMacStateListener(MacStateListener listener);
	
	/**
	 * Return the maximum bandwidth available on the MAC (this is configured in
	 * the xml-file or determined by the PHY-Type)
	 * 
	 * @param phy
	 * @return
	 */
	public BandwidthImpl getMaxBandwidth(PhyType phy);

	/**
	 * The MAC-Layers maintain a moving average of the current upload bandwidth
	 * (which essentially means taking the time a message had to wait in the
	 * queue). The current download bandwidth will be the same as the maximum
	 * bandwidth configured by the user.
	 * 
	 * @param phy
	 * @return
	 */
	public BandwidthImpl getCurrentBandwidth(PhyType phy);

	/**
	 * Adds a connectivity listener to this LinkLayer
	 * 
	 * @param listener
	 *            the connectivity listener
	 */
	// public void addConnectivityListener(ConnectivityListener listener);
	// TODO has to be extended to allow component-based reactions...

	/**
	 * Removes the installed connectivity listener.
	 * 
	 * @param listener
	 *            the connectivity listener to be removed
	 */
	// public void removeConnectivityListener(ConnectivityListener listener);

}
