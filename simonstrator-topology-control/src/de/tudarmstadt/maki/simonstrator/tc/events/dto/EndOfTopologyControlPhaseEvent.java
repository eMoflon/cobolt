package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class EndOfTopologyControlPhaseEvent extends Event {
	public static final String TYPE_ID = "endOfTopologyControlPhase";

	public EndOfTopologyControlPhaseEvent() {
		super(TYPE_ID);
	}

}
