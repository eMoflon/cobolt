package de.tudarmstadt.maki.simonstrator.peerfact.application.topopologymonitoring;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.topology.monitoring.DistributedTopologyMonitoringComponent;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyObserver;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyProvider;

public class TopologyMonitoringApplication implements HostComponent, TopologyObserver {

	private final SimHost host;

	private DistributedTopologyMonitoringComponent topologyComponent;

	public TopologyMonitoringApplication(SimHost host) {
		this.host = host;
	}

	@Override
	public void initialize() {
		try {
			topologyComponent = this.getHost().getComponent(DistributedTopologyMonitoringComponent.class);
		} catch (ComponentNotAvailableException e) {
			throw new ConfigurationException("this application needs the "
					+ DistributedTopologyMonitoringComponent.class.getName() + " component");
		}

		topologyComponent.addTopologyObserver(this);
	}

	@Override
	public void shutdown() {
		//
	}

	@Override
	public SimHost getHost() {
		return this.host;
	}

	@Override
	public void topologyChanged(TopologyProvider topologyProvider, TopologyID topologyIdentifier) {
		System.err.println("The kTCParameterK-hop topology of node " + topologyProvider.getNode(topologyIdentifier)
				+ " has changed. This is the new local view:\n" + topologyProvider.getLocalView(topologyIdentifier));
	}
}
