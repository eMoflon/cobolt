package de.tudarmstadt.maki.simonstrator.tc.events.dto;

public class CoalaVizReconfigureSasEvent extends Event
{
   public CoalaVizReconfigureSasEvent()
   {
      super("reconfigure-sas");
   }

   public String getConfigurationFile() {
      return "/coalaviz-wsn/sasconfig.properties";
   }

}
