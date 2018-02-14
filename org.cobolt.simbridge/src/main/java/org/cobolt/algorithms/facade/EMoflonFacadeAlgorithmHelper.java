package org.cobolt.algorithms.facade;

import org.cobolt.algorithms.AbstractTopologyControlAlgorithm;
import org.cobolt.algorithms.AlgorithmsFactory;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;;

/**
 * Helper class for algorithms in the {@link EMoflonFacade} 
 * 
 * @author Roland Kluge - Initial implementation
 */
public class EMoflonFacadeAlgorithmHelper
{

   /**
    * Maps the given {@link TopologyControlAlgorithmID} to an (executable) {@link AbstractTopologyControlAlgorithm}.
    */
   public static AbstractTopologyControlAlgorithm createAlgorithmForID(final TopologyControlAlgorithmID algorithmId)
   {

      if (UnderlayTopologyControlAlgorithms.D_KTC.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createPlainKTC();
      else if (UnderlayTopologyControlAlgorithms.E_KTC.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createEnergyAwareKTC();
      else if (UnderlayTopologyControlAlgorithms.MAXPOWER_TC.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createMaxpowerTopologyControlAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.GABRIEL_GRAPH.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createGabrielGraphAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.LSTAR_KTC.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createLStarKTC();
      else if (UnderlayTopologyControlAlgorithms.RELATIVE_NEIGHBORHOOD_GRAPH.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createRelativeNeighborhoodGraphAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.XTC.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createXTCAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.YAO.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createYaoGraphAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.LMST.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createLocalMinimumSpanningTreeAlgorithm();
      else if (UnderlayTopologyControlAlgorithms.GMST.equals(algorithmId))
         return AlgorithmsFactory.eINSTANCE.createGlobalMinimumSpanningTreeAlgorithm();
      else
         throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
   }
}
