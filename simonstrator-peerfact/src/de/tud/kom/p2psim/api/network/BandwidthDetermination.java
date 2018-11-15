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


package de.tud.kom.p2psim.api.network;

import de.tud.kom.p2psim.impl.util.BackToXMLWritable;

/**
 * This interface defines the methods, which an implementing class that realizes
 * a certain distribution (e.g. random distribution, distribution from the
 * OECD-report) for different types of network devices must implement. In
 * general, the provided methods return a <code>Bandwidth</code>-object, that
 * contains the upload- and download-bandwidth for a network device
 * 
 * @author Dominik Stingl
 * 
 * @param <T>
 *            specifies the type of the object, which can be utilized within the
 *            process of bandwidth-determination
 */
public interface BandwidthDetermination<T extends Object> extends BackToXMLWritable {

	/**
	 * This method returns a <code>Bandwidth</code>-object. Depending on the
	 * implementing class, this can be done by a random function or in
	 * accordance to a certain distribution, that is realized.
	 * 
	 * @return a <code>Bandwidth</code>-object, that contains the upload- and
	 *         download-bandwidth for a network device in bytes per second
	 */
	public BandwidthImpl getRandomBandwidth();

	/**
	 * This method returns a <code>Bandwidth</code>-object sensitive to the
	 * object provided by the parameter
	 * 
	 * @param object
	 *            is used to influence the determination of the returned
	 *            <code>Bandwidth</code>-object
	 * @return a <code>Bandwidth</code>-object, that contains the upload- and
	 *         download-bandwidth for a network device in bytes per second
	 */
	public BandwidthImpl getBandwidthByObject(T object);

}
