package de.tud.kom.p2psim.impl.topology.monitoring;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.LinkMessageEvent;
import de.tud.kom.p2psim.api.linklayer.LinkMessageListener;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView.LogicalWiFiTopology;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Graphs;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.common.graph.DirectedEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.ElementwiseGraphEqualityChecker;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IElement;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Node;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.AdaptableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.ObservableTopologyProvider;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge;
import de.tudarmstadt.maki.simonstrator.api.component.topology.OperationalEdge.EdgeOperationType;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyObserver;
import de.tudarmstadt.maki.simonstrator.api.component.topology.event.ComplexTopologyChangeEvent;
import de.tudarmstadt.maki.simonstrator.api.component.topology.event.DeltaBasedTopologyChangeObserver;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;

/**
 * This component broadcasts messages in order to determine a k-local view for
 * each node in the network
 *
 * @author Michael Stein
 */
public class DistributedTopologyMonitoringComponent
		implements HostComponent, LinkMessageListener,
		ObservableTopologyProvider, AdaptableTopologyProvider {

	/**
	 * K specifies the size of the nodes' local view. A 2-local view (default)
	 * contains 1-hop neighbors and 2-hop neighbors. It does not contain edges
	 * between strict 2-hop neighbors (a strict 2-hop neighbor is a 2-hop
	 * neighbor that is no 1-hop neighbor)
	 */
	private int K = 2;

	/**
	 * time between sending of beacons is uniformly distributed in
	 * [BEACON_INTERVAL - 2 seconds; BEACON_INTERVAL + 2 seconds]
	 */
	protected long BEACON_INTERVAL = Simulator.SECOND_UNIT * 5;

	protected SimHost host;

	private final LinkedList<TopologyObserver> topologyListeners = new LinkedList<>();
	private final LinkedList<DeltaBasedTopologyChangeObserver> deltaBasedTopologyListeners = new LinkedList<>();

	private final PhyType phyType = PhyType.WIFI;

	/**
	 * This component throws edges/nodes away if their timestamp is older than
	 * this value. The value depends on 'k' because it takes some time to travel
	 * through the topology...
	 */
	private long TOPOLOGY_TIMEOUT;

	/**
	 * Currently known view of the network topology
	 */
	private Graph localView;

	private final Logger log = Logger
			.getLogger(DistributedTopologyMonitoringComponent.class);

	// topology identifiers provided by this component
	//TODO MSt If we plan to continue working with this component, we should make it generic w.r.t supported TopologyID (rkluge)
	public static final TopologyID TOPOLOGY_IDENTIFIER_UDG = LogicalWifiTopologyView
			.getUDGTopologyID();

	public static final TopologyID TOPOLOGY_IDENTIFIER_WIFI = LogicalWifiTopologyView
			.getAdaptableTopologyID();

	// this map keeps track of the time stamps
	private final Map<IElement, Long> udgTimestamps = new LinkedHashMap<>();

	// this collection keeps track of the filtered edges. the WIFI topology is
	// equal to the UDG topology minus the filtered edges
	private final Collection<IEdge> filteredEdges = new LinkedHashSet<>();

	// we have an additional map for the filter timestamps
	private final Map<IElement, Long> filterTimestamps = new LinkedHashMap<>();

	private final Set<INode> removedNeighbors = new HashSet<>();

	/**
	 * Provides evaluation values
	 */
	public static long _operationalMessagesSent;

	public DistributedTopologyMonitoringComponent(final SimHost host) {
		this.host = host;
	}

	protected void broadcastLocalView() {

		// compute (k-1)-local view because this is the view that this node will
		// propagate in the network
		final Graph subgraph = this.localView
				.getLocalView(getLocalNode().getId(), this.K - 1, true);

		final LinkedList<IEdge> localFilteredEdges = getFilteredEdges(subgraph,
				this.K - 1);

		final Set<IElement> elements = getAllElements(subgraph);

		final Map<IElement, Long> udgStamps = getUdgTimeStamps(elements);

		final Map<IEdge, Long> filterStamps = getFilterTimeStamps(
				localFilteredEdges);

		// broadcast this local view
		getHost().getLinkLayer().send(this.phyType, MacAddress.BROADCAST,
				new ViewMessage(subgraph, udgStamps, localFilteredEdges,
						filterStamps));

	}

	private Set<IElement> getAllElements(final Graph graph) {
		// graph elements in subgraph
		final Set<IElement> elements = new HashSet<>();
		elements.addAll(graph.getEdges());
		elements.addAll(graph.getNodes());
		return elements;
	}

	/*
	 * Returns the filtered edges within the k-local view
	 */
	private LinkedList<IEdge> getFilteredEdges(final Graph subgraph,
			final int k) {
		final LinkedList<IEdge> localFilteredEdges = new LinkedList<>();
		final Set<? extends IEdge> localViewAllEdges = subgraph.getEdges();
		for (final IEdge directedEdge : this.filteredEdges) {
			if (localViewAllEdges.contains(directedEdge)) {
				localFilteredEdges.add(directedEdge);
			}
		}
		return localFilteredEdges;
	}

	@Override
	public void initialize() {
		// TODO define useful timeout value. this one here is only a guess what
		// could be good...
		TOPOLOGY_TIMEOUT = (K + 1) * BEACON_INTERVAL;

		// register listener at LinkLayer in order to be informed about incoming
		// packets
		getHost().getLinkLayer().addLinkMessageListener(this);

		// create initial graph that consists only of the local node
		final LinkedHashSet<INode> nodes = new LinkedHashSet<>();
		nodes.add(getLocalNode());
		this.localView = Graphs.createGraph();
		this.localView.addNodes(nodes);

		// start periodic beaconing
		new PeriodicBeaconingOperation().scheduleImmediately();
	}

	@Override
	public void shutdown() {
		//
	}

	@Override
	public SimHost getHost() {
		return this.host;
	}

	public void setK(final int k) {
		if (k < 1) {
			throw new IllegalArgumentException();
		}

		this.K = k;
	}

	public void setBeaconInterval(final long beaconInterval) {
		this.BEACON_INTERVAL = beaconInterval;
	}

	public INode getLocalNode() {
		final MacAddress macAddress = getHost().getLinkLayer()
				.getMac(this.phyType).getMacAddress();
		LogicalWifiTopologyView topologyView = null;
		try {
			topologyView = (LogicalWifiTopologyView) Binder.getComponent(Topology.class).getTopologyView(PhyType.WIFI);
		} catch (ComponentNotAvailableException e) {
			// Should never happen
		}
		return topologyView.getNodeForMacAddress(macAddress);
//		return getNode(macAddress);
	}

	@Override
	public void messageArrived(final LinkMessageEvent linkMsgEvent) {

		final Message message = linkMsgEvent.getPayload();

		if (message instanceof ViewMessage) {

			final Graph neighborGraph = ((ViewMessage) message).getGraph();
			final Map<IElement, Long> neighborUdgStamps = ((ViewMessage) message)
					.getUdgTimestamps();

			final Graph initialGraph = this.localView.clone();
			final Collection<IEdge> initialFiltered = new LinkedHashSet<>(
					this.filteredEdges);

			final Set<IElement> neighborGraphElements = getAllElements(
					neighborGraph);

			LogicalWifiTopologyView topologyView = null;
			try {
				topologyView = (LogicalWifiTopologyView) Binder
						.getComponent(Topology.class).getTopologyView(PhyType.WIFI);
			} catch (ComponentNotAvailableException e) {
				// Will never happen
			}
			// manually add an in-edge that results from the fact that i
			// received a message from my neighbor
			// TODO MSt: Adjusted to the new API.
			final INode start = topologyView.getNodeForMacAddress(linkMsgEvent.getSender());
			// final INode start = getNode(linkMsgEvent.getSender());
			final INode end = getLocalNode();
			final MacAddress startMac = linkMsgEvent.getSender();
			final MacAddress endMac = getHost().getLinkLayer()
					.getMac(this.phyType).getMacAddress();
			final double edgeWeight = getEdgeWeight(startMac, endMac);
			final IEdge inEdge = Graphs.createDirectedWeightedEdge(
					start.getId(), end.getId(), edgeWeight);
			neighborUdgStamps.put(inEdge, Simulator.getCurrentTime());
			neighborGraphElements.add(inEdge);

			for (final IElement neighborElement : neighborGraphElements) {

				if (!this.localView.contains(neighborElement)) {
					if (isElementUpToDate(
							neighborUdgStamps.get(neighborElement))) {
						this.localView.add(neighborElement);
						this.updateUdgTimeStamp(neighborElement,
								neighborUdgStamps.get(neighborElement));
					}

				} else { // graph element is already contained in my local view.
					// update my local view if this element has a newer
					// time stamp

					if (neighborUdgStamps
							.get(neighborElement) > getUdgTimeStamp(
									neighborElement)) {

						// replace the object in my Graph because even if they
						// are equal according to the equals() method, they
						// might still differ with respect to the weight or
						// other attributes that might be added in future
						this.localView.remove(neighborElement);
						this.localView.add(neighborElement);

						updateUdgTimeStamp(neighborElement,
								neighborUdgStamps.get(neighborElement));
					}
				}
			}

			final Map<IEdge, Long> neighborFilterStamps = ((ViewMessage) message)
					.getFilterTimestamps();
			for (final IEdge neighborFilteredEdge : ((ViewMessage) message)
					.getFilteredEdges()) {
				if (!this.filteredEdges.contains(neighborFilteredEdge)) {
					if (isElementUpToDate(
							neighborFilterStamps.get(neighborFilteredEdge))) {
						this.filteredEdges.add(neighborFilteredEdge);
						this.updateFilterTimeStamp(neighborFilteredEdge,
								neighborFilterStamps.get(neighborFilteredEdge));
					}
				} else {
					// filtered edge is already contained in my local view.
					// update my local view if this element has a newer
					// time stamp
					if (neighborFilterStamps
							.get(neighborFilteredEdge) > getFilterTimeStamp(
									neighborFilteredEdge)) {
						// replace the object in my Graph because even if they
						// are equal according to the equals() method, they
						// might still differ with respect to the weight or
						// other attributes that might be added in future
						this.filteredEdges.remove(neighborFilteredEdge);
						this.filteredEdges.add(neighborFilteredEdge);

						updateFilterTimeStamp(neighborFilteredEdge,
								neighborUdgStamps.get(neighborFilteredEdge));

					}
				}
			}

			if (new ElementwiseGraphEqualityChecker().unequal(localView, initialGraph)
					|| !this.filteredEdges.equals(initialFiltered)) {
				notifyListeners();
			} else {
				log.info(
						"Received a topology message but this did not contain any topology changes.");
			}

		}
		// receiving a message that control removing/adding of edges
		else if (message instanceof EdgeOperationMessage) {

			final EdgeOperationMessage eom = ((EdgeOperationMessage) message)
					.copy();
			// System.out.println("Received " + eom);
			eom.decreaseHopsToLive();
			final Collection<OperationalEdge> operationalEdges = ((EdgeOperationMessage) message)
					.getOperationalEdges();
			for (final OperationalEdge operationalEdge : operationalEdges) {
				// System.out.println(getLocalNode() + "got " +
				// operationalEdge);
				this.handleEdgeOperation(operationalEdge);
			}
			if (!eom.isDead()) {
				broadcast(eom);
				// System.out.println("Forwarding " + eom);
			}
			// broadcastLocalView();
		}
		updateNeighborLinks();
	}

	private void updateNeighborLinks() {
		final Set<INodeID> toUpdate = new HashSet<>();
		for (final IEdge e : this.filteredEdges) {
			if (e.fromId().equals(getLocalNode().getId())) {
				toUpdate.add(e.toId());
			} else if (e.toId().equals(getLocalNode().getId())) {
				toUpdate.add(e.fromId());
			}
		}
		for (final INodeID n : toUpdate) {
			this.removeNeighbor(TOPOLOGY_IDENTIFIER_WIFI, new Node(n));
		}
	}

	private double getEdgeWeight(final MacAddress from, final MacAddress to) {
		return this.getHost().getTopologyComponent().getTopology()
				.getTopologyView(this.phyType).getDistance(from, to);
	}

	private long getUdgTimeStamp(final IElement graphElement) {
		if (graphElement.equals(this.getLocalNode())) {
			// we can always be sure that this node is up to date
			return Simulator.getCurrentTime();
		} else {
			return udgTimestamps.get(graphElement);
		}
	}

	private long getFilterTimeStamp(final IEdge filteredEdge) {
		if (filteredEdge.fromId().equals(this.getLocalNode().getId())) {
			// as the incident start node is responsible for deciding if the
			// edge is filtered, this node maintains the timestamp
			return Simulator.getCurrentTime();
		} else {
			return filterTimestamps.get(filteredEdge);
		}
	}

	private void updateUdgTimeStamp(final IElement graphElement,
			final long timeStamp) {
		if (!udgTimestamps.containsKey(graphElement)) {
			udgTimestamps.put(graphElement, timeStamp);
		} else if (udgTimestamps.get(graphElement) < timeStamp) {
			udgTimestamps.put(graphElement, timeStamp);
		}
	}

	private void updateFilterTimeStamp(final IElement graphElement,
			final long timeStamp) {
		if (!filterTimestamps.containsKey(graphElement)) {
			filterTimestamps.put(graphElement, timeStamp);
		} else if (filterTimestamps.get(graphElement) < timeStamp) {
			filterTimestamps.put(graphElement, timeStamp);
		}
	}

	private boolean isUdgElementUpToDate(final IElement graphElement) {
		return isElementUpToDate(udgTimestamps.get(graphElement));
	}

	private boolean isElementUpToDate(final long timeStamp) {
		final long oldness = Simulator.getCurrentTime() - timeStamp;

		assert oldness >= 0;

		return oldness < TOPOLOGY_TIMEOUT;
	}

	private boolean isFilterElementUpToDate(final IElement graphElement) {
		return isElementUpToDate(filterTimestamps.get(graphElement));
	}

	// returns only those timestamps that refer to the elements in the given set
	private Map<IElement, Long> getUdgTimeStamps(
			final Set<IElement> graphElements) {
		final Map<IElement, Long> subStamps = new LinkedHashMap<>();
		for (final IElement graphElement : graphElements) {
			subStamps.put(graphElement, getUdgTimeStamp(graphElement));
		}
		return subStamps;
	}

	// returns only those timestamps that refer to the elements in the given set
	private Map<IEdge, Long> getFilterTimeStamps(
			final Collection<IEdge> graphElements) {
		final Map<IEdge, Long> subStamps = new LinkedHashMap<>();
		for (final IEdge graphElement : graphElements) {
			subStamps.put(graphElement, getFilterTimeStamp(graphElement));
		}
		return subStamps;
	}

	protected void filterOutdatedEntries() {

		final int initialSize = this.localView.getEdges().size()
				+ this.localView.getNodes().size() + this.filteredEdges.size();

		final Set<? extends INode> nodes = this.localView.getNodes();
		for (final INode node : nodes) {
			if (!isUdgElementUpToDate(node)) {
				this.localView.remove(node);
			}
		}

		final Set<? extends IEdge> edges = this.localView.getEdges();
		for (final IEdge edge : edges) {
			if (!isUdgElementUpToDate(edge)) {
				this.localView.remove(edge);
			}
		}

		for (final IEdge filteredEdge : new LinkedList<>(filteredEdges)) {
			if (!isFilterElementUpToDate(filteredEdge)) {
				this.filteredEdges.remove(filteredEdge);
			}
		}

		final int newSize = this.localView.getEdges().size()
				+ this.localView.getNodes().size() + this.filteredEdges.size();
		if (newSize < initialSize) {
			notifyListeners();
		}
	}

	private void notifyListeners() {
		// TODO I always notify about change in WIFI topology, even though also
		// the other topology might have changed
		topologyListeners.forEach(topologyListener ->
			topologyListener.topologyChanged(this, TOPOLOGY_IDENTIFIER_WIFI));

		if (!this.deltaBasedTopologyListeners.isEmpty())
		{
			final ComplexTopologyChangeEvent event = null;
			deltaBasedTopologyListeners.forEach(listener -> listener.handleEvent(event));
		}
	}

	/**
	 * Operation for periodic filtering of outdated entries
	 */
	private class PeriodicTimestampMaintenanceOperation extends
			PeriodicOperation<DistributedTopologyMonitoringComponent, Void> {
		protected PeriodicTimestampMaintenanceOperation() {
			super(DistributedTopologyMonitoringComponent.this, null,
					1 * Simulator.SECOND_UNIT);
		}

		@Override
		protected void executeOnce() {
			filterOutdatedEntries();
		}

		@Override
		public Void getResult() {
			return null;
		}
	}

	/**
	 * Periodic beaconing operation that initiates sending of packets that
	 * contain (k-1)-local view
	 */
	private class PeriodicBeaconingOperation extends
			PeriodicOperation<DistributedTopologyMonitoringComponent, Void> {
		protected PeriodicBeaconingOperation() {
			super(DistributedTopologyMonitoringComponent.this, null,
					new UniformDistributionRandomWrapper(
							(double) BEACON_INTERVAL
									- 2 * Simulator.SECOND_UNIT,
							(double) BEACON_INTERVAL
									+ 2 * Simulator.SECOND_UNIT),
					1L);
		}

		protected PeriodicBeaconingOperation(final long interval) {
			super(DistributedTopologyMonitoringComponent.this, null, interval);
			// super(DistributedTopologyMonitoringComponent.this, null,
			// new UniformDistributionRandomWrapper(
			// (double) BEACON_INTERVAL - 2
			// * Simulator.SECOND_UNIT,
			// (double) BEACON_INTERVAL + 2
			// * Simulator.SECOND_UNIT), 1L);
		}

		@Override
		protected void executeOnce() {
			// System.out.println(getLocalNode() + " broadcastin");
			broadcastLocalView();

			// if(this.getIterationCount() > 10) {
			// this.stop();
			// long interval = (long) (this.getInterval() * 1.3);
			// System.out.println("new interval: " + interval);
			// new
			// PeriodicBeaconingOperation(interval).startWithDelay(interval);
			// }
		}

		@Override
		public Void getResult() {
			return null;
		}

	}

	private static MacAddress getMacAddress(final INodeID nodeID) {
		LogicalWifiTopologyView topologyView = null;
		try {
			topologyView = (LogicalWifiTopologyView) Binder.getComponent(Topology.class).getTopologyView(PhyType.WIFI);
		} catch (ComponentNotAvailableException e) {
			// Should never happen
		}
		return topologyView.getMacAddressForNode(nodeID);
	}

	@Override
	public INode getNode(final TopologyID topologyIdentifier) {
		if (!topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_UDG)
				&& !topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {
			throw new IllegalArgumentException();
		}

		return this.getLocalNode();
	}

	@Override
	public Iterable<TopologyID> getTopologyIdentifiers() {
		return Arrays.asList(new TopologyID[] { this.TOPOLOGY_IDENTIFIER_UDG,
				this.TOPOLOGY_IDENTIFIER_WIFI });
	}

	@Override
	public void addTopologyObserver(final TopologyObserver observer) {
		this.topologyListeners.add(observer);
	}

	@Override
	public Set<IEdge> getNeighbors(final TopologyID topologyIdentifier) {
		if (!topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_UDG)
				&& !topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {
			throw new IllegalArgumentException();
		}

		return this.getLocalView(topologyIdentifier)
				.getOutgoingEdges(getLocalNode());
	}

	public Set<INode> getNeighborNodes(final TopologyID topologyIdentifier) {
		if (!topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_UDG)
				&& !topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {
			throw new IllegalArgumentException();
		}

		final Graph lv = this.getLocalView(topologyIdentifier);
		final Set<INodeID> neighborsIDs = lv.getNeighbors(getLocalNode(), true);

		final HashSet<INode> neighborsNodes = new HashSet<>();
		for (final INodeID id : neighborsIDs) {
			neighborsNodes.add(new Node(id));
		}
		return neighborsNodes;
	}

	@Override
	public Graph getLocalView(final TopologyID topologyIdentifier) {
		final Graph lV = this.localView.getLocalView(getLocalNode().getId(), K,
				true);
		if (topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_UDG)) {
			return lV;
		} else if (topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {

			final Set<IEdge> edges = new LinkedHashSet<>(lV.getEdges());
			for (final IEdge edge : edges) {
				if (filteredEdges.contains(edge)) {
					lV.remove(edge);
				}
			}

			return lV;
		}

		throw new IllegalArgumentException("Unknown topology identifier");
	}

	@SuppressWarnings({ "unchecked" })
	private void handleEdgeOperation(final OperationalEdge operationalEdge) {
		if (operationalEdge.getType() == EdgeOperationType.Remove) {
			if (operationalEdge.fromId().equals(getLocalNode().getId())) {
				// remove link locally
				this.removeNeighbor(TOPOLOGY_IDENTIFIER_WIFI,
						new Node(operationalEdge.toId()));
			} else {
				// update local view
				final IEdge edgeToFilter = Graphs.createDirectedWeightedEdge(
						operationalEdge.fromId(), operationalEdge.toId(),
						this.getEdgeWeight(
								getMacAddress(operationalEdge.fromId()),
								getMacAddress(operationalEdge.toId())));
				filteredEdges.add(edgeToFilter);
				updateFilterTimeStamp(edgeToFilter, Simulator.getCurrentTime());
			}
		} else {
			if (operationalEdge.fromId().equals(getLocalNode().getId())) {
				// add link locally
				this.addNeighbor(TOPOLOGY_IDENTIFIER_WIFI,
						new Node(operationalEdge.toId()));
			} else {
				// update local view
				filteredEdges.remove(

						Graphs.createDirectedWeightedEdge(operationalEdge.fromId(),
								operationalEdge.toId(),
								this.getEdgeWeight(
										getMacAddress(operationalEdge.fromId()),
										getMacAddress(
												operationalEdge.toId()))));
			}
		}
	}

	private void broadcast(final Message msg) {
		if (msg instanceof EdgeOperationMessage) {
			_operationalMessagesSent++;
		}
		getHost().getLinkLayer().send(this.phyType, MacAddress.BROADCAST, msg);
	}

	@Override
	public void performOperation(final TopologyID topologyIdentifier,
			final OperationalEdge edgeOperation) {

		if (!topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {
			throw new IllegalArgumentException();
		}

		// set hops to live = 2
		final EdgeOperationMessage eom = new EdgeOperationMessage(edgeOperation,
				2);

		this.filteredEdges.add(edgeOperation.getEdge());
		this.updateFilterTimeStamp(edgeOperation.getEdge(),
				Simulator.getCurrentTime());

		broadcast(eom);

		// this.broadcastLocalView();

		//

		// System.out.println(getLocalNode() + " sending: " + edgeOperation);
		// this.handleEdgeOperation(edgeOperation);
		/*
		 * // conduct operation locally if edge is incident and this is the
		 * start // node. otherwise, conduct operation remotely if
		 * (edgeOperation.getStartNode().equals(
		 * this.getNode(topologyIdentifier))) {
		 *
		 * if (edgeOperation.getType() == EdgeOperationType.Add) {
		 * this.addNeighbor(topologyIdentifier, edgeOperation.getEndNode()); }
		 * else { this.removeNeighbor(topologyIdentifier,
		 * edgeOperation.getEndNode()); }
		 *
		 * } else {
		 *
		 * // TODO currently we can only handle operation where the remote node
		 * // is a UDG neighbor. otherwise, we would need some routing. think //
		 * about handling that
		 *
		 * // send message to the node to conduct the operation if
		 * (this.getNeighbors(TOPOLOGY_IDENTIFIER_UDG).contains(
		 * edgeOperation.getStartNode())) {
		 *
		 * Node neighborToAdd = null, neighborToRemove = null; if
		 * (edgeOperation.getType() == EdgeOperationType.Add) { neighborToAdd =
		 * edgeOperation.getEndNode(); } else { neighborToRemove =
		 * edgeOperation.getEndNode(); }
		 *
		 * NeighborOperationMessage opMessage = new NeighborOperationMessage(
		 * neighborToAdd, neighborToRemove);
		 *
		 * // broadcast this local view
		 * System.out.println("("+this.getNode(TOPOLOGY_IDENTIFIER_WIFI )+
		 * ") Sending rem msg to " + edgeOperation.getStartNode());
		 * getHost().getLinkLayer().send(this.phyType,MacAddress.BROADCAST,
		 * opMessage);
		 *
		 * } else { log.warn(
		 * "Operation gets lost because the start node of the edge is neither this node nor a neighbor."
		 * ); }
		 *
		 * }
		 */

	}

	@Override
	public void addNeighbor(final TopologyID topologyIdentifier,
			final INode node) {

		if (isFilteredNeighbor(node)) { // only do anything if this node is
			// filtered yet
			try {
				LogicalWiFiTopology topology = getHost()
						.getComponent(LogicalWiFiTopology.class);
				topology.addNeighbor(topologyIdentifier, node);
				this.filteredEdges
						.remove(new DirectedEdge(this.getLocalNode().getId(), node.getId()));

			} catch (final ComponentNotAvailableException e) {
				throw new IllegalStateException();
			}

		}

	}

	/**
	 * Returns true if the given node is a filtered neighbor by the local node
	 */
	private boolean isFilteredNeighbor(final INode node) {

		for (final IEdge directedEdge : filteredEdges) {
			if (directedEdge.fromId().equals(getLocalNode().getId())
					&& directedEdge.toId().equals(node.getId())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public void removeNeighbor(final TopologyID topologyIdentifier,
			final INode node) {

		if (!topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {
			throw new IllegalArgumentException();
		}

		try {
			getHost().getComponent(AdaptableTopologyProvider.class);
			this.removedNeighbors.add(node);

			final double edgeWeight = this.getEdgeWeight(
					getMacAddress(getLocalNode().getId()),
					getMacAddress(node.getId()));
			final IEdge edgeToFilter = Graphs.createDirectedWeightedEdge(
					getLocalNode().getId(), node.getId(), edgeWeight);


			filteredEdges.add(edgeToFilter);
			updateFilterTimeStamp(edgeToFilter, Simulator.getCurrentTime());
		} catch (final ComponentNotAvailableException e) {
			e.printStackTrace();
		}
	}

	@Override
	// this node can remove/add all possible edges within the extended 1-local
	// view of this node
	public Collection<OperationalEdge> getPossibleEdgeOperations(
			final TopologyID topologyIdentifier) {

		if (topologyIdentifier.equals(TOPOLOGY_IDENTIFIER_WIFI)) {

			final LinkedList<OperationalEdge> possibleOperations = new LinkedList<>();

			// compute local view
			final Graph localView = this.getLocalView(topologyIdentifier);

			// all nodes contained in the 1-local view
			final LinkedHashSet<INode> extended1LocalViewNodes = new LinkedHashSet<>();
			extended1LocalViewNodes.add(getLocalNode());

			final Set<INodeID> nodeIDs = localView.getNeighbors(getLocalNode(),
					false);
			final Set<INode> nodes = new HashSet<>();
			for (final INodeID nodeID : nodeIDs) {
				nodes.add(new Node(nodeID));
			}
			extended1LocalViewNodes.addAll(nodes);

			// check for each edge whether it belongs to extended 1-local view
			final Set<? extends IEdge> edges = localView.getEdges();
			for (final IEdge edge : edges) {

				if (extended1LocalViewNodes.contains(new Node(edge.fromId()))
						&& extended1LocalViewNodes
								.contains(new Node(edge.toId()))) {

					// check if add/or remove operation possible (if the edge is
					// filtered -> add, otherwise -> remove)
					final EdgeOperationType opType = filteredEdges
							.contains(edge) ? EdgeOperationType.Add
									: EdgeOperationType.Remove;
					// <<ADDED BY JULIAN
					possibleOperations.add(new OperationalEdge(edge, opType));
					// >>
				}
			}

			return possibleOperations;
		}

		return null;
	}
}