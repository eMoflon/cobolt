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

package de.tudarmstadt.maki.simonstrator.api.common.metric;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.common.metric.Metric.MetricValue;

/**
 * Abstract base class for a {@link Metric}
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.08.2012
 */
public abstract class AbstractMetric<M extends MetricValue<?>> implements
		Metric<M> {

	private final String name;

	private final String description;

	private final MetricUnit unit;

	private Map<INodeID, M> perHost;

	private List<M> allPerHost;

	private M overallMetric;

	/**
	 * 
	 * @param description
	 * @param unit
	 */
	public AbstractMetric(String description, MetricUnit unit) {
		this.name = createName();
		this.description = description;
		this.unit = unit;
	}

	/**
	 * Directly set a name
	 * 
	 * @param description
	 * @param unit
	 */
	public AbstractMetric(String name, String description, MetricUnit unit) {
		this.name = name;
		this.description = description;
		this.unit = unit;
	}

	/**
	 * Allows a metric to overwrite the naming scheme
	 * 
	 * @return
	 */
	protected String createName() {
		return getClass().getSimpleName();
	}

	protected void addHost(Host host, M value) {
		if (perHost == null) {
			perHost = new LinkedHashMap<INodeID, M>();
			allPerHost = new LinkedList<M>();
		}
		if (perHost.containsKey(host.getId())) {
			throw new AssertionError("Each host is only allowed once!");
		}
		allPerHost.add(value);
		perHost.put(host.getId(), value);
	}

	protected void setOverallMetric(M aggregate) {
		if (this.overallMetric != null) {
			throw new AssertionError("Only one overallMetric is allowed.");
		}
		this.overallMetric = aggregate;
	}

	@Override
	public boolean isOverallMetric() {
		assert (perHost == null && overallMetric != null)
				|| (perHost != null && overallMetric == null);
		return perHost == null;
	}

	@Override
	public M getOverallMetric() {
		return overallMetric;
	}

	@Override
	public M getPerHostMetric(INodeID nodeId) {
		return perHost == null ? null : perHost.get(nodeId);
	}

	@Override
	public List<M> getAllPerHostMetrics() {
		return allPerHost;
	}

	@Override
	public final String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public MetricUnit getUnit() {
		return unit;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		AbstractMetric<?> other = (AbstractMetric<?>) obj;
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}


}
