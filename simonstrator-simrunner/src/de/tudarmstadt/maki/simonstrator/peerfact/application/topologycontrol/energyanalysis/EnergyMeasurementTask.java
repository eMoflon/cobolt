package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.Execute.ExceptionSimulatorRunner;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;

public final class EnergyMeasurementTask implements SimulationTask {
	private final EnergyMeasurementConfiguration config;

	public EnergyMeasurementTask(EnergyMeasurementConfiguration config) {
		this.config = config;
	}

	@Override
	public Class<?> getSimulationClass() {
		return ExceptionSimulatorRunner.class;
	}

	@Override
	public List<String> getParams() {
		LinkedList<String> params = new LinkedList<String>();
		params.add(config.configFile.getPath());
		params.add("enableVisualization=false");
		params.add("batteryCapacitySensor=" + this.config.batteryCapacityInJoule);
		params.add("seed=" + this.config.seed);
		params.add("outputFile=" + this.config.outputFile.getAbsolutePath());
		params.add("phyType=" + this.config.phyType.toString());
		params.add("transmissionFrequencyAverageInSeconds=" + this.config.transmissionFrequencyAverageInSeconds);
		params.add("movementStepSizeInMeters=" + this.config.movementStepSizeInMeters);
		params.add("movementTimeInterval=" + this.config.movementTimeInterval);
		params.add("initialDistanceInMeters=" + this.config.initialDistanceInMeters);
		return params;
	}

	@Override
	public String toString() {
		return "Energy model calibration simulation. Simulation: " + getSimulationClass() + "; Params: " + getParams();
	}
}