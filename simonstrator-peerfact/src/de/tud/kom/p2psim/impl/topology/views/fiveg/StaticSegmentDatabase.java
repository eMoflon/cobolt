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

package de.tud.kom.p2psim.impl.topology.views.fiveg;

import java.util.Random;

import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Simple static configuration of the 5G topology view database.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Nov 6, 2015
 */
public class StaticSegmentDatabase extends AbstractGridBasedTopologyDatabase {

	private double dropProbability = 0.0;

	private long latency = 100 * Time.MILLISECOND;

	private long latencyVariance = 50 * Time.MILLISECOND;

	private long bandwidthUpload = 1 * Rate.Mbit_s;

	private long bandwidthDownload = 16 * Rate.Mbit_s;

	private long cloudletBandwidthUpload = 4 * Rate.Mbit_s;

	private long cloudletBandwidthDownload = 16 * Rate.Mbit_s;

	private long cloudletLatency = 50 * Time.MILLISECOND;

	private long cloudletLatencyVariance = 10 * Time.MILLISECOND;

	private final Random rnd = Randoms.getRandom(StaticSegmentDatabase.class);

	public StaticSegmentDatabase() {
		super(100, true);
		super.setSupportCloudlets(true);
	}

	@Override
	protected Entry createEntryFor(int segmentID, boolean isCloudlet) {
		if (isCloudlet) {
			long latency = this.cloudletLatency + (rnd.nextBoolean() ? +1 : -1)
					* (long) (rnd.nextDouble() * cloudletLatencyVariance);
			assert latency > 0;
			return new StaticEntry(segmentID, dropProbability, dropProbability,
					latency, latency, cloudletBandwidthUpload,
					cloudletBandwidthDownload);
		} else {
			long latency = this.latency + (rnd.nextBoolean() ? +1 : -1)
					* (long) (rnd.nextDouble() * latencyVariance);
			assert latency > 0;
			return new StaticEntry(segmentID, dropProbability, dropProbability,
					latency, latency, bandwidthUpload, bandwidthDownload);
		}
	}

	public void setDropRate(double dropProbability) {
		this.dropProbability = dropProbability;
	}

	public void setLatency(long latency) {
		this.latency = latency;
	}

	public void setLatencyVariance(long latencyVariance) {
		this.latencyVariance = latencyVariance;
	}

	public void setCloudletLatency(long cloudletLatency) {
		this.cloudletLatency = cloudletLatency;
	}

	public void setCloudletLatencyVariance(long cloudletLatencyVariance) {
		this.cloudletLatencyVariance = cloudletLatencyVariance;
	}
	
	public void setBandwidthUp(long bandwidthUp) {
		this.bandwidthUpload = bandwidthUp;
	}
	
	public void setBandwidthDown(long bandwidthDown) {
		this.bandwidthDownload = bandwidthDown;
	}
	
	public void setCloudletBandwidthUp(long bandwidthUp) {
		this.cloudletBandwidthUpload = bandwidthUp;
	}
	
	public void setCloudletBandwidthDown(long bandwidthDown) {
		this.cloudletBandwidthDownload = bandwidthDown;
	}

}
