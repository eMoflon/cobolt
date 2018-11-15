package de.tudarmstadt.maki.simonstrator.peerfact.application.topopologymonitoring;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class TopologyMonitoringApplicationFactory implements
		HostComponentFactory {

	@Override
	public HostComponent createComponent(Host host) {
		return new TopologyMonitoringApplication((SimHost) host);
	}

}
