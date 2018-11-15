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

package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import java.awt.Color;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.util.geo.maps.Node;
import de.tud.kom.p2psim.api.util.geo.maps.Way;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.WeakWaypoint;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.geo.maps.AbstractMap;

public class OSMMap extends AbstractMap {

	private FuzzyWaypointCache waypointCache = new FuzzyWaypointCache();

	protected HashMap<Long, OSMNode> nodes = Maps.newHashMap();

	protected PositionVector pixelPerMeter = new PositionVector(2);

	protected PositionVector mappedTo = null;

	protected Map<Tuple<String, String>, Color> obstacleColors = Maps
			.newHashMap();

	public OSMMap() {
		//
	}

	public PositionVector getPixelPerMeter() {
		return pixelPerMeter.clone();
	}

	public Collection<OSMNode> getNodes() {
		return nodes.values();
	}

	public OSMNode getNode(Long id) {
		return nodes.get(id);
	}

	public void addNode(Long id, PositionVector lonlat) {
		OSMNode node = new OSMNode(id);
		node.setPosition(lonlat);
		node.setWorldPosition(lonlat);

		nodes.put(id, node);
	}

	public void addWay(String name, Vector<Long> idList,
			HashMap<String, String> attributes) {
		OSMWay way = new OSMWay();
		way.setName(name);
		way.setAttributes(attributes);

		for (Long id : idList) {
			way.addNode(nodes.get(id));
		}

		super.ways.add(way);
	}

	@Override
	public void doLoadMap() {
		OSMReader.loadMap(getFilename(), this);

		buildPaths();
	}

	private void buildPaths() {
		clearPaths();

		for (Way w : getWays()) {
			OSMWay way = (OSMWay) w;
			if (way.getAttribute("highway") != null) {
				processPath(way);
			} else if (way.getAttribute("building") != null) {
				processObstacle(way);
			}
		}

		waypointCache.clear();
		waypointCache = null;
	}

	private void processPath(Way way) {
		Stack<Node> nodes = new Stack<Node>();
		nodes.addAll(way.getNodes());

		Node node1 = nodes.pop();
		Node node2 = null;

		while (!nodes.empty()) {
			node2 = nodes.pop();

			Waypoint wp1 = getWaypoint(node1);
			Waypoint wp2 = getWaypoint(node2);

			if (!wp1.equals(wp2))
				createPath(wp1, wp2);

			node1 = node2;
		}
	}

	private void processObstacle(Way way) {
		List<PositionVector> vertices = new Vector<PositionVector>();

		Map<String, String> mergedAttribute = Maps.newHashMap();
		
		for (Node node : way.getNodes()) {
			vertices.add(node.getWorldPosition());
			mergedAttribute.putAll(way.getAttributes());
		}

		// LinearRing doesn't support those:
		if (vertices.size() > 0 && vertices.size() < 4) {
			return;
		}

		OSMObstacle obstacle = new OSMObstacle(vertices, 1);
		obstacle.setAttributes(mergedAttribute);

		for (String keyName : way.getAttributes().keySet()) {
			Tuple<String, String> key = Tuple.create(keyName,
					way.getAttribute(keyName));
			if (obstacleColors.containsKey(key)) {
				Color color = obstacleColors.get(key);
				obstacle.setCustomColor(color);
			}
		}

		// TODO Calculate a new dampening factor based on the size of the
		// obstacle
		obstacle.setDampeningFactor(1);

		addObstacle(obstacle);
	}

	private Waypoint getWaypoint(Node node) {
		Waypoint wp = waypointCache.getWaypoint(node.getPosition());
		if (wp == null) {
			wp = new WeakWaypoint(node.getWorldPosition());
			waypointCache.addWaypoint(node.getPosition(), wp);
		}
		return wp;
	}

	public void setAddColor(String[] colorPattern) {
		if (colorPattern.length != 3)
			return;

		int cint = Integer.parseInt(colorPattern[2], 16);

		Color color = new Color(cint);

		obstacleColors.put(Tuple.create(colorPattern[0], colorPattern[1]),
				color);
	}

	public Map<Tuple<String, String>, Color> getObstacleColors() {
		return this.obstacleColors;
	}
	
	public void mapToWorld(PositionVector world) {
		super.mapToWorld(world);

		PositionVector pixelPerMeter = world.clone();
		pixelPerMeter.divide(getDimensions());
		
		for (OSMNode n : this.nodes.values()) {
			n.setPosition(toPixelCoords(n.getPosition(), pixelPerMeter));
		}
	}
	
	public void swapWorld(Axis axis, double max) {
		super.swapWorld(axis, max);
		
		for (Node n : this.nodes.values()) {
			n.getPosition().setEntry(axis.ordinal(),
					max - n.getPosition().getEntry(axis.ordinal()));
		}
	}

}
