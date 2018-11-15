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


package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.BandwidthDetermination;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.util.BackToXMLWritable;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

public abstract class AbstractNetLayerFactory implements HostComponentFactory,
		BackToXMLWritable {

	/**
	 * In bytes per second. This variable is used to initialize
	 * <code>downBandwidth</code> with a default value if the config-file does
	 * not specify a separate value.
	 */
	protected final static long DEFAULT_DOWN_BANDWIDTH = 5000l;

	/**
	 * In bytes per second. This variable is used to initialize
	 * <code>upBandwidth</code> with a default value if the config-file does not
	 * specify a separate value.
	 */
	protected final static long DEFAULT_UP_BANDWIDTH = 5000l;

	/**
	 * In bytes per second. <code>downBandwidth</code> is initialized with a
	 * value, that can be provided by the config-file, otherwise it gets the
	 * value stored in <code>DEFAULT_DOWN_BANDWIDTH</code>.
	 */
	protected long downBandwidth;

	/**
	 * In bytes per second. <code>upBandwidth</code> is initialized with a
	 * value, that can be provided by the config-file, otherwise it gets the
	 * value stored in <code>DEFAULT_UP_BANDWIDTH</code>.
	 */
	protected long upBandwidth;

	protected BandwidthDetermination bandwidthDetermination;
	
	protected NetMeasurementDB db;

	public AbstractNetLayerFactory() {
		this.downBandwidth = DEFAULT_DOWN_BANDWIDTH;
		this.upBandwidth = DEFAULT_UP_BANDWIDTH;
		this.bandwidthDetermination = null;
	}

	protected BandwidthImpl getBandwidth(NetID netID) {
		if (bandwidthDetermination == null) {
			return new BandwidthImpl(this.downBandwidth, this.upBandwidth);
		}
		return bandwidthDetermination.getRandomBandwidth();
	}

	public void setBandwidthDetermination(
			BandwidthDetermination bandwidthDetermination) {
		this.bandwidthDetermination = bandwidthDetermination;
	}

	/**
	 * Sets the downstream bandwidth in bit/sec, use {@link Rate}
	 * 
	 * @param downBandwidth
	 */
	public void setDownBandwidth(long downBandwidth) {
		this.downBandwidth = downBandwidth;
	}

	/**
	 * Sets the upstream bandwidth in bit/sec, use {@link Rate}
	 * 
	 * @param upBandwidth
	 */
	public void setUpBandwidth(long upBandwidth) {
		this.upBandwidth = upBandwidth;
	}
	
	/**
	 * Sets the measurement db.
	 *
	 * @param db the new measurement db
	 */
	public void setMeasurementDB(NetMeasurementDB db) {
		this.db = db;
	}
	
	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("downBandwidth", downBandwidth);
		bw.writeSimpleType("upBandwidth", upBandwidth);
		bw.writeComplexType("BandwidthDetermination", bandwidthDetermination);
		bw.writeComplexType("MeasurementDB", db);
	}

}
