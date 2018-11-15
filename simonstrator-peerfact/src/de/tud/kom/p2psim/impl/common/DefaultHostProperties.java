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

package de.tud.kom.p2psim.impl.common;

import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.api.common.HostProperties;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;

/**
 * Default implementation of host properties.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 10.12.2007
 * 
 */
public class DefaultHostProperties implements HostProperties {

	private boolean churnAffected = true;

	private String groupID;

	private boolean minMovementSpeedConfigured = false;

	private boolean maxMovementSpeedConfigured = false;

	private double minMovementSpeed;

	private double maxMovementSpeed;

	private Map<String, String> customProperties = new HashMap<String, String>();

	/**
	 * Create new and empty default host properties.
	 */
	public DefaultHostProperties() {
		//
	}

	public void setEnableChurn(boolean churn) {
		this.churnAffected = churn;
	}

	public boolean isChurnAffected() {
		return this.churnAffected;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	@Override
	public void setProperty(String key, String value) {
		customProperties.put(key, value);
	}

	@Override
	public String getProperty(String key) {
		return customProperties.get(key);
	}

	@Override
	public void setMinMovementSpeed(double minSpeed) {
		minMovementSpeedConfigured = true;
		if (minSpeed < 0) {
			throw new ConfigurationException(
					"Minimal movement speed is negativ! Only positiv values allowed");
		}
		minMovementSpeed = minSpeed;
	}

	@Override
	public void setMaxMovementSpeed(double maxSpeed) {
		maxMovementSpeedConfigured = true;
		if (maxSpeed < 0) {
			throw new ConfigurationException(
					"Maximal movement speed is negativ! Only positiv values allowed");
		}
		maxMovementSpeed = maxSpeed;
	}

	@Override
	public double getMinMovementSpeed() {
		if (!minMovementSpeedConfigured) {
			throw new ConfigurationException(
					"Minimal movement speed is not configured! Please add this property to your configuration!");
		}
		return minMovementSpeed;
	}

	@Override
	public double getMaxMovementSpeed() {
		if (!maxMovementSpeedConfigured) {
			throw new ConfigurationException(
					"Maximal movement speed is not configured! Please add this property to your configuration!");
		}
		return maxMovementSpeed;
	}

	@Override
	public void updateMovementSpeed(double min, double max, long duration,
			int steps) {
		if (!minMovementSpeedConfigured || !maxMovementSpeedConfigured) {
			throw new ConfigurationException(
					"Minimal or maximal movement speed is not configured! Please add this properties to your configuration!");
		}
		if (steps < 0) {
			throw new ConfigurationException(
					"The update steps are to small. Minimal update step is 0!");
		}
		if (min > max) {
			throw new ConfigurationException(
					"Minimal value is bigger then the maximal value!");
		}
		if (duration == 0) {
			setMinMovementSpeed(min);
			setMaxMovementSpeed(max);
			return;
		}

		double deltaMin = min - this.minMovementSpeed;
		double deltaMax = max - this.maxMovementSpeed;

		for (int i = 0; i < (steps + 1); i++) {
			long updateTime = (duration * (i + 1)) / (steps + 1);
			double updateMin = getMinMovementSpeed()
					+ ((deltaMin * (i + 1)) / (steps + 1));
			double updateMax = getMaxMovementSpeed()
					+ ((deltaMax * (i + 1)) / (steps + 1));
			Event.scheduleWithDelay(updateTime, new UpdateSpeed(updateMin,
					updateMax), null, 0);

		}
	}

	private class UpdateSpeed implements EventHandler {
		private double minSpeed;

		private double maxSpeed;

		public UpdateSpeed(double minSpeed, double maxSpeed) {
			this.minSpeed = minSpeed;
			this.maxSpeed = maxSpeed;
		}

		@Override
		public void eventOccurred(Object content, int type) {
			DefaultHostProperties.this.setMinMovementSpeed(minSpeed);
			DefaultHostProperties.this.setMaxMovementSpeed(maxSpeed);
		}

	}

}
