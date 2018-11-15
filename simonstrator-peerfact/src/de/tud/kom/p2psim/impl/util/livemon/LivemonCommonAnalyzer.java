/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.util.livemon;

import java.io.Writer;

import de.tud.kom.p2psim.api.analyzer.NetlayerAnalyzer;
import de.tud.kom.p2psim.api.analyzer.OperationAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.NetMessage;
import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;
import de.tud.kom.p2psim.impl.util.toolkits.NumberFormatToolkit;
import de.tud.kom.p2psim.impl.util.toolkits.TimeToolkit;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.operation.Operation;

public class LivemonCommonAnalyzer implements NetlayerAnalyzer,
		OperationAnalyzer {

	int messagesIn = 0;
	int messagesOut = 0;
	int operationsInitiated = 0;
	int operationsFinished = 0;
	int netMsgsDropped = 0;
	int netMsgsReceived = 0;
	int netMsgsSent = 0;
	
	// private HostsOnline hostsOnline;
	
	public LivemonCommonAnalyzer() {
	
		LiveMonitoring.addProgressValue(new Duration());
		LiveMonitoring.addProgressValue(new SimulationTime());
		LiveMonitoring.addProgressValue(new SimulationSpeed());
		//view.addProgressValue(new MessagesIn());
		//view.addProgressValue(new MessagesOut());
		
		// hostsOnline = new HostsOnline();
		// LiveMonitoring.addProgressValue(hostsOnline);
		
		LiveMonitoring.addProgressValue(new OperationsInitiated());
		LiveMonitoring.addProgressValue(new OperationsFinished());
		LiveMonitoring.addProgressValue(new NetMsgsDropped());
		LiveMonitoring.addProgressValue(new NetMsgsReceived());
		LiveMonitoring.addProgressValue(new NetMsgsSent());
		LiveMonitoring.addProgressValue(new MemoryConsumption());
		
	}

	@Override
	public void operationFinished(Operation<?> op) {
		operationsFinished++;
	}

	@Override
	public void operationInitiated(Operation<?> op) {
		operationsInitiated++;
	}
	
	@Override
	public void netMsgEvent(NetMessage msg, SimHost host, Reason reason) {
		switch (reason) {
		case SEND:
			netMsgsSent++;
			break;

		case RECEIVE:
			netMsgsReceived++;
			break;

		case DROP:
			netMsgsDropped++;
			break;

		default:
			break;
		}
	}

	public class SimulationTime implements ProgressValue {

		@Override
		public String getName() {
			return "Simulation time";
		}

		@Override
		public String getValue() {
			long time = Time.getCurrentTime();
			return String.valueOf(Time.getFormattedTime(time));
		}
		
	}
	
	public class Duration implements ProgressValue {

		long time = System.currentTimeMillis();
		TimeToolkit tk = new TimeToolkit(1);
		
		@Override
		public String getName() {
			return "Duration (Real Time)";
		}

		@Override
		public String getValue() {
			return tk.timeStringFromLong(System.currentTimeMillis() - time);
		}
		
	}

	public class SimulationSpeed implements ProgressValue {

		public static final long RECHECK_INTERVAL = 3000; //in REAL milliseconds
		
		long lastRealTimeChecked = System.currentTimeMillis();
		long lastSimTimeWasChecked = 0;
		
		String lastValue = "n.a.";
		
		@Override
		public String getName() {
			return "Simulation speed";
		}

		@Override
		public String getValue() {
			
			long curTime = System.currentTimeMillis();
			long simTime = Time.getCurrentTime() / Time.MILLISECOND;
			
			if (curTime - RECHECK_INTERVAL > lastRealTimeChecked) {
				
				long realTimeProgress = curTime - lastRealTimeChecked;
				
				if (realTimeProgress == 0) return lastValue = "n/a";
				
				long simTimeProgress = simTime - lastSimTimeWasChecked;
				
				double simTimePerRealTime = simTimeProgress/(double)realTimeProgress;
				
				lastValue = NumberFormatToolkit.floorToDecimalsString(simTimePerRealTime, 3) + " sim time / real time";
				
				lastRealTimeChecked = curTime;
				lastSimTimeWasChecked = simTime;
				
			}
			
			return lastValue;
			
		}
		
	}

	public class MessagesIn implements ProgressValue {

		@Override
		public String getName() {
			return "Received messages";
		}

		@Override
		public String getValue() {
			return String.valueOf(messagesIn);
		}
		
	}
	
	public class MessagesOut implements ProgressValue {

		@Override
		public String getName() {
			return "Sent messages";
		}

		@Override
		public String getValue() {
			return String.valueOf(messagesOut);
		}
		
	}
	
	public class OperationsInitiated implements ProgressValue {

		@Override
		public String getName() {
			return "Started operations";
		}

		@Override
		public String getValue() {
			return String.valueOf(operationsInitiated);
		}
		
	}
	
	public class OperationsFinished implements ProgressValue {

		@Override
		public String getName() {
			return "Finished operations";
		}

		@Override
		public String getValue() {
			return String.valueOf(operationsFinished);
		}
		
	}
	
	public class NetMsgsDropped implements ProgressValue {

		@Override
		public String getName() {
			return "Lost packets";
		}

		@Override
		public String getValue() {
			return String.valueOf(netMsgsDropped);
		}
		
	}
	
	public class NetMsgsReceived implements ProgressValue {

		@Override
		public String getName() {
			return "Received packets";
		}

		@Override
		public String getValue() {
			return String.valueOf(netMsgsReceived);
		}
		
	}
	
	public class NetMsgsSent implements ProgressValue {

		@Override
		public String getName() {
			return "Sent packets";
		}

		@Override
		public String getValue() {
			return String.valueOf(netMsgsSent);
		}
		
	}
	
	public class MemoryConsumption implements ProgressValue {

		@Override
		public String getName() {
			return "Memory consumption";
		}

		@Override
		public String getValue() {
			
			long total = Runtime.getRuntime().totalMemory();
			
			return getMemStr(total - Runtime.getRuntime().freeMemory())
			+ " of " + getMemStr(total)
			+ ", max. " + getMemStr(Runtime.getRuntime().maxMemory());
		}
		
		public String getMemStr(long mem) {
			return Math.floor(mem/1048576)  + " MB";
		}
		
	}

	//
	// public class HostsOnline implements ProgressValue, ConnectivityListener {
	//
	// Set<Object> hostsOnline = new HashSet<Object>();
	//
	// Set<Object> hostsOffline = new HashSet<Object>();
	//
	// @Override
	// public String getName() {
	// return "Hosts (NetLayers) online";
	// }
	//
	// @Override
	// public String getValue() {
	// return String.valueOf(hostsOnline.size()) + " / "
	// + String.valueOf(hostsOffline.size());
	// }
	//
	// public void registerHost(SimHost host) {
	//
	// NetLayer nl = host.getNetLayer();
	//
	// if (nl != null) {
	// if (nl.isOnline())
	// hostsOnline.add(nl);
	// else
	// hostsOffline.add(nl);
	// nl.addConnectivityListener(this);
	// } else {
	// throw new AssertionError();
	// }
	// }
	//
	// @Override
	// public void connectivityChanged(ConnectivityEvent ce) {
	// if (ce.isOnline()) {
	// hostsOnline.add(ce.getSource());
	// hostsOffline.remove(ce.getSource());
	// } else {
	// hostsOnline.remove(ce.getSource());
	// hostsOffline.add(ce.getSource());
	// }
	// }
	//
	// }

	@Override
	public void start() {
		// for (SimHost host : GlobalOracle.getHosts()) {
		// hostsOnline.registerHost(host);
		// }
	}

	@Override
	public void stop(Writer output) {
		//Nothing to do
	}
}











