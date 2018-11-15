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

package de.tud.kom.p2psim.impl.network.realNetworking;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * The Class RealNetworkRoutingTable.
 */
public class RealNetworkRoutingTable {

	
	private static RealNetworkRoutingTable instance;
	
	private HashMap<NetID, InetAddrAndPort> routingHashMap;

	/**
	 * Instantiates a new real network routing table.
	 */
	private RealNetworkRoutingTable() {
		routingHashMap = new HashMap<NetID, InetAddrAndPort>();
	}

	/**
	 * Gets the single instance of RealNetworkRoutingTable.
	 *
	 * @return single instance of RealNetworkRoutingTable
	 */
	public static RealNetworkRoutingTable getInstance() {
		
		if (null == instance) {
			instance = new RealNetworkRoutingTable();
		}
		
		return instance;
	}
	
	/**
	 * Adds the real inet addr and port.
	 *
	 * @param virtualID the virtual netID from Peerfact
	 * @param realID the real IPv4 Adress
	 * @param realPort the real port
	 */
	public void addRealInetAddrAndPort( NetID virtualID, InetAddress realID, short realPort) {
		
		if( virtualID == null )
			return;
		
		Monitor.log(RealNetworkRoutingTable.class, Level.DEBUG,
				"Adding new host to routing table: Virtual/" + virtualID
						+ " is reachable at Real/" + realID + ":" + realPort);
		routingHashMap.put(virtualID, new InetAddrAndPort(realID, realPort));
	}
	

	public void addRealInetAddrAndPort(NetID virtualID, InetAddress realID) {
		addRealInetAddrAndPort(virtualID, realID, (short) 13000);
	}

	/**
	 * Gets the real inet addr and port for virtual ip.
	 *
	 * @param receiverId the receiver id
	 * @return the real inet addr and port for virtual ip
	 */
	public InetAddrAndPort getRealInetAddrAndPortForVirtualIP(NetID receiverId) {
		return routingHashMap.get(receiverId);		
	}	
	
	
	/**
	 * Gets the boot strap info with all known NetIDs
	 *
	 * @return the boot strap info
	 */
	public Set<NetID> getBootStrapInfo() {
		return this.routingHashMap.keySet();
	}
	
	
	/**
	 * The Class InetAddrAndPort.
	 */
	protected class InetAddrAndPort {

		private InetAddress realID;
		private short realPort;

		/**
		 * Instantiates a new "inet addr and port" - Bundle
		 *
		 * @param realID the real id
		 * @param realPort the real port
		 */
		public InetAddrAndPort(InetAddress realID, short realPort) {
			this.realID = realID;
			this.realPort = realPort;
		}
		
		/**
		 * Gets the inet addr.
		 *
		 * @return the inet addr
		 */
		public InetAddress getInetAddr () {
			return realID;
		}

		/**
		 * Gets the port.
		 *
		 * @return the port
		 */
		public short getPort() {
			return realPort;
		}
		
	}

	
}
