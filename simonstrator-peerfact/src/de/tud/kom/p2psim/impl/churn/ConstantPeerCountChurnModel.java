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

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Random;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Another simple churn model that maintains a constant client count (i.e. if a
 * client goes offline, it is replaced with a new one). The arrival rate is
 * constant until the threshold is reached. Session times are configurable as
 * either static values or based on a distribution.
 * 
 * In addition, a flash crowd phase can be configured for a sudden burst of
 * connected clients.
 * 
 * @author Bjoern Richerzhagen, Refactored by Julius Rueckert
 * @version 1.0, 01.10.2012
 */
public class ConstantPeerCountChurnModel implements ChurnModel {

	/**
	 * Maximum number of online clients in non-flash crowd phase
	 */
	private final int peersThreshold;

	/**
	 * Inter-arrival time as specified during configuration
	 */
	private final long initialInterArrivalTime;

	/**
	 * Minimum session length as specified during configuration
	 */
	private long minSessionLength = 10 * Time.SECOND;

	/*
	 * Parameters a and b of exponential distribution defining session lengths
	 */
	private double a = 0.6378;
	private double b = -0.05944;

	/**
	 * Tells if the model includes a flash crowd or not
	 */
	private boolean hasFlashcrowd = false;

	/*
	 * Flash crowd-related information
	 */
	private long flashcrowdStart = 4 * Time.HOUR;
	private double flashcrowdMultiplicator = 2;
	private long flashcrowdInterval = 10 * Time.MINUTE;

	/*
	 * Local state variables
	 */
	private boolean flashCrowdFinished = false;
	private boolean inFlashcrowd = false;

	private long lastJoin = -1;

	private List<SimHost> hosts;

	private Map<Long, ClientSessionInfo> clientSessionInfos = new LinkedHashMap<Long, ClientSessionInfo>();

	private PriorityQueue<ClientSessionInfo> clientsSortedByOfflineTime;

	/**
	 * Comparator used to sort client infos by offline time
	 */
	private static final Comparator<ClientSessionInfo> COMP_OFFLINE_TIME = new Comparator<ConstantPeerCountChurnModel.ClientSessionInfo>() {
		@Override
		public int compare(ClientSessionInfo o1, ClientSessionInfo o2) {
			return ((Long) o1.leavingAt).compareTo(o2.leavingAt);
		}
	};

	private final Random rnd = Randoms
			.getRandom(ConstantPeerCountChurnModel.class);

	/**
	 * 
	 * @param maxPeersOnline
	 * @param interArrivalTime
	 */
	@XMLConfigurableConstructor({ "maxPeersOnline", "interArrivalTime" })
	public ConstantPeerCountChurnModel(int maxPeersOnline, long interArrivalTime) {
		this.peersThreshold = maxPeersOnline;
		this.initialInterArrivalTime = interArrivalTime;
	}

	@Override
	public long getNextUptime(SimHost host) {
		long currentTime = Time.getCurrentTime();

		if (!hosts.remove(host)) {
			/*
			 * FIXME: This is to avoid reusing peer instances. Needs to be fixed
			 * by properly resetting peers!
			 */
			return 1000 * Time.HOUR;
		}

		if (hasFlashcrowd && inFlashcrowd && !flashCrowdFinished) {
			/*
			 * Join peers in a burst according to flash crowd configuration.
			 * 
			 * Info: The flash crowd peak is reached with (flash crowd
			 * multiplier * peer threshold) present peers. The length of the
			 * period is defined by the flashcrowdInterval.
			 */
			long flashCrowdInterArrivalTime = (long) (flashcrowdInterval / ((flashcrowdMultiplicator - 1) * peersThreshold));
			long currentJoin = lastJoin + flashCrowdInterArrivalTime;
			ClientSessionInfo info = new ClientSessionInfo(currentJoin);
			clientSessionInfos.put(host.getHostId(), info);
			if (currentJoin > flashcrowdStart + flashcrowdInterval) {
				inFlashcrowd = false;
				flashCrowdFinished = true;
			}
			lastJoin = currentJoin;
			return info.joiningAt - currentTime;

		} else if (clientsSortedByOfflineTime.size() < peersThreshold) {
			/*
			 * Initially, join peers until the peer threshold is reached.
			 */
			if (lastJoin < 0) {
				lastJoin = currentTime;
			}
			long currentJoin = lastJoin + initialInterArrivalTime;
			ClientSessionInfo info = new ClientSessionInfo(currentJoin);
			clientsSortedByOfflineTime.add(info);
			clientSessionInfos.put(host.getHostId(), info);
			if (hasFlashcrowd && !flashCrowdFinished
					&& currentJoin > flashcrowdStart) {
					currentJoin = flashcrowdStart;
				inFlashcrowd = true;
			}
			lastJoin = currentJoin;
			return info.joiningAt - currentTime;

		} else {
			/*
			 * After reaching the peer threshold, only peers that went offline
			 * are replaced to keep the threshold.
			 */
			ClientSessionInfo nextToGoOffline = clientsSortedByOfflineTime
					.poll();

			if (nextToGoOffline == null) {
				throw new AssertionError();
			} else {
				// Use the next time a peer goes offline as joining time.
				long currentJoin = nextToGoOffline.leavingAt;
				ClientSessionInfo info = new ClientSessionInfo(currentJoin);
				clientsSortedByOfflineTime.add(info);
				clientSessionInfos.put(host.getHostId(), info);
				if (hasFlashcrowd && !flashCrowdFinished
						&& currentJoin > flashcrowdStart) {
						currentJoin = flashcrowdStart;
					inFlashcrowd = true;
				}
				lastJoin = currentJoin;
				return info.joiningAt - currentTime;
			}
		}
	}

	@Override
	public long getNextDowntime(SimHost host) {
		ClientSessionInfo info = clientSessionInfos.get(host.getHostId());
		return info.sessionLength;
	}

	@Override
	public void prepare(List<SimHost> churnHosts) {
		hosts = new LinkedList<SimHost>(churnHosts);
		for (SimHost host : churnHosts) {
			for (SimNetInterface netI : host.getNetworkComponent()
					.getSimNetworkInterfaces()) {
				if (netI.isOnline()) {
					netI.goOffline();
				}
			}
		}
		clientsSortedByOfflineTime = new PriorityQueue<ConstantPeerCountChurnModel.ClientSessionInfo>(
				(int) Math.ceil(hosts.size() / 10.0), COMP_OFFLINE_TIME);
	}

	/**
	 * Returns the next session length following the exponential distribution
	 * (using parameter a and b)
	 * 
	 * @return the session length
	 */
	protected long getSessionLength() {
		double random = rnd.nextDouble();

		/*
		 * The session length is calculated according an exponential
		 * distribution as defined by Vu et al. (2010) to model PPLive traces.
		 * 
		 * p = a * e^{b*x}
		 * 
		 * Resulting in the following formula:
		 * 
		 * x = log(p/a)/b
		 * 
		 * FIXME: Why do we add another random number of minutes here??
		 */
		long sessionLength = (long) (Math.log(random / a) / b) * Time.MINUTE
				+ (long) (random * Time.MINUTE);

		/*
		 * The minimum session length is limited to avoid too short sessions
		 */
		return Math.max(minSessionLength + (long) (random * Time.MINUTE),
				sessionLength);
	}

	public void setMinSessionLength(long minSessionLength) {
		this.minSessionLength = minSessionLength;
	}

	public void setFlashcrowdStart(long flashcrowdStart) {
		this.flashcrowdStart = flashcrowdStart;
		this.hasFlashcrowd = true;
	}

	public void setFlashcrowdInterval(long flashcrowdInterval) {
		this.flashcrowdInterval = flashcrowdInterval;
	}

	public void setFlashcrowdMultiplicator(double flashcrowdMultiplicator) {
		this.flashcrowdMultiplicator = flashcrowdMultiplicator;
	}

	public void setA(double a) {
		this.a = a;
	}

	public void setB(double b) {
		this.b = b;
	}

	/**
	 * Client session information
	 */
	private class ClientSessionInfo {

		public final long joiningAt;

		public final long leavingAt;

		public final long sessionLength;

		public ClientSessionInfo(long joiningAt) {
			this.sessionLength = getSessionLength();
			this.joiningAt = joiningAt;
			this.leavingAt = joiningAt + sessionLength;
		}

	}

}