package de.tudarmstadt.maki.simonstrator.tc.reconfiguration;

/**
 * The {@link TopologyControlFrequencyMode} determines the frequency of
 * executing TC
 * 
 * @author Roland Kluge - Initial implementation
 */
public enum TopologyControlFrequencyMode {
	/**
	 * In this mode, topology control is executed once.
	 */
	SINGLESHOT,
	/**
	 * In this mode, topology control is executed periodically.
	 */
	PERIODIC

}
