package de.tudarmstadt.maki.simonstrator.tc.facade;

/**
 * This enum models the possible operation modes of topology control algorithms.
 */
public enum TopologyControlOperationMode {
	/**
	 * This mode can be used to indicate that the active topology control
	 * algorithm does not care or provide a distinction between different modes.
	 */
	NOT_SET,
	/**
	 * When this mode is enabled, the topology control algorithm should operate
	 * in incremental mode, i.e., reuse previous decisions whenever possible
	 */
	INCREMENTAL,
	/**
	 * When this mode is enabled, the topology control algorithm should operate
	 * in batch mode, i.e., ignore context events and re-process the whole
	 * topology upon each execution.
	 */
	BATCH

}
