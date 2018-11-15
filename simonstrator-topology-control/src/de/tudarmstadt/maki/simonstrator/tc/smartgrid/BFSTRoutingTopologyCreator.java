/**
 * 
 */
package de.tudarmstadt.maki.simonstrator.tc.smartgrid;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.GraphUtil;


/**
 * @author Marvin Peter Haus
 *
 */
public class BFSTRoutingTopologyCreator implements RoutingTopologyCreator {

	/**
	 * The maximum amount of child nodes per node
	 */
	public static final int K = 5;
	
	@Override
	public Graph createRoutingTopology(Graph basicTopology) {
		
		//there has to be a root
		INode topologyRoot = basicTopology.getNode(INodeID.get("root"));
		if(topologyRoot == null){
			return null;
		}

		Queue<INodeID> queue = new LinkedList<INodeID>();
		HashSet<INodeID> visited = new HashSet<INodeID>();
		HashSet<DirectedEdge> treeEdges = new HashSet<DirectedEdge>();
		
		//put all neighbors of root into the queue
		visited.add(INodeID.get("root"));
		for (final INodeID node : basicTopology.getNeighbors(INodeID.get("root"))) {
			queue.add(node);
			visited.add(node);
			treeEdges.add(new DirectedEdge(INodeID.get("root"), node));
		}
		
		//start the breadth first search
		while (queue.isEmpty() == false) {
			final INodeID node = queue.poll();
			for (final INodeID neighbor : basicTopology.getNeighbors(node)){
				if(!visited.contains(neighbor)){
					queue.add(neighbor);
					visited.add(neighbor);
					treeEdges.add(new DirectedEdge(node, neighbor));
				}
			}
		}
		
		
		final Graph g = GraphUtil.createGraph(treeEdges);
		/*
		//children rearrange
		//clear and reuse queue
		queue.clear();
		
		queue.add(INodeID.get("root"));
		while(!queue.isEmpty()){
			Set<INodeID> children = g.getNeighbors(queue.poll());
			if(K > g.getDegree(INodeID.get("root"))){
				for (INodeID iNodeID : children) {
				}
			}
		}*/
		
		rebalanceTree(INodeID.get("root"), null, basicTopology, g);
		
		
		
		return g;
	}
	
	/**
	 * recursively rebalances the Graph g
	 * @param node the INodeID of the node that should be checked for rebalancing
	 * @param parent the parent of node in g. null for root
	 * @param basicTopology the basic topology containing all possible connections
	 * @param g the breadth first search tree
	 */
	private void rebalanceTree(INodeID node, INodeID parent, Graph basicTopology, Graph g){
		
		//step 1 try to rearrange to siblings
		//if there is no parent, then step 1 is finished here
		if(parent != null){
			Set<INodeID> siblings = g.getNeighbors(parent);
		
			if(K < g.getDegree(node)){
				
				for (INodeID child : g.getNeighbors(node)) {
					//if the node has to many children, see for each child if a connection to a sibling is possible
					if(K >= g.getDegree(node)){
						break;
					}
					for (INodeID sibling : siblings) {
						/*
						 * if the sibling has space left and the edge from the sibling to the child exists in the
						 * baseTopology then rebalance g  
						 */
						if(!node.equals(sibling) && K > g.getDegree(sibling)){
							DirectedEdge tempEdge = new DirectedEdge(sibling, child);
							if(basicTopology.containsEdge(tempEdge)){
								g.addEdge(tempEdge);
								g.removeEdge(g.getEdge(node, child));
								break; //moved the child to a sibling, so stop trying to move it to siblings
							}
						}
					}
				}
			}
		}
		
		//step 2 rearrange to a child
		if(K < g.getDegree(node)){
			for (INodeID child : g.getNeighbors(node)) {
				if(K >= g.getDegree(node)){
					break;
				}
				for (INodeID childSibling : g.getNeighbors(node)) {
					//check if the two children arent the same
					if(!child.equals(childSibling)){
						DirectedEdge tempEdge = new DirectedEdge(childSibling, child);
						if(basicTopology.containsEdge(tempEdge)){
							g.addEdge(tempEdge);
							g.removeEdge(g.getEdge(node, child));
							break; //moved the child to a childsibling, so stop trying to move it to childsiblings
						}
					}
				}
			}
		}
		
		//recursively call this function
		for (INodeID child : g.getNeighbors(node)) {
			rebalanceTree(child, node, basicTopology, g);
		}
		
	}

}
