package de.tudarmstadt.maki.simonstrator.tc.component;

import java.io.Writer;

import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

public class TopologyControlComponentShutdownAnalyzer implements Analyzer
{

   @Override
   public void start()
   {
      // Do nothing
   }

   @Override
   public void stop(Writer out)
   {
      TopologyControlComponent.find().shutdown();
   }

}
