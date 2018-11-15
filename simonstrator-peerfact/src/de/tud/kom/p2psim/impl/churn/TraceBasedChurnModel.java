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
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This churn model uses traces for the arrival rates and session times to
 * generate a scenario. To use this churnModel, each churn-affected host should
 * be offline at the beginning. Each host will only participate in the scenario
 * once.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 16.08.2012
 */
public class TraceBasedChurnModel implements ChurnModel {

	private List<SimHost> churnHosts;

	private Map<SimHost, ChurnInfo> churnInfos = new LinkedHashMap<SimHost, ChurnInfo>();

	private final int PROPERTY_ISONLINE = 0;

	private boolean STAY_ONLINE = false;

	/**
	 * Interval-length of trace in seconds
	 */
	protected int granularity = -1;

	private String filename;

	private int maxEventsInInterval = 1000;

	/**
	 * 
	 * 
	 * @param file
	 * @param scale
	 */
	@XMLConfigurableConstructor({ "file" })
	public TraceBasedChurnModel(String file) {
		this.filename = file;
	}

	@Override
	public long getNextUptime(SimHost host) {
		boolean firstEvent = churnHosts.remove(host);
		ChurnInfo info = churnInfos.get(host);
		if (firstEvent && info != null) {
			long on = info.getJoinOffset();
			// System.out.println("Host " + host.getHostId() + " joins after "
			// + Simulator.getFormattedTime(on)
			// + " and stays online for "
			// + Simulator.getFormattedTime(churnInfos.get(host)
			// .getUptime()));
			return on;
		} else {
			return 1000 * Time.HOUR;
		}
	}

	@Override
	public long getNextDowntime(SimHost host) {
		if (STAY_ONLINE) {
			return 1000 * Time.HOUR;
		}
		return churnInfos.get(host).getUptime();
	}

	@Override
	public void prepare(List<SimHost> churnHosts) {
		/*
		 * Randomize host-list, otherwise hosts belonging to one group will be
		 * joining roughly at the same time.
		 */
		this.churnHosts = new LinkedList<SimHost>(churnHosts);
		for (SimHost host : churnHosts) {
			for (SimNetInterface netI : host.getNetworkComponent()
					.getSimNetworkInterfaces()) {
				if (netI.isOnline()) {
					netI.goOffline();
				}
			}
		}
		parseTrace(filename);
	}

	private void parseTrace(String filename) {
		System.out.println("==============================");
		System.out.println("Reading trace from " + filename);

		/*
		 * IDs to exclude as they are already online and we want to simulate the
		 * startup by adding new hosts over time
		 */
		List<Integer> exclude = new LinkedList<Integer>();
		Map<Integer, ChurnInfo> infos = new LinkedHashMap<Integer, ChurnInfo>();

		int timeOffset = -1;
		int lastTime = 0;
		int eventsInInterval = 0;

		/*
		 * This parser works for traces from the TU Delft archive that only
		 * contain dynamic online properties. The property "isOnline" is defined
		 * by the given integral propery value, in our case "0". The trace
		 * format is defined on
		 * http://p2pta.ewi.tudelft.nl/pmwiki/?n=Main.TraceFormat
		 * 
		 * We only consider timestamp and peer_id where the property_id is equal
		 * to PROPERTY_ISONLINE
		 */

		BufferedReader csv = null;
		boolean entrySuccessfullyRead = false;
		int lines = 0;

		try {
			csv = new BufferedReader(new FileReader(filename));

			while (csv.ready()) {
				String line = csv.readLine();
				lines++;

				if (lines % 10000 == 0) {
					System.out.print(".");
				}

				if (line.indexOf("\t") > -1) {
					String[] parts = line.split("\t");

					if (parts.length == 7) {
						try {
							int time = Integer.parseInt(parts[0]);
							int peerId = Integer.parseInt(parts[2]);
							int propertyId = Integer.parseInt(parts[3]);

							if (propertyId != PROPERTY_ISONLINE) {
								continue;
							}

							if (timeOffset == -1) {
								timeOffset = time;
							}
							if (lastTime < time) {
								lastTime = time;
								eventsInInterval = 0;
							}

							if (time == timeOffset) {
								/*
								 * Hosts included in the first measurement
								 * interval are excluded in the churn model, as
								 * otherwise we would have a mass-join!
								 */
								exclude.add(Integer.valueOf(peerId));
								continue;
							}

							if (granularity == -1 && time > timeOffset) {
								granularity = time - timeOffset;
							}

							if (exclude.contains(Integer.valueOf(peerId))) {
								continue;
							}

							if (eventsInInterval > maxEventsInInterval) {
								exclude.add(Integer.valueOf(peerId));
								continue;
							}

							ChurnInfo info = infos.get(Integer.valueOf(peerId));
							if (info == null) {
								info = new ChurnInfo(time - timeOffset);
								infos.put(Integer.valueOf(peerId), info);
								eventsInInterval++;
							}

							info.setLeaveTime(time - timeOffset);
							entrySuccessfullyRead = true;
						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								throw new AssertionError("Unknown format");
							}
						}
					} else {
						throw new AssertionError(
								"Wrong number of columns in CSV.");
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

		System.out.println("");
		System.out.println("Found " + infos.size() + " distinct hosts.");
		System.out.println("Overall Trace length: " + (lastTime - timeOffset)
				/ 60 + " mins");
		System.out.println("Granularity: " + granularity + " seconds.");
		Date start = new Date((long) timeOffset * 1000);
		Date end = new Date((long) lastTime * 1000);
		System.out.println("Time from " + start.toString() + " to "
				+ end.toString());

		/*
		 * Assign infos to hosts
		 */

		LinkedList<ChurnInfo> validInfos = new LinkedList<ChurnInfo>(
				infos.values());
		// pick hosts randomly
		LinkedList<SimHost> hosts = new LinkedList<SimHost>(churnHosts);
		Collections.shuffle(hosts, new Random(1l));

		while (!hosts.isEmpty()) {
			if (validInfos.isEmpty()) {
				break;
			}
			churnInfos.put(hosts.removeFirst(), validInfos.removeFirst());
		}
	}

	private class ChurnInfo {

		private long joined;

		private long left = -1;

		private boolean finalized = false;

		public ChurnInfo(long joined) {
			this.joined = joined;
		}

		public void setLeaveTime(long leaveTime) {
			if (!finalized) {
				left = leaveTime;
			}
		}

		public void finalizeTimes() {
			if (!finalized) {
				long offset = (long) (Randoms.getRandom(
						TraceBasedChurnModel.class).nextDouble()
						* granularity * Time.SECOND);
				long skew = (long) (Randoms.getRandom(
						TraceBasedChurnModel.class).nextDouble()
						* granularity * Time.SECOND);
				joined = joined * Time.SECOND + offset;
				left = (left * Time.SECOND + offset + skew) - joined;
			}
			finalized = true;
		}

		public long getJoinOffset() {
			finalizeTimes();
			return joined;
		}

		public long getUptime() {
			finalizeTimes();
			return left;
		}

	}

}
