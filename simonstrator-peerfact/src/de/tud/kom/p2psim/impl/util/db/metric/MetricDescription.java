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

import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

/**
 * This class provides a compact object for information on a metric.
 *
 * It stores the name, a comment, and the unit of a metric, and also
 * the name of the responsible analyzer.
 *
 * @author Christoph Muenker
 * @version 1.0, 07/05/2011
 */
public class MetricDescription {

	/**
	 * The name of this metric
	 */
	private String name;

	/**
	 * The comment to this metric, which describes the metric in more words
	 */
	private String comment;

	/**
	 * The analyzer Name
	 */
	private String analyzerName;

	/**
	 * The unit of this metric.
	 */
	private String unit;

	/**
	 * Creates a {@link MetricDescription} with the given parameters.
	 *
	 * @param analyzer
	 *            The class of analyzer
	 * @param name
	 *            The name of this metric
	 * @param comment
	 *            The comment to this metric, which describes the metric in more
	 *            words
	 * @param unit
	 *            The unit of this metric.
	 */
	public MetricDescription(Class<? extends Analyzer> analyzer, String name,
			String comment, String unit) {
		this(analyzer.getName(), name, comment, unit);
	}

	/**
	 * Creates a {@link MetricDescription} with the given parameters.
	 *
	 * @param analyzerName
	 *            The name of the analyzer.
	 * @param name
	 *            The name of this metric
	 * @param comment
	 *            The comment to this metric, which describes the metric in more
	 *            words
	 * @param unit
	 *            The unit of this metric.
	 */
	public MetricDescription(String analyzerName, String name, String comment,
			String unit) {
		this.analyzerName = analyzerName;
		this.name = name;
		this.comment = comment;
		this.unit = unit;
	}

	/**
	 * Gets the name of the metric.
	 *
	 * @return the name of the metric
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the comment to the metric
	 *
	 * @return the comment to the metric
	 */
	public String getComment() {
		return comment;
	}

	/**
	 * Gets the unit to this metric
	 *
	 * @return the unit to this metric
	 */
	public String getUnit() {
		return unit;
	}

	/**
	 * Gets the name of the Analyzer
	 *
	 * @return the name of the analyzer
	 */
	public String getAnalyzerName() {
		return analyzerName;
	}

	// *****************
	// For compare of two objects of this class for equals...
	// only used the name and analyzerName for comparing.
	// *****************

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((analyzerName == null) ? 0 : analyzerName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetricDescription other = (MetricDescription) obj;
		if (analyzerName == null) {
			if (other.analyzerName != null)
				return false;
		} else if (!analyzerName.equals(other.analyzerName))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
