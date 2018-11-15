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

package de.tud.kom.p2psim.impl.topology.social;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.social.graph.GraphMLWriter;
import de.tud.kom.p2psim.impl.topology.social.graph.IGraphExtender;
import de.tud.kom.p2psim.impl.topology.social.graph.IGraphLoader;
import de.tud.kom.p2psim.impl.topology.social.graph.KleinbergSmallWorldGenerator;
import de.tud.kom.p2psim.impl.topology.social.graph.SimpleGraphExtender;
import de.tud.kom.p2psim.impl.topology.social.graph.SocialEdge;
import de.tud.kom.p2psim.impl.topology.social.graph.SocialNode;
import de.tud.kom.p2psim.impl.topology.social.graph.jung.VoltageClusterer;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * This is implementation of die {@link SocialView} Interface. It loads a graph
 * with the initialize method and creates an extended Graph. Additionally, it
 * assigns to every host a vertex from the graph.
 * <p>
 * 
 * 
 * 
 * It is possible to set:<br>
 * graphLoader: Possible to change the loader. For example to load a graph from
 * a file or from a generator. <br>
 * graphExtener: The class, which extends the graph with the information of
 * activity and interaction.<br>
 * id: The identifier for this SocialView, to find the right SocialView from the
 * using system.<br>
 * numberOfCluster: Gives the number of Clusters, which should be found in the
 * graph.<br>
 * outputFile: If the outputFile is set, then will be write out the graph as a
 * GraphML File.
 * <p>
 * 
 * Actually the interface not allow to change the structure of the social graph.
 * So we precalculate and cache the needed values.
 * 
 * <p>
 * NOTE: The class is ready after the calls of the initialize {@link Component}
 * s.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.06.2013
 */
public class DefaultSocialView implements SocialView {

	// for default a default id!
	private static int idCounter = 0;

	private IGraphLoader graphLoader = new KleinbergSmallWorldGenerator();

	private IGraphExtender graphExtender = new SimpleGraphExtender();

	private DirectedGraph<SocialNode, SocialEdge> graph;

	private Set<TopologyComponent> toCos = new LinkedHashSet<TopologyComponent>();

	private Map<SocialNode, SimHost> mapSocialNodeHost = new HashMap<SocialNode, SimHost>();

	private Map<SimHost, SocialNode> mapHostSocialNode = new HashMap<SimHost, SocialNode>();

	private Map<SimHost, List<SimHost>> neighbors = new HashMap<SimHost, List<SimHost>>();

	private Map<SimHost, Double> activity = new HashMap<SimHost, Double>();

	private Map<SimHost, Map<SimHost, Double>> edges = new HashMap<SimHost, Map<SimHost, Double>>();

	private Set<Set<SimHost>> clusters = new HashSet<Set<SimHost>>();

	private String id;

	/**
	 * Number of clusters, which should be find in the graph
	 */
	private int numberOfClusters = 4;

	/**
	 * If the outputFile is set, then will be write out the used Graph!
	 */
	private String outputFile;

	public DefaultSocialView() {
		idCounter++;
		id = Integer.toString(idCounter);
	}

	@XMLConfigurableConstructor({ "id" })
	public DefaultSocialView(String id) {
		this();
		this.id = id;
	}

	@Override
	public void addedComponent(TopologyComponent comp) {
		toCos.add(comp);
	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		// not interested!
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		// not interested!
	}

	@Override
	public void initialize() {
		loadGraph();
		if (outputFile != null) {
			storeGraph();
		}
		mapHostsToSocialNode();
		cacheNeighbors();
		cacheActivity();
		cacheEdges();
		findClusters();
	}

	private void cacheNeighbors() {
		for (SocialNode node : this.graph.getVertices()) {
			SimHost host = mapSocialNodeHost.get(node);
			List<SimHost> neighbors = new Vector<SimHost>();
			for (SocialNode neighbor : graph.getNeighbors(node)) {
				neighbors.add(mapSocialNodeHost.get(neighbor));
			}
			this.neighbors.put(host, neighbors);
		}

	}

	private void cacheActivity() {
		for (SocialNode node : this.graph.getVertices()) {
			SimHost host = mapSocialNodeHost.get(node);
			this.activity.put(host, node.getActivity());
		}
	}

	private void cacheEdges() {
		for (SocialNode source : this.graph.getVertices()) {
			SimHost host = mapSocialNodeHost.get(source);
			Map<SimHost, Double> sourceOutEdges = new HashMap<SimHost, Double>();
			for (SocialEdge edge : this.graph.getOutEdges(source)) {
				SocialNode dest = this.graph.getDest(edge);
				SimHost destHost = mapSocialNodeHost.get(dest);
				sourceOutEdges.put(destHost, edge.getInteraction());
			}
			edges.put(host, sourceOutEdges);
		}

	}

	private void mapHostsToSocialNode() {
		// List of ToCos to remove the mapped ToCos.
		LinkedList<TopologyComponent> tempToCos = new LinkedList<TopologyComponent>(
				this.toCos);

		assert tempToCos.size() == this.graph.getVertices().size();

		for (SocialNode node : this.graph.getVertices()) {
			SimHost host = tempToCos.removeFirst().getHost();
			mapHostSocialNode.put(host, node);
			mapSocialNodeHost.put(node, host);
		}
		assert tempToCos.size() == 0;

	}

	private void findClusters() {
		numberOfClusters = Math.min(numberOfClusters, graph.getVertexCount());
		// TODO! If num_Clusert < 2 dann selbst zuoordnen... Bei zwei und zwei
		// dann auch

		// VOLTAGE CLUSTER anders AUSGABEN!
		Collection<Set<SocialNode>> perfectClusters = new VoltageClusterer<SocialNode, SocialEdge>(
				graph, numberOfClusters).cluster(numberOfClusters);

		// System.out.println("Ahhhhh" + perfectClusters.size());
		// int min = 0;
		// int max = Math.max(graph.getVertexCount(), graph.getEdgeCount());
		// int mid = 0;
		//
		// Set<Set<SocialNode>> perfectClusters = null;
		// log.info("Start to find clusters");
		// // binary search!
		// while (min <= max) {
		// mid = (min + max) / 2;
		// EdgeBetweennessClusterer<SocialNode, SocialEdge> clusterer = new
		// EdgeBetweennessClusterer<SocialNode, SocialEdge>(
		// mid);
		// Set<Set<SocialNode>> clusters = clusterer.transform(this.graph);
		// if (clusters.size() < numberOfClusters) {
		// min = mid + 1;
		// } else if (clusters.size() > numberOfClusters) {
		// max = mid - 1;
		// } else if (clusters.size() == numberOfClusters) {
		// // found the right size of clusters!
		// perfectClusters = clusters;
		// break;
		// }
		// }
		// System.out.println(mid);
		if (perfectClusters == null) {
			throw new RuntimeException(
					"Could not found the right assginment for communities. Change the number of clusters and hope the best...");
		}

		// transform the found clusters to a Set of Hosts
		for (Set<SocialNode> cluster : perfectClusters) {
			Set<SimHost> hostCluster = new HashSet<SimHost>();
			for (SocialNode node : cluster) {
				hostCluster.add(mapSocialNodeHost.get(node));
			}
			this.clusters.add(hostCluster);
		}
	}

	private void loadGraph() {
		int numberOfNodes = toCos.size();
		this.graph = graphLoader.getGraph(numberOfNodes);
		if (!graphLoader.isExtendedGraph()) {
			this.graph = graphExtender.extendGraph(this.graph);
		}
	}

	private void storeGraph() {
		GraphMLWriter writer = new GraphMLWriter();
		try {
			writer.write(this.graph, this.outputFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getIdentifier() {
		return id;
	}

	@Override
	public boolean isHostInView(SimHost host) {
		return mapHostSocialNode.containsKey(host);
	}

	@Override
	public List<SimHost> getNeighbors(SimHost host) {
		if (neighbors.containsKey(host)) {
			return new Vector<SimHost>(neighbors.get(host));
		}
		return null;
	}

	@Override
	public boolean isXWithYConnected(SimHost x, SimHost y) {
		if (edges.containsKey(x)) {
			Map<SimHost, Double> xEdges = edges.get(x);
			return xEdges.containsKey(y);
		}
		return false;
	}

	@Override
	public Set<Set<SimHost>> getClusters() {
		Set<Set<SimHost>> copy = new HashSet<Set<SimHost>>();
		for (Set<SimHost> cluster : clusters) {
			copy.add(new HashSet<SimHost>(cluster));
		}
		return copy;
	}

	@Override
	public Set<SimHost> getCluster(SimHost host) {
		for (Set<SimHost> cluster : clusters) {
			if (cluster.contains(host)) {
				return new HashSet<SimHost>(cluster);
			}
		}
		return null;
	}

	@Override
	public boolean isInSameCluster(SimHost x, SimHost y) {
		Set<SimHost> cluster = getCluster(x);
		if (cluster != null) {
			return cluster.contains(y);
		}
		return false;
	}

	@Override
	public double getActivity(SimHost host) {
		if (activity.containsKey(host)) {
			return activity.get(host);
		}
		return 0;
	}

	@Override
	public Map<SimHost, Double> getInteractions(SimHost host) {
		if (edges.containsKey(host)) {
			return new HashMap<SimHost, Double>(edges.get(host));
		}
		return null;
	}

	@Override
	public double getInteractionBetween(SimHost x, SimHost y) {
		if (edges.containsKey(x)) {
			Map<SimHost, Double> xEdges = edges.get(x);
			if (xEdges.containsKey(y)) {
				return xEdges.get(y);
			}
		}
		return 0;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setNumberOfClusters(int numberOfClusters) {
		if (numberOfClusters <= 0) {
			throw new ConfigurationException(
					"Wrong size of numberOfClusters. This value must be bigger than 0");
		}
		this.numberOfClusters = numberOfClusters;
	}

	public void setGraphExtender(IGraphExtender graphExtender) {
		this.graphExtender = graphExtender;
	}

	public void setGraphLoader(IGraphLoader graphLoader) {
		this.graphLoader = graphLoader;
	}

}
