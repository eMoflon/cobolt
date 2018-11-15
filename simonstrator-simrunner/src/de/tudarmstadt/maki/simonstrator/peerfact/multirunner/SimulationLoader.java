package de.tudarmstadt.maki.simonstrator.peerfact.multirunner;

/**
 * Handler that loads all Simulations into the MultiRunner.
 * 
 * Such a handler could (i) read variations.txt files or any other kind of
 * file-based input to create a batch of simulations, (ii) even create the
 * xml-configs on the fly based on any other kind of (potentially far simpler)
 * input data (iii) provide a GUI or CMD-line frontend for simultion
 * configuration.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface SimulationLoader {

	/**
	 * Retrieves all arguments passed via the commandLine when MultiRunner was
	 * called.
	 * 
	 * @param args
	 */
	public void initialize(String[] args);

	/**
	 * Has to be blocking until all loading is done. This is called by the
	 * MultiRunner.
	 * 
	 * @return true to autostart simulation, false otherwise (user will have to
	 *         start simulations using the interactive shell)
	 */
	public boolean loadSimulations(SimulationLoader.Callback callback);

	/**
	 * Parallel number of simulations
	 * 
	 * @return
	 */
	public int getNumberOfParallelInstances();

	/**
	 * Interface to pass Simulations back to the MultiRunner.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public interface Callback {

		/**
		 * Call this for each simulation. Calls are no longer allowed after
		 * loadSimulation as implemented by the {@link SimulationLoader}
		 * returned.
		 * 
		 * @param simulation
		 */
		public void addSimulation(SimulationDescription simulation);

	}

}
