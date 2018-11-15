/*
 * Copyright (c) 2005-2015 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction;

import java.util.Random;

import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This is the implementation of a AttractionPoint. This type of
 * {@link AttractionPoint} has not the ability to be moved. Its data come from
 * osm-POIs, which are static locations
 * 
 * @author Martin Hellwig
 * @version 1.0, 02.07.2015
 */
public class AttractionPoint {
	protected static Random rnd = Randoms.getRandom(AttractionPoint.class);

	private PositionVector posVec;

	private String name;

	@XMLConfigurableConstructor({ "x", "y", "name" })
	public AttractionPoint(int x, int y, String name) {
		this(new PositionVector(x, y), name);
	}

	public AttractionPoint(PositionVector posVec, String name) {
		this.posVec = posVec;
		this.name = name;
	}

	public PositionVector getRealPosition() {
		return posVec;
	}

	public String getName() {
		return name;
	}
}
