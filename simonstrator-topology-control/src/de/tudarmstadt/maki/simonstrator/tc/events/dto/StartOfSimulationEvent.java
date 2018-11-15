package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class StartOfSimulationEvent extends Event
{
   private static final String TYPE_ID = "startOfSimulation";

   public StartOfSimulationEvent()
   {
      super(TYPE_ID);
   }
}
