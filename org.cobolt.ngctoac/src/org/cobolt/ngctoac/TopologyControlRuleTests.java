package org.cobolt.ngctoac;

import java.util.Arrays;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TopologyControlRuleTests {
	private static final String TOPOLOGY_INSTANCE_TWO_NODES = "../../../../instance/Topology.xmi";

	private HenshinResourceSet resourceSet;

	private Module rulesModule;

	private Engine engine;

	private EGraphImpl graph;

	private TopologyControlRuleExecutionHelper ruleExecutionHelper;

	@Before
	public void setUp() {
		this.resourceSet = new HenshinResourceSet(HenshinRules.getRulesDirectory());
		this.rulesModule = resourceSet.getModule(HenshinRules.RULES_FILE, false);
		this.engine = new EngineImpl();
		final Resource testTopologyResource = resourceSet.getResource(TOPOLOGY_INSTANCE_TWO_NODES);
		this.graph = new EGraphImpl(testTopologyResource);
		this.ruleExecutionHelper = new TopologyControlRuleExecutionHelper(graph, rulesModule, engine);
	}

	@Test
	public void testTestmodelValidity() {
		final EObject topology = getTopology();
		Assert.assertEquals(3, GraphUtils.getNodes(topology).size());
		Assert.assertEquals(0, GraphUtils.getLinks(topology).size());
	}

	@Test
	public void testAddLinkBetweenNodes() {
		final EObject topology = getTopology();

		final String linkIdToAdd = "1->2";

		Assert.assertFalse(GraphUtils.containsLinkWithId(topology, linkIdToAdd));
		Assert.assertTrue(addLink(linkIdToAdd));
		Assert.assertTrue(GraphUtils.containsLinkWithId(topology, linkIdToAdd));
		Assert.assertTrue(GraphUtils.containsLinkWithIdAndWeight(topology, linkIdToAdd, 1));
		Assert.assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkIdToAdd, LinkState.UNMARKED));
	}

	@Test
	public void testSetLinkState() {
		final EObject topology = getTopology();

		final String linkId = "1->2";

		Assert.assertTrue(addLink(linkId));
		Assert.assertTrue(setLinkState(linkId, LinkState.INACTIVE));
		Assert.assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkId, LinkState.INACTIVE));
		Assert.assertTrue(setLinkState(linkId, LinkState.ACTIVE));
		Assert.assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkId, LinkState.ACTIVE));
	}

	@Test
	public void testCreateTriangleSuccessful() {
		final EObject topology = getTopology();

		for (final String linkIdToAdd : Arrays.asList("1->2", "1->3", "3->2")) {
			Assert.assertTrue(addLink(linkIdToAdd));
		}
		Assert.assertEquals(3, GraphUtils.getLinks(topology).size());
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * inactive link is allowed
	 */
	@Test
	public void testCreateTriangleAllowedByApplicationCondition() {
		Assert.assertTrue(addLink_Refined("1->2", 7));
		Assert.assertTrue(setLinkState("1->2", LinkState.INACTIVE));
		Assert.assertTrue(addLink_Refined("1->3", 2));

		Assert.assertTrue(addLink_Refined("3->2", 3));
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * active link is forbidden
	 */
	@Test
	public void testCreateTriangleForbiddenByApplicationCondition() {
		Assert.assertTrue(addLink_Refined("1->2", 7));
		Assert.assertTrue(setLinkState("1->2", LinkState.ACTIVE));
		Assert.assertTrue(addLink_Refined("1->3", 1));

		Assert.assertFalse(addLink_Refined("3->2", 6));
	}

	private boolean addLink(final String linkIdToAdd) {
		return this.ruleExecutionHelper.prepare_addLink(linkIdToAdd).execute(null);
	}

	private boolean addLink_Refined(final String linkIdToAdd, final double weight) {
		return this.ruleExecutionHelper.prepare_addLink_Refined(linkIdToAdd, weight).execute(null);
	}

	private boolean setLinkState(final String linkId, final int newState) {
		return this.ruleExecutionHelper.prepare_setLinkState(linkId, newState).execute(null);
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