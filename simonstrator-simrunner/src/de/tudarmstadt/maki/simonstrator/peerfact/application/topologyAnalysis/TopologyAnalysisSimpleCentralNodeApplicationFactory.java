package de.tudarmstadt.maki.simonstrator.peerfact.application.topologyAnalysis;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class TopologyAnalysisSimpleCentralNodeApplicationFactory implements HostComponentFactory {

	@Override
	public HostComponent createComponent(Host host) {
		return new TopologyAnalysisSimpleCentralNodeApplication(host);
	}

}
