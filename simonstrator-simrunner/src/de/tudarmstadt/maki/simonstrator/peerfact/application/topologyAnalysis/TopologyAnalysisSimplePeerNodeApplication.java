package de.tudarmstadt.maki.simonstrator.peerfact.application.topologyAnalysis;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

public class TopologyAnalysisSimplePeerNodeApplication implements HostComponent {

	private Host host;

	public TopologyAnalysisSimplePeerNodeApplication(Host host) {
		this.host = host;
	}

	@Override
	public void initialize() {
		// Monitor.log(getClass(), Level.INFO, "Initialized");
	}

	@Override
	public void shutdown() {

	}

	@Override
	public Host getHost() {
		return this.host;
	}

}
