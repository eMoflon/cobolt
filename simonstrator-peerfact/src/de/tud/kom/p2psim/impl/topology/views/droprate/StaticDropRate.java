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

package de.tud.kom.p2psim.impl.topology.views.droprate;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.DropProbabilityDeterminator;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A fixed drop probability
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 24.07.2012
 */
public class StaticDropRate implements DropProbabilityDeterminator {

	private double dropRate;

	@XMLConfigurableConstructor({ "dropRate" })
	public StaticDropRate(double dropRate) {
		this.dropRate = dropRate;
	}

	@Override
	public void onMacAdded(MacLayer mac, TopologyView viewParent) {
		//
	}

	@Override
	public double getDropProbability(TopologyView view, MacAddress source,
			MacAddress destination, Link link) {
		return dropRate;
	}

}
