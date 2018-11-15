package de.tudarmstadt.maki.simonstrator.application.chat;

import de.tudarmstadt.maki.simonstrator.api.common.UniqueID;

/**
 * Implemented by a chat-node
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface ChatNode {

	/**
	 * Send a chat message
	 * 
	 * @param message
	 * @param to
	 */
	public void sendChatMessage(String message, UniqueID to);

	public void setChatListener(ChatListener chatListener);

}
