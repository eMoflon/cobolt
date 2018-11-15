/**
 * 
 */
package de.tudarmstadt.maki.simonstrator.tc.smartgrid;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

/**
 * Generates a routing topology by taking an ordinary topology and creating a 
 * sub graph of the input graph
 * @author Marvin Peter Haus
 * 
 */
public interface RoutingTopologyCreator {
	
	/**
	 * Creates a sub graph of the input graph
	 * @param basicTopology
	 * @return
	 */
	public Graph createRoutingTopology(Graph basicTopology);
}
