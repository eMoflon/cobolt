package de.tudarmstadt.maki.simonstrator.peerfact.multirunner;


/**
 * A basic Simulation Loader Test.
 * 
 * @author bjoern
 * 
 */
public class SimLoaderTest implements SimulationLoader {

	@Override
	public void initialize(String[] args) {
		// not interested.
	}

	@Override
	public boolean loadSimulations(Callback callback) {
		callback.addSimulation(new SimulationDescription("Test 1",
				"config/pubsub/batch/minimal/pubsub_mini.xml"));
		callback.addSimulation(new SimulationDescription("Test 2",
				"config/pubsub/batch/minimal/pubsub_mini.xml"));
		callback.addSimulation(new SimulationDescription("Test 3",
				"config/pubsub/batch/minimal/pubsub_mini.xml"));
		callback.addSimulation(new SimulationDescription("Test 4",
				"config/pubsub/batch/minimal/pubsub_mini.xml"));
		System.out.println("Loaded four test configs...");
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int getNumberOfParallelInstances() {
		return 2;
	}

}
