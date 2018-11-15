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

package de.tud.kom.p2psim.impl.util.geo.maps;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.util.geo.maps.Map;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView;
import de.tud.kom.p2psim.impl.util.geo.maps.AbstractMap.Axis;
import de.tud.kom.p2psim.impl.util.geo.maps.osm.OSMMap;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.Component;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

public class MapLoader implements Component {
	private java.util.Map<String, Map> maps = Maps.newHashMap();
	private PositionVector dimensions = null;
	
	@XMLConfigurableConstructor({"worldX", "worldY"})
	public MapLoader(double worldX, double worldY) {
		dimensions = new PositionVector(worldX, worldY);

		VisualizationTopologyView.VisualizationInjector
				.setWorldCoordinates((int) worldX, (int) worldY);
	}

    /**
     * Returns the dimensions the maps are scaled to. This should match the
     * visual world size configured in the visualization topology.
     *
     * @return
     */
    public PositionVector getScaledDimensions() {
        return dimensions;
    }
	
	public void setMap(Map map) {
		maps.put(map.getName(), map);
	}
	
	public Map getMap(String name) {
		Map map = maps.get(name);
		
		if (map == null)
			return null;
		
		if (!map.isLoaded()) {
			map.loadMap();
			
			configureToScenario(map);
		}
		
		return map;
	}

	private void configureToScenario(Map map) {
		// Map the longitude and latitude to width and height of the simulated world 
		if (map instanceof AbstractMap) {
			Monitor.log(MapLoader.class, Level.INFO, "Map " + map.getName()
					+ " is being scaled to " + dimensions);
			((AbstractMap)map).mapToWorld(dimensions);
		}
		if (map instanceof OSMMap) {
			if (!((AbstractMap)map).isSwapped(Axis.Y_AXIS)) {
				((AbstractMap)map).swapWorld(Axis.Y_AXIS, dimensions.getY());
			}
		}
	}
}
