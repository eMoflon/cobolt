package de.tudarmstadt.maki.simonstrator.overlay.api;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;

/**
 * Generic Overlay-Message Analyzer. This should be triggered actively by your
 * respective overlay.
 * 
 * @author Bjoern Richerzhagen
 * @version Jun 30, 2014
 */
public interface IOverlayMessageAnalyzer extends Analyzer {

	/**
	 * Call this, as soon as an OverlayMessage is sent by the given host.
	 * 
	 * @param msg
	 * @param host
	 */
	public void onSentOverlayMessage(OverlayMessage msg, Host host, NetInterfaceName netName);

	/**
	 * Call this, as soon as an OverlayMessage is received by the given host.
	 * 
	 * @param msg
	 * @param host
	 */
	public void onReceivedOverlayMessage(OverlayMessage msg, Host host);

}
