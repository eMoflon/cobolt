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



package de.tud.kom.p2psim.impl.network.simple;

import java.util.HashSet;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.network.AbstractNetLayerFactory;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.positioning.SimpleEuclidianPositioning;
import de.tudarmstadt.maki.simonstrator.api.Host;

public class SimpleNetFactory extends AbstractNetLayerFactory {

	final private SimpleSubnet subnet;
	
	final private PositioningStrategy positioningStrategy;

	final private Set<Integer> usedIds = new HashSet<Integer>();

	private int idCounter = 0;

	public SimpleNetFactory() {
		subnet = new SimpleSubnet();
		positioningStrategy = new SimpleEuclidianPositioning();
	}

	public void setLatencyModel(LatencyStrategy model) {
		subnet.setLatencyModel(model);
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.api.common.ComponentFactory#createComponent(de.tud.kom.p2psim.api.common.Host)
	 */
	public SimpleNetLayer createComponent(Host host) {
		final BandwidthImpl bw = getBandwidth(null);
		return new SimpleNetLayer((SimHost) host, subnet, this.createNewID(),
				positioningStrategy.getPosition((SimHost) host, null, null), bw);
	}

	/**
	 * Creates a new SimpleNet object.
	 *
	 * @return the simple net id
	 */
	public SimpleNetID createNewID() {
		while (usedIds.contains(idCounter))
			idCounter++;
		SimpleNetID nextId = new SimpleNetID(idCounter);
		usedIds.add(idCounter++);
		return nextId;
	}

	/**
	 * Parses the id.
	 *
	 * @param s the s
	 * @return the simple net id
	 */
	public SimpleNetID parseID(String s) {
		int id = Integer.parseInt(s);
		SimpleNetID nextId = new SimpleNetID(id);
		usedIds.add(id);
		return nextId;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Simple Network Factory";
	}
}
