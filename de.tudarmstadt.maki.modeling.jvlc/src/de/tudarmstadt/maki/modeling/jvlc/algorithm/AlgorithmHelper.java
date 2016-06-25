package de.tudarmstadt.maki.modeling.jvlc.algorithm;

import de.tudarmstadt.maki.modeling.jvlc.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlAlgorithms;

public class AlgorithmHelper {

	public static AbstractTopologyControlAlgorithm createAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {

		if (UnderlayTopologyControlAlgorithms.D_KTC.equals(algorithmId))
			return JvlcFactory.eINSTANCE.createPlainKTC();
		else if (UnderlayTopologyControlAlgorithms.E_KTC.equals(algorithmId))
			return JvlcFactory.eINSTANCE.createEnergyAwareKTC();
		else if (UnderlayTopologyControlAlgorithms.MAXPOWER_TC.equals(algorithmId))
			return JvlcFactory.eINSTANCE.createMaxpowerTopologyControlAlgorithm();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
	}
}
