package de.tudarmstadt.maki.simonstrator.tc.facade;

/**
 * Null implementation of {@link TopologyControlFacade_ImplBase} that does nothing
 * 
 * @author Roland Kluge - Initial implementation
 */
public class NullTopologyControlFacade extends TopologyControlFacade_ImplBase
{
   @Override
   public void run(final TopologyControlAlgorithmParamters parameters)
   {
      // do nothing
   }

}
