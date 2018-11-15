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

package de.tudarmstadt.maki.simonstrator.overlay;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;

/**
 * Base for Overlay-Contacts
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07/07/2011
 */
public class BasicOverlayContact implements OverlayContact, Serializable {

	private static final long serialVersionUID = 1L;

	private INodeID nodeId;

	private Map<NetInterfaceName, NetID> netIds = new LinkedHashMap<>();

	private Map<NetInterfaceName, Integer> ports = new LinkedHashMap<>();

	private transient int _cachedSize = -1;

	@SuppressWarnings("unused")
	private BasicOverlayContact() {
		// for Kryo
	}

	/**
	 * Using the {@link INodeID} of the host
	 * 
	 * @param nodeId
	 */
	public BasicOverlayContact(INodeID nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * Convenience constructor contacts with only one netInterface
	 * 
	 * @param nodeID
	 * @param transInfo
	 */
	public BasicOverlayContact(INodeID nodeId, NetInterfaceName netName,
			NetID netId, int port) {
		this.nodeId = nodeId;
		this.netIds.put(netName, netId);
		this.ports.put(netName, port);
	}

	/**
	 * @deprecated explicitly specify the NetInterfaceName Instead!
	 * @param nodeID
	 * @param transInfo
	 */
	@Deprecated
	public BasicOverlayContact(INodeID nodeID, TransInfo transInfo) {
		this(nodeID, NetInterfaceName.ETHERNET, transInfo.getNetId(), transInfo.getPort());
	}
	
	/**
	 * Add a new contact information to this contact. Only one info per NetName
	 * is stored - existing entries are replaced by new ones if the netName is
	 * already in use.
	 * 
	 * @param netName
	 * @param transInfo
	 * @return
	 */
	public BasicOverlayContact addInformation(NetInterfaceName netName, NetID netId, int port) {
		this.netIds.put(netName, netId);
		this.ports.put(netName, port);
		_cachedSize = -1;
		return this;
	}

	/**
	 * Legacy support for {@link TransInfo}
	 * 
	 * @param netName
	 * @param transInfo
	 * @return
	 */
	public BasicOverlayContact addTransInfo(NetInterfaceName netName, TransInfo transInfo) {
		this.netIds.put(netName, transInfo.getNetId());
		this.ports.put(netName, transInfo.getPort());
		_cachedSize = -1;
		return this;
	}

	/**
	 * Removes the TransInfo for the given NetName
	 * 
	 * @param netName
	 * @return
	 */
	public BasicOverlayContact removeTransInfo(NetInterfaceName netName) {
		this.netIds.remove(netName);
		this.ports.remove(netName);
		_cachedSize = -1;
		return this;
	}

	@Override
	public int getTransmissionSize() {
		if (_cachedSize == -1) {
			_cachedSize += nodeId.getTransmissionSize();
			for (NetID netId : netIds.values()) {
				_cachedSize += netId.getTransmissionSize();
				_cachedSize += 2; // port
			}
		}
		return _cachedSize;
	}

	@Override
	public INodeID getNodeID() {
		return nodeId;
	}

	@Override
	public NetID getNetID(NetInterfaceName netInterface) {
		if (!netIds.containsKey(netInterface)) {
			return null;
		}
		return netIds.get(netInterface);
	}

	@Override
	public int getPort(NetInterfaceName netInterface) {
		if (!ports.containsKey(netInterface)) {
			return -1;
		}
		return ports.get(netInterface);
	}

	@Override
	public Collection<NetInterfaceName> getInterfaces() {
		return Collections.unmodifiableCollection(netIds.keySet());
	}

	@Override
	public String toString() {
		return "Contact " + nodeId + ": " + netIds.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nodeId == null) ? 0 : nodeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicOverlayContact other = (BasicOverlayContact) obj;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		return true;
	}

}
