package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class EndOfContextEventHandlingPhaseEvent extends Event {
	public static final String TYPE_ID = "endOfContextEventSequence";

	public EndOfContextEventHandlingPhaseEvent() {
		super(TYPE_ID);
	}
}
