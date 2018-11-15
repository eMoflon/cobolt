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

package de.tud.kom.p2psim.impl.analyzer.metric.filter;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetInterface;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ConnectivityListener;

/**
 * This filter is used to write metric values for one host as soon as that host
 * goes offline - this is achieved by setting isValid to true ONLY for the
 * metric value of this specific host.
 * 
 * This filter works great in conjunction with a database-Output.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.09.2012
 */
public class OnChurnFilter extends AbstractFilter<MetricValue<?>> {

	private final List<OnChurnMV> allMvs = new LinkedList<OnChurnMV>();
	
	protected int rndCounter = 0;

	@Override
	public void onStop() {
		/*
		 * Persist values of hosts that are still online on simulation end
		 */
		for (OnChurnMV mv : allMvs) {
			mv.onStop();
		}
		notifyListenersOfUpdate();
	}
	
	/**
	 * A Metric Value that is only active in one specific Simulation Tick
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 29.09.2012
	 */
	private class OnChurnMV implements MetricValue<Double>,
			ConnectivityListener {

		private long valid = 0;

		private int thisRoundCounter = 0;
		
		private final MetricValue in;

		private final Host host;

		public OnChurnMV(MetricValue in, Host host) {
			this.in = in;
			this.host = host;
		}

		@Override
		public void wentOffline(Host host, NetInterface netInterface) {
			valid = Time.getCurrentTime();
			thisRoundCounter = ++rndCounter;
			notifyListenersOfUpdate();
		}

		@Override
		public void wentOnline(Host host, NetInterface netInterface) {
			// not interested
		}

		@Override
		public Double getValue() {
			Object val = in.getValue();
			if (val instanceof Double) {
				return (Double) val;
			} else if (val instanceof Number) {
				return Double.valueOf(((Number) val).doubleValue());
			}
			return null;
		}

		public void onStop() {
			if (host.getNetworkComponent().getNetworkInterfaces().iterator()
					.next().isUp()) {
				// Host is still online at simulation end, write metric!
				valid = Time.getCurrentTime();
				thisRoundCounter = rndCounter;
			}
		}

		@Override
		public boolean isValid() {
			/*
			 * Incoming metric might not be valid anymore, as the host is now
			 * offline...
			 */
			return valid == Time.getCurrentTime()
					&& thisRoundCounter == rndCounter;
		}

	}

	@Override
	protected void onInitialize(List<Metric<?>> incomingMetrics) {
		for (Metric metric : incomingMetrics) {
			createDerivedMetric(metric, false, metric.getUnit(), "onChurn "
					+ metric.getDescription(), true);
		}
	}

	@Override
	protected MetricValue getDerivedMetricValueFor(Metric<?> derivedMetric,
			List<Metric<?>> inputs, Host host) {
		assert inputs.size() == 1;
		MetricValue mvIn = inputs.get(0).getPerHostMetric(host.getId());
		if (mvIn != null) {
			OnChurnMV mv = new OnChurnMV(mvIn, host);
			host.getNetworkComponent().getNetworkInterfaces().iterator().next()
					.addConnectivityListener(mv);
			// host.getProperties().addConnectivityListener(mv);
			allMvs.add(mv);
			return mv;
		} else {
			return null;
		}
	}

}
