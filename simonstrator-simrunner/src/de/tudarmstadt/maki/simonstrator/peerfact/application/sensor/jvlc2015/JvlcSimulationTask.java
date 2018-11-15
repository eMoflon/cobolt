package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.jvlc2015;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.Execute.ExceptionSimulatorRunner;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;

public final class JvlcSimulationTask implements SimulationTask {
	public static final String CONFIG_FILE = "config/jvlc/jvlc_complete_evaluation.xml";
	private int seed;
	private TopologyControlAlgorithmID topologyControlAlgorithmID;
	private int nodeCount;
	private double k;
	private double requiredTransmissionPowerExponent;
	private int worldSize;
	private File outputFolder;
	private final double batteryCapacitySensor;

	public JvlcSimulationTask(int seed, TopologyControlAlgorithmID topologyControlAlgorithmID, int worldSize,
			int nodeCount, double k, double requiredTransmissionPowerExponent, double batteryCapacitySensor,
			File outputFolder) {
		this.seed = seed;
		this.topologyControlAlgorithmID = topologyControlAlgorithmID;
		this.worldSize = worldSize;
		this.nodeCount = nodeCount;
		this.k = k;
		this.requiredTransmissionPowerExponent = requiredTransmissionPowerExponent;
		this.batteryCapacitySensor = batteryCapacitySensor;
		this.outputFolder = outputFolder;
	}

	@Override
	public Class<?> getSimulationClass() {
		return ExceptionSimulatorRunner.class;
	}

	@Override
	public List<String> getParams() {
		LinkedList<String> params = new LinkedList<String>();
		params.add(CONFIG_FILE);
		params.add("batteryCapacitySensor=" + batteryCapacitySensor);
		params.add("size=" + this.nodeCount);
		params.add("seed=" + this.seed);
		params.add("world_size=" + this.worldSize);
		params.add("enableVisualization=false");
		params.add("topologyControlParameterK=" + this.k);
		params.add("incrementalAlgorithm=" + this.topologyControlAlgorithmID.getName());
		params.add("requiredTransmissionPowerExponent=" + this.requiredTransmissionPowerExponent);
		params.add("outputFolder=" + this.outputFolder.getAbsolutePath());
		params.add("outputFilePrefix=run_");
		return params;
	}

	@Override
	public String toString() {
		return "JVLC Simulation. Simulation: " + getSimulationClass() + "; Params: " + getParams();
	}
}