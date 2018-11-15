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

package de.tud.kom.p2psim.api.topology.obstacles;

import java.util.List;

import com.vividsolutions.jts.geom.Geometry;

import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.impl.topology.PositionVector;

/**
 * An obstacle (Hosts can not run through them, and communication may be either
 * impossible or at least disturbed by them).
 * 
 * It makes use of {@link PositionVector} instead of {@link Position} as it is
 * only used within a Topology where we operate solely with PositionVectors.
 * 
 * Just a thought: you might implement a Class that acts as a obstacle
 * (representing a whole scenario of obstacles) and a movement-model at the same
 * time to add trace-parsing behavior to PeerfactSim.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 21.02.2012
 */
public interface Obstacle {

	/**
	 * This method checks if the line defined by the two points intersects the
	 * Obstacle.
	 * 
	 * @param l
	 * @return
	 */
	public boolean intersectsWith(PositionVector a, PositionVector b);

	/**
	 * Length of the intersection of the line with the obstacle
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public double totalIntersectionLength(PositionVector a, PositionVector b);

	/**
	 * Depending on the Obstacle (material...) you might return a damping factor
	 * 
	 * @return damping factor between 0 and 1.0, where 1.0 expresses total loss
	 *         of the signal. The {@link TopologyView} has to interpret this
	 *         value - this method does not imply any additional meaning to the
	 *         value.
	 */
	public double dampingFactor();

	public boolean contains(Geometry createPoint);
	
	public Geometry getGeometry();

	/**
	 * Return the vertices of the underlying structure or a close
	 * approximation.
	 * 
	 * @return
	 */
	public List<PositionVector> getVertices();

}
