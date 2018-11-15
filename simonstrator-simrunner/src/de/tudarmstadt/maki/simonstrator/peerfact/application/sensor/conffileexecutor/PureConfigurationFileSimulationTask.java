package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.conffileexecutor;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.Execute.ExceptionSimulatorRunner;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;

/**
 * This class represents an individual simulation run based on a
 * {@link TopologyControlComponentConfig} from which only the simulation
 * configuration file and the configuration number are extracted
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public final class PureConfigurationFileSimulationTask implements SimulationTask {
	private TopologyControlComponentConfig config;

	public PureConfigurationFileSimulationTask(final TopologyControlComponentConfig config) {
		this.config = config;
	}

	@Override
	public Class<?> getSimulationClass() {
		return ExceptionSimulatorRunner.class;
	}

	@Override
	public List<String> getParams() {
		LinkedList<String> params = new LinkedList<String>();
		params.add(config.simulationConfigurationFile);
		params.add("configurationNumber=" + this.config.configurationNumber);
		params.add("enableVisualization=false");
		params.add("outputFilePrefix=" + this.config.outputFilePrefix);
		params.add("outputFolder=" + this.config.outputFolder.getAbsolutePath());
		params.add("tracesOutputFolder=" + this.config.tracesOutputFolder.getAbsolutePath());
		return params;
	}

	@Override
	public String toString() {
		return String.format("Simulation #%d [Params: %s]", this.config.configurationNumber, getParams());
	}
}