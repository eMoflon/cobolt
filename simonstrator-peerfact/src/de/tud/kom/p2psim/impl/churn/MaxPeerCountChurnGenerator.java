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

package de.tud.kom.p2psim.impl.churn;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.impl.scenario.DefaultConfigurator;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tud.kom.p2psim.impl.util.toolkits.CollectionHelpers;
import de.tud.kom.p2psim.impl.util.toolkits.Predicates;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.component.LifecycleComponent;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * ChurnModel that follows a defined "trace" in a csv file. The trace must
 * consist of the following parameters per line:
 * 
 * startTime, intervalLength numberOfClients
 * 
 * startTime - the time when the specified number of nodes is to be set.
 * 
 * intervalLength - the time in which the specified number of nodes (startTime)
 * must be achieved by the model. Those two together also form the inter
 * join/leaving rate.
 * 
 * numberOfClients - the number of clients to be achieved.
 * 
 * The model checks for a minimum intervalLength of 1 minute.
 * 
 * 
 * @author Nils Richerzhagen
 * @version 1.0, Nov 24, 2015
 */
public class MaxPeerCountChurnGenerator
		implements EventHandler, GlobalComponent {

	private static final int _PeerCountEvent = 1001;

	private static final int _CHURN_START = 1002;

	private static final int _CHURN_EVENT = 1003;

	private static final int _CHURN_NOCHURN_HOSTS = 1004;

	private final String commentsDelimiter = "#";

	private final String SEP = ",";

	private final long _minBurstLength = 1 * Time.MINUTE;

	private final int maxNumberOfNodes;

	/**
	 * Default behavior: this generator operates on a NetLayer.
	 */
	private Class<? extends LifecycleComponent> targetClass = SimNetworkComponent.class;

	/**
	 * {@link ChurnInfo} from the csv file.
	 */
	private LinkedList<ChurnInfo> churnInfos = new LinkedList<ChurnInfo>();

	private PriorityQueue<HostSessionInfo> onlineHostsSortedByOnlineTime;

	private PriorityQueue<HostSessionInfo> offlineHostsSortedByOfflineTime;
	
	private Random rnd = Randoms.getRandom(new Object());

	/**
	 * Comparator used to sort client infos by offline time
	 */
	private static final Comparator<HostSessionInfo> COMP_TIME = new Comparator<MaxPeerCountChurnGenerator.HostSessionInfo>() {
		@Override
		public int compare(HostSessionInfo o1, HostSessionInfo o2) {
			return ((Long) o1.timestamp).compareTo(o2.timestamp);
		}
	};

	@XMLConfigurableConstructor({ "file", "maxNumberOfNodes" })
	public MaxPeerCountChurnGenerator(String file, int maxNumberOfNodes) {
		this.maxNumberOfNodes = maxNumberOfNodes;
		parseTrace(file);
	}

	/**
	 * A class that implements the {@link LifecycleComponent}-interface and can
	 * then be controlled by this generator.
	 * 
	 * @param targetClass
	 */
	@SuppressWarnings("unchecked")
	public void setTargetClass(Class<?> targetClass) {
		this.targetClass = (Class<? extends LifecycleComponent>) targetClass;
	}

	/**
	 * Called by the configurator.
	 * 
	 * @param churnStart
	 */
	public void setChurnStart(long churnStart) {
		// Event.scheduleWithDelay(churnStart, this, null, _CHURN_START);
		Event.scheduleImmediately(this, null, _CHURN_START);
	}

	public void initialize() {
		for (ChurnInfo churnInfo : churnInfos) {
			Event.scheduleWithDelay(churnInfo.getStartTime(), this, churnInfo,
					_PeerCountEvent);
		}
	}

	/**
	 * Start adapting on new churn rate, when next churnInfo is valid.
	 */
	private void configureMaxPeerCount(ChurnInfo currentChurnInfo) {
		long currentTime = Simulator.getCurrentTime();

		assert currentChurnInfo
				.getStartTime() == currentTime : "The ChurnInfo to use is scheduled for the exact time, thus it should be the same.";

		/*
		 * Wanted number > current number. --> need nodes = go online
		 */
		if (currentChurnInfo
				.getNumberOfClients() >= onlineHostsSortedByOnlineTime.size()) {
			int count = currentChurnInfo.getNumberOfClients()
					- onlineHostsSortedByOnlineTime.size();

			for (int i = 0; i < count; i++) {
				/*
				 * Schedule the required number of hosts for going online
				 * churnEvent. Get oldest entry in sortedOffline list and put
				 * online.
				 */
				HostSessionInfo hostSessionInfo = offlineHostsSortedByOfflineTime
						.poll();
				assert hostSessionInfo != null : "HostSessionInfo shouldn't be null - means to few hosts were configured.";

				ChurnEvent churnEvent = new ChurnEvent(hostSessionInfo.component, true);
				long currentJoin = i
						* (currentChurnInfo.getBurstLength() / count);
				// Add rnd-offset
				currentJoin += (long) (rnd.nextDouble() * (currentChurnInfo.getBurstLength() / count));
				Event.scheduleWithDelay(currentJoin, this, churnEvent,
						_CHURN_EVENT);
			}
		}
		/*
		 * Wanted number < currentNumber --> remove nodes = goOffline
		 */
		else if (currentChurnInfo
				.getNumberOfClients() < onlineHostsSortedByOnlineTime.size()) {
			int count = onlineHostsSortedByOnlineTime.size()
					- currentChurnInfo.getNumberOfClients();

			for (int i = 0; i < count; i++) {
				/*
				 * Schedule the required number of hosts for going offline
				 * churnEvent. Get oldest entry in sortedOnline list and put
				 * offline.
				 */
				HostSessionInfo hostSessionInfo = onlineHostsSortedByOnlineTime
						.poll();
				assert hostSessionInfo != null : "HostSessionInfo shouldn't be null - means no hosts were online.";

				ChurnEvent churnEvent = new ChurnEvent(hostSessionInfo.component,
						false);
				long currentLeave = i
						* (currentChurnInfo.getBurstLength() / count);
				// Add rnd-offset
				currentLeave += (long) (rnd.nextDouble() * (currentChurnInfo.getBurstLength() / count));
				Event.scheduleWithDelay(currentLeave, this, churnEvent,
						_CHURN_EVENT);
			}
		} else {
			throw new AssertionError();
		}
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == _PeerCountEvent) {
			ChurnInfo churnInfo = (ChurnInfo) content;
			configureMaxPeerCount(churnInfo);
		} else if (type == _CHURN_EVENT) {
			long currentTime = Simulator.getCurrentTime();
			ChurnEvent churnEvent = (ChurnEvent) content;
			if (churnEvent.start) {
				churnEvent.component.startComponent();
				onlineHostsSortedByOnlineTime
						.add(new HostSessionInfo(churnEvent.component, currentTime));
			} else {
				churnEvent.component.stopComponent();
				offlineHostsSortedByOfflineTime
						.add(new HostSessionInfo(churnEvent.component, currentTime));
			}
		} else if (type == _CHURN_START) {
			initialize();

			/*
			 * FIXME we might want to add means to filter not only based on the host property?
			 */
			List<SimHost> hosts = new ArrayList<SimHost>(this.filterHosts());
			this.prepare(hosts);

			offlineHostsSortedByOfflineTime = new PriorityQueue<HostSessionInfo>(
					(int) Math.ceil(hosts.size() / 10.0), COMP_TIME);
			onlineHostsSortedByOnlineTime = new PriorityQueue<HostSessionInfo>(
					(int) Math.ceil(hosts.size() / 10.0), COMP_TIME);

			long currentTime = Simulator.getCurrentTime();
			/*
			 * Find class (LifecycleComponent) implementation per host
			 */
			for (SimHost simHost : hosts) {
				try {
					LifecycleComponent comp = simHost.getComponent(targetClass);
					offlineHostsSortedByOfflineTime.add(new HostSessionInfo(comp, currentTime));
				} catch (ComponentNotAvailableException e) {
					throw new AssertionError("No implementation of "+targetClass+" found on host "+simHost);
				}
			}
		} else if (type == _CHURN_NOCHURN_HOSTS) {
			// Start on all no-churn hosts (e.g., they will be active right from the beginning)
			List<SimHost> nochurnhosts = (List<SimHost>) content;
			
			for (SimHost host : nochurnhosts) {
				try {
					LifecycleComponent comp = host.getComponent(targetClass);
					if (!comp.isActive()) {
						comp.startComponent();
					}
				} catch (ComponentNotAvailableException e) {
					// Filtered hosts might not even have the component
				}
			}
		}
	}

	/**
	 * Send hosts offline! Should be used to start with churnHosts in offline
	 * state.
	 * 
	 * @param hosts
	 * @deprecated use startOffline-flag in the netlayer-config instead!
	 */
	@Deprecated
	private void prepare(List<SimHost> hosts) {
		for (SimHost host : hosts) {
			try {
				LifecycleComponent comp = host.getComponent(targetClass);
				if (comp.isActive()) {
					comp.stopComponent();
				}
			} catch (ComponentNotAvailableException e) {
				// Filtered hosts might not even have the component
			}
		}
	}

	/**
	 * Gets all hosts and takes all churn affected hosts. Schedules the non
	 * affected host to go online immediately.
	 * 
	 * @return
	 */
	private List<SimHost> filterHosts() {
		List<SimHost> tmp = GlobalOracle.getHosts();
		List<SimHost> filteredHosts = new LinkedList<SimHost>();

		CollectionHelpers.filter(tmp, filteredHosts,
				Predicates.IS_CHURN_AFFECTED);
		List<SimHost> noChurn = new LinkedList<SimHost>();
		noChurn.addAll(tmp);
		noChurn.removeAll(filteredHosts);
		Event.scheduleImmediately(this, noChurn, _CHURN_NOCHURN_HOSTS);
		return filteredHosts;
	}

	/**
	 * Reads the file given by the configuration and parses the churn events.
	 * 
	 * @param filename
	 */
	private void parseTrace(String filename) {
		System.out.println("==============================");
		System.out.println("Reading trace from " + filename);

		/*
		 * This parser works for the following csv file structure.
		 * 
		 * startTime, intervalLength numberOfClients
		 * 
		 */
		BufferedReader csv = null;
		boolean entrySuccessfullyRead = false;

		try {
			csv = new BufferedReader(new FileReader(filename));

			long previousEndTime = 0;

			while (csv.ready()) {
				String line = csv.readLine();
				if (line.length() == 0 || line.startsWith(commentsDelimiter))
					continue;

				if (line.indexOf(SEP) > -1) {
					String[] parts = line.split(SEP);

					if (parts.length == 3) {
						try {

							long startTime = DefaultConfigurator.parseNumber(
									parts[0].replaceAll("\\s+", ""),
									Long.class);
							long burstLength = DefaultConfigurator.parseNumber(
									parts[1].replaceAll("\\s+", ""),
									Long.class);
							int numberOfClients = DefaultConfigurator
									.parseNumber(
											parts[2].replaceAll("\\s+", ""),
											Integer.class);

							// Insanity Checks
							assert startTime >= previousEndTime : "Start time for next fluctuation must be greater than previous end time.";

							assert burstLength >= _minBurstLength : "The minimal length of the burst must be at least 1m.";

							assert numberOfClients > 0 : "Number of nodes must be positive.";

							assert numberOfClients <= maxNumberOfNodes : "Cannot configure more nodes than configured in configuration.";

							previousEndTime = startTime + burstLength;

							churnInfos.add(new ChurnInfo(startTime, burstLength,
									numberOfClients));

							entrySuccessfullyRead = true;

						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								// System.err.println("CSV ParseError " +
								// line);
							}
						}
					} else {
						throw new AssertionError("To many/few columns in CSV.");
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Could not open " + filename);
			throw new RuntimeException("Could not open " + filename);
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

	/**
	 * 
	 * @author Nils Richerzhagen
	 * @version 1.0, Nov 25, 2015
	 */
	private class HostSessionInfo {

		public final LifecycleComponent component;

		public final long timestamp;

		public HostSessionInfo(LifecycleComponent component, long timestamp) {
			this.component = component;
			this.timestamp = timestamp;
		}
	}

	private class ChurnEvent {

		public final LifecycleComponent component;

		public final boolean start;

		ChurnEvent(LifecycleComponent component, boolean start) {
			this.component = component;
			this.start = start;
		}
	}

	/**
	 * Churn Info for the fluctuation intervals.
	 * 
	 * @author Nils Richerzhagen
	 */
	private class ChurnInfo {

		/**
		 * The time the burst starts.
		 */
		private long startTime;

		/**
		 * The time the burst takes.
		 */
		private long burstLength;

		/**
		 * The max number of nodes that join during that burst.
		 */
		private int numberOfClients;

		public ChurnInfo(long startTime, long burstLength,
				int numberOfClients) {
			this.startTime = startTime;
			this.burstLength = burstLength;
			this.numberOfClients = numberOfClients;
		}

		public long getStartTime() {
			return startTime;
		}

		public int getNumberOfClients() {
			return numberOfClients;
		}

		public long getBurstLength() {
			return burstLength;
		}
	}
}
