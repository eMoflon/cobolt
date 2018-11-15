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

package de.tud.kom.p2psim.impl.churn;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.churn.ChurnGenerator;
import de.tud.kom.p2psim.api.churn.ChurnModel;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.api.network.SimNetworkComponent;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.impl.util.toolkits.CollectionHelpers;
import de.tud.kom.p2psim.impl.util.toolkits.Predicates;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Provides a random number generator for times. do sth
 * 
 * @author Sebastian Kaune
 */
public class DefaultChurnGenerator implements EventHandler, ChurnGenerator {

	HostBuilder hostBuilder;

	ChurnModel churnModel;

	List<SimHost> hosts = null;

	long endTime = Long.MAX_VALUE;

	boolean testMode = false;

	public final static int CHURN_START = 1;

	public final static int CHURN_STOP = 2;

	public final static int CHURN_EVENT = 3;

	public final static int CHURN_NOCHURN_HOSTS = 4;

	public void setChurnModel(ChurnModel model) {
		this.churnModel = model;
	}

	public void setStart(long time) {
		Event.scheduleWithDelay(time - Time.getCurrentTime(), this, null,
				CHURN_START);
	}

	public void setStop(long time) {
		this.endTime = time;
		Event.scheduleWithDelay(time - Time.getCurrentTime(), this, null,
				CHURN_STOP);
	}

	public void compose(Configurator config) {
		hostBuilder = (HostBuilder) config
				.getConfigurable(Configurator.HOST_BUILDER);
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == CHURN_EVENT) {
			ChurnEvent churnEvent = (ChurnEvent) content;
			SimNetworkComponent net = churnEvent.host.getNetworkComponent();
			if (churnEvent.goOnline) {
				for (SimNetInterface netI : net.getSimNetworkInterfaces()) {
					if (netI.isOffline()) {
						netI.goOnline();
					}
				}
			} else {
				for (SimNetInterface netI : net.getSimNetworkInterfaces()) {
					if (netI.isOnline()) {
						netI.goOffline();
					}
				}
			}
			scheduleChurnEvent(churnEvent.host);
		} else if (type == CHURN_START) {
			this.hosts = new ArrayList<SimHost>(this.filterHosts());
			this.churnModel.prepare(this.hosts);
			this.activate();
		} else if (type == CHURN_STOP) {
			if (hosts == null)
				throw new RuntimeException(
						"Tried to stop churn at "
								+ Time.getCurrentTime()
								+ ", but it never started. Maybe you should correct your churn start and stop.");
			for (SimHost host : this.hosts) {
				for (SimNetInterface netI : host.getNetworkComponent()
						.getSimNetworkInterfaces()) {
					if (netI.isOffline()) {
						netI.goOnline();
					}
				}
			}
		} else if (type == CHURN_NOCHURN_HOSTS) {
			// Send no-churn-hosts online immediately
			List<SimHost> nochurnhosts = (List<SimHost>) content;
			for (SimHost host : nochurnhosts) {
				for (SimNetInterface netI : host.getNetworkComponent()
						.getSimNetworkInterfaces()) {
					if (netI.isOffline()) {
						netI.goOnline();
					}
				}
			}
		}
	}

	void activate() {
		for (SimHost host : hosts) {
			scheduleChurnEvent(host);
		}
		Monitor.log(DefaultChurnGenerator.class, Level.INFO,
				"Scheduled %s churn events", hosts.size());
	}

	private List<SimHost> filterHosts() {
		List<SimHost> tmp = hostBuilder.getAllHosts();
		List<SimHost> filteredHosts = new LinkedList<SimHost>();

		CollectionHelpers.filter(tmp, filteredHosts,
				Predicates.IS_CHURN_AFFECTED);
		List<SimHost> noChurn = new LinkedList<SimHost>();
		noChurn.addAll(tmp);
		noChurn.removeAll(filteredHosts);
		Event.scheduleImmediately(this, noChurn, CHURN_NOCHURN_HOSTS);
		return filteredHosts;
	}

	private void scheduleChurnEvent(SimHost host) {
		SimNetworkComponent net = host.getNetworkComponent();
		long offset;
		boolean goOnline;
		long currentTime = Time.getCurrentTime();

		/*
		 * Sanity check: currently, we assume within this churn generator, that
		 * all NetInterfaces of a host behave identical, i.e., they are all
		 * either online or offline.
		 */
		boolean oneOffline = false;
		boolean oneOnline = false;
		for (SimNetInterface netI : net.getSimNetworkInterfaces()) {
			if (netI.isOffline()) {
				oneOffline = true;
			} else {
				oneOnline = true;
			}
		}
		if (oneOffline && oneOnline) {
			throw new AssertionError(
					"Unexpectedly, one NetInterface of the host is offline, while another one is online...");
		}

		if (oneOnline) {
			offset = this.churnModel.getNextDowntime(host);
			goOnline = false;
			// if (testMode)
			// this.testStub.onlineEvent(host, currentTime);
		} else {
			offset = this.churnModel.getNextUptime(host);
			goOnline = true;
			// if (testMode)
			// this.testStub.offlineEvent(host, currentTime);
		}

		long timepoint = currentTime + offset;

		if (currentTime < endTime && timepoint < endTime) {
			Event.scheduleWithDelay(offset, this,
					new ChurnEvent(host, goOnline), CHURN_EVENT);
		}
	}

	private class ChurnEvent {

		public final SimHost host;

		public final boolean goOnline;

		ChurnEvent(SimHost host, boolean goOnline) {
			this.host = host;
			this.goOnline = goOnline;
		}
	}

	// void setTestStub(ChurnTestStub testStub) {
	// this.testStub = testStub;
	// this.testMode = true;
	// }

}
