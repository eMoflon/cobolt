package de.tud.kom.p2psim.impl.network;

import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Borrowed from the P2Pstream-overlay this component is able to estimate the
 * current bandwidth consumption at a host. It can be used in the NetLayer or
 * any other component that estimates a bandwidth by passing message sizes.
 * 
 * It supports two modes of operation: estimation of used bandwidth as well as
 * estimation of remaining free bandwidth - have a look at the constructors.
 * 
 * @author Bjoern Richerzhagen, based on sources by unknown
 * @version 1.0, 08.08.2012
 */
public class BandwidthEstimator {

	/**
	 * Estimation interval in seconds
	 */
	private final int estimationInterval;

	private final BandwidthImpl estimatedBandwidth;

	/**
	 * if this is not null, the estimator will return the remaining FREE
	 * bandwidth (i.e. max - estimate)
	 */
	private final BandwidthImpl maxBandwidth;

	private final boolean isFreeBandwidth;

	private final long[] inTransRing;

	private final long[] outTransRing;

	private long inLastEditInSec = -1;

	private long outLastEditInSec = -1;

	private long inTransBytes = 0;

	private long outTransBytes = 0;

	/**
	 * 
	 * @param estimationInterval
	 *            time that is used for the estimation in seconds
	 */
	public BandwidthEstimator(int estimationInterval) {
		this(new BandwidthImpl(0, 0), estimationInterval);
	}

	/**
	 * A bandwidth estimator that counts the bandwidth that is USED (i.e. not
	 * the remaining free BW).
	 * 
	 * @param estimatedBandwidth
	 *            this object is updated with the estimations each time a change
	 *            occurs.
	 * @param estimationInterval
	 *            seconds of historical data that is used to estimate the
	 *            current BW (window smoothing)
	 */
	public BandwidthEstimator(BandwidthImpl estimatedUsedBandwidth,
			int estimationInterval) {
		this(estimatedUsedBandwidth, null, estimationInterval);
	}

	/**
	 * A bandwidth estimator that counts the bandwidth that is remaining free
	 * with respect to the maximum bandwidth provided.
	 * 
	 * @param estimatedRemainingBandwidth
	 * @param maxBandwidth
	 * @param estimationInterval
	 */
	public BandwidthEstimator(BandwidthImpl estimatedRemainingBandwidth,
			BandwidthImpl maxBandwidth, int estimationInterval) {
		this.estimationInterval = estimationInterval;
		this.estimatedBandwidth = estimatedRemainingBandwidth;
		this.maxBandwidth = maxBandwidth;
		this.isFreeBandwidth = maxBandwidth != null;
		this.inTransRing = new long[estimationInterval];
		this.outTransRing = new long[estimationInterval];
	}

	/**
	 * Current estimation (the object is updated with every new estimate)
	 * 
	 * @return
	 */
	public BandwidthImpl getEstimatedBandwidth() {
		refresh();
		return estimatedBandwidth;
	}

	/**
	 * If true, this estimator returns an estimation of remaining free BW with
	 * respect to a max-BW. If false, it just returns an estimation of the used
	 * bandwidth.
	 * 
	 * @return
	 */
	public boolean isFreeBandwidth() {
		return isFreeBandwidth;
	}

	/**
	 * Seconds of historical data that is used to compute the bandwidth.
	 * 
	 * @return
	 */
	public int getEstimationInterval() {
		return estimationInterval;
	}

	/**
	 * Call this whenever an incoming transmission took place
	 * 
	 * @param size
	 */
	public void incomingTransmission(long size) {
		long currentTimeInSec = Time.getCurrentTime() / Time.SECOND;
		int index = (int) (currentTimeInSec % inTransRing.length);
		refresh();
		inTransRing[index] += size;
		inTransBytes += size;
		inLastEditInSec = currentTimeInSec;
		if (isFreeBandwidth) {
			estimatedBandwidth.setDownBW(Math.max(0, maxBandwidth.getDownBW()
					- inTransBytes * 8 / inTransRing.length));
		} else {
			estimatedBandwidth.setDownBW(inTransBytes * 8 / inTransRing.length);
		}
	}

	/**
	 * Call this whenever an outgoing transmission took place
	 * 
	 * @param size
	 */
	public void outgoingTransmission(long size) {
		long currentTimeInSec = Time.getCurrentTime() / Time.SECOND;
		int index = (int) (currentTimeInSec % outTransRing.length);
		refresh();
		outTransRing[index] += size;
		outTransBytes += size;
		outLastEditInSec = currentTimeInSec;
		if (isFreeBandwidth) {
			estimatedBandwidth.setUpBW(Math.max(0, maxBandwidth.getUpBW()
					- outTransBytes * 8 / outTransRing.length));
		} else {
			estimatedBandwidth.setUpBW(outTransBytes * 8 / outTransRing.length);
		}
	}

	/**
	 * Triggers a refresh of the Bandwidth-Object (this might be necessary if no
	 * transmission was counted for a long time)
	 */
	public void refresh() {
		long currentTimeInSec = Time.getCurrentTime() / Time.SECOND;
		if (currentTimeInSec > outLastEditInSec) {
			if (currentTimeInSec - outLastEditInSec > outTransRing.length) {
				/*
				 * There was no transmission for a long time --> Reset of ring
				 * is the best.
				 */
				for (int i = 0; i < outTransRing.length; i++) {
					outTransRing[i] = 0;
				}
				outTransBytes = 0;
			} else {
				for (long i = outLastEditInSec + 1; i <= currentTimeInSec; i++) {
					int eraseIndex = (int) (i % outTransRing.length);
					outTransBytes -= outTransRing[eraseIndex];
					outTransRing[eraseIndex] = 0;
				}
			}
			if (isFreeBandwidth) {
				estimatedBandwidth.setUpBW(Math.max(0, maxBandwidth.getUpBW()
						- outTransBytes * 8 / outTransRing.length));
			} else {
				estimatedBandwidth
						.setUpBW(outTransBytes * 8 / outTransRing.length);
			}
		}
		if (currentTimeInSec > inLastEditInSec) {
			if (currentTimeInSec - inLastEditInSec > inTransRing.length) {
				/*
				 * There was no transmission for a long time --> Reset of ring
				 * is the best.
				 */
				for (int i = 0; i < inTransRing.length; i++) {
					inTransRing[i] = 0;
				}
				inTransBytes = 0;
			} else {
				for (long i = inLastEditInSec + 1; i <= currentTimeInSec; i++) {
					int eraseIndex = (int) (i % inTransRing.length);
					inTransBytes -= inTransRing[eraseIndex];
					inTransRing[eraseIndex] = 0;
				}
			}
			if (isFreeBandwidth) {
				estimatedBandwidth
						.setDownBW(Math.max(0, maxBandwidth.getDownBW()
								- inTransBytes * 8
								/ inTransRing.length));
			} else {
				estimatedBandwidth
						.setDownBW(inTransBytes * 8 / inTransRing.length);
			}
		}
	}
}
