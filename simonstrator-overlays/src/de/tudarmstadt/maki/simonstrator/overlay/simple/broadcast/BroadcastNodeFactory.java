package de.tudarmstadt.maki.simonstrator.overlay.simple.broadcast;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class BroadcastNodeFactory implements HostComponentFactory {

	public static int PORT = 12665;

	public BroadcastNodeFactory() {
		// do nothing
	}

	@Override
	public HostComponent createComponent(Host host) {
		return new BroadcastNode(host, PORT);
	}

}
