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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.views.TopologyView;

/**
 * 
 * @author Michael Stein
 *
 */
public class TopologyUtil {

	/**
	 * Returns the hop count of the shortest path.
	 * 
	 * @param source
	 * @param target
	 * @return Integer.MAX_VALUE if no path exists.
	 */
	@Deprecated
	public static int getHops(TopologyView tv, MacAddress source,
			MacAddress target) {
		if (source.equals(target))
			return 0;

		Link nextHop = tv.getBestNextLink(source, source, source, target);
		if (nextHop == null || !nextHop.isConnected())
			return Integer.MAX_VALUE;
		else
			return 1 + getHops(tv, nextHop.getDestination(), target);
	}

	/**
	 * Calculates the number of hops between source and target (BFS-based) This
	 * is because for some reason the getBestNextLink method does not always
	 * find a path
	 * 
	 * @param tv
	 * @param source
	 * @param target
	 * @return number of hops or Integer.MAX_VALUE if no path exists
	 */
	public static int calculateHops(TopologyView tv, MacAddress source,
			MacAddress target) {
		List<MacAddress> shortestPath = getMinimalHopsPath(tv, source, target);
		return (shortestPath == null) ? Integer.MAX_VALUE
				: shortestPath.size() - 1;
	}

	/**
	 * Calculates the shortest path between source and target. The minimal hops
	 * count is used as underlying metric (underlay edge weights as e.g.
	 * distance are not considered at all).
	 * 
	 * @param tv
	 * @param source
	 * @param target
	 * @return the shortest path as list containing start and end node, or null
	 *         in case that no path exists
	 */
	public static List<MacAddress> getMinimalHopsPath(TopologyView tv,
			MacAddress source, MacAddress target) {
		// further reading:
		// http://www.eecs.yorku.ca/course_archive/2006-07/W/2011/Notes/BFS_part2.pdf

		LinkedList<MacAddress> shortestPath = new LinkedList<MacAddress>();

		if (source.equals(target)) {
			shortestPath.add(source);
			return shortestPath;
		}

		LinkedList<MacAddress> queue = new LinkedList<MacAddress>();
		queue.add(source);

		// avoid duplicates in the queue
		HashSet<MacAddress> enqueued = new HashSet<MacAddress>();
		enqueued.add(source);

		HashMap<MacAddress, MacAddress> previous = new HashMap<MacAddress, MacAddress>();

		while (!queue.isEmpty()) {
			MacAddress current = queue.poll();

			List<MacAddress> neighbors = tv.getNeighbors(current);
			for (MacAddress n : neighbors)
				if (!enqueued.contains(n)) {
					previous.put(n, current);
					queue.add(n);
					enqueued.add(n);

					if (n.equals(target)) {
						// construct shortest path using "previous"-map
						MacAddress pathNode = target;
						shortestPath.add(pathNode);
						while ((pathNode = previous.get(pathNode)) != null)
							shortestPath.addFirst(pathNode);

						assert shortestPath.getFirst().equals(source)
								&& shortestPath.getLast().equals(target)
								&& shortestPath.size() >= 2;

						return shortestPath;
					}
				}
		}
		// if our search was not successful we return null, because no
		// path exists
		return null;
	}

	/**
	 * @param tv
	 * @param source
	 * @param target
	 * @return the euclidian distance of the shortest path between source and
	 *         target. This is sum over the euclidian distance between
	 *         succeeding nodes.
	 */
	// public static double shortestPathDistance(TopologyView tv, MacAddress
	// source,
	// MacAddress target) {
	// // Determine shortest path with algorithm that considers edge length
	// List<MacAddress> shortestPath = getShortestPath(tv, source, target,
	// distanceMetric);
	// if(shortestPath == null)
	// return Integer.MAX_VALUE;
	// else if(shortestPath.size() <= 1)
	// return 0;
	//
	// Iterator<MacAddress> iterator = shortestPath.iterator();
	// MacAddress current = iterator.next();
	// double sumDistance = 0.0;
	// while(iterator.hasNext()) {
	// MacAddress next = iterator.next();
	// sumDistance += tv.getDistance(current, next);
	// current = next;
	// }
	//
	// return sumDistance;
	// }

	/*
	 * Returns true if the graph is connected with respect to the given hosts.
	 * That is if there exists a path between each pair of hosts in the hosts
	 * list.
	 */
	public static boolean isConnected(TopologyView tv, List<SimHost> hosts) {

		/*
		 * starts at arbitrary node and traverses the whole graph. the graph is
		 * connected if and only if the number of traversed nodes is equal to
		 * the number of nodes in the graph
		 */
		if (hosts == null || hosts.isEmpty())
			return true;

		Set<MacAddress> visited = new HashSet<MacAddress>();
		Queue<MacAddress> queue = new LinkedList<MacAddress>();

		MacAddress startNode = hosts.get(0).getLinkLayer().getMac(PhyType.WIFI)
				.getMacAddress();
		visited.add(startNode);
		queue.add(startNode);

		while (!queue.isEmpty()) {
			for (MacAddress macAddress : tv.getNeighbors(queue.poll())) {
				if (!visited.contains(macAddress)) {
					visited.add(macAddress);
					queue.add(macAddress);
				}
			}
		}
		return visited.size() == hosts.size();
	}

	/**
	 * Computes the power consumption for 1-hop communication on the given
	 * distance with the given path loss parameter
	 * 
	 * @param euclideanDistance
	 * @param alpha
	 */
	public static double computePowerConsumption(double euclideanDistance,
			double alpha) {
		return Math.pow(euclideanDistance, alpha);
	}
}
