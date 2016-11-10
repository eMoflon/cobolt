package de.tudarmstadt.maki.tc.cbctc.algorithms.algorithm;

import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.underlay.UnderlayTopologyControlAlgorithms;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.tc.cbctc.algorithms.AlgorithmsFactory;;

public class AlgorithmHelper {

	public static AbstractTopologyControlAlgorithm createAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {

		if (UnderlayTopologyControlAlgorithms.D_KTC.equals(algorithmId))
			return AlgorithmsFactory.eINSTANCE.createPlainKTC();
		else if (UnderlayTopologyControlAlgorithms.E_KTC.equals(algorithmId))
			return AlgorithmsFactory.eINSTANCE.createEnergyAwareKTC();
		else if (UnderlayTopologyControlAlgorithms.MAXPOWER_TC.equals(algorithmId))
			return AlgorithmsFactory.eINSTANCE.createMaxpowerTopologyControlAlgorithm();
		else if (UnderlayTopologyControlAlgorithms.GABRIEL_GRAPH.equals(algorithmId))
		   return AlgorithmsFactory.eINSTANCE.createGabrielGraphAlgorithm();
		else if (UnderlayTopologyControlAlgorithms.L_KTC.equals(algorithmId))
		   return AlgorithmsFactory.eINSTANCE.createLocalKTC();
		else if (UnderlayTopologyControlAlgorithms.RELATIVE_NEIGHBORHOOD_GRAPH.equals(algorithmId))
		   return AlgorithmsFactory.eINSTANCE.createRelativeNeighborhoodGraphAlgorithm();
		else if (UnderlayTopologyControlAlgorithms.XTC.equals(algorithmId))
		   return AlgorithmsFactory.eINSTANCE.createXTCAlgorithm();
		else if (UnderlayTopologyControlAlgorithms.YAO.equals(algorithmId))
		   return AlgorithmsFactory.eINSTANCE.createYaoGraphAlgorithm();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
	}
}
