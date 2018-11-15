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

package de.tud.kom.p2psim.impl.common;

import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.common.HostProperties;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.linklayer.LinkLayer;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransportComponent;

/**
 * Default implementation of a host.
 */
public class DefaultHost implements SimHost {

	/*
	 * "New Host"
	 */
	private final List<HostComponent> components = new LinkedList<HostComponent>();

	private final long uniqueHostId;

	private SimNetworkComponent networkComponent = null;

	private TransportComponent transportComponent = null;

	public DefaultHost() {
		hostCounter++;
		this.uniqueHostId = hostCounter;
	}

	@Override
	public <T extends HostComponent> T getComponent(Class<T> componentClass)
			throws ComponentNotAvailableException {
		for (HostComponent component : components) {
			if (componentClass.isInstance(component)) {
				return componentClass.cast(component);
			}
		}
		throw new ComponentNotAvailableException();
	}

	@Override
	public <T extends HostComponent> List<T> getComponents(
			Class<T> componentClass) throws ComponentNotAvailableException {
		List<T> match = new LinkedList<T>();
		for (HostComponent component : components) {
			if (componentClass.isInstance(component)) {
				match.add(componentClass.cast(component));
			}
		}
		if (match.isEmpty()) {
			throw new ComponentNotAvailableException();
		} else {
			return match;
		}
	}

	@Override
	public <T extends HostComponent> void registerComponent(T component) {
		if (!components.contains(component)) {
			components.add(component);
		} else {
			throw new UnsupportedOperationException("The component "
					+ component.toString() + " is already registered!");
		}
	}

	@Override
	public <T extends HostComponent> boolean removeComponent(T component) {
		assert component != null;
		if (components.remove(component)) {
			component.shutdown();
			return true;
		}
		return false;
	}

	@Override
	public SimNetworkComponent getNetworkComponent() {
		if (networkComponent == null) {
			try {
				networkComponent = getComponent(SimNetworkComponent.class);
			} catch (ComponentNotAvailableException e) {
				Monitor.log(DefaultHost.class, Level.ERROR, "No NetworkComponent found in the current configuration!");
			}
		}
		return networkComponent;
	}

	@Override
	public TransportComponent getTransportComponent() {
		if (transportComponent == null) {
			try {
				transportComponent = getComponent(TransportComponent.class);
			} catch (ComponentNotAvailableException e) {
				Monitor.log(DefaultHost.class, Level.ERROR, "No TransportComponent found in the current configuration!");
			}
		}
		return transportComponent;
	}

	/**
	 * @deprecated use getId instead (and the value() method)
	 */
	@Override
	@Deprecated
	public long getHostId() {
		return uniqueHostId;
	}
	
	@Override
	public INodeID getId() {
		return INodeID.get(uniqueHostId);
	}

	/*
	 * "Old Host"
	 */

	private NetLayer netLayer;

	private LinkLayer linkLayer;

	private EnergyModel energyModel;

	private TopologyComponent topoComponent;

	private HostProperties properties;

	private static long hostCounter = 0;

	public void setProperties(HostProperties properties) {
		this.properties = properties;
	}

	@Override
	public LinkLayer getLinkLayer() {
		if (linkLayer == null) {
			try {
				linkLayer = getComponent(LinkLayer.class);
			} catch (ComponentNotAvailableException e) {
				//
			}
		}
		return linkLayer;
	}

	@Override
	public EnergyModel getEnergyModel() {
		if (energyModel == null) {
			try {
				energyModel = getComponent(EnergyModel.class);
			} catch (ComponentNotAvailableException e) {
				//
			}
		}
		return energyModel;
	}

	@Override
	public TopologyComponent getTopologyComponent() {
		if (topoComponent == null) {
			try {
				topoComponent = getComponent(TopologyComponent.class);
			} catch (ComponentNotAvailableException e) {
				//
			}
		}
		return topoComponent;
	}

	@Override
	public HostProperties getProperties() {
		return properties;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Host {");
		sb.append("ID=").append(this.getId()).append(", ");
		sb.append("GroupID=");
		if (this.properties != null)
			sb.append(this.properties.getGroupID());
		sb.append(", nw=");
		if (this.netLayer != null)
			sb.append(netLayer.getNetID());
		sb.append(", #olays=");
		sb.append("}");

		return sb.toString();
	}

	@Override
	public void initialize() {
		try {
			List<HostComponent> components = getComponents(HostComponent.class);
			for (HostComponent hostComponent : components) {
				hostComponent.initialize();
			}
		} catch (ComponentNotAvailableException e) {
			throw new AssertionError(
					"This host was configured without any components!");
		}
	}

}
