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


package de.tud.kom.p2psim.impl.network.fairshareng.livemon;

import de.tud.kom.p2psim.impl.util.LiveMonitoring;
import de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue;

/**
 * The Class FairshareLiveMonitoring.
 */
public class FairshareLiveMonitoring {

	/** The current subgraph value. */
	private static SubgraphCurrentValue subgraphCurrentValue = new SubgraphCurrentValue();
	
	/** The avg subgraph value. */
	private static SubgraphAvgValue subgraphAvgValue = new SubgraphAvgValue();
	
	/**
	 * Register a new value.
	 */
	public static void register() {
		LiveMonitoring.addProgressValue(subgraphCurrentValue);
		LiveMonitoring.addProgressValue(subgraphAvgValue);
	}

	/**
	 * Gets the subgraph value.
	 *
	 * @return the subgraph value
	 */
	public static SubgraphCurrentValue getSubgraphValue() {
		return subgraphCurrentValue;
	}

	/**
	 * Adds the new value.
	 *
	 * @param d the d
	 */
	public static void addNewValue(float d) {
		FairshareLiveMonitoring.subgraphCurrentValue.addNewValue(d);
		FairshareLiveMonitoring.subgraphAvgValue.addNewValue(d);
	}
	
}

class SubgraphCurrentValue implements ProgressValue  {

	private float currentValue = 1; 
	
	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue#getName()
	 */
	@Override
	public String getName() {
		return "Size of subgraphs (percent)";
	}

	/**
	 * Adds the new value, apply low pass filter to smooth peaks.
	 *
	 * @param d the d
	 */
	public void addNewValue(float d) {
		final float alpha = 0.9f;
		currentValue = alpha * currentValue + (1 - alpha ) * d;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue#getValue()
	 */
	@Override
	public String getValue() {
		return (currentValue * 100) + "%";
	}
	
}

/**
 * The Class SubgraphAvgValue.
 */
class SubgraphAvgValue implements ProgressValue  {

	private float currentValue = 0;
	private int currentCount = 0;
	
	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue#getName()
	 */
	@Override
	public String getName() {
		return "Avg size of subgraphs (percent)";
	}

	/**
	 * Adds the new value.
	 *
	 * @param d the d
	 */
	public void addNewValue(float d) {
		currentValue += d;
		currentCount++;
	}

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.util.LiveMonitoring.ProgressValue#getValue()
	 */
	@Override
	public String getValue() {
		return (currentValue / currentCount * 100f) + "%";
	}
}