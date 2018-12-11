package org.cobolt.ngctoac;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.junit.Before;
import org.junit.Test;

public class TopologyControlRuleTests {
	private static final String PROJECT_ROOT_RELATIVE_PATH = "../../../..";

	private static final String TOPOLOGY_INSTANCE_TWO_NODES = PROJECT_ROOT_RELATIVE_PATH + "/instance/Topology.xmi";
	private static final String TOPOLOGY_INSTANCE_TEMP = PROJECT_ROOT_RELATIVE_PATH + "/output/Topology.xmi";

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
		assertEquals(3, GraphUtils.getNodes(topology).size());
		assertEquals(0, GraphUtils.getLinks(topology).size());
	}

	@Test
	public void testAddLinkBetweenNodes() {
		final EObject topology = getTopology();

		final String linkIdToAdd = "1->2";

		assertFalse(GraphUtils.containsLinkWithId(topology, linkIdToAdd));
		assertTrue(addLink(linkIdToAdd));
		assertTrue(GraphUtils.containsLinkWithId(topology, linkIdToAdd));
		assertTrue(GraphUtils.containsLinkWithIdAndWeight(topology, linkIdToAdd, 1));
		assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkIdToAdd, LinkState.UNMARKED));
	}

	@Test
	public void testSetLinkState() {
		final EObject topology = getTopology();

		final String linkId = "1->2";

		assertTrue(addLink(linkId));
		assertTrue(setLinkState(linkId, LinkState.INACTIVE));
		assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkId, LinkState.INACTIVE));
		assertTrue(setLinkState(linkId, LinkState.ACTIVE));
		assertTrue(GraphUtils.containsLinkWithStateAndId(topology, linkId, LinkState.ACTIVE));
	}

	@Test
	public void testCreateTriangleSuccessful() {
		final EObject topology = getTopology();

		for (final String linkIdToAdd : Arrays.asList("1->2", "1->3", "3->2")) {
			assertTrue(addLink(linkIdToAdd));
		}
		assertEquals(3, GraphUtils.getLinks(topology).size());
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * inactive link is allowed
	 */
	@Test
	public void testCreateTriangleAllowedByApplicationCondition() {
		assertTrue(addLink_Refined("1->2", 7));
		assertTrue(setLinkState("1->2", LinkState.INACTIVE));
		assertTrue(addLink_Refined("1->3", 2));

		assertTrue(addLink_Refined("3->2", 3));
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * active link is forbidden. Order: e12[A], e13, e32
	 */
	@Test
	public void testCreateTriangleForbiddenByApplicationCondition1() {
		assertTrue(addLink_Refined("1->2", 7));
		assertTrue(setLinkState("1->2", LinkState.ACTIVE));
		assertTrue(addLink_Refined("1->3", 1));

		saveCurrentTopology();

		assertFalse(addLink_Refined("3->2", 6));
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * active link is forbidden. Order: e13, e12[A], e32
	 */
	@Test
	public void testCreateTriangleForbiddenByApplicationCondition2() {
		assertTrue(addLink_Refined("1->3", 1));
		assertTrue(addLink_Refined("1->2", 7));
		assertTrue(setLinkState("1->2", LinkState.ACTIVE));

		assertFalse(addLink_Refined("3->2", 6));
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * active link is forbidden. Order: e32, e12[A], e13
	 */
	@Test
	public void testCreateTriangleForbiddenByApplicationCondition3() {
		assertTrue(addLink_Refined("3->2", 6));
		assertTrue(addLink_Refined("1->2", 7));
		assertTrue(setLinkState("1->2", LinkState.ACTIVE));

		assertFalse(addLink_Refined("1->3", 1));
	}

	/**
	 * This test case illustrates that closing creating a triangle on top of an
	 * active link is forbidden. Order: e32, e13, e12[A]
	 */
	@Test
	public void testCreateTriangleForbiddenByApplicationCondition4() {
		assertTrue(addLink_Refined("3->2", 6));
		assertTrue(GraphUtils.containsLinkWithStateAndId(getTopology(), "3->2", LinkState.UNMARKED));

		assertTrue(addLink_Refined("1->3", 1));
		assertTrue(GraphUtils.containsLinkWithStateAndId(getTopology(), "1->3", LinkState.UNMARKED));

		assertTrue(addLink_Refined("1->2", 7));
		assertTrue(GraphUtils.containsLinkWithStateAndId(getTopology(), "1->2", LinkState.UNMARKED));
		assertFalse(setLinkState_Refined("1->2", LinkState.ACTIVE));
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

	private boolean setLinkState_Refined(final String linkId, final int newState) {
		return this.ruleExecutionHelper.prepare_setLinkState_Refined(linkId, newState).execute(null);
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

	@SuppressWarnings("unused")
	/**
	 * Utility function for saving the current state of the topology
	 */
	private void saveCurrentTopology() {
		final Resource topologyResource = this.resourceSet.getResource(TOPOLOGY_INSTANCE_TWO_NODES);
		final Resource tempResource = this.resourceSet.createResource(TOPOLOGY_INSTANCE_TEMP);
		final EObject copiedTopology = EcoreUtil.copy(topologyResource.getContents().get(0));
		tempResource.getContents().add(copiedTopology);
		try {
			tempResource.save(null);
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}
}