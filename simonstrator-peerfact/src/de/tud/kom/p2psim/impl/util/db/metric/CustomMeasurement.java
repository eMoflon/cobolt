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

package de.tud.kom.p2psim.impl.util.db.metric;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "custom_measurements", indexes = { @Index(columnList = "hostMetricId", name = "hostMetricId") })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class CustomMeasurement implements HostMetricBound {
	/**
	 * The id of this table
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	/**
	 * The simulation time for to this measurement in simulator time, that is, microseconds.
	 */
	@SuppressWarnings("unused")
	protected long time;

	/**
	 * The mapping Object of this measurement to the {@link Metric}-Object,
	 * which describes this metric.
	 */
    @ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "hostMetricId")
	protected HostMetric hostMetric;
	
	protected CustomMeasurement() {
		
	}

	public void setTime(long time) {
		this.time = time;
	}

	public void setHostMetric(HostMetric hostMetric) {
		this.hostMetric = hostMetric;
	}
	
	public void afterPersist() {
		
	}

    public HostMetric getHostMetric() {
        return hostMetric;
    }
}
