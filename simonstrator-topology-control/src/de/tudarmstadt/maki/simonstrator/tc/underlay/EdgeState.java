package de.tudarmstadt.maki.simonstrator.tc.underlay;

public enum EdgeState {
   /**
    * Indicates that an edge is crucial/important/necessary
    */
	ACTIVE, 
	/**
	 * Indicates that an edge can be dropped without risking crucial properties (e.g., connectivity)
	 */
	INACTIVE, 
	/**
	 * Indicates that an edge has not been processed yet or needs to be re-processed (e.g., after context events).
	 */
	UNCLASSIFIED;
}
