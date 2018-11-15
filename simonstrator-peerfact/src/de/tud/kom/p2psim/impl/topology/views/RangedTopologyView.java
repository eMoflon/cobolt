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

package de.tud.kom.p2psim.impl.topology.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.obstacles.Obstacle;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * A topology view where connectivity is based on the distance between hosts.
 * This basic version supports movement (ie. Neighborhoods change over time) and
 * obstacles in that connectivity is only enabled if a link does not intersect
 * with any obstacles. Extending classes could add a more versatile handling of
 * obstacles, for example in allowing them to dampen the signal rather than
 * completely destroying it.
 * 
 * This View provides GlobalKnowledgeRouting information by implementing a
 * cached version of Dijkstra. Depending on the requirements of your simulation
 * you might find it useful to alter some parameters of the
 * Dijkstra-Implementation - they are commented in the Dijkstra-subclass.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.03.2012
 */
public class RangedTopologyView extends AbstractTopologyView<RangedLink> {

	/**
	 * communication range for the static case
	 */
	private double range = 150;

	/**
	 * Just a List of all Links
	 */
	private List<RangedLink> linkList = new ArrayList<RangedLink>();

	/**
	 * These are used for the GlobalKnowledge-Functions
	 */
	private Map<MacAddress, Dijkstra> dijkstras = new HashMap<MacAddress, Dijkstra>();

	/**
	 * Again, only used for the GlobalKnowledge-Functions
	 */
	protected List<MacAddress> allMacAddresses = new ArrayList<MacAddress>();

	/**
	 * A Pointer to the Obstacle Model
	 */
	private ObstacleModel obstacleModel;

	/**
	 * A Pointer to the Waypoint Model
	 */
	private WaypointModel waypointModel;

	public static long _dijkstraCalculated, _dijkstraCached, _dijkstraPartly;

	/**
	 * This View supports movement as well as obstacles (at least in a very
	 * basic form)
	 * 
	 * @param phy
	 * 
	 */
	public RangedTopologyView(PhyType phy, double range) {
		super(phy, true);
		this.range = range;
		setHasRealLinkLayer(true);
		LiveMonitoring.addProgressValueIfNotThere(new DijkstraMonitor());
	}

	@XMLConfigurableConstructor({ "phy", "range" })
	public RangedTopologyView(String phy, double range) {
		this(PhyType.WIFI, range);
		setPhy(phy);
	}

	@Override
	protected void addedMac(MacLayer mac) {
		dijkstras.put(mac.getMacAddress(), new Dijkstra(mac.getMacAddress()));
		allMacAddresses.add(mac.getMacAddress());
	}

	@Override
	protected RangedLink createLink(MacAddress source, MacAddress destination) {
		RangedLink link = new RangedLink(source, destination, true,
				determineLinkDropProbability(source, destination),
				determineLinkBandwidth(source, destination),
				determineLinkLatency(source, destination), getPhyType()
						.getDefaultMTU(), getRange());
		synchronized (linkList) {
			linkList.add(link);
		}
		return link;
	}

	/**
	 * This might be overwritten to calculate ranges based on
	 * neighborhood-density or a statistical distribution. The default
	 * implementation just returns a constant
	 * 
	 * @return max distance between hosts to still be able to communicate (ie.
	 *         wireless range)
	 */
	public double getRange() {
		return range;
	}

	protected void setRange(double range) {
		this.range = range;
	}

	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		/*
		 * This one is a bit tricky... how to define a "best" link? Shortest
		 * path? Energy? DropRate? Bandwidth?
		 */

		// System.err.println(source + " => " + lastHop + " => " + currentHop
		// + " => " + destination);

		/*
		 * Dijkstra-Based implementation, try to use the one-time calculated
		 * path - this also prevents loops due to equi-distant nodes in a
		 * grid-setting.
		 */
		List<RangedLink> path = dijkstras.get(source).getPath(destination);
		RangedLink toReturn = null;
		for (RangedLink link : path) {
			if (link.getSource().equals(currentHop)) {
				assert link.isConnected();
				assert !link.isOutdated();
				toReturn = link;
			}
		}
		if (toReturn != null) {
			assert toReturn.isConnected();
			return toReturn;
		}

		/*
		 * It may happen that meanwhile the route changed to not include this
		 * node anymore. If this happens, we use dijkstra with the current node
		 * as starting point.
		 */
		_dijkstraPartly++;
		path = dijkstras.get(currentHop).getPath(destination);
		if (!path.isEmpty()) {
			toReturn = path.get(0);
			assert toReturn.isConnected();
			return path.get(0);
		}

		/*
		 * No path, returning default Link (not connected)
		 */
		return getLinkBetween(currentHop, destination);
	}

	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {
		/*
		 * We cache this information as well to reach consistency between
		 * getNeighbors and Link-Information.
		 */
		List<MacAddress> updatedNeighbors = new ArrayList<MacAddress>();
		for (MacAddress neighbor : allMacAddresses) {
			RangedLink link = getLinkBetween(source, neighbor);
			if (link.isConnected()) {
				assert !neighbor.equals(source);
				updatedNeighbors.add(neighbor);
			}
		}
		return updatedNeighbors;
	}
	
	long lastMovementTime = 0;
	
	@Override
	public void onLocationChanged(Host host, Location location) {
		super.onLocationChanged(host, location);
		if (lastMovementTime != Time.getCurrentTime()) {
			lastMovementTime = Time.getCurrentTime();
			/*
			 * mark all links as outdated
			 */
			synchronized (linkList) {
				for (RangedLink link : linkList) {
					link.setOutdated(true);
				}
				for (Dijkstra dijkstra : dijkstras.values()) {
					dijkstra.afterComponentsMoved();
				}
			}
		}
	}

	@Override
	protected void updateOutdatedLink(RangedLink link) {
		link.updateNodeDistance(getCachedPosition(link.getSource())
				.distanceTo(getCachedPosition(link.getDestination())));

		/*
		 * Update latency and drop rate - note: it depends on the actual
		 * implementation of the determinators, whether the value is actually
		 * changed.
		 */
		if (link.isConnected()) {
			link.updateLatency(getLatencyDeterminator().getLatency(this,
					link.getSource(), link.getDestination(), link));
			link.updateDropProbability(
					getDropProbabilityDeterminator().getDropProbability(this,
							link.getSource(), link.getDestination(), link));
		}

		/*
		 * The distance has already been updated, we just have to check for
		 * obstacles. More advanced Views might update the properties based on
		 * the type or size of the obstacle. Or they might provide a way to
		 * iterate over obstacles in the order they occur on the link.
		 */
		if (link.isConnected() && obstacleModel != null) {
			double rangeWeighted = 0;
			for (Obstacle obstacle : obstacleModel.getObstacles()) {
				if (obstacle.dampingFactor() == 0) {
					continue;
				}
				if (obstacle.intersectsWith(
						getCachedPosition(link.getSource()),
						getCachedPosition(link.getDestination()))) {

					if (obstacle.dampingFactor() == 1) {
						link.setConnected(false);
						break;
					}

					double intersectionLength = obstacle
							.totalIntersectionLength(
									getCachedPosition(link.getSource()),
									getCachedPosition(link.getDestination()));
					/*
					 * Just an arbitrary formula to add some damping effect -
					 * this does in no way reflect a proper model, but showcases
					 * the functionality a view could have
					 */
					rangeWeighted += Math.pow(intersectionLength,
							obstacle.dampingFactor() + 1);
				}
			}
			if (link.getNodeDistance() > range - rangeWeighted) {
				// no connection possible
				link.setConnected(false);
			}
		}
	}

	/**
	 * Dijkstra-Implementation for the GlobalKnowledge-Functions. A path is
	 * calculated as soon as one of the Links on the old path is no longer
	 * connected (this adds some caching to the algorithm in order to ease
	 * computational load on scenarios where paths are rather persistent)
	 * 
	 * FIXME currently, Dijkstra does not take into account if a MAC is online
	 * or not - this information should however be known in a GlobalKnowledge
	 * routing environment. It may on the other hand have a not-so-small impact
	 * on performance -> make it configurable
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 21.03.2012
	 */
	private class Dijkstra {

		/**
		 * A vertex inside Dijkstra - this is used for the {@link PriorityQueue}
		 * -based implementation.
		 * 
		 * @author Bjoern Richerzhagen
		 * @version 1.0, 26.03.2012
		 */
		private class Vertex implements Comparable<Vertex> {

			public MacAddress mac;

			public double minDistance = Double.POSITIVE_INFINITY;

			public Vertex previous = null;

			public Vertex(MacAddress mac) {
				this.mac = mac;
			}

			public void reset() {
				this.minDistance = Double.POSITIVE_INFINITY;
				this.previous = null;
			}

			@Override
			public int compareTo(Vertex o) {
				return Double.compare(minDistance, o.minDistance);
			}

		}

		/**
		 * To choose a "robust" path, Dijkstra only selects connected Links
		 * where the node distance is less than the maxDistance minus this
		 * threshold. This improves performance, because paths can be used
		 * longer - but on the other hand you do not get the total 100% of
		 * available paths, which might lead to packet drops, even if a path
		 * would be available.
		 */
		private double RANGE_THRESHOLD = 10;

		/**
		 * After MOVEMENT_THRESHOLD movements (calls to afterComponentsMoved) we
		 * will update Dijkstra. Otherwise, paths could still be valid but
		 * contain far to many hops (zig-zag). If this is set to 1, we
		 * re-compute the path on every movement, if set to zero, we compute
		 * graphs only if a path is broken
		 */
		private int MOVEMENT_THRESHOLD = 5;

		private final boolean USE_NEW = true;

		private int movement_counter = 0;

		private MacAddress source;

		private Map<MacAddress, Double> dist = new HashMap<MacAddress, Double>();

		private Map<MacAddress, MacAddress> previous = new HashMap<MacAddress, MacAddress>();

		private List<MacAddress> q = new LinkedList<MacAddress>();

		private PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();

		private HashMap<MacAddress, Vertex> toVertex = new HashMap<MacAddress, RangedTopologyView.Dijkstra.Vertex>();

		private boolean recalculate = true;

		public Dijkstra(MacAddress source) {
			this.source = source;
		}

		public void afterComponentsMoved() {
			/*
			 * Re-calculate
			 */
			if (MOVEMENT_THRESHOLD != 0) {
				if (movement_counter % MOVEMENT_THRESHOLD == 0) {
					recalculate = true;
				}
				movement_counter++;
			}
		}

		public List<RangedLink> getPath(MacAddress destination) {

			if (recalculate) {
				movement_counter = 0;
				initialize();
				calculate();
			}

			boolean pathBroken = false;
			List<RangedLink> path = new LinkedList<RangedLink>();

			if (USE_NEW) {
				for (Vertex vertex = toVertex.get(destination); vertex.previous != null; vertex = vertex.previous) {
					RangedLink link = getLinkBetween(vertex.previous.mac,
							vertex.mac);

					assert !link.isOutdated();

					path.add(link);
					if (!link.isConnected()) {
						pathBroken = true;
					}
				}
				Collections.reverse(path);
			} else {
				List<MacAddress> pathMacs = new LinkedList<MacAddress>();
				MacAddress u = destination;
				while (previous.get(u) != null) {
					pathMacs.add(u);
					u = previous.get(u);
				}
				pathMacs.add(u);
				Collections.reverse(pathMacs);

				Iterator<MacAddress> it = pathMacs.iterator();
				MacAddress prevHop = null;
				while (it.hasNext() && !pathBroken) {
					if (prevHop == null) {
						prevHop = it.next();
						continue;
					}
					MacAddress actHop = it.next();
					RangedLink l = getLinkBetween(prevHop, actHop);
					path.add(l);
					prevHop = actHop;

					assert !l.isOutdated();

					if (!l.isConnected()) {
						pathBroken = true;
					}
				}

				// if (pathMacs.size() == 1) {
				// pathBroken = true; // no path at all.
				// }
			}

			if (pathBroken && !recalculate) {
				recalculate = true;
				return getPath(destination);
			} else if (pathBroken) {
				path.clear();
			} else {
				_dijkstraCached++;
			}

			recalculate = false;
			return path;
		}

		private void calculate() {
			_dijkstraCalculated++;

			if (USE_NEW) {
				// NEW
				while (!vertexQueue.isEmpty()) {
					Vertex u = vertexQueue.poll();

					List<MacAddress> neighbors = getNeighbors(u.mac);
					for (MacAddress neighbor : neighbors) {
						RangedLink l = getLinkBetween(u.mac, neighbor);
						Vertex v = toVertex.get(neighbor);

						double weight = Double.POSITIVE_INFINITY;
						if (l.isConnected()
								&& l.getNodeDistance() + RANGE_THRESHOLD < l
										.getMaxDistance()) {
							weight = l.getNodeDistance();
						}

						assert (l.isConnected() && l.getNodeDistance() < l
								.getMaxDistance()) || !l.isConnected();

						double distanceWithU = u.minDistance + weight;
						if (distanceWithU < v.minDistance) {
							vertexQueue.remove(v);
							v.minDistance = distanceWithU;
							v.previous = u;
							assert l.isConnected();
							vertexQueue.add(v);
						}

					}
				}
			} else {
				// OLD
				while (!q.isEmpty()) {
					// find smallest distance

					double smallestDistance = Double.MAX_VALUE;
					MacAddress u = null;
					for (MacAddress candidate : q) {
						if (u == null || dist.get(candidate) < smallestDistance) {
							u = candidate;
							smallestDistance = dist.get(candidate);
						}
					}

					if (smallestDistance == Double.MAX_VALUE) {
						break; //
					}

					q.remove(u);

					List<MacAddress> neighbors = getNeighbors(u);
					for (MacAddress neighbor : neighbors) {
						RangedLink l = getLinkBetween(u, neighbor);
						if (l.isConnected()
								&& l.getNodeDistance() < l.getMaxDistance()
										- RANGE_THRESHOLD) {
							assert !l.isOutdated() : "Outdated Link!";
							double alt = dist.get(u) + l.getNodeDistance();
							if (alt < dist.get(neighbor)) {
								dist.put(neighbor, new Double(alt));
								previous.put(neighbor, u);
							}
						}
					}
				}
			}
		}

		private void initialize() {
			if (USE_NEW) {
				if (toVertex.isEmpty()) {
					for (MacAddress macAddr : allMacAddresses) {
						Vertex v = new Vertex(macAddr);
						v.reset();
						toVertex.put(macAddr, v);
					}
				} else {
					for (Vertex v : toVertex.values()) {
						v.reset();
					}
				}
				Vertex sourceVertex = toVertex.get(source);
				sourceVertex.minDistance = 0;
				vertexQueue.clear();
				vertexQueue.add(sourceVertex);
			} else {
				q = new ArrayList<MacAddress>(allMacAddresses);
				for (MacAddress macAddr : allMacAddresses) {
					dist.put(macAddr, Double.MAX_VALUE);
					previous.put(macAddr, null);
				}
				dist.put(source, new Double(0));
			}
		}

	}

	public class DijkstraMonitor implements ProgressValue {

		@Override
		public String getName() {
			return "TopologyView Dijkstra (calc, cached, partly): ";
		}

		@Override
		public String getValue() {
			return Long.toString(_dijkstraCalculated) + " / "
					+ Long.toString(_dijkstraCached) + " / "
					+ Long.toString(_dijkstraPartly);
		}

	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		this.waypointModel = model;
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		this.obstacleModel = model;
	}

	public WaypointModel getWaypointModel() {
		return waypointModel;
	}

}
