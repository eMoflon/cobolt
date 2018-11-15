package de.tudarmstadt.maki.simonstrator.application.chat;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;


/**
 * Application-Interface for the Chat-Enabled Node
 * 
 * @author bjoern
 * 
 */
public interface ChatListener {

	/**
	 * Received a message
	 * 
	 * @param message
	 *            Application-Layer chat message
	 * @param from
	 */
	public void receivedMessage(String message, UniqueID from);

}
