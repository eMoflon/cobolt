package de.tudarmstadt.maki.simonstrator.tc.smartgrid;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.common.graph.BasicGraph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;

/**
 * Generates a topology representing possible connections inside a smart grid
 * @author Marvin Peter Haus
 * 
 */
public class BasicTopologyCreator implements TopologyCreator {

	/**
	 * The minimum amount of nodes generated (inclusive)
	 */
	private static final int LOWER_BOUND = 10;
	/**
	 * The maximum amount of nodes generated (exclusive)
	 */
	private static final int UPPER_BOUND = 10;
	
	/**
	 * The Erdos–Renyi model is used to generate the topology with a semi-random n 
	 * between LOWER_BOUND and UPPER_BOUND and a p of 2ln(n)/n
	 */
	@Override
	public Graph createTopology(long seed) {
		
		Random randomizer = new Random(seed);
		
		//the generated graph will have n nodes
		int n = LOWER_BOUND + randomizer.nextInt((UPPER_BOUND+1) - LOWER_BOUND);
		//an edge between 2 nodes has the probability p to exist
		double p = 2 * Math.log(n)/n;
		
		//create a list of nodes with length n
		List<Node> nodelist = new ArrayList<Node>();
		
		nodelist.add(new Node(INodeID.get("root")));
		
		for (int i = 1; i < n; i++) {
			nodelist.add(new Node(INodeID.get(i)));			
		}

		
		//create a list of edges
		List<DirectedEdge> edgelist = new ArrayList<DirectedEdge>();
		
		for (Node src_node : nodelist) {
			for (Node dst_node : nodelist) {
				if(src_node.getId() != dst_node.getId() && randomizer.nextDouble() < p){
					edgelist.add(new DirectedEdge(src_node.getId(), dst_node.getId()));
				}
			}
		}
		
		final Graph g = new BasicGraph(nodelist,edgelist);
		
		return g;
	}
}
