/**
 * 
 */
package de.tudarmstadt.maki.simonstrator.tc.smartgrid;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;

/**
 * @author Marvin Peter Haus
 *
 */
public class GenerateTopologies {

	@SuppressWarnings("unused")
	public static void main(String[] args) {
		//generate the basic topology
		BasicTopologyCreator btc = new BasicTopologyCreator();
		final Graph basicTopology = btc.createTopology(123456);
		
		//modify the basic topology
		BFSTRoutingTopologyCreator rtc = new BFSTRoutingTopologyCreator();
		final Graph bfstTopology = rtc.createRoutingTopology(basicTopology);
		
		// System.out.println(basicTopology.toString());
		// System.out.println(bfstTopology.toString());

	}

}
