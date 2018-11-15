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
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * AVT Churn Model implements an AVT file parser and generates events based on the given file. 
 * @author Alexander Nigl
 * @version 1.0, 08/14/2011
 */
public class AVTChurnModel implements ChurnModel {

	/**
	 * AVTNode calculates the times on which this node has to be online or offline. 
	 * @author Alexander Nigl
	 * @version 1.0, 08/14/2011
	 */
	public class AVTNode {
		/**
		 * id of the node (currently node used)
		 */
		String id;

		/**
		 * Array of up- and downtimes
		 */
		long[] times;

		/**
		 * 
		 * @param config single line of AVT file
		 * @param unit time unit to calculate the simulator times
		 */
		public AVTNode(String config, Long unit) {
			String[] splited = config.split("(\\s)+");
			this.id = splited[0];
			int numberOfValues = Integer.valueOf(splited[1]);
			times = new long[numberOfValues * 2];
			BigDecimal unitCon = new BigDecimal(unit);
			for (int i = 0; i < numberOfValues * 2; i++) {
				times[i] = new BigDecimal(splited[2 + i]).multiply(unitCon)
						.longValue();
			}
		}

		/**
		 * Returns offset until next downtime based on Simulator.getCurrentTime().
		 * @return offset of downtime,
		 * if host should be online it returns 0,
		 * if host will not return it returns the offset to Simulator.getEndTime() +1
		 */
		public long getNextDowntime() {
			long curTime = Time.getCurrentTime();
			if (curTime < times[0]) {
				return times[0] - curTime;
			}
			if (curTime >= times[times.length-1]) {
				return Simulator.getEndTime() - curTime + 1;
			}
			int insertPlace = Arrays.binarySearch(times, curTime);
			if (insertPlace%2 == 1) {
				if (curTime < times[insertPlace]) return 0l;
				if (curTime == times[insertPlace]) return times[insertPlace+1] - curTime;
			}
			if (insertPlace%2 == 0) {
				if (curTime < times[insertPlace]) return times[insertPlace] -curTime;
				if (curTime == times[insertPlace]) return 0l;
			}
			throw new RuntimeException("Should never ever happen");
		}

		/**
		 * Returns offset until next uptime
		 * @return offset of uptime,
		 * if it should be offline it returns 0
		 */
		public long getNextUptime() {
			long curTime = Time.getCurrentTime();
			if (curTime < times[0]) {
				return 0l;
			}
			if (curTime >= times[times.length-1]) {
				return 0l;
			}
			int insertPlace = Arrays.binarySearch(times, curTime);
			if (insertPlace%2 == 1) {
				if (curTime < times[insertPlace]) return times[insertPlace]-curTime;
				if (curTime == times[insertPlace]) return 0l;
			}
			if (insertPlace%2 == 0) {
				if (curTime < times[insertPlace]) return 0l;
				if (curTime == times[insertPlace]) return times[insertPlace+1]-curTime;
			}
			throw new RuntimeException("Should never ever happen");
			
		}

	}

	/**
	 * all churn nodes in the same order as in the AVT file
	 */
	private LinkedBlockingQueue<AVTNode> churnNodes;

	/**
	 * Mapping between churn enabled hosts an the associated AVTNode 
	 */
	private HashMap<SimHost, AVTNode> hosts;
	
	/**
	 * 
	 * @param avtFilePath Path to file
	 * @param unit time unit in which the AVT file was written in (most of the time this will be 1s)
	 * @throws IOException if file is not readable 
	 */
	@XMLConfigurableConstructor({"avtFilePath", "unit"})
	public AVTChurnModel(String avtFilePath, long unit) throws IOException {
		FileInputStream fstream = new FileInputStream(avtFilePath);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;
		churnNodes = new LinkedBlockingQueue<AVTNode>();
		while ((strLine = br.readLine()) != null) {
			int commentIndex = strLine.indexOf("#"); //ignore comments
			if (commentIndex == 0) {
				continue;
			} else if (commentIndex != -1) {
				strLine = strLine.substring(0, commentIndex);
			}
			churnNodes.add(new AVTNode(strLine, unit));
		}
		in.close();
		hosts = new HashMap<SimHost, AVTNode>();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextDowntime(SimHost host) {
		return this.hosts.get(host).getNextDowntime();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getNextUptime(SimHost host) {
		return this.hosts.get(host).getNextUptime();
	}

	/**
	 * {@inheritDoc}
	 * <br>Throws an Exception if number of churnHosts exceeds available nodes from the AVT File.  
	 */
	@Override
	public void prepare(List<SimHost> churnHosts) {
		if (churnHosts.size() > churnNodes.size()) {
			throw new IllegalArgumentException(
					"Number of churn affected hosts is greater than the number of hosts prepared from the AVTModel.");
		} else if (churnHosts.size() < churnNodes.size()) {
			Monitor.log(AVTChurnModel.class, Level.WARN,
					"churnHosts (%s) < Hosts in AVT File(%s)",
					churnHosts.size(), churnNodes.size());
		}
		for (SimHost host : churnHosts) {
			this.hosts.put(host, churnNodes.poll());
		}
		this.churnNodes = null; //Delete unused churnNodes
	}

}
