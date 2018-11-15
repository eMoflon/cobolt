package de.tudarmstadt.maki.simonstrator.application.streaming;

/**
 * Application-Interface for the Video Player Application
 * 
 * 
 * @author Julius Rueckert
 * 
 */
public interface TransitListener {

	/**
	 * TODO: Further specify and decide what to pass to application. For sure
	 * some kind of block identifier.
	 */
	public void newBlockReceived();
}
