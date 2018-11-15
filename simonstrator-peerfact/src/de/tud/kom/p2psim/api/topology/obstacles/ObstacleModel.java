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

import java.awt.Color;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.Tuple;


public interface ObstacleModel {
	public void setWorldDimensions(PositionVector worldDimensions);
	public void addListener(ObstacleModelListener listener);
	public void removeListener(ObstacleModelListener listener);
	public List<Obstacle> getObstacles();
	public void generateObstacles();
	public Map<Tuple<String, String>, Color> getObstacleColors();
	
	public void setWaypointModel(WaypointModel model);
	public boolean contains(PositionVector loc);
}
