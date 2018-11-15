/**
 * 
 */
package de.tudarmstadt.maki.simonstrator.tc.smartgrid;


import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;


/**
 * Generates a topology representing possible connections inside a smart grid
 * @author Marvin Peter Haus
 * 
 */
public interface TopologyCreator {
	
	/**
	 * Randomly generates a graph using the seed. The generated graph can
	 * be used
	 * @param seed
	 * @return
	 */
	public Graph createTopology(long seed);
}
