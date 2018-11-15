package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlOperationMode;
import de.tudarmstadt.maki.simonstrator.tc.reconfiguration.TopologyControlComponentConfig;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

public class TopologyControlComponentFactory implements HostComponentFactory {

	private TopologyControlComponentConfig configuration;

	public void setConfig(TopologyControlComponentConfig configuration) {
		this.configuration = configuration;
	}

	@Override
	public HostComponent createComponent(final Host host) {
		configuration.host = host;
		configuration.seed = Simulator.getSeed();
		// Correct simulation size because there is always a dedicated node that
		// is not counted
		++configuration.nodeCount;
		configuration.simulationConfigurationFile = Simulator.getConfigurator().getConfigFile().getAbsolutePath();

		if (configuration.topologyControlOperationMode == TopologyControlOperationMode.NOT_SET)
			configuration.topologyControlOperationMode = TopologyControlOperationMode.INCREMENTAL;

		if (configuration.outputFilePrefix == null || configuration.outputFilePrefix.isEmpty())
			configuration.outputFilePrefix = "tceval_" + DateHelper.getFormattedDate() + "_";

		configuration.validate();

		final TopologyControlComponent topologyControlComponent = new TopologyControlComponent(configuration);
		if (configuration.incrementalTopologyControlFacade != null) {
			topologyControlComponent.setIncrementalFacade(configuration.incrementalTopologyControlFacade);
		}

		if (configuration.eventRecordingFacade != null) {
			topologyControlComponent.setEventRecordingFacade(configuration.eventRecordingFacade);
		}

		return topologyControlComponent;
	}

	protected static TopologyControlAlgorithmID mapToTopologyControlID(String algorithmId) {
		return UnderlayTopologyControlAlgorithms.mapToTopologyControlID(algorithmId);
	}
}
