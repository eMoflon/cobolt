package org.cobolt.ngctoac;

import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Unit;

public final class HenshinRules {

	static final String RULES_FILE = "tc.henshin";
	static final String RULE_ADD_LINK = "addLink";
	static final String RULE_ADD_LINK_REFINED = "addLink_updated_ActiveLinkIsNotLongestLinkInTriangle";
	static final String RULE_SET_LINK_STATE = "setLinkState";
	static final String RULE_SET_LINK_STATE_REFINED = "setLinkState_updated_ActiveLinkIsNotLongestLinkInTriangle";

	private HenshinRules() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Returns the project-relative path to the folder containing models and
	 * metamodels
	 */
	public static String getRulesDirectory() {
		return "src/org/cobolt/ngctoac";
	}

	/**
	 * Extracts the {@link Unit} with the given name from the given {@link Module}
	 * (if exists)
	 *
	 * If no such unit exists, an exception is thrown
	 *
	 * @param rulesModule
	 *            the module
	 * @param unitName
	 *            the unit
	 * @return the {@link Unit} if exists
	 */
	static Unit getUnitChecked(final Module rulesModule, final String unitName) {
		final Unit unit = rulesModule.getUnit(unitName);
		if (unit == null)
			throw new IllegalArgumentException(
					String.format("No unit with name %s in module %s", unitName, rulesModule));
		else
			return unit;
	}
}
