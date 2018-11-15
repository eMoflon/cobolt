package de.tud.kom.p2psim.impl.network.fairshareng;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The Class DirectedGraph.
 * 
 * This algorithm is based on the Paper
 * 	Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks
 * by Alexandros Gkogkas et al.
 * 
 */
public class DirectedGraph {

	/** The flowList: contains all flows in graph. */
	private final Collection<FairshareFlow> flowList;

	/** The node map: contains all nodes in graph and respective flows of those. */
	private final HashMap<FairshareNode, HashSet<FairshareFlow>[]> nodeMap;

	/** Constants. */
	private final static int UPLOADING_FLOWS = 0;
	private final static int DOWNLOADING_FLOWS = 1;

	/** Constants. */
	private final static int AFFECTED_BY_UPLOAD_DECREASE = 0;
	private final static int AFFECTED_BY_UPLOAD_INCREASE = 1;
	private final static int AFFECTED_BY_DNLOAD_DECREASE = 2;
	private final static int AFFECTED_BY_DNLOAD_INCREASE = 3;

	/** Constants. */
	public final static boolean EVENT_STREAM_NEW = true;
	public final static boolean EVENT_STREAM_ENDED = false;

	/** Constants. */
	public final static boolean UPLOADING_FLOW = true;
	public final static boolean DOWNLOADING_FLOW = false;

	/** Constants. */
	public final static boolean USED_FOR_SCHEDULING = true;
	public final static boolean NOT_USED_FOR_SCHEDULING = false;

	/** Depending on usage, use different data structures: Set in constructor */
	private final boolean useForScheduling;

	/**
	 * Instantiates a new empty directed graph.
	 *
	 * @param useForScheduling use graph for scheduling
	 */
	public DirectedGraph(boolean useForScheduling) {
		
		this.nodeMap = new LinkedHashMap<FairshareNode, HashSet<FairshareFlow>[]>();

		this.useForScheduling = useForScheduling;
		if( useForScheduling ) {
			this.flowList = new LinkedList<FairshareFlow>();
		} else {
			this.flowList = new LinkedHashSet<FairshareFlow>();
		}
	}

	/**
	 * Instantiates a new directed graph containing given graph.
	 *
	 * @param graphToClone the full graph
	 */
	public DirectedGraph(DirectedGraph graphToClone) {

		this(DirectedGraph.NOT_USED_FOR_SCHEDULING);

		this.addAllNodes(graphToClone.getAllNodes());
		this.addAllFlows(graphToClone.getAllFlows());
		
	}

	/**
	 * Adds a node to the graph.
	 * 
	 * @param node
	 *            the node
	 */
	@SuppressWarnings("unchecked")
	public void addNode(FairshareNode node) {

		if (node == null) {
			return;
		}

		if (!this.nodeMap.containsKey(node)) {
			this.nodeMap.put(node, new LinkedHashSet[] { new LinkedHashSet<FairshareFlow>(30), new LinkedHashSet<FairshareFlow>(30) });
		}

	}

	/**
	 * Adds all nodes to the graph.
	 *
	 * @param nodes the nodes
	 */
	public void addAllNodes(Set<? extends FairshareNode> nodes) {
		for (final FairshareNode node : nodes) {
			this.addNode(node);
		}
	}

	/**
	 * Adds a the flow to the graph. Nodes have to be existent or
	 * exception will be thrown.
	 * 
	 * @param flow
	 *            the flow
	 * @return true, if successful
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public boolean addFlow(FairshareFlow flow) {

		/*
		 * Save in nodeMap to enable fast lookup as well as in flowSet for fast
		 * iteration.
		 */
		this.nodeMap.get(flow.getSrc())[UPLOADING_FLOWS].add(flow);
		this.nodeMap.get(flow.getDst())[DOWNLOADING_FLOWS].add(flow);

		if( !this.useForScheduling ) {
			return this.flowList.add(flow);
		}

		// Ok: We've got a list. Check for duplicates.
		if( this.flowList.contains(flow) ) {
			return false;
		}

		return this.flowList.add(flow);

	}

	/**
	 * Adds a the flow to the graph. Nodes _do not_ have to be existent before calling.
	 * In case nodes are not existent, nodes will be added to graph.
	 * 
	 * @param flow
	 *            the flow
	 * @return true, if successful
	 */
	public boolean addFlowWithNodes(FairshareFlow flow) {

		this.addNode(flow.getSrc());
		this.addNode(flow.getDst());

		boolean result = false;
		try {
			result = this.addFlow(flow);
		} catch (final Exception e) {
			// None.
		}

		return result;

	}

	/**
	 * Adds the all flows to the graph.
	 *
	 * @param flows the flows
	 * @throws Exception the exception
	 */
	public void addAllFlows(Collection<FairshareFlow> flows) {
		for (final FairshareFlow flow : flows) {
			this.addFlow(flow);
		}
	}


	/**
	 * Gets all flows.
	 * 
	 * @return the all flows
	 */
	public Collection<FairshareFlow> getAllFlows() {
		return this.flowList;
	}

	/**
	 * Gets all nodes.
	 * 
	 * @return the all nodes
	 */
	public Set<FairshareNode> getAllNodes() {
		return this.nodeMap.keySet();
	}

	/**
	 * Gets all uploading flows from Node
	 * 
	 * @param node
	 *            the node
	 * @return the uploading flows from node
	 */
	public Set<FairshareFlow> getUploadingFlowsFrom(FairshareNode node) {
		return node == null ? null : this.nodeMap.get(node)[UPLOADING_FLOWS];
	}

	/**
	 * Gets the uploading flows from given node in ascending order.
	 *
	 * @param node the node
	 * @return the uploading flows from given node in asc order
	 */
	private List<FairshareFlow> getUploadingFlowsFromInAscOrder(FairshareNode node) {

		final LinkedList<FairshareFlow> flows = new LinkedList<FairshareFlow>(this.getUploadingFlowsFrom(node));
		Collections.sort(flows);

		return flows;

	}

	/**
	 * Gets all downloading flows from Node
	 * 
	 * @param node
	 *            the node
	 * @return the downloading flows from
	 */
	public Set<FairshareFlow> getDownloadingFlowsTo(FairshareNode node) {
		return node == null ? null : this.nodeMap.get(node)[DOWNLOADING_FLOWS];
	}


	/**
	 * Gets the downloading flows in asc order.
	 *
	 * @param node the node
	 * @return the downloading flows in asc order
	 */
	private List<FairshareFlow> getDownloadingFlowsInAscOrder(FairshareNode node) {

		final LinkedList<FairshareFlow> flows = new LinkedList<FairshareFlow>(this.getDownloadingFlowsTo(node));
		Collections.sort(flows);

		return flows;
	}

	/**
	 * Tries to remove flow from graph. If flow does not exist in graph, no
	 * exception is raised.
	 *
	 * @param flow the flow
	 */
	public void tryRemoveFlow(FairshareFlow flow) {

		if( this.flowList.contains(flow) ) {
			this.removeFlow(flow);
		}

	}

	/**
	 * Removes flow from the graph.
	 *
	 * @param flow the flow
	 */
	public void removeFlow(FairshareFlow flow) {

		this.flowList.remove(flow);

		this.nodeMap.get(flow.getSrc())[UPLOADING_FLOWS].remove(flow);
		this.nodeMap.get(flow.getDst())[DOWNLOADING_FLOWS].remove(flow);

	}

	/**
	 * Removes node from the graph
	 *
	 * @param node the node
	 * 
	 * @throws Exception Throws exception if node has active flows.
	 */
	public void removeNode(FairshareNode node) throws Exception {

		if( (this.nodeMap.get(node)[UPLOADING_FLOWS].size() + this.nodeMap.get(node)[DOWNLOADING_FLOWS].size())  > 0 ) {
			throw new Exception(node + " still has active links. Need to be removed first.");
		}

		this.nodeMap.remove(node);

	}

	/**
	 * Allocate bandwidth on full graph, min-max implementation based on Gkogkas et. al.
	 * 
	 * Warning: method deletes flows from graph. If you need original graph,
	 * invoke clone();
	 * 
	 * @throws Exception
	 *             the exception
	 */
	public void allocateBandwidthOnFullGraph_Alg01() throws Exception {

		final List<FairshareNode> satUp = new LinkedList<FairshareNode>();
		final List<FairshareNode> satDown = new LinkedList<FairshareNode>();

		// Reset all flows in subgraph.
		for (final FairshareFlow flow : this.getAllFlows()) {
			flow.setRate(0);
		}

		while (!this.getAllFlows().isEmpty()) {

			/* Line 4 + 5. */
			long fairshare_rate_upload = Long.MAX_VALUE;
			long fairshare_rate_download = Long.MAX_VALUE;
			for (final FairshareNode node : this.getAllNodes()) {

				/* Calculate UPLOADING c_i / |F_i| */
				final long c_i_up = node.getCurrentBandwidth().getUpBW();
				if (c_i_up > 0) {

					final int outgoing_links = this.getUploadingFlowsFrom(node).size();
					final long current_fairshare_rate = (outgoing_links > 0)
							? c_i_up / outgoing_links : Long.MAX_VALUE;

					/* Add to minimal set. */
					if (current_fairshare_rate <= fairshare_rate_upload) {

						if (current_fairshare_rate < fairshare_rate_upload) {
							satUp.clear();
							fairshare_rate_upload = current_fairshare_rate;
						}

						satUp.add(node);

					}
				}

				/* Calculate DOWNLOADING c_i / |F_i| */
				final long c_i_down = node.getCurrentBandwidth().getDownBW();
				if (c_i_down > 0) {

					final int outgoing_links = this.getDownloadingFlowsTo(node).size();
					final long current_fairshare_rate = (outgoing_links > 0)
							? c_i_down / outgoing_links : Long.MAX_VALUE;

					/* Add to minimal set. */
					if (current_fairshare_rate <= fairshare_rate_download) {

						if (current_fairshare_rate < fairshare_rate_download) {
							satDown.clear();
							fairshare_rate_download = current_fairshare_rate;
						}

						satDown.add(node);

					}
				}

			}

			/* Line 6 - 10. */
			final long fairshare_rate = fairshare_rate_upload > fairshare_rate_download
					? fairshare_rate_download : fairshare_rate_upload;
			if (fairshare_rate_upload > fairshare_rate_download) {
				satUp.clear();
			} else if (fairshare_rate_upload < fairshare_rate_download) {
				satDown.clear();
			}

			//log.debug(".. Calculating Fairshare Rate r*=" + fairshare_rate + "@" + satUp + satDown);

			/*
			 * Part 1 of algorithm done.
			 */

			if (!satUp.isEmpty()) {
				for (final FairshareNode node : satUp) {
					for (final FairshareFlow flow : new LinkedList<FairshareFlow>(this.getUploadingFlowsFrom(node))) {
						flow.setRate(fairshare_rate);
						this.removeFlow(flow);
						//log.debug("\t... Applying (satUp) to " + flow);
					}
				}
			}

			if (!satDown.isEmpty()) {
				for (final FairshareNode node : satDown) {
					for (final FairshareFlow flow : new LinkedList<FairshareFlow>(this.getDownloadingFlowsTo(node))) {
						flow.setRate(fairshare_rate);
						this.removeFlow(flow);
						//log.debug("\t... Applying (satDown) to " + flow);
					}
				}
			}

			/*
			 * Part 2 of algorithm done.
			 */

		}

	}

	/**
	 * Discover affected subgraph according to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param triggeringFlow the triggering flow
	 * @param newFlow flow is new (created) or old (gets deleted)
	 * 
	 * @return the affected directed graph
	 * 
	 * @throws Exception the exception
	 */
	@SuppressWarnings("unchecked")
	public DirectedGraph discoverAffectedSubgraph_Alg02(FairshareFlow triggeringFlow, boolean newFlow) throws Exception {

		int hop = 0;

		final DirectedGraph affectedSubGraph = new DirectedGraph(false);
		final HashSet<FairshareNode>[] affectedNodesByDiscovery = new LinkedHashSet[] { new LinkedHashSet<FairshareNode>(), new LinkedHashSet<FairshareNode>(), new LinkedHashSet<FairshareNode>(), new LinkedHashSet<FairshareNode>() };

		/* New flow? Add. */
		if (newFlow) {
			affectedSubGraph.addFlowWithNodes(triggeringFlow);
		}

		Set<FairshareNode> sourceAffectedNodes_SAU = new LinkedHashSet<FairshareNode>();
		sourceAffectedNodes_SAU.add(triggeringFlow.getSrc());

		Set<FairshareNode> destinationAffectedNodes_DAU = new LinkedHashSet<FairshareNode>();
		destinationAffectedNodes_DAU.add(triggeringFlow.getDst());

		/* Line 6. */
		while ( !(destinationAffectedNodes_DAU.isEmpty() && sourceAffectedNodes_SAU.isEmpty()) && ! sourceAffectedNodes_SAU.equals(destinationAffectedNodes_DAU) ) {

			//log.debug("\n\nCalculating iteration/hop = " + (hop) + "/" + (((hop % 2) == 0) ? "even" : "odd"));

			final Set<FairshareNode> tmp_sourceAffectedNodes_SAU_DASH = new LinkedHashSet<FairshareNode>();
			final Set<FairshareNode> tmp_destinationAffectedNodes_DAU_DASH = new LinkedHashSet<FairshareNode>();

			//log.debug(".. SAU = " + sourceAffectedNodes_SAU);
			//log.debug(".. DAU = " + destinationAffectedNodes_DAU);

			for (final FairshareNode node : sourceAffectedNodes_SAU) {

				if ((((hop % 2) != 0) && newFlow)) { /* ODD, UP, NEW */
					tmp_sourceAffectedNodes_SAU_DASH.addAll(downDecrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) == 0) && newFlow)) { /* EVEN, UP, NEW */
					tmp_sourceAffectedNodes_SAU_DASH.addAll(upIncrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) != 0) && !newFlow)) { /* ODD, UP, FINISH */
					tmp_sourceAffectedNodes_SAU_DASH.addAll(downIncrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) == 0) && !newFlow)) { /* EVEN, UP, FINISH */
					tmp_sourceAffectedNodes_SAU_DASH.addAll(upDecrease(affectedSubGraph, affectedNodesByDiscovery, node));
				}

			}

			for (final FairshareNode node : destinationAffectedNodes_DAU) {

				if ((((hop % 2) != 0) && newFlow)) { /* ODD, DOWN, NEW */
					tmp_destinationAffectedNodes_DAU_DASH.addAll(upDecrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) == 0) && newFlow)) { /* EVEN, DOWN, NEW */
					tmp_destinationAffectedNodes_DAU_DASH.addAll(downIncrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) != 0) && !newFlow)) { 	/* ODD, DOWN, FINISH */
					tmp_destinationAffectedNodes_DAU_DASH.addAll(upIncrease(affectedSubGraph, affectedNodesByDiscovery, node));
				} else if ((((hop % 2) == 0) && !newFlow)) { /* EVEN, DOWN, FINISH */
					tmp_destinationAffectedNodes_DAU_DASH.addAll(downDecrease(affectedSubGraph, affectedNodesByDiscovery, node));
				}

			}

			/* Line 19. */
			sourceAffectedNodes_SAU = tmp_sourceAffectedNodes_SAU_DASH;
			destinationAffectedNodes_DAU = tmp_destinationAffectedNodes_DAU_DASH;

			//log.debug("SAU' = " + tmp_sourceAffectedNodes_SAU_DASH);
			//log.debug("DAU' = " + tmp_destinationAffectedNodes_DAU_DASH);

			/* Line 20. */
			hop++;

		}

		return affectedSubGraph;

	}

	/**
	 * "Upload decrease": all affected upload decrease hosts. According to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param affectedGraph the affected graph
	 * @param affectedNodesByDiscovery the affected nodes by discovery
	 * @param node the node
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<FairshareNode> upDecrease(DirectedGraph affectedGraph, HashSet<FairshareNode>[] affectedNodesByDiscovery, FairshareNode node) throws Exception {

		//log.debug(".. upDecrease on " + node);

		affectedNodesByDiscovery[AFFECTED_BY_UPLOAD_DECREASE].add(node);
		final List<FairshareNode> result = new LinkedList<FairshareNode>();

		/* Line 2. */
		for (final FairshareFlow flow : this.getUploadingFlowsFrom(node)) {
			if (flow.isLocallyBottlenecked(node)) {

				//log.debug(".... adding " + flow + " to AF");

				if (affectedGraph.addFlowWithNodes(flow)) {
					if (!affectedNodesByDiscovery[AFFECTED_BY_DNLOAD_INCREASE].contains(flow.getDst())) {
						result.add(flow.getDst());
					}
				}

			}
		}

		//log.debug(".... result = " + result);
		return result;

	}

	/**
	 * "Upload increase": all affected upload increase hosts. According to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param affectedGraph the affected graph
	 * @param affectedNodesByDiscovery the affected nodes by discovery
	 * @param node the node
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<FairshareNode> upIncrease(DirectedGraph affectedGraph, HashSet<FairshareNode>[] affectedNodesByDiscovery, FairshareNode node) throws Exception {

		//log.debug(".. upIncrease on " + node);

		affectedNodesByDiscovery[AFFECTED_BY_UPLOAD_INCREASE].add(node);
		final List<FairshareNode> result = new LinkedList<FairshareNode>();

		/* Line 2. */
		for (final FairshareFlow flow : this.getUploadingFlowsFrom(node)) {
			if (flow.isLocallyBottlenecked(node)
					|| (flow.isRemotelyBottlenecked(node) && (remotelyBottleneckedTurnIntoLocallyBottlenecked(node, flow, UPLOADING_FLOW) == false))) {

				//log.debug(".... adding " + flow + " to AF");

				if (affectedGraph.addFlowWithNodes(flow)) {
					if (!affectedNodesByDiscovery[AFFECTED_BY_DNLOAD_DECREASE].contains(flow.getDst())) {
						result.add(flow.getDst());
					}
				}

			}
		}

		//log.debug(".... result = " + result);
		return result;

	}

	/**
	 * "Down decrease": all affected download decrease hosts. According to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param affectedGraph the affected graph
	 * @param affectedNodesByDiscovery the affected nodes by discovery
	 * @param node the node
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<FairshareNode> downDecrease(DirectedGraph affectedGraph, HashSet<FairshareNode>[] affectedNodesByDiscovery, FairshareNode node) throws Exception {

		//log.debug(".. downDecrease on " + node);

		affectedNodesByDiscovery[AFFECTED_BY_DNLOAD_DECREASE].add(node);
		final List<FairshareNode> result = new LinkedList<FairshareNode>();

		/* Line 2. */
		for (final FairshareFlow flow : this.getDownloadingFlowsTo(node)) {
			if (flow.isLocallyBottlenecked(node)) {

				//log.debug(".... adding " + flow + " to AF");
				if (affectedGraph.addFlowWithNodes(flow)) {
					if (!affectedNodesByDiscovery[AFFECTED_BY_UPLOAD_INCREASE].contains(flow.getSrc())) {
						result.add(flow.getSrc());
					}
				}

			}
		}

		//log.debug(".... result = " + result);
		return result;

	}

	/**
	 * "Down increase": all affected download increase hosts. According to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param affectedGraph the affected graph
	 * @param affectedNodesByDiscovery the affected nodes by discovery
	 * @param node the node
	 * @return the list
	 * @throws Exception the exception
	 */
	private List<FairshareNode> downIncrease(DirectedGraph affectedGraph, HashSet<FairshareNode>[] affectedNodesByDiscovery, FairshareNode node) throws Exception {

		//log.debug(".. downIncrease on " + node);

		affectedNodesByDiscovery[AFFECTED_BY_DNLOAD_INCREASE].add(node);
		final List<FairshareNode> result = new LinkedList<FairshareNode>();

		/* Line 2. */
		for (final FairshareFlow flow : this.getDownloadingFlowsTo(node)) {
			if (flow.isLocallyBottlenecked(node)
					|| (flow.isRemotelyBottlenecked(node) && (remotelyBottleneckedTurnIntoLocallyBottlenecked(node, flow, DOWNLOADING_FLOW) == false))) {

				//log.debug(".... adding " + flow + " to AF");

				if (affectedGraph.addFlowWithNodes(flow)) {
					if (!affectedNodesByDiscovery[AFFECTED_BY_UPLOAD_DECREASE].contains(flow.getSrc())) {
						result.add(flow.getSrc());
					}
				}

			}
		}

		//log.debug(".... result = " + result);
		return result;

	}

	/**
	 * Return true if given node remotely bottlenecked node turns into locally bottlenecked on BW increase.
	 * 
	 * According to
	 * 	"Accurate and Efficient Simulation of Bandwidth Dynamics for Peer-To-Peer Overlay Networks"
	 * by Alexandros Gkogkas.
	 *
	 * @param node the node
	 * @param flow the flow
	 * @return true, if successful
	 * @throws Exception the exception
	 */
	private boolean remotelyBottleneckedTurnIntoLocallyBottlenecked(FairshareNode node, FairshareFlow flow, boolean uploading) throws Exception {

		final int ARRAY_INDEX_OFFSET = -1;

		//log.debug(".... remotelyBottleneckedTurnIntoLocallyBottlenecked on " + node + "/" + flow);

		/* Get remotely bottlenecked flows in ascending bandwidth order. */
		final LinkedList<FairshareFlow> remotelyBottleNeckedFlows = new LinkedList<FairshareFlow>();

		final List<FairshareFlow> flowList = (uploading) ? this.getUploadingFlowsFromInAscOrder(node) : this.getDownloadingFlowsInAscOrder(node);
		for (final FairshareFlow curFlow : flowList) {
			if (curFlow.isRemotelyBottlenecked(node)) {
				remotelyBottleNeckedFlows.add(curFlow);
			}
		}

		final int flowCount = flowList.size();
		final double capacity_max = (uploading) ? node.getMaxBandwidth().getUpBW() : node.getMaxBandwidth().getDownBW();

		double delta_C= 0;
		for (int i = 1; i <= remotelyBottleNeckedFlows.size(); i++) {

			if( i == 2 ) {
				// Calculate C_1 --> i - 1
				delta_C += ( (capacity_max / (flowCount + 1 )) - remotelyBottleNeckedFlows.get(1 + ARRAY_INDEX_OFFSET).getRate()) / flowCount;
			} else if( i > 2 ) {
				// Calculate C_2+ --> i - 1

				double nominator = (capacity_max / ( flowCount + 1 )) + delta_C;

				for (int j = 0; j < (i - 2); j++) {
					nominator -= remotelyBottleNeckedFlows.get((i - 1) + ARRAY_INDEX_OFFSET).getRate();
				}

				final double denominator = ( (flowCount - i) + 1 );
				delta_C += nominator / denominator;
			}
			final double r_i_calculated = (capacity_max / (flowCount + 1)) + delta_C;

			boolean r_i_affected = false;
			if (!(remotelyBottleNeckedFlows.get(i + ARRAY_INDEX_OFFSET).getRate() < r_i_calculated)) {
				//log.debug("...... found affected flow: " + remotelyBottleNeckedFlows.get(i + ARRAY_INDEX_OFFSET));
				r_i_affected = true;
			}

			//log.debug("........ i=" + i + " r_i=" + remotelyBottleNeckedFlows.get(i + ARRAY_INDEX_OFFSET).getRate() + " < " + r_i_calculated
			//		+ " " + remotelyBottleNeckedFlows.get(i + ARRAY_INDEX_OFFSET));

			if (remotelyBottleNeckedFlows.get(i + ARRAY_INDEX_OFFSET).getRate() >= flow.getRate()) {
				//log.debug("........ AFFECTED=" + r_i_affected);
				return r_i_affected ? false : true;
			}

		}

		/* No flow affected. */
		//log.debug("...... not affected/returning true!");
		return true;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected DirectedGraph clone() throws CloneNotSupportedException {

		final DirectedGraph newGraph = new DirectedGraph(false);

		newGraph.addAllNodes(this.getAllNodes());
		try {
			newGraph.addAllFlows(this.getAllFlows());
		} catch (final Exception e) {
			// No action.
		}

		return newGraph;

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		final StringBuilder string = new StringBuilder();
		for (final FairshareNode node : this.getAllNodes()) {
			string.append(node.toString() + "\n");
			for (final FairshareFlow flow : this.getUploadingFlowsFrom(node)) {
				string.append(".. " + flow.toString() + "\n");
			}
		}
		string.append("\n");

		return string.toString();

	}

	/**
	 * Reset all flows in the graph.
	 */
	public void resetGraph() {

		for (final FairshareFlow flow : this.getAllFlows()) {
			flow.reset();
		}

		for (final FairshareNode node : this.getAllNodes()) {
			node.reset();
		}

	}

	/**
	 * Adds a subgraph to this graph.
	 *
	 * @param affectedGraph the affected graph
	 * @return true, if successful
	 */
	public boolean addGraph(DirectedGraph affectedGraph) {

		this.addAllNodes(affectedGraph.getAllNodes());

		try {
			this.addAllFlows(affectedGraph.getAllFlows());
		} catch (final Exception e) {
			return false;
		}

		return true;

	}

}
