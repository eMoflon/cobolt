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

package de.tud.kom.p2psim.impl.topology.waypoints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jgrapht.alg.DijkstraShortestPath;

import com.google.common.collect.Sets;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModelListener;
import de.tud.kom.p2psim.api.util.geo.maps.Map;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.DefaultWeightedEdgeRetrievableGraph;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.geo.maps.MapChangeListener;
import de.tud.kom.p2psim.impl.util.geo.maps.MapLoader;
import de.tud.kom.p2psim.impl.util.structures.KdTree;
import de.tud.kom.p2psim.impl.util.structures.KdTree.Entry;
import de.tud.kom.p2psim.impl.util.structures.WaypointKdTree;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * The abstract way point model manages a graph for the map data and holds
 * additional information for the fast retrieval of way points.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 */
public abstract class AbstractWaypointModel implements WaypointModel {

	private PositionVector worldDimensions;

	private Map map;

	// private DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> graph;
	private ArrayList<WaypointModelListener> listeners = new ArrayList<WaypointModelListener>();

	// k-d tree for fast retrieval of nearby waypoints
	private KdTree<Waypoint> kdTree = new WaypointKdTree(2);

	private ObstacleModel obstacleModel = null;
	
	// Determines if the loaded model should be scaled or truncated to fit the given world size
	private boolean scaleModel = true;

	private String mapName = "";

	public AbstractWaypointModel() {
		//
	}

	@Configure()
	@After(optional={ObstacleModel.class})
	public void _configure(Configurator configurator) {
		MapLoader mapLoader = (MapLoader)configurator.getConfigurable(MapLoader.class.getSimpleName());
		
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
				if (event instanceof PathEvent) {
					notifyAddedPath(((PathEvent)event).getPath());
				} else if (event instanceof WaypointEvent) {
					notifyAddedWaypoint(((WaypointEvent)event).getWaypoint());
				} else {
					notifyModifiedWaypoints();
				}
			}
		});

		Monitor.log(AbstractWaypointModel.class, Level.INFO,
				"The '" + map.getName() + "' " + map.getClass().getSimpleName()
						+ " has dimensions of " + map.getDimensions().getX()
						+ "x" + map.getDimensions().getY() + ".");

		PositionVector mapBorder = map.getDimensions().clone();
		mapBorder.divide(getWorldDimensions());
		if (mapBorder.getEntry(0) != 1 || mapBorder.getEntry(1) != 1) {
			Monitor.log(
					AbstractWaypointModel.class,
					Level.WARN,
					"You specified WORLD to be "
							+ getWorldDimensions().toString()
							+ " and used an OSM Map with "
							+ map.getDimensions()
							+ ", resulting in wrong scaling.");
		}
		
		init();
	}
	
	public abstract void init();

	public Map getMap() {
		return map;
	}

	public void setMap(String mapName) {
		this.mapName = mapName;
	}
	
	public void setScale(boolean shouldScale) {
		this.scaleModel = shouldScale;
	}
	
	public boolean isScaled() {
		return scaleModel;
	}

	@Override
	public double getScaleFactor() {
		if (!this.scaleModel) {
			return 1;
		}

        double mapX = getMap().getDimensions().getX();
        double mapY = getMap().getDimensions().getY();

        double worldX = getWorldDimensions().getX();
        double worldY = getWorldDimensions().getY();

        double factor = Math.abs((worldX + worldY) / (mapX + mapY));

		return factor;
	}

	@Override
	public void setObstacleModel(ObstacleModel model) {
		obstacleModel = model;
	}

	public PositionVector getWorldDimensions() {
		return worldDimensions;
	}

	@Override
	public void setWorldDimensions(PositionVector worldDimensions) {
		this.worldDimensions = worldDimensions;
	}

	@Override
	public Collection<Waypoint> getWaypoints(Class type) {
		return map.getWaypoints(type);
	}

	@Override
	public Waypoint getClosestWaypoint(PositionVector position) {
		return getClosestWaypoint(position, Waypoint.class);
	}

	@Override
	public Waypoint getClosestWaypoint(PositionVector position, Class type) {
		double distance = -1;
		Waypoint waypoint = null;

		for (Waypoint w : map.getGraph().vertexSet()) {
			if (!w.getClass().equals(type))
				continue;

			double d = position.distanceTo(w.getPosition());

			if (distance > d || distance == -1) {
				distance = d;
				waypoint = w;
			}
		}

		return waypoint;
	}

	public Waypoint getClosestWaypointKd(PositionVector position, Class type) {
		List<Entry<Waypoint>> waypoints = kdTree.nearestNeighbor(
				position.asDoubleArray(), 1, false);

		if (waypoints.isEmpty())
			return null;

		return waypoints.get(0).value;
	}

	@Override
	public List<Tuple<Waypoint, Path>> getConnectedWaypoints(Waypoint waypoint) {
		return getConnectedWaypoints(waypoint, Waypoint.class);
	}

	@Override
	public List<Tuple<Waypoint, Path>> getConnectedWaypoints(Waypoint waypoint,
			Class type) {
		Set<Path> paths = map.getGraph().edgesOf(waypoint);
		ArrayList<Tuple<Waypoint, Path>> waypointsAndPaths = new ArrayList<Tuple<Waypoint, Path>>();

		for (Path p : paths) {
			Waypoint destinationWaypoint = null;

			if (p.getSource().equals(waypoint))
				destinationWaypoint = p.getTarget();
			else if (p.getTarget().equals(waypoint))
				destinationWaypoint = p.getSource();

			if (destinationWaypoint.getClass().equals(type))
				waypointsAndPaths.add(new Tuple<Waypoint, Path>(
						destinationWaypoint, p));
		}

		return waypointsAndPaths;
	}

	@Override
	public int getNumberOfWaypoints(Class type) {
		Collection<Waypoint> wpList = map.getWaypoints(type);
		if (wpList == null)
			return -1;

		return wpList.size();
	}

	@Override
	public void addListener(WaypointModelListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void removeListener(WaypointModelListener listener) {
		this.listeners.remove(listener);
	}

	private void notifyAddedWaypoint(Waypoint waypoint) {
		for (WaypointModelListener l : listeners) {
			l.addedWaypoint(waypoint);
		}
	}
	
	private void notifyModifiedWaypoints() {
		for (WaypointModelListener l : listeners) {
			l.modifiedWaypoints();
		}
	}

	private void notifyAddedPath(Path path) {
		for (WaypointModelListener l : listeners) {
			l.addedPath(path);
		}
	}

	@Override
	public PositionVector getMetricDimensions() {
		return worldDimensions;
	}

	@Override
	public abstract void generateWaypoints();

	@Override
	public Set<Waypoint> getWaypoints() {
		if (map != null) {
			return map.getGraph().vertexSet();
		} else {
			return Sets.newHashSet();
		}
	}

	@Override
	public Set<Path> getPaths() {
		return map.getGraph().getAllEdges();
	}

	@Override
	public List<Path> getShortestPath(Waypoint start, Waypoint end) {
		DijkstraShortestPath<Waypoint, Path> dijkstrashortestpath = new DijkstraShortestPath<Waypoint, Path>(
				map.getGraph(), start, end);
		
		List<Path> paths = dijkstrashortestpath.getPathEdgeList();
		

		return dijkstrashortestpath.getPathEdgeList();
	}

	@Override
	public int getNumberOfWaypoints() {
		return map.getGraph().vertexSet().size();
	}

	@Override
	public DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> getGraph() {
		if (map == null)
			return null;
		
		return map.getGraph();
	}

	public void addWaypoint(Waypoint wp) {
		map.addWaypoint(wp);
	}
}
