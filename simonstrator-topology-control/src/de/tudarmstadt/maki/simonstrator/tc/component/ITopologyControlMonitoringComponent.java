package de.tudarmstadt.maki.simonstrator.tc.component;

public interface ITopologyControlMonitoringComponent
{
   void setParentComponent(TopologyControlComponent component);

   void performMeasurement();
}
