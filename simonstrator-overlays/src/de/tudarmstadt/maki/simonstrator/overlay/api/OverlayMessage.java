package de.tudarmstadt.maki.simonstrator.overlay.api;

import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.OverlayContact;
import de.tudarmstadt.maki.simonstrator.overlay.AbstractOverlayMessage;

/**
 * Base-Interface for Overlay Messages. Consider extending
 * {@link AbstractOverlayMessage}
 * 
 * @author Bjoern Richerzhagen
 * @version May 3, 2014
 */
public interface OverlayMessage extends Message {

	/**
	 * The overlay contact of the intended receiver or null, if not specified.
	 * 
	 * @return
	 */
	public OverlayContact getReceiver();

	/**
	 * The overlay contact of the sender or null, if not specified.
	 * 
	 * @return
	 */
	public OverlayContact getSender();
}
