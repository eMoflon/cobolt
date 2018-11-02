package org.cobolt.ngctoac;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;

public class TopologyControlRuleExecutionHelper {

	private final EGraphImpl graph;
	private final Module rulesModule;
	private final Engine engine;

	public TopologyControlRuleExecutionHelper(final EGraphImpl graph, final Module rulesModule, final Engine engine) {
		this.graph = graph;
		this.rulesModule = rulesModule;
		this.engine = engine;
	}

	UnitApplication prepare_addLink(final String linkIdToAdd) {
		final EObject topology = getTopology();
		final String srcId = GraphUtils.extractSourceNodeId(linkIdToAdd);
		final String trgId = GraphUtils.extractTargetNodeId(linkIdToAdd);
		final double weight = 1.0;
		final UnitApplication unit = new UnitApplicationImpl(engine);
		unit.setEGraph(graph);
		unit.setUnit(HenshinRules.getUnitChecked(rulesModule, HenshinRules.RULE_ADD_LINK));
		unit.setParameterValue("srcId", srcId);
		unit.setParameterValue("trgId", trgId);
		unit.setParameterValue("linkId", linkIdToAdd);
		unit.setParameterValue("weight", weight);
		unit.setParameterValue("topology", topology);
		return unit;
	}

	UnitApplication prepare_addLink_Refined(final String linkIdToAdd, final double weight) {
		final EObject topology = getTopology();
		final String srcId = GraphUtils.extractSourceNodeId(linkIdToAdd);
		final String trgId = GraphUtils.extractTargetNodeId(linkIdToAdd);
		final UnitApplication unit = new UnitApplicationImpl(engine);
		unit.setEGraph(graph);
		unit.setUnit(HenshinRules.getUnitChecked(rulesModule, HenshinRules.RULE_ADD_LINK_REFINED));
		unit.setParameterValue("srcId", srcId);
		unit.setParameterValue("trgId", trgId);
		unit.setParameterValue("linkId", linkIdToAdd);
		unit.setParameterValue("weight", weight);
		unit.setParameterValue("topology", topology);
		return unit;
	}

	UnitApplication prepare_setLinkState(final String linkId, final int newState) {
		final EObject topology = getTopology();
		final UnitApplication unit = new UnitApplicationImpl(engine);
		unit.setEGraph(graph);
		unit.setUnit(HenshinRules.getUnitChecked(rulesModule, "setLinkState"));
		unit.setParameterValue("linkId", linkId);
		unit.setParameterValue("newState", newState);
		unit.setParameterValue("topology", topology);
		return unit;
	}

	/**
	 * Extracts the topology object from the graph. By convention, the topology is
	 * always the first root.
	 *
	 * @return the extracted topology
	 */
	private EObject getTopology() {
		return this.graph.getRoots().get(0);
	}
}
