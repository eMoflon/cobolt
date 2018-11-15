package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class EndOfSimulationEvent extends Event {
	private static final String TYPE_ID = "endOfSimulation";

	public EndOfSimulationEvent()
   {
      super(TYPE_ID);
   }
}
