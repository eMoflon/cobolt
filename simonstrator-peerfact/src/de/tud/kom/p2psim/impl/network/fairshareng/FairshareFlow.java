package de.tud.kom.p2psim.impl.network.fairshareng;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * The Class Flow.
 */
public class FairshareFlow implements Comparable<FairshareFlow> {

	/** The dst. */
	private final FairshareNode src, dst;

	/** The hash code. */
	private final int hashCode;

	/** The propagation delay. */
	private final long propagationDelay;

	/** The current rate. */
	private long currentRate;

	/** Time when transfer is expected to end. */
	private long transferEndTime;

	/** Time when flow was first created (or burst was called). */
	private long creationTime;

	/** Remaining bytes. */
	private double remainingBytes;

	/** The last time, old current time. Needed to calculate remaining bytes. */
	private long lastTime;

	/** The old bandwidth. */
	private double oldBandwidth;

	/** The subnet. */
	private final FairshareSubnet subnet;

	/**
	 * Instantiates a new flow.
	 * @param fairshareSubnet
	 * 
	 * @param src
	 *            the src node
	 * @param dst
	 *            the dst node
	 * @param messageSize
	 * 				size of message
	 * @param propagationDelay
	 * 				propagation delay of message
	 */
	public FairshareFlow(FairshareSubnet subnet, FairshareNode src, FairshareNode dst, double messageSize, long propagationDelay) {

		this.subnet = subnet;
		this.src = src;
		this.dst = dst;

		this.remainingBytes = messageSize;
		this.propagationDelay = propagationDelay;

		this.creationTime = Time.getCurrentTime();
		this.lastTime = this.creationTime;

		this.currentRate = 0;
		this.oldBandwidth = 0;

		
		/**
		 * FIXME [JR]: Using the following calculation, two distinct flows between the same nodes have the same hash code... !?
		 * Shouldn't parallel transfers between the same nodes be possible?
		 */
		final int hc = 17;
		final int hashMultiplier = 59;
		this.hashCode = (((hc * hashMultiplier) + this.src.hashCode()) * hc) + this.dst.hashCode();

	}

	/**
	 * Gets the dst of the flow.
	 * 
	 * @return the dst
	 */
	public FairshareNode getDst() {
		return this.dst;
	}

	/**
	 * Gets the src of the flow.
	 * 
	 * @return the src
	 */
	public FairshareNode getSrc() {
		return this.src;
	}

	/**
	 * Gets the current rate.
	 * 
	 * @return the rate
	 */
	public double getRate() {
		return this.currentRate;
	}

	/**
	 * Gets the propagation delay.
	 *
	 * @return the propagation delay
	 */
	public long getPropagationDelay() {
		return this.propagationDelay;
	}

	/**
	 * Gets the transfer end time.
	 *
	 * @return the transfer end time
	 */
	public long getTransferEndTime() {
		return this.transferEndTime;
	}

	/**
	 * Gets the creation time.
	 *
	 * @return the creation time
	 */
	public long getCreationTime() {
		return this.creationTime;
	}


	/**
	 * Adds a new message to the flow.
	 *
	 * @param msgSize the msg size
	 */
	public void addBurstMessage(double msgSize) {

		this.creationTime = Time.getCurrentTime();
		this.lastTime = this.creationTime;

		this.remainingBytes = msgSize;

		assert (this.remainingBytes > 0);

		/* Delete flow from scheduler as transferEndTime will change and we need to reinsert. */
		this.subnet.removeEventFromSchedule(this);

		this.calculateNewTransferEndTime();

		/* Reinsert, according to new transferEndTime. */
		this.subnet.addEventToSchedule(this);

	}

	/**
	 * Sets the flow's rate. Flow will automatically query Nodes for free
	 * bandwidth and reserve it.
	 * 
	 * Usually called two times. Once: Set rate to zero, then again for real rate.
	 * 
	 * @param newRate
	 *            the new rate
	 * @throws Exception
	 *             thrown if not enough bandwidth could be reserved
	 */
	public long setRate(long newRate) throws Exception {

		assert( newRate >= 0 );

		if( newRate != this.currentRate ) {

			this.getSrc().addCurrentUpRate(newRate - this.currentRate);
			this.getDst().addCurrentDownRate(newRate - this.currentRate);

			this.oldBandwidth = this.currentRate;
			this.currentRate = newRate;

			//			if( newRate > 0 ) {
			//				log.debug("Setting rate to " + newRate + "bytes/sec for " + this);
			//			}

			if( newRate == 0 ) {
				/* Rate is zero, so message will never be fully received. */
				this.subnet.removeEventFromSchedule(this);
			}

			this.calculateNewTransferEndTime();

			if( newRate > 0 ) {
				/* Reschedule Flow. */
				this.subnet.addEventToSchedule(this);
			}

		}

		return this.transferEndTime;

	}

	/**
	 * Resets the Flow: Sets current rate to zero.
	 */
	public void reset() {
		try {
			this.setRate(0);
		} catch (final Exception e) {
			//None.
		}
	}

	/**
	 * Checks if a node is locally bottlenecked.
	 * 
	 * @param inRespectToNode
	 *            the node in respect to
	 * @return true, if is locally bottlenecked
	 * 
	 * @throws Exception
	 *             thrown if neither src or dst match one end of the flow
	 */
	public boolean isLocallyBottlenecked(FairshareNode inRespectToNode) throws Exception {

		if ((inRespectToNode != this.src) && (inRespectToNode != this.dst)) {
			throw new Exception(inRespectToNode + "  is neither src or dst.");
		}

		if (inRespectToNode == this.src) {
			return (this.src.getCurrentBandwidth().getUpBW() == 0);
		} else if (inRespectToNode == this.dst) {
			return (this.dst.getCurrentBandwidth().getDownBW() == 0);
		}

		return false;

	}

	/**
	 * Checks if a node is remotely bottlenecked.
	 * 
	 * @param inRespectToNode
	 *            the node in respect to
	 * @return true, if is remotely bottlenecked
	 * 
	 * @throws Exception
	 *             thrown if neither src or dst match one end of the flow
	 */
	public boolean isRemotelyBottlenecked(FairshareNode inRespectToNode) throws Exception {

		if ((inRespectToNode != this.src) && (inRespectToNode != this.dst)) {
			throw new Exception(inRespectToNode + "  is neither src or dst.");
		}

		if (inRespectToNode == this.dst) {
			return (this.src.getCurrentBandwidth().getUpBW() == 0);
		} else if (inRespectToNode == this.src) {
			return (this.dst.getCurrentBandwidth().getDownBW() == 0);
		}

		return false;

	}


	private void calculateNewTransferEndTime() {

		final long currentTime = Time.getCurrentTime();
		final long lastTransferInterval = currentTime - this.lastTime;
		this.lastTime = currentTime;

		assert (lastTransferInterval >= 0);

		final double byteBurst = (lastTransferInterval / (double) Time.SECOND)
				* this.oldBandwidth;

		this.remainingBytes -= byteBurst;

		double time;
		if( this.getRate() == 0 ) {
			time = Double.POSITIVE_INFINITY;
		} else {
			time = this.remainingBytes * 8 / this.getRate();
		}

		this.transferEndTime = Math.round(time * Time.SECOND) + currentTime;
		if (this.transferEndTime < currentTime) {
			this.transferEndTime = currentTime;
		}

		assert (this.creationTime <= this.transferEndTime);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (obj instanceof FairshareFlow) {
			final FairshareFlow flow = (FairshareFlow) obj;
			/**
			 * FIXME [JR]: Just to be sure: can't there be two distinct parallel flows between the same nodes?! 
			 * With the following comparison they would be the same... make sure that is right! 
			 * Maybe consider calculating a unique hash code (see comment above) and using this to compare them!
			 */
			return flow.getSrc().equals(this.getSrc()) && flow.getDst().equals(this.getDst());
		}

		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(FairshareFlow flow) {
		return ((Double)this.getRate()).compareTo(flow.getRate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		try {
			return "Flow[ " + this.getSrc() + " -> " + this.getDst() + " Arr: " + this.transferEndTime + "]";
		} catch (final Exception e) {
			//None.
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		/* Precomputed so save time. */
		return this.hashCode;
	}

}