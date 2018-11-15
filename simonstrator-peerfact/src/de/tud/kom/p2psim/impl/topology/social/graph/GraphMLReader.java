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

package de.tud.kom.p2psim.impl.topology.social.graph;

import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.collections15.Transformer;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.io.GraphIOException;
import edu.uci.ics.jung.io.graphml.EdgeMetadata;
import edu.uci.ics.jung.io.graphml.GraphMLReader2;
import edu.uci.ics.jung.io.graphml.GraphMetadata;
import edu.uci.ics.jung.io.graphml.HyperEdgeMetadata;
import edu.uci.ics.jung.io.graphml.NodeMetadata;

/**
 * Reads a GraphML file, to generate a {@link DirectedGraph}.<br>
 * A correct filePath is needed, to read the graph.<br>
 * It is possible to change the identifier of the interaction and activity, if
 * in the GraphML other identifier are used.
 * <p>
 * Properties, which are not interaction or activity, will be discarded!
 * <p>
 * The GraphML-File must have at least the same size of Vertexes, how the number
 * of host are added to the {@link SocialView}!<br>
 * If the file has more Vertexes, then will be random deleted!
 * <p>
 * 
 * Note: The GraphMLReader can read an extended Graph!
 * 
 * @author Christoph Muenker
 * @version 1.0, 08.06.2013
 */
public class GraphMLReader implements IGraphLoader {

	private String filePath;

	private String interactionId = "interaction";

	private String activityId = "activity";

	private Random rand;

	private boolean extendedGraph = false;

	@XMLConfigurableConstructor({ "filePath" })
	public GraphMLReader(String filePath) {
		this.filePath = filePath;
		setSeed(Simulator.getSeed());
	}

	@Override
	public DirectedGraph<SocialNode, SocialEdge> getGraph(int numberOfNodes) {
		if (filePath == null) {
			throw new ConfigurationException("filePath is not set");
		}
		DirectedGraph<SocialNode, SocialEdge> graph = null;
		try {
			FileReader fr = new FileReader(filePath);

			Transformer<NodeMetadata, SocialNode> vTrans = new Transformer<NodeMetadata, SocialNode>() {
				public SocialNode transform(NodeMetadata nmd) {
					SocialNode v = new SocialNode(new Integer(nmd.getId()));
					if (nmd.getProperties().containsKey(activityId)) {
						v.setActivity(Double.parseDouble(nmd
								.getProperty(activityId)));
						extendedGraph = true;
					}
					return v;
				}
			};
			Transformer<EdgeMetadata, SocialEdge> eTrans = new Transformer<EdgeMetadata, SocialEdge>() {
				public SocialEdge transform(EdgeMetadata emd) {
					SocialEdge e = new SocialEdge();
					if (emd.getProperties().containsKey(interactionId)) {
						e.setInteraction(Double.parseDouble(emd
								.getProperty(interactionId)));
						extendedGraph = true;
					}
					return e;
				}
			};
			Transformer<HyperEdgeMetadata, SocialEdge> heTrans = new Transformer<HyperEdgeMetadata, SocialEdge>() {

				public SocialEdge transform(HyperEdgeMetadata emd) {
					SocialEdge e = new SocialEdge();
					if (emd.getProperties().containsKey(interactionId)) {
						e.setInteraction(Double.parseDouble(emd
								.getProperty(interactionId)));
						extendedGraph = true;
					}
					return e;
				}
			};
			Transformer<GraphMetadata, DirectedGraph<SocialNode, SocialEdge>> gTrans = new Transformer<GraphMetadata, DirectedGraph<SocialNode, SocialEdge>>() {
				public DirectedGraph<SocialNode, SocialEdge> transform(
						GraphMetadata gmd) {
					return new DirectedSparseGraph<SocialNode, SocialEdge>();
				}
			};

			GraphMLReader2<DirectedGraph<SocialNode, SocialEdge>, SocialNode, SocialEdge> gmlReader = new GraphMLReader2<DirectedGraph<SocialNode, SocialEdge>, SocialNode, SocialEdge>(
					fr, gTrans, vTrans, eTrans, heTrans);

			graph = gmlReader.readGraph();
		} catch (GraphIOException e) {
			throw new ConfigurationException(
					"Could not parse the GraphML file! Is it a GraphML file and valid?",
					e);
		} catch (IOException e) {
			throw new ConfigurationException(
					"Is the filepath correct? It was not possible to open the file!",
					e);

		}
		if (graph.getVertexCount() > numberOfNodes) {
			Monitor.log(
					GraphMLReader.class,
					Level.INFO,
					"Delete Vertexes from the Graph, because the number of vertexes is bigger then the requested graph!");
		} else if (graph.getVertexCount() < numberOfNodes) {
			throw new ConfigurationException(
					"The given graph has to few vertexes!");
		}

		// delete nodes, which are to many
		while (graph.getVertexCount() > numberOfNodes) {
			int i = 0;
			int random = rand.nextInt(graph.getVertexCount());
			SocialNode toDelete = null;
			for (SocialNode v : graph.getVertices()) {
				if (i == random) {
					toDelete = v;
					break;
				}
				i++;
			}
			graph.removeVertex(toDelete);
		}
		return graph;
	}

	/**
	 * @see de.tud.kom.p2psim.impl.topology.social.graph.IGraphLoader#isExtendedGraph()
	 * @return If an identifier for activity or interaction is used, then will
	 *         be return <code>true</code>, other <code>false</code>
	 */
	@Override
	public boolean isExtendedGraph() {
		return extendedGraph;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public void setSeed(long seed) {
		rand = Randoms.getRandom(GraphMLReader.class);
		rand.setSeed(seed);
	}

	public void setInteractionId(String interactionId) {
		this.interactionId = interactionId;
	}

	public void setActivityId(String activityId) {
		this.activityId = activityId;
	}

}
