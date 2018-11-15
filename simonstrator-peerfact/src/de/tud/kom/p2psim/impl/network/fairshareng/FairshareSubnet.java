/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * 
 * PeerfactSim.KOM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tud.kom.p2psim.impl.network.fairshareng;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.analyzer.MessageAnalyzer.Reason;
import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.network.NetLayer;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.network.AbstractSubnet;
import de.tud.kom.p2psim.impl.network.IPv4Message;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.fairshareng.livemon.FairshareLiveMonitoring;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PLossStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.latency.GNPLatency;
import de.tud.kom.p2psim.impl.network.modular.st.ploss.NoPacketLoss;
import de.tud.kom.p2psim.impl.transport.AbstractTransMessage;
import de.tud.kom.p2psim.impl.util.BackToXMLWritable;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public class FairshareSubnet extends AbstractSubnet implements EventHandler,
		BackToXMLWritable {

	// Actual graph with Nodes.
	private final DirectedGraph fullGraph;

	// Mapping from NetID to Nodes.
	private final Map<NetID, FairshareNode> nodes;

	// Scheduler List.
	private final SchedulerList schedulerList;

	// Next message arrival.
	private long nextMessageArrival = 0;

	// Enable subgraph discovery.
	private boolean useSubgraphDiscovery = true;

	// Enable subgraph Live monitor
	private boolean useLiveMonitor = false;

	// Net latency model, default GNP. User can overwrite to SimpleLatencyModel
	private LatencyStrategy strategyLatency;
	
	// Packet loss strategy
	private PLossStrategy strategyPLoss;

	private final static int EVENT_RECEIVE = 1;

	private final static int EVENT_STATUS = 2;

	private NetlayerAnalyzer netAnalyzerProxy = null;

	private boolean analyzerInit = false;

	private boolean hasAnalyzer = false;


	/**
	 * Instantiates a new fairshare subnet.
	 */
	public FairshareSubnet() {
		
		this.fullGraph = new DirectedGraph(DirectedGraph.USED_FOR_SCHEDULING);
		this.nodes = new LinkedHashMap<NetID, FairshareNode>();
		this.schedulerList = new SchedulerList(this.fullGraph);

		
		/** DEFAULTS, override in XML: **/
		/* Static latency */
		this.strategyLatency = new GNPLatency();
		/* No packet loss */
		this.strategyPLoss = new NoPacketLoss();
		
	}

	private boolean hasAnalyzer() {
		if (!analyzerInit) {
			try {
				analyzerInit = true;
				netAnalyzerProxy = Monitor.get(NetlayerAnalyzer.class);
				hasAnalyzer = true;
			} catch (AnalyzerNotAvailableException e) {
				//
			}
		}
		return hasAnalyzer;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.AbstractSubnet#registerNetLayer(de.tud.kom.p2psim.api.network.NetLayer)
	 */
	@Override
	public void registerNetLayer(NetLayer net) {

		if( ! (net instanceof FairshareNode) ) {

			/*
			 * Can't throw exception here as registerNetLayer is overwritten.
			 */

			Monitor.log(FairshareSubnet.class, Level.ERROR,
					"Registered wrong netlayer with faireshare subnet.");
			assert (false) : "Registered wrong netlayer with fairshare subnet.";

		}


		//log.debug("Registering new node " + net.getNetID() + " in graph. ");

		net.goOnline();

		this.nodes.put(net.getNetID(), (FairshareNode) net);
		this.fullGraph.addNode((FairshareNode) net);

	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.network.AbstractSubnet#send(de.tud.kom.p2psim.api.network.NetMessage)
	 */
	@Override
	public void send(NetMessage msg) {

		/*
		 * Can't throw exception here as send(NetMessage msg) is overwritten.
		 */

		Monitor.log(FairshareSubnet.class, Level.ERROR,
				"send(NetMessage msg) is not supported. Use sendUDP or sendTCP instead.");
		assert (false) : "send(NetMessage msg) is not supported. Use sendUDP or sendTCP instead.";

	}

	/**
	 * Send a udp message through the subnet
	 *
	 * @param netMsg the net msg
	 */
	public void sendUDP(NetMessage netMsg) {

		
		// Sender of msg
		final FairshareNode sender = getNetLayer(netMsg.getSender());

		// Receiver of msg
		final FairshareNode receiver = getNetLayer(netMsg.getReceiver());
		
		// Get latency
		final long latency = this.strategyLatency.getMessagePropagationDelay(netMsg, sender, receiver, getDB());

		if (netMsg.getSender().equals(netMsg.getReceiver())) {
			Monitor.log(FairshareSubnet.class, Level.ERROR,
					"Sender and receiver are the same ("
					+ netMsg.getSender() + ") for msg "
					+ netMsg.getPayload().getPayload());

		}

		if( strategyPLoss.shallDrop(netMsg, sender, receiver, getDB()) )  {

			// Monitor dropped message. The message loss is assigned to the
			// sender as the receiver does not know, that it would have
			// almost received a message
			final int assignedMsgId = determineTransMsgNumber(netMsg);
			//log.debug("During Drop: Assigning MsgId " + assignedMsgId + " to dropped message");
			((AbstractTransMessage) netMsg.getPayload()).setCommId(assignedMsgId);
			if (hasAnalyzer()) {
				netAnalyzerProxy.netMsgEvent(netMsg, sender.getHost(),
						Reason.DROP);
			}
			//log.debug("Packet loss occured while transfer \"" + netMsg + "\" (packetLossProb: " + packetLossProb + ")");

		} else {

			//log.info("Sending UDP Message to " + receiver + " with latency of " + (latency/Simulator.MILLISECOND_UNIT) + "ms at " + (Simulator.getCurrentTime()/Simulator.MILLISECOND_UNIT) + "ms");
			Event.scheduleWithDelay(latency, this, netMsg, EVENT_RECEIVE);

		}

	}


	/**
	 * Send a tcp message through the subnet
	 *
	 * @param netMsg the net msg
	 * @throws Exception the exception if flow can't be added
	 */
	public void sendTCPMessage(NetMessage netMsg) throws Exception {

		// Sender of msg
		final FairshareNode sender = getNetLayer(netMsg.getSender());

		// Receiver of msg
		final FairshareNode receiver = getNetLayer(netMsg.getReceiver());
		
		// Get latency
		final long latency = this.strategyLatency.getMessagePropagationDelay(netMsg, sender, receiver, getDB());

		//log.info("Sending TCP Message to " + receiver + " with latency of " + (latency/Simulator.MILLISECOND_UNIT) + "ms at " + (Simulator.getCurrentTime()/Simulator.MILLISECOND_UNIT) + "ms, size=" + messageSize);

		// Create new flow. If flow already exists, Node would have taken care of burst.
		final FairshareFlow triggeringFlow = new FairshareFlow(this, sender, receiver, netMsg.getSize(), latency);

		if( this.useSubgraphDiscovery ) {

			// Find affected subgraph
			final DirectedGraph affectedGraph = this.fullGraph.discoverAffectedSubgraph_Alg02(triggeringFlow, DirectedGraph.EVENT_STREAM_NEW);

			// Add flow to full graph
			this.fullGraph.addFlow(triggeringFlow);

			if( this.useLiveMonitor  ) {
				FairshareLiveMonitoring.addNewValue( affectedGraph.getAllFlows().size() / (float) this.fullGraph.getAllFlows().size() );
			}


			// Calculate new rates _only_ on subgraph. Fullgraph will automatically be updated as same objects used.
			affectedGraph.allocateBandwidthOnFullGraph_Alg01();

		} else {

			// Add flow to full graph
			this.fullGraph.addFlow(triggeringFlow);

			// Fake affectedGraph -> everything is affected.
			// 	Need for cloing as flows will get deleted in graph.
			final DirectedGraph affectedGraph = new DirectedGraph(this.fullGraph);

			// Calculate new rates _only_ on subgraph. Fullgraph will automatically be updated as same objects used.
			affectedGraph.allocateBandwidthOnFullGraph_Alg01();

		}

		// Schedule only on affectedFlows.
		scheduleNextMessageArrival();

	}


	/**
	 * Schedule next message arrival.
	 */
	private void scheduleNextMessageArrival() {

		final long newNextMsgArrival = this.schedulerList.getNextArrival();

		assert (newNextMsgArrival >= Time.getCurrentTime()) : "nextMsgArrival is "
		+ newNextMsgArrival
		+ "; current time is "
		+ Time.getCurrentTime();

		//log.debug("Scheduling next Msg arrival for " + newNextMsgArrival + ","	+ ((newNextMsgArrival -Simulator.getCurrentTime()) / Simulator.MILLISECOND_UNIT) + "ms from now.");

		/* Only schedule if different arrival time is expected. */
		if( this.nextMessageArrival != newNextMsgArrival ) {
			Event.scheduleWithDelay(newNextMsgArrival - Time.getCurrentTime(),
					this, null, EVENT_STATUS);
		}

		this.nextMessageArrival = newNextMsgArrival;

	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == EVENT_RECEIVE) {
			final IPv4Message msg = (IPv4Message) content;

			/* Boardcast: Deliver msg to all hosts. */
			if (msg.getReceiver().equals(IPv4NetID.LOCAL_BROADCAST)) {
				for (FairshareNode receiver : nodes.values()) {
					if (!receiver.getNetID().equals(msg.getSender())) {
						receiver.receive(msg);
					}
				}
				/* Else: Unicast. */
			} else {
				final FairshareNode receiver = getNetLayer(msg.getReceiver());
				receiver.receive(msg);
			}
		} else if (type == EVENT_STATUS) {
			if (this.schedulerList.hasCurrentlyArrivingFlow()) {

				final List<FairshareFlow> obsoleteLinks = new LinkedList<FairshareFlow>();
				final List<FairshareFlow> requeueLinks = new LinkedList<FairshareFlow>();

				while (this.schedulerList.hasCurrentlyArrivingFlow()) {

					// log.debug("New Message arrival at -----------------------------------------------------> "
					// + Simulator.getCurrentTime() + "us");

					final FairshareFlow flow = this.schedulerList
							.getAndRemoveCurrentArrival();

					assert (flow.getTransferEndTime() == Time.getCurrentTime()) : "Message arrived too late: "
							+ flow.getTransferEndTime()
							+ "/"
							+ Time.getCurrentTime();

					final FairshareNode sender = flow.getSrc();
					final FairshareNode receiver = flow.getDst();

					assert (sender.isMessageQueueEmpty(receiver) == false) : "Sender queue is empty";

					final NetMessage message = sender
							.removeMessageFromQueue(receiver);
					Event.scheduleWithDelay(flow.getPropagationDelay(), this,
							message, EVENT_RECEIVE);

					assert ((Time.getCurrentTime() - flow.getCreationTime()) > 0);
					// log.info(Simulator.getCurrentTime() +
					// ">TCP Message arrived ("
					// + message.getSender()
					// + " -> "
					// + message.getReceiver()
					// + ") | "
					// + message.getPayload()
					// + ") | "
					// + message.getPayload().getPayload()
					// + " | Transmission time: "
					// + ((Simulator.getCurrentTime() - flow.getCreationTime())
					// / (double) Simulator.MILLISECOND_UNIT) + " ms"
					// + "; Scheduling for " + (Simulator.getCurrentTime() +
					// flow.getPropagationDelay()) + ".");

					if (!sender.isMessageQueueEmpty(receiver)) {
						requeueLinks.add(flow);
					} else {
						obsoleteLinks.add(flow);
					}

				}

				for (FairshareFlow flow : requeueLinks) {
					final FairshareNode sender = flow.getSrc();
					final FairshareNode receiver = flow.getDst();

					flow.addBurstMessage(sender.peekMessageQueue(receiver));
				}

				/* Assign new bandwidth. */
				final DirectedGraph fullyAffectedGraph = new DirectedGraph(
						false);
				for (final FairshareFlow triggeringFlow : obsoleteLinks) {

					// log.debug("Deleting " + triggeringFlow);
					try {

						/* Remove flow from full graph */
						this.fullGraph.removeFlow(triggeringFlow);

						/* Find affected subgraph */
						fullyAffectedGraph.addGraph(this.fullGraph
								.discoverAffectedSubgraph_Alg02(triggeringFlow,
										DirectedGraph.EVENT_STREAM_ENDED));

					} catch (final Exception e) {
						// None.
					}

				}
				/*
				 * Reset all flows. Has to be called *after* all
				 * subgraphDiscovery for all obsolete Flows.
				 */
				for (final FairshareFlow fairshareFlow : obsoleteLinks) {

					fairshareFlow.reset();

					this.schedulerList.removeEvent(fairshareFlow);

					/*
					 * Try to remove flow from affected graph: May be included
					 * if multiple messages arrive at same time.
					 * allocateBandwidth will then only be called once.
					 */
					fullyAffectedGraph.tryRemoveFlow(fairshareFlow);

				}

				try {

					if (this.useSubgraphDiscovery) {

						if (this.useLiveMonitor) {
							float ratio = fullyAffectedGraph.getAllFlows()
									.size()
									/ (float) this.fullGraph.getAllFlows()
											.size();
							if (Float.isNaN(ratio)) { /*
													 * Happens when
													 * fullyAffectedGraph is
													 * empty.
													 */
								ratio = 0f;
							}
							FairshareLiveMonitoring.addNewValue(ratio);
						}

						fullyAffectedGraph.allocateBandwidthOnFullGraph_Alg01();

					} else {

						final DirectedGraph affectedGraph = new DirectedGraph(
								this.fullGraph);

						// Calculate new rates _only_ on subgraph. Fullgraph
						// will automatically be updated as same objects used.
						affectedGraph.allocateBandwidthOnFullGraph_Alg01();

					}

				} catch (final Exception e) {
					// None.
				}

				// Schedule next message.
				scheduleNextMessageArrival();

			}
		}
	}

	/**
	 * Gets the net layer of given NetID
	 *
	 * @param receiverId the receiver id
	 * @return the net layer
	 */
	public FairshareNode getNetLayer(NetID receiverId) {
		return this.nodes.get(receiverId);
	}

	/**
	 * Disconnect host, e.g. JUNIT test goOffline().
	 *
	 * @param fairshareNode the fairshare node to go offline.
	 */
	public void disconnectHost(FairshareNode fairshareNode) {

		/* Get all affected Flows */
		List<FairshareFlow> obsoleteLinks = new LinkedList<FairshareFlow>();
		obsoleteLinks.addAll(this.fullGraph.getUploadingFlowsFrom(fairshareNode));
		obsoleteLinks.addAll(this.fullGraph.getDownloadingFlowsTo(fairshareNode));

		
		/* Assign new bandwidth. */
		final DirectedGraph fullyAffectedGraph = new DirectedGraph(false);
		for (final FairshareFlow triggeringFlow : obsoleteLinks) {

			try {

				/* Remove flow from full graph */
				this.fullGraph.removeFlow(triggeringFlow);

				/* Find affected subgraph */
				fullyAffectedGraph.addGraph(this.fullGraph.discoverAffectedSubgraph_Alg02(triggeringFlow, DirectedGraph.EVENT_STREAM_ENDED));

			}
			catch (final Exception e) {
				// None.
			}

		}
		/* Reset all flows. Has to be called *after* all subgraphDiscovery for all obsolete Flows. */
		for (final FairshareFlow fairshareFlow : obsoleteLinks) {

			fairshareFlow.reset();

			this.schedulerList.removeEvent(fairshareFlow);

			/* Try to remove flow from affected graph: May be included if multiple messages arrive at same time.
			 * allocateBandwidth will then only be called once. */
			fullyAffectedGraph.tryRemoveFlow(fairshareFlow);

		}

		try {

			if( this.useSubgraphDiscovery ) {

				fullyAffectedGraph.allocateBandwidthOnFullGraph_Alg01();

			} else {

				final DirectedGraph affectedGraph = new DirectedGraph(this.fullGraph);

				// Calculate new rates _only_ on subgraph. Fullgraph will automatically be updated as same objects used.
				affectedGraph.allocateBandwidthOnFullGraph_Alg01();

			}

		} catch (final Exception e) {
			// None.
		}

		// Schedule next message.
		scheduleNextMessageArrival();
		
	}

	/**
	 * Adds the event to schedule.
	 *
	 * @param fairshareFlow the fairshare flow
	 */
	public void addEventToSchedule(FairshareFlow fairshareFlow) {
		this.schedulerList.addEvent(fairshareFlow);
	}

	/**
	 * Removes the event from schedule.
	 *
	 * @param fairshareFlow the fairshare flow
	 */
	public void removeEventFromSchedule(FairshareFlow fairshareFlow) {
		this.schedulerList.removeEvent(fairshareFlow);
	}

	/**
	 * Use subgraph discovery.
	 *
	 * @param useSubgraphDiscovery the use subgraph discovery
	 */
	public void useSubgraphDiscovery(boolean useSubgraphDiscovery) {
		this.useSubgraphDiscovery = useSubgraphDiscovery;
	}

	/**
	 * Use live monitor.
	 *
	 * @param useMonitor the use monitor
	 */
	public void useMonitor(boolean useMonitor) {
		this.useLiveMonitor = useMonitor;
	}

	/**
	 * Sets the latency strategy.
	 *
	 * @param latency the new latency strategy
	 */
	public void setLatencyStrategy(LatencyStrategy latency) {
		this.strategyLatency = latency;		
	}

	/**
	 * Sets the p loss strategy.
	 *
	 * @param pLoss the new p loss strategy
	 */
	public void setPLossStrategy(PLossStrategy pLoss) {
		this.strategyPLoss = pLoss;	
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		// no types to write back
	}

}
