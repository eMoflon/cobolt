/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.fossa;

/**
 * Instances of the MetricHelper can be used to store statistics and information
 * from the Fossa ECA Engine. These values might be used for analysis, e.g. in
 * the Fossa learner.
 * 
 * @author Alexander Froemmgen
 *
 */
public interface MetricSubscriber {

	/**
	 * Had the execution any effect, e.g. did it actually change a parameter or
	 * was it already like this? The value might be uncertain (e.g. due to
	 * missing network connections)
	 */
	public enum RuleEffect {
		none, yes, uncertain
	}

	void informRuleMatch(long hostId, int ruleId, RuleEffect ruleEffect);

	void informExtremValueMetric(long hostId, String metricName, long value);

}
