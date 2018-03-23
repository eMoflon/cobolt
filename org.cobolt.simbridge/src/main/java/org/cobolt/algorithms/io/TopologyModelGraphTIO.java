package org.cobolt.algorithms.io;

import java.util.HashMap;
import java.util.Map;

import org.cobolt.model.Edge;
import org.cobolt.model.EdgeState;
import org.cobolt.model.ModelPackage;
import org.cobolt.model.Topology;
import org.cobolt.model.TopologyElement;
import org.eclipse.emf.ecore.EStructuralFeature;

import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

public final class TopologyModelGraphTIO {
	public TopologyModelGraphTIO() {
		throw new UtilityClassNotInstantiableException();
	}

	static Map<String, EStructuralFeature> initializeAttributeIdentifierMapping() {
		final Map<String, EStructuralFeature> modelAttributeMapping = new HashMap<>();

		modelAttributeMapping.put("E", ModelPackage.eINSTANCE.getNode_EnergyLevel());
		modelAttributeMapping.put("h", ModelPackage.eINSTANCE.getNode_HopCount());
		modelAttributeMapping.put("x", ModelPackage.eINSTANCE.getNode_X());
		modelAttributeMapping.put("y", ModelPackage.eINSTANCE.getNode_Y());

		modelAttributeMapping.put("a", ModelPackage.eINSTANCE.getEdge_Angle());
		modelAttributeMapping.put("d", ModelPackage.eINSTANCE.getEdge_Distance());
		modelAttributeMapping.put("L", ModelPackage.eINSTANCE.getEdge_ExpectedLifetime());
		modelAttributeMapping.put("P", ModelPackage.eINSTANCE.getEdge_TransmissionPower());
		modelAttributeMapping.put("R", ModelPackage.eINSTANCE.getEdge_ReverseEdge());
		modelAttributeMapping.put("s", ModelPackage.eINSTANCE.getEdge_State());
		modelAttributeMapping.put("w", ModelPackage.eINSTANCE.getEdge_Weight());
		return modelAttributeMapping;
	}

	static Object convertToObject(String attributeValue, String attributeIdentifier, Topology topology,
			TopologyElement topologyElement) {
		switch (attributeIdentifier) {
		case "a":
		case "d":
		case "E":
		case "L":
		case "P":
		case "w":
		case "x":
		case "y":
			return Double.parseDouble(attributeValue);
		case "h":
			return Integer.parseInt(attributeValue);
		case "s":
			return parseEdgeState(attributeValue);
		case "R":

			if (attributeValue.equals(topologyElement.getId()))
				throw new IllegalArgumentException(String.format(
						"Reverse link ID '%s' must be distinct from the ID of the current topology element",
						attributeValue));

			final Edge reverseEdge = topology.getEdgeById(attributeValue);
			if (reverseEdge == null)
				throw new IllegalArgumentException(
						String.format("No edge with ID '%s' is known (yet)", attributeValue));
			return reverseEdge;
		default:
			throw new IllegalArgumentException("Unsupported attribute identifier: " + attributeIdentifier);
		}
	}

	static EdgeState parseEdgeState(String stateIdentifier) {
		switch (stateIdentifier) {
		case "A":
			return EdgeState.ACTIVE;
		case "I":
			return EdgeState.INACTIVE;
		case "U":
			return EdgeState.UNCLASSIFIED;
		default:
			throw new IllegalArgumentException("Unsupported state identifier: " + stateIdentifier);
		}
	}
}
