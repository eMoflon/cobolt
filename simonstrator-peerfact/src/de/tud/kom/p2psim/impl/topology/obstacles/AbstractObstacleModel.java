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

package de.tud.kom.p2psim.impl.topology.obstacles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModelListener;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.api.util.geo.maps.Map;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.geo.maps.MapChangeListener;
import de.tud.kom.p2psim.impl.util.geo.maps.MapLoader;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This abstract obstacle model helps to created new 
 * models for generating obstacles.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 */
public abstract class AbstractObstacleModel implements ObstacleModel {

	private PositionVector worldDimensions;

	private ArrayList<ObstacleModelListener> listeners = new ArrayList<ObstacleModelListener>();
	private GeometryFactory factory = new GeometryFactory();
	protected java.util.Map<Tuple<String, String>, Color> colors = Maps.newHashMap();
	
	protected WaypointModel waypointModel = null;
	
	private String mapName = "";
	private Map map = null;
    private boolean defaultConfiguratorSupportInit = false;
	
	public void setMap(String mapName) {
		this.mapName = mapName;
	}
	
	public void setInit(String dummy) {
        defaultConfiguratorSupportInit = true;
    }
	
	@Configure
	public void _configure(Configurator configurator) {
		MapLoader mapLoader = (MapLoader)configurator.getConfigurable("MapLoader");
		
		if (mapLoader == null) {
			throw new ConfigurationException("No MapLoader was configured. Unable to retrieve the map '" + mapName + "'.");
		}
		
		Map map = mapLoader.getMap(mapName);
		
		if (map == null) {
			throw new ConfigurationException("Couldn't retrieve the map '" + mapName + "' from the MapLoader. Make sure the map is configured.");
		}
		
		this.map = map;
		
		map.addMapChangeListener(new MapChangeListener() {
			@Override
			public void mapChanged(MapEvent event) {
				if (event instanceof ObstacleEvent) {
					notifyAddedObstacle(((ObstacleEvent)event).getObstacle());
				}
			}
		});

		PositionVector mapBorder = map.getDimensions().clone();
		mapBorder.divide(getWorldDimensions());
		if (mapBorder.getEntry(0) != 1 || mapBorder.getEntry(1) != 1) {
			Monitor.log(AbstractObstacleModel.class, Level.WARN,
					"You specified WORLD to be "
					+ getWorldDimensions().toString()
					+ " and used an Map with " + map.getDimensions()
					+ ", resulting in wrong scaling.");
		}
	}
	
	protected PositionVector getWorldDimensions() {
		return worldDimensions;
	}

	@Override
	public void setWorldDimensions(PositionVector worldDimensions) {
		this.worldDimensions = worldDimensions;
		
		if (defaultConfiguratorSupportInit) {
            _configure(Simulator.getConfigurator());
        }
	}
	
	@Override
	public List<Obstacle> getObstacles() {
		if (map == null)
			return Lists.newArrayList();
		
		return map.getObstacles();
	}
	
	@Override
	public void addListener(ObstacleModelListener listener) {
		this.listeners.add(listener);
	}
	
	@Override
	public void removeListener(ObstacleModelListener listener) {
		this.listeners.remove(listener);
	}

	protected void notifyAddedObstacle(Obstacle obstacle) {
		for (ObstacleModelListener l : listeners) {
			l.addedObstacle(obstacle);
		}
	}
	
	public void setAddColor(String[] colorPattern) {
		if (colorPattern.length != 3) 
			return;
		
		int cint = Integer.parseInt(colorPattern[2], 16);
		
		Color color = new Color(cint);
		
		colors.put(Tuple.create(colorPattern[0], colorPattern[1]), color);
	}

	public java.util.Map<Tuple<String, String>, Color> getObstacleColors() {
		return this.colors;
	}
	
	@Override
	public void setWaypointModel(WaypointModel model) {
		this.waypointModel = model;
	}
	
	public Obstacle getEnclosing(PositionVector loc) {
        Coordinate coordinate = new Coordinate(loc.getX(), loc.getY());
        Point point = factory.createPoint(coordinate);

        for (Obstacle o : map.getObstacles()) {
            if (o.contains(point)) {
                return o;
            }
        }

        return null;

    }

	@Override
	public boolean contains(PositionVector loc) {
		Coordinate coordinate = new Coordinate(loc.getX(), loc.getY());
		Point point = factory.createPoint(coordinate);
		
		for (Obstacle o : map.getObstacles()) {
			if (o.contains(point)) {
				return true;
			}
		}
		
		return false;
	}
	
	public abstract void generateObstacles();
}
