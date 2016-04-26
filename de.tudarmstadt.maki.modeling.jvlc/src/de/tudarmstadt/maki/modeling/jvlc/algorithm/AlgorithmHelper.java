package de.tudarmstadt.maki.modeling.jvlc.algorithm;

import de.tudarmstadt.maki.modeling.jvlc.IncrementalKTC;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.ktc.KTCConstants;

public class AlgorithmHelper {

	public static IncrementalKTC createAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {
	
		if (KTCConstants.ID_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		else if (KTCConstants.IE_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		else if (KTCConstants.NULL_TC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createNullkTC();
		else if (KTCConstants.D_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createDistanceKTC();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
	
	}

}
