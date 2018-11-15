package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.Execute.ExceptionSimulatorRunner;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run.SimulationTask;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.analysis.SplConquerorHelper;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;

/**
 * This class represents an individual simulation run based on a complete
 * {@link TopologyControlComponentConfig}
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public final class TopologyControlSimulationTask implements SimulationTask {
	private final TopologyControlComponentConfig config;

	public TopologyControlSimulationTask(final TopologyControlComponentConfig config) {
		this.config = config;
	}

	@Override
	public Class<?> getSimulationClass() {
		return ExceptionSimulatorRunner.class;
	}

	@Override
	public List<String> getParams() {
		final LinkedList<String> params = new LinkedList<String>();
		params.add(config.simulationConfigurationFile);
		params.add("name=" + this.config.name);
		params.add("configurationNumber=" + this.config.configurationNumber);
		params.add("seed=" + this.config.seed);
		params.add("enableVisualization=false");
		params.add("outputFilePrefix=" + this.config.outputFilePrefix);
		params.add("outputFolder=" + this.config.outputFolder.getAbsolutePath());
		params.add("tracesOutputFolder=" + this.config.tracesOutputFolder.getAbsolutePath());
		params.add("size=" + this.config.nodeCount);
		params.add("world_size=" + this.config.worldSize);
		params.add("batteryCapacitySensor=" + this.config.batteryCapacitySensor);
		params.add("batteryCapacityMaster=" + this.config.batteryCapacityMaster);
		params.add("scenario=" + this.config.scenario.toString());
		params.add("datacollectionProbability=" + this.config.datacollectionProbability);
		params.add("topologyControlAlgorithm=" + this.config.topologyControlAlgorithmID.getName());

		final Object ktcParameterK = this.config.topologyControlAlgorithmParamters
				.getValue(UnderlayTopologyControlAlgorithms.KTC_PARAM_K);
		if (ktcParameterK != null)
			params.add("topologyControlParameterK=" + ktcParameterK);

		final Object lStarKtcParamterA = this.config.topologyControlAlgorithmParamters
				.getValue(UnderlayTopologyControlAlgorithms.LSTAR_KTC_PARAM_A);
		if (lStarKtcParamterA != null)
			params.add("topologyControlParameterA=" + lStarKtcParamterA);

		final Object yaoParameterConeCount = this.config.topologyControlAlgorithmParamters
				.getValue(UnderlayTopologyControlAlgorithms.YAO_PARAM_CONE_COUNT);
		if (yaoParameterConeCount != null)
			params.add("topologyControlParameterConeCount=" + yaoParameterConeCount);

		params.add("requiredTransmissionPowerExponent=" + this.config.requiredTransmissionPowerExponent);
		params.add(
				"topologyControlEdgeWeightProvider=" + this.config.edgeWeightCalculatingFunction.getClass().getName());

		params.add("end=" + this.config.end);
		params.add("movementModel=" + this.config.movementModel);
		params.add("movementModelMaster=" + this.config.movementModelMaster);
		params.add("movementMaxSpeed=" + this.config.movementMaxSpeedInMetersPerSecond);
		params.add("movementInterval=" + this.config.movementInterval);
		params.add("minimumDistanceThresholdInMeters=" + this.config.minimumDistanceThresholdInMeters);
		params.add("nodePreprocessorClass=" + this.config.nodePreprocessorClass);
		params.add("nodePreprocessorShallReverseOrder=" + this.config.nodePreprocessorShallReverseOrder);
		params.add("topologyControlIntervalInMinutes=" + this.config.topologyControlIntervalInMinutes);
		params.add("topologyControlExecutionMode=" + this.config.topologyControlFrequencyMode.toString());
		params.add("topologyControlOperationMode=" + this.config.topologyControlOperationMode.toString());
		params.add("topologyMonitoringLocalViewSize=" + this.config.topologyMonitoringLocalViewSize);
		params.add("reconfigurationEnabled=" + this.config.isReconfigurationEnabled());
		if (this.config.splcOutputFile != null)
			params.add(SplConquerorHelper.SIM_CFG_OPTION_SPLC_FILE + "=" + this.config.splcOutputFile);
		params.add("splcFeatureInteractionLineNumber=" + this.config.splcFeatureInteractionLineNumber);
		params.add("adaptationLogicSeed=" + this.config.adaptationLogicSeed);
		params.add("adaptationLogicPlanner=" + this.config.adaptationLogicPlanner);
		if (this.config.goalNonfunctionalProperty != null)
			params.add("goalNonfunctionalProperty=" + this.config.goalNonfunctionalProperty.getName());
		return params;
	}

	@Override
	public String toString() {
		return String.format("Simulation #%d [Params: %s]", this.config.configurationNumber, getParams());
	}
}