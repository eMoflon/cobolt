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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction;

import java.util.List;

/**
 * This is the interface for the generator of the {@link AttractionPoint}s. It
 * gets the set number of AttractionPoints back. This mean, it will be generate
 * them and set the Position of them.
 * 
 * @author Christoph Muenker
 * @version 1.0, 02.07.2013
 */
public interface IAttractionGenerator {

	public List<AttractionPoint> getAttractionPoints();
	
}
