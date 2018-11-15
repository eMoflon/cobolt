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

package de.tudarmstadt.maki.simonstrator.peerfact.analyzer;

import java.io.Writer;

import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;

/**
 * This class represents an interval analyzer. This class should help to
 * implement an easy interval analyzer.It will be execute in defined time
 * intervals a collect of the data and then write out of the data.<br>
 * Of course, it is possible to collect data during an interval, and write this
 * data out, without to use the collectData method.
 * 
 * 
 * @author Christoph Muenker
 * @version 1.0, 07/24/2011
 */
public abstract class AbstractIntervalAnalyzer implements EventHandler,
		Analyzer {

	/**
	 * Information, whether the Analyzer is active
	 */
	private boolean active;

	/**
	 * Should be set, with the setter.
	 */
	private long measurementInterval = Time.MINUTE;

	/**
	 * standard constructor
	 */
	public AbstractIntervalAnalyzer() {
		// standard constructor
	}

	/**
	 * Sets the intervalTime and reset the analyzers.
	 * 
	 * @param measurementInterval
	 *            The time between two measurements.
	 */
	public AbstractIntervalAnalyzer(long measurementInterval) {
		this();
		this.measurementInterval = measurementInterval;
	}

	/**
	 * Is called if the Analyzer is started. It starts the interval and
	 * initialize the data structures. Additionally it call reset, to reset the
	 * data structures.
	 */
	@Override
	public final void start() {
		if (!isActive()) {
			active = true;
			Event.scheduleImmediately(this, null, 0);

			initialize();
			resetCollectedData();
		}
	}

	/**
	 * It is called if the analyzer is stopped. It stops the intervalEvents
	 * (indirect active=false).<br>
	 * Additionally it calls the collect data and the write out of the collected
	 * data methods.
	 */
	@Override
	public final void stop(Writer output) {
		if (isActive()) {
			active = false;
			collectData();
			writeCollectedData();
			stop();
		}
	}

	/**
	 * This is called every interval. It executes the collect of the data, the
	 * write out of the collected data and the reset of the collected data.
	 */
	@Override
	public final void eventOccurred(Object se, int type) {
		if (isActive()) {
			collectData();
			writeCollectedData();
			resetCollectedData();
			Event.scheduleWithDelay(measurementInterval, this, null, 0);
		}
	}

	/**
	 * Called if the analyzer is started. It should be used, to initialize the
	 * data structure, which are used to collect the measurements.
	 */
	public abstract void initialize();

	/**
	 * Called if the analyzer is stopped.
	 */
	public abstract void stop();

	/**
	 * Collect the data, if a sample to the interval time should be collect.
	 */
	public abstract void collectData();

	/**
	 * Writes out the collected data, which are collected during the interval or
	 * collected with the method collectData()
	 */
	public abstract void writeCollectedData();

	/**
	 * Resets the collected data for the next interval.
	 */
	public abstract void resetCollectedData();

	/**
	 * Gets the state of this Analyzer.
	 * 
	 * @return If it is started, then <code>true</code>, otherwise
	 *         <code>false</code>
	 */
	public boolean isActive() {
		return active;
	}

	// ************************************
	// Setting and preparing the analyzer
	// ************************************

	public void setMeasurementInterval(long timeInterval) {
		this.measurementInterval = timeInterval;
	}

	/**
	 * Gets the measurement interval.
	 * 
	 * @return The measurement interval.
	 */
	public long getMeasurementInterval() {
		return measurementInterval;
	}

}
