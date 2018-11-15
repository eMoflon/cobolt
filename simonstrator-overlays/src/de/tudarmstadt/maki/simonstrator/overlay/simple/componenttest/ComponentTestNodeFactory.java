package de.tudarmstadt.maki.simonstrator.overlay.simple.componenttest;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class ComponentTestNodeFactory implements HostComponentFactory {

	@Override
	public HostComponent createComponent(Host host) {
		return new ComponentTestNode(host);
	}

}
