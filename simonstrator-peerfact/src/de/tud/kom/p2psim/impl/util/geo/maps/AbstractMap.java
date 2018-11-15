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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.alg.DijkstraShortestPath;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.util.geo.maps.Map;
import de.tud.kom.p2psim.api.util.geo.maps.Way;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.obstacles.PolygonObstacle;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.DefaultWeightedEdgeRetrievableGraph;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Path;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.PathEdgeFactory;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.geo.maps.MapChangeListener.MapEvent;

public abstract class AbstractMap implements Map {
	private List<Obstacle> obstacles = Lists.newLinkedList();

	private List<Path> paths = Lists.newLinkedList();

	protected DefaultWeightedEdgeRetrievableGraph<Waypoint, Path> graph = new DefaultWeightedEdgeRetrievableGraph<Waypoint, Path>(
			new PathEdgeFactory());
	
	private Multimap<Class, Waypoint> typeWaypointMap = ArrayListMultimap.create();

	protected PositionVector minPosition = new PositionVector(2);

	protected PositionVector maxPosition = new PositionVector(2);

	protected List<Way> ways = Lists.newLinkedList();

	protected boolean[] swapped = new boolean[2];

	protected String filename = null;

	private boolean isLoaded = false;
	
	private String name = "";

	private List<MapChangeListener> mapListeners = Lists.newLinkedList();
	
	// TEMP
	protected PositionVector ppm;
	
	public void loadMap() {
		if (filename == null) {
			throw new ConfigurationException(
					"Unable to load map. Missing a filename, please make sure the configuration contains a file attribute.");
		}

		doLoadMap();

		buildGraph();
		
		//forcefullyConnectGraphs();
		
		removeNotConnectedGraphs();
		
		isLoaded = true;
	}

	protected abstract void doLoadMap();

	protected String getFilename() {
		return filename;
	}
	
	public void setFile(String filename) {
		this.filename = filename;
	}

	protected void buildGraph() {
		graph = new DefaultWeightedEdgeRetrievableGraph<Waypoint, Path>(
				new PathEdgeFactory());

		for (Path path : paths) {
			graph.addVertex(path.getSource());
			graph.addVertex(path.getTarget());
			typeWaypointMap.put(path.getSource().getClass(), path.getSource());
			typeWaypointMap.put(path.getTarget().getClass(), path.getTarget());
			graph.addEdge(path.getSource(), path.getTarget(), path);
		}
	}

	public void addObstacle(Obstacle obstacle) {
		obstacles.add(obstacle);
		raiseMapChanged(new MapChangeListener.ObstacleEvent(obstacle));
	}

	public void addPath(Path path) {
		paths.add(path);
		raiseMapChanged(new MapChangeListener.PathEvent(path));
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void clearPaths() {
		paths.clear();
	}

	/**
	 * Scales the world based on the given vector: coordinate * (world /
	 * dimensions)
	 * 
	 * @param world
	 */
	public void mapToWorld(PositionVector world) {
		PositionVector pixelPerMeter = world.clone();
		pixelPerMeter.divide(getDimensions());
		
		this.ppm = pixelPerMeter;
		
		mapWaypoints(pixelPerMeter);
		
		mapObstacles(pixelPerMeter);
		
		raiseMapChanged(new MapChangeListener.MapToWorldEvent(world));
	}

	private void mapObstacles(PositionVector pixelPerMeter) {
		for (Obstacle o : obstacles) {
			PolygonObstacle p = (PolygonObstacle) o;
			
			List<PositionVector> vertices = p.getVertices();
			List<PositionVector> newVertices = Lists.newArrayList();

			for (PositionVector v : vertices) {
				newVertices.add(toPixelCoords(v, pixelPerMeter));
			}
			p.rebuildPolygon(newVertices);
		}
	}

	private void mapWaypoints(PositionVector pixelPerMeter) {
		for (Waypoint w : graph.vertexSet()) {
			w.setPosition(toPixelCoords(w.getPosition(), pixelPerMeter));
		}
	}

	protected PositionVector toPixelCoords(PositionVector position, PositionVector pixelPerMeter) {
		PositionVector clonedPosition = position.clone();
		PositionVector relativePosition = clonedPosition.minus(getMinPosition());
		relativePosition.multiply(pixelPerMeter);

		return relativePosition;
	}

	public List<Obstacle> getObstacles() {
		return obstacles;
	}

	public PositionVector pos(double x, double y) {
		return new PositionVector(x, y);
	}

	protected void createPath(Waypoint wp1, Waypoint wp2) {
		Path path = new Path(wp1, wp2);
		addPath(path);
	}

	@SuppressWarnings("unchecked")
	public DefaultWeightedEdgeRetrievableGraph getGraph() {
		return graph;
	}

    @Override
	public PositionVector getMinPosition() {
		return cut(minPosition);
	}

    @Override
	public PositionVector getMaxPosition() {
		return cut(maxPosition);
	}

	public void setMinPosition(PositionVector minPosition) {
		this.minPosition = minPosition;
	}

	public void setMaxPosition(PositionVector maxPosition) {
		this.maxPosition = maxPosition;
	}

	public PositionVector getDimensions() {
		return getMaxPosition().minus(getMinPosition());
	}

	public List<Way> getWays() {
		return ways;
	}

	public void swapWorld(Axis axis, double max) {
		for (Waypoint w : graph.vertexSet()) {
			w.getPosition().setEntry(axis.ordinal(),
					max - w.getPosition().getEntry(axis.ordinal()));
		}
		
		for (Obstacle o : obstacles) {
			PolygonObstacle p = (PolygonObstacle) o;
			
			List<PositionVector> vertices = p.getVertices();

			for (PositionVector v : vertices) {
				v.setEntry(axis.ordinal(), max - v.getEntry(axis.ordinal()));
			}
			p.rebuildPolygon(vertices);
		}

		swapped[axis.ordinal()] = !swapped[axis.ordinal()];
		
		raiseMapChanged(new MapChangeListener.SwapMapEvent(axis, max));
	}

	public enum Axis {
		X_AXIS, Y_AXIS
	}

	public boolean isSwapped(Axis axis) {
		return swapped[axis.ordinal()];
	}

	public List<Path> getShortestPath(Waypoint start, Waypoint end) {
		DijkstraShortestPath<Waypoint, Path> dijkstrashortestpath = new DijkstraShortestPath<Waypoint, Path>(
				graph, start, end);

		return dijkstrashortestpath.getPathEdgeList();
	}

	public void addWaypoint(Waypoint wp) {
		graph.addVertex(wp);
		typeWaypointMap.put(wp.getClass(), wp);
	}
	
	public Collection<Waypoint> getWaypoints(Class type) {
		return typeWaypointMap.get(type);
	}
	
	public boolean isLoaded() {
		return this.isLoaded;
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	public void addMapChangeListener(MapChangeListener listener) {
		mapListeners .add(listener);
	}
	
	public void removeMapChangeListener(MapChangeListener listener) {
		mapListeners.remove(listener);
	}
	
	private void raiseMapChanged(MapEvent event) {
		for (MapChangeListener l : mapListeners) {
			l.mapChanged(event);
		}
	}

	/**
	 * Calculates all connected sets in the graph and removes
	 * all waypoints that aren't part of the largest connected set.
	 */
	private void removeNotConnectedGraphs() {
		Queue<List<Waypoint>> queue = getConnectedComponents();
		
		if (queue.size() <= 1)
			return;

		// Remove the connected set with the highest number of
		// waypoints so it won't be touched
		queue.poll();
		
		List<Waypoint> waypoints = queue.poll();
		
		while (waypoints != null) {
			//removeWaypoints(waypoints, WeakWaypoint.class);
			for (Waypoint w : waypoints) {
				Set<Path> edges = Sets.newHashSet(graph.edgesOf(w));
				for (Path p : edges) {
					graph.removeEdge(p);
				}
				graph.removeVertex(w);
			}
			
			waypoints = queue.poll();
		}
	}

	private void forcefullyConnectGraphs() {
		Set<Waypoint> waypoints = graph.vertexSet();
		Queue<List<Waypoint>> queue = getConnectedComponents();
		
		if (queue.size() <= 1)
			return;
		
		queue.poll();
		
		List<Waypoint> connectedSet = queue.poll();
		
		while (connectedSet != null) {
			ArrayList<Tuple<Waypoint, Waypoint>> shortestDistances = new ArrayList<Tuple<Waypoint, Waypoint>>();
			
			for (Waypoint w : connectedSet) {
				Waypoint sd = findClosestWaypoint(w, waypoints);
				
				shortestDistances.add(new Tuple<Waypoint, Waypoint>(w, sd));
			
			}
			
			Tuple<Waypoint, Waypoint> shortestDistancePair = findClosestTuple(shortestDistances);
			
			try {
				createPath(shortestDistancePair.getA(), shortestDistancePair.getB());
			} catch (IllegalArgumentException e) {
				//removeWaypoint(shortestDistancePair.getA());
				
				Set<Path> edges = graph.outgoingEdgesOf(shortestDistancePair.getA());
				for (Path p : edges) {
					graph.removeEdge(p);
				}
				graph.removeVertex(shortestDistancePair.getA());
				
			}
			
			connectedSet = queue.poll();
		}
	}
	
	/**
	 * Searches for the waypoint in the set that is the closest
	 * to the target waypoint.
	 * 
	 * @param target
	 * @param waypoints
	 * @return
	 */
	private Waypoint findClosestWaypoint(Waypoint target, Collection<Waypoint> waypoints) {
		double d = -1;
		Waypoint wp = null;
		
		for (Waypoint w : waypoints) {
			double ld = target.getPosition().distanceTo(w.getPosition());
			
			if (ld < d || d == -1) {
				d = ld;
				wp = w;
			}
		}
		
		return wp;
	}

	/**
	 * Searches for the tuple whos two waypoints are the closest to each other.
	 * 
	 * @param shortestDistances
	 * @return
	 */
	private Tuple<Waypoint, Waypoint> findClosestTuple(ArrayList<Tuple<Waypoint, Waypoint>> shortestDistances) {
		Tuple<Waypoint, Waypoint> shortestDistance = null;
		double d = -1;
		
		for (Tuple<Waypoint, Waypoint> wpT : shortestDistances) {
			double ld = wpT.getA().getPosition()
					.distanceTo(wpT.getB().getPosition());
			
			if (ld < d || d == -1) {
				d = ld;
				shortestDistance = wpT;
			}
		}
		
		return shortestDistance;
	}

	private Queue<List<Waypoint>> getConnectedComponents() {
		ConnectivityInspector ci = new ConnectivityInspector<Waypoint, Path>(getGraph());

		@SuppressWarnings("unchecked")
		List<Set<Waypoint>> connectedSets = ci.connectedSets();
		
		PriorityQueue<List<Waypoint>> queue = new PriorityQueue<List<Waypoint>>(1, new Comparator<List<Waypoint>>() {
			@Override
			public int compare(List<Waypoint> o1, List<Waypoint> o2) {
				return o2.size() - o1.size();
			}
		});
		
		for (Set<Waypoint> waypointSet : connectedSets) {
			queue.add(new ArrayList<Waypoint>(waypointSet));
		}
		
		return queue;
	}

	private PositionVector cut(PositionVector v1) {
		int x = (int) v1.getX();
		int y = (int) v1.getY();
		
		PositionVector v2 = new PositionVector(x, y);
		
		return v2;
	}
}
