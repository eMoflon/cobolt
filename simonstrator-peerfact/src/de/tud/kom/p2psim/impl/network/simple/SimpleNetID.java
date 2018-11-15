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

import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class SimpleNetID implements NetID {
	Integer id;

	public SimpleNetID(Integer id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object obj) {
		if (!SimpleNetID.class.isInstance(obj))
			return false;
		SimpleNetID id2 = (SimpleNetID) obj;
		return id.equals(id2.id);
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id.toString();
	}

	@Override
	public int getTransmissionSize() {
		return 2;
	}

}
