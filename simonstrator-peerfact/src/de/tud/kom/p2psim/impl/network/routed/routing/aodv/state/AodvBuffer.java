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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv.state;

import java.util.HashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 *
 * @author Christoph Neumann
 */
public class AodvBuffer<T> {

	private Map<T, Long> buffer = new HashMap<T, Long>();

	public void put(T obj, long duration) {
		Long validUntil = Time.getCurrentTime() + duration;
		buffer.put(obj, validUntil);
	}

	public boolean contains(T obj) {
		if (!buffer.containsKey(obj))
			return false;

		Long validUntil = buffer.get(obj);

		boolean valid = Time.getCurrentTime() <= validUntil;

		// remove expired objects from the buffer
		if (!valid)
			buffer.remove(obj);

		return valid;
	}
}
