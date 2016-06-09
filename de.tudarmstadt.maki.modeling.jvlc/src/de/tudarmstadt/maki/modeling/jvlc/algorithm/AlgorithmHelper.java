package de.tudarmstadt.maki.modeling.jvlc.algorithm;

import java.util.Arrays;
import java.util.List;

import de.tudarmstadt.maki.modeling.graphmodel.EdgeState;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.ConstraintsFactory;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.EdgeStateBasedConnectivityConstraint;
import de.tudarmstadt.maki.modeling.graphmodel.constraints.GraphConstraint;
import de.tudarmstadt.maki.modeling.jvlc.AbstractTopologyControlAlgorithm;
import de.tudarmstadt.maki.modeling.jvlc.DistanceKTCActiveLinkConstraint;
import de.tudarmstadt.maki.modeling.jvlc.DistanceKTCInactiveLinkConstraint;
import de.tudarmstadt.maki.modeling.jvlc.JvlcFactory;
import de.tudarmstadt.maki.simonstrator.tc.facade.TopologyControlAlgorithmID;
import de.tudarmstadt.maki.simonstrator.tc.ktc.UnderlayTopologyControlConstants;

public class AlgorithmHelper {

	public static AbstractTopologyControlAlgorithm createAlgorithmForID(final TopologyControlAlgorithmID algorithmId) {

		if (UnderlayTopologyControlConstants.ID_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalDistanceKTC();
		else if (UnderlayTopologyControlConstants.IE_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createIncrementalEnergyKTC();
		else if (UnderlayTopologyControlConstants.NULL_TC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createNullTopologyControlAlgorithm();
		else if (UnderlayTopologyControlConstants.D_KTC.asString().equals(algorithmId.asString()))
			return JvlcFactory.eINSTANCE.createDistanceKTC();
		else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmId);
	}

	public static List<GraphConstraint> getGraphConstraintsOfWeakConsistency(
			final TopologyControlAlgorithmID algorithmID) {

		if (Arrays.asList(UnderlayTopologyControlConstants.ID_KTC, UnderlayTopologyControlConstants.D_KTC).contains(algorithmID)) {
			EdgeStateBasedConnectivityConstraint weakConnectivityConstraint = ConstraintsFactory.eINSTANCE
					.createEdgeStateBasedConnectivityConstraint();
			weakConnectivityConstraint.getStates().add(EdgeState.ACTIVE);
			weakConnectivityConstraint.getStates().add(EdgeState.UNCLASSIFIED);

			DistanceKTCInactiveLinkConstraint inactiveLinkKTCConstraint = JvlcFactory.eINSTANCE
					.createDistanceKTCInactiveLinkConstraint();
			DistanceKTCActiveLinkConstraint activeLinkKTCConstraint = JvlcFactory.eINSTANCE
					.createDistanceKTCActiveLinkConstraint();
			return Arrays.asList(//
					inactiveLinkKTCConstraint, //
					activeLinkKTCConstraint, //
					ConstraintsFactory.eINSTANCE.createNoUnclassifiedLinksConstraint(), //
					weakConnectivityConstraint //
			);
		} else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmID);
	}

	public static List<GraphConstraint> getGraphConstraintsOfStrongConsistency(TopologyControlAlgorithmID algorithmID) {
		if (Arrays.asList(UnderlayTopologyControlConstants.ID_KTC, UnderlayTopologyControlConstants.D_KTC).contains(algorithmID)) {
			EdgeStateBasedConnectivityConstraint strongConnectivityConstraint = ConstraintsFactory.eINSTANCE
					.createEdgeStateBasedConnectivityConstraint();
			strongConnectivityConstraint.getStates().add(EdgeState.ACTIVE);

			DistanceKTCInactiveLinkConstraint inactiveLinkKTCConstraint = JvlcFactory.eINSTANCE
					.createDistanceKTCInactiveLinkConstraint();
			DistanceKTCActiveLinkConstraint activeLinkKTCConstraint = JvlcFactory.eINSTANCE
					.createDistanceKTCActiveLinkConstraint();
			return Arrays.asList(//
					inactiveLinkKTCConstraint, //
					activeLinkKTCConstraint, //
					strongConnectivityConstraint //
			);
		} else
			throw new IllegalArgumentException("Unsupported algorithm ID: " + algorithmID);
	}

}
