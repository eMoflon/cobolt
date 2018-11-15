package de.tudarmstadt.maki.simonstrator.tc.monitoring;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * Factory for {@link TopologyControlInformationStoreComponent}
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class TopologyControlInformationStoreComponentFactory implements HostComponentFactory
{
   @Override
   public TopologyControlInformationStoreComponent createComponent(Host host)
   {
      return new TopologyControlInformationStoreComponent(host);
   }

}
