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

package de.tud.kom.p2psim.impl.analyzer.metric.output;

import java.util.List;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.dao.metric.MeasurementDAO;
import de.tud.kom.p2psim.impl.util.db.metric.MetricDescription;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.ActiveMetric.ActiveMetricListener;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This class maps {@link Metric}s to calls to the DAO on regular intervals or
 * special actions.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 13.08.2012
 */
public class MetricOutputDAO extends AbstractOutput {

	protected long timeEnableDao = 0;

	protected long timeStopDao = Long.MAX_VALUE;

	/**
	 * 
	 * @param table
	 */
	@XMLConfigurableConstructor({ "table" })
	public MetricOutputDAO(String table) {
		DAO.database = table;
	}

	public void setUser(String user) {
		DAO.username = user;
	}

	public void setPassword(String password) {
		DAO.password = password;
	}

	public void setTimeEnableDao(long timeEnableDao) {
		this.timeEnableDao = timeEnableDao;
	}

	public void setTimeStopDao(long timeStopDao) {
		this.timeStopDao = timeStopDao;
	}


	@Override
	public void onInitialize(List<Metric> metrics) {
		for (Metric metric : metrics) {
			/*
			 * Only active metrics are allowed. We register as a listener and
			 * wait for our call.
			 */
			if (metric instanceof ActiveMetric) {
				ActiveMetric am = (ActiveMetric) metric;
				am.addActiveMetricListener(new MetricDaoAdapter(am));
			}
		}
	}

	@Override
	public void onStop() {
		/*
		 * Commit missing values
		 */
		DAO.commitQueue();
	}

	/**
	 * This class helps in persisting a metric using the {@link MeasurementDAO}
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 13.08.2012
	 */
	private class MetricDaoAdapter implements ActiveMetricListener {

		private final ActiveMetric metric;

		private final MetricDescription md;

		private final MeasurementDAO dao = new MeasurementDAO();

		private final List<SimHost> hosts;

		public MetricDaoAdapter(ActiveMetric metric) {
			this.metric = metric;
			this.md = new MetricDescription(MetricOutputDAO.class.getName(),
					metric.getName(), metric.getDescription(), metric.getUnit()
							.toString());
			this.hosts = GlobalOracle.getHosts();
		}

		@Override
		public void onMetricUpdate(ActiveMetric metric) {
			long time = Time.getCurrentTime();

			if (time < timeEnableDao || time > timeStopDao) {
				return;
			}

			if (metric.isOverallMetric()) {
				// global
				MetricValue mv = metric.getOverallMetric();
				Object val = mv.getValue();
				if (mv.isValid()) {
					if (val instanceof Number) {
						double vd = ((Number) val).doubleValue();
						dao.storeGlobalSingleMeasurement(md, time, vd);
					}
				}
			} else {
				// per-host metric
				for (SimHost host : hosts) {
					MetricValue mv = metric.getPerHostMetric(host.getId());
					if (mv != null) {
						Object val = mv.getValue();
						if (mv.isValid()) {
							if (val instanceof Number) {
								double vd = ((Number) val).doubleValue();
								if (Double.isNaN(vd)) {
									continue;
								}
								dao.storeSingleMeasurement(md,
										host.getHostId(), time, vd);
							}
						}
					}
				}
			}
		}

	}

}
