package org.cobolt.ngctoac;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;

public class GraphUtils {

	static final String NODE_ID_SEPARATOR = "->";

	private static String[] extractNodeIds(final String linkId) {
		if (!linkId.contains(NODE_ID_SEPARATOR))
			throw new IllegalArgumentException(
					String.format("Link ID %s should contain separator token %s", linkId, NODE_ID_SEPARATOR));
		return linkId.split(Pattern.quote(NODE_ID_SEPARATOR));
	}

	static String extractSourceNodeId(final String linkId) {
		return extractNodeIds(linkId)[0];
	}

	static String extractTargetNodeId(final String linkId) {
		return extractNodeIds(linkId)[1];
	}

	static boolean containsLinkWithId(final EObject topology, final String linkId) {
		return getLinks(topology).stream().map(o -> o.eGet(findIdStructuralFeature(o))).anyMatch(id -> {
			return linkId.equals(id);
		});
	}

	static boolean containsLinkWithIdAndWeight(final EObject topology, final String linkId,
			final Integer weight) {
		return getLinks(topology).stream().filter(o -> linkId.equals(o.eGet(findIdStructuralFeature(o)))).filter(o -> {
			return Math.abs(weight - (Double) o.eGet(findWeightStructuralFeature(o))) < 1e-7;
		}).findAny().isPresent();
	}

	static boolean containsLinkWithStateAndId(final EObject topology, final String linkId, final int state) {
		return getLinks(topology).stream().filter(o -> linkId.equals(o.eGet(findIdStructuralFeature(o))))
				.filter(link -> hasState(link, state)).findAny().isPresent();
	}

	static boolean hasState(final EObject link, final Integer expectedState) {
		return expectedState.equals(link.eGet(findStateStructuralFeature(link)));
	}

	@SuppressWarnings("unchecked")
	static List<? extends EObject> getLinks(final EObject topology) {
		return (List<EObject>) topology.eGet(findFeatureByName(topology, "links"));
	}

	@SuppressWarnings("unchecked")
	static List<? extends EObject> getNodes(final EObject topology) {
		return (List<EObject>) topology.eGet(findFeatureByName(topology, "nodes"));
	}

	private static EStructuralFeature findIdStructuralFeature(final EObject o) {
		return findFeatureByName(o, "id");
	}

	static EStructuralFeature findWeightStructuralFeature(final EObject o) {
		return findFeatureByName(o, "weight");
	}

	private static EStructuralFeature findStateStructuralFeature(final EObject o) {
		return findFeatureByName(o, "state");
	}

	private static EStructuralFeature findFeatureByName(final EObject o, final String featureName) {
		final Optional<EStructuralFeature> maybeFeature = o.eClass().getEAllStructuralFeatures().stream()
				.filter(f -> featureName.equals(f.getName())).findAny();
		if (maybeFeature.isPresent()) {
			return maybeFeature.get();
		} else {
			throw new IllegalArgumentException(
					String.format("Cannot find feature '%s' in class '%s' of %s", featureName, o.eClass(), o));
		}
	}

}
