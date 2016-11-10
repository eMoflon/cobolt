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
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
	}
}
