package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.alltoall.AllToAllTransmissionApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.alltoall.PointToPointTransmissionApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.TopologyControlEvaluationApplicationConfig;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.datacollection.BaseStationApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.datacollection.DataCollectionTransmissionApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.gossip.GossipTransmissionApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.silence.NoTransmissionApplication;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.wildfire.WildfireMonitoringApplication;
import de.tudarmstadt.maki.simonstrator.tc.scenario.ScenarioType;

public class TopologyControlEvaluationScenarioFactory implements HostComponentFactory {

	private ScenarioType scenario;
	private ScenarioRole role;

	private TopologyControlEvaluationApplicationConfig config;

	public void setConfig(final TopologyControlEvaluationApplicationConfig config) {
		this.config = config;
	}

	public void setScenario(String scenario) {
		this.scenario = ScenarioType.valueOf(scenario);
	}

	public void setRole(String role) {
		this.role = ScenarioRole.valueOf(role);
	}

	@Override
	public HostComponent createComponent(Host host) {
		this.config.host = host;
		switch (scenario) {
		case DATACOLLECTION:
			switch (role) {
			case TCNODE:
				return new BaseStationApplication(config);
			case GROUPNODE:
				return new DataCollectionTransmissionApplication(config);
			}
		case ALLTOALL:
			switch (role) {
			case TCNODE:
			case GROUPNODE:
				return new AllToAllTransmissionApplication(config);
			}
		case GOSSIP:
			switch (role) {
			case TCNODE:
			case GROUPNODE:
				return new GossipTransmissionApplication(config);
			}
		case POINTTOPOINT:
			switch (role) {
			case TCNODE:
			case GROUPNODE:
				return new PointToPointTransmissionApplication(config);
			}
		case SILENCE:
			switch (role) {
			case TCNODE:
			case GROUPNODE:
				return new NoTransmissionApplication(config);
			}
		case WILDFIRE:
			switch (role) {
			case TCNODE:
			case GROUPNODE:
				return new WildfireMonitoringApplication(config);
			}
		default:
			throw new IllegalArgumentException("Cannot create component for scenario " + scenario);
		}
	}

}
