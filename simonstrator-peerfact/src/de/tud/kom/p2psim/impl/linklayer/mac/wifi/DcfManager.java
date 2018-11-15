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

package de.tud.kom.p2psim.impl.linklayer.mac.wifi;

import java.util.Random;

import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.Standard_802_11;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

// in backoffTimer is the time for DIFS to the idle state!

/**
 * This Class represents the DCF-Manager (Distributed Coordination Function) for
 * the AdHoc MAC - IEEE 802.11.<br>
 * It handles additionally the WiFi States, and the correct calculation of the
 * BackoffTimer. This is dependent of the state changes.<br>
 * We use not a real state machine. The states are dependent of a endTime, which
 * represents the end of the state. So it is possible, that the DCF is in
 * multiple states, which are not exclude self.<br>
 * The BackoffTimer is not a counter. It stores an endTime, which will be
 * extended with the start of a state. In the backoffTimer will be handled the
 * DIFS to the idle state. So the DIFS is not contained in the {@link WifiState}
 * .
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.02.2013
 */
public class DcfManager {

	// times are for 802.11a
	// will be replaced in the constructor.
	private long slotTime = 9;

	private long eifs = 94;

	private long sifs = 16;

	private long difs = 34;

	private long ctsTimeout = 69;

	private int cwMin = 15;

	private int cwMax = 1023;

	private int cw = 15;

	// The times for the "state machine"

	private long navEndTime = 0;

	private long rxStartTime = 0;

	private long rxEndTime = 0;

	private long txStartTime = 0;

	private long txEndTime = 0;

	private long ccaStartTime = 0;

	private long ccaEndTime = 0;

	private long ctsTimeoutEndTime = 0;

	private long backoffTimerEnd = 0;

	private long backoffTimerLastRunningStart = 0;

	private boolean runningBackoffTimer;

	/**
	 * If the backofftimer should be extended, but it was 0, then should be this
	 * flag true. So we can simulate collision (other events would be extend the
	 * backofftimer and the WifiState).
	 */
	private boolean ignoreWifiState = false;

	private Ieee80211AdHocMac mac = null;

	private final Random rand = Randoms.getRandom(DcfManager.class);

	/**
	 * The possible states of the Mac/Wifi.
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 22.02.2013
	 */
	public enum WifiState {
		/**
		 * The Medium is free
		 */
		IDLE,
		/**
		 * The Clear Channel Assessment (CCA)
		 */
		CCA_BUSY,
		/**
		 * NAV
		 */
		NAV,
		/**
		 * Receiving, from start RTS to ACK
		 */
		RX,
		/**
		 * Transmission, from send RTS to ACK
		 */
		TX,
		/**
		 * CTS timeout
		 */
		CTS_TIMEOUT;
	}

	/**
	 * Creates the DcfManager for the given {@link MacLayer}.
	 * 
	 * @param mac
	 *            The {@link MacLayer} which uses this {@link DcfManager}.
	 * @param standard
	 *            The used {@link Standard_802_11}.
	 */
	public DcfManager(Ieee80211AdHocMac mac, Standard_802_11 standard) {
		this.mac = mac;

		this.setCwMin(standard.getCwMin());
		this.setCwMax(standard.getCwMax());
		this.setSifs(standard.getSifs());
		this.setSlotTime(standard.getSlotTime());
		this.setDifs(standard.getDifs());
		this.setEifs(standard.getEifs());
		this.setCtsTimeout(standard.getCtsTimeout());
	}

	/**
	 * Gets the actually (important) WifiState.<br>
	 * The State of this {@link DcfManager} can be multiple. So the most
	 * important {@link WifiState} will be returned.
	 * 
	 * @return The actually {@link WifiState}.
	 */
	public WifiState getWifiState() {
		// order of checks is important!
		long current = Time.getCurrentTime();
		if (txEndTime > current) {
			return WifiState.TX;
		}
		if (rxEndTime > current) {
			return WifiState.RX;
		}
		if (navEndTime > current) {
			return WifiState.NAV;
		}
		if (ccaEndTime > current) {
			return WifiState.CCA_BUSY;
		}
		if (ctsTimeoutEndTime > current) {
			return WifiState.CTS_TIMEOUT;
		}
		return WifiState.IDLE;
	}

	public boolean isInRxState() {
		return rxEndTime > Time.getCurrentTime();
	}

	public boolean isInTxState() {
		return txEndTime > Time.getCurrentTime();
	}

	public boolean isInCcaState() {
		return ccaEndTime > Time.getCurrentTime();
	}

	public boolean isInNavState() {
		return navEndTime > Time.getCurrentTime();
	}

	public long getRxStartTime() {
		return rxStartTime;
	}

	public long getTxStartTime() {
		return txStartTime;
	}

	/**
	 * Resets the CW (Contention Window). After a successfully transmitted
	 * packet, the cw should be reseted to the cwMin.
	 */
	public void resetCw() {
		cw = cwMin;
	}

	/**
	 * Double the CW (Contention Window), up to cwMax. If a packet is
	 * unsuccessfully transmitted, the CW should be increased.
	 */
	public void increaseCw() {
		cw = Math.min(2 * (cw + 1) - 1, cwMax);
	}

	/**
	 * Calculate the time to the Idle State. This is not a fix time, because
	 * other events can be extend this time.
	 * 
	 * @return The actually time to the next Idle State.
	 */
	public long getTimeToIdleState() {
		long temp1 = Math.max(txEndTime, rxEndTime);
		long temp2 = Math.max(navEndTime, ccaEndTime);
		long temp3 = Math.max(ctsTimeoutEndTime, temp1);
		long max = Math.max(temp2, temp3);
		long timeToIdle = max + getDifs() - Time.getCurrentTime();

		return Math.max(timeToIdle, 0);
	}

	private void extendBackoffTimer(long duration) {
		if (runningBackoffTimer) {
			backoffTimerEnd += duration;
		}
	}

	/**
	 * Update the BackoffTimer-EndTime, after the change of a state. It handles
	 * multiple calls of state changes or state extends.
	 * 
	 * @param duration
	 *            The duration of the new state change/extends.
	 */
	public void updateBackoffTimer(long duration) {
		// if no Backofftimer is running
		if (!runningBackoffTimer) {
			return;
		}

		long current = Time.getCurrentTime();
		// if the backofftimer is 0, then we can send as next.
		if (backoffTimerEnd == current) {
			// important to set this flag, so we ignore the state during the
			// sending of this MAC, because the State is changed through an
			// other event for the same time.
			ignoreWifiState = true;
			return;
		}
		duration += getDifs();
		// calculate the minimal extend time for the BackoffTimerEndTime
		// Multiple state changes/extends on the same time, should not be extend
		// the BackoffTimerEndTime.
		long idleStart = current + getTimeToIdleState();
		long diff = idleStart - backoffTimerLastRunningStart;
		backoffTimerLastRunningStart = idleStart;

		// if diff is bigger then the duration, then should be used the
		// duration, because the diff contains time from the IdleState.
		if (diff < duration) {
			extendBackoffTimer(diff);
		} else {
			extendBackoffTimer(duration);
		}

	}

	/**
	 * Stops and resets the BackoffTimer.
	 */
	public void stopBackoffTimer() {
		resetBackoffTimer();
	}

	/**
	 * Resets the backoffTimer. The flag "ignoreWifiState" will be reseted, too.
	 */
	public void resetBackoffTimer() {
		backoffTimerEnd = 0;
		runningBackoffTimer = false;
		ignoreWifiState = false;
	}

	/**
	 * Starts the internal clock for the BackoffTimer. This should be called
	 * every time, before a transmission should be started. Because in this
	 * method will be checked, whether a backoffTimer is needed or not. The
	 * BackoffTimer is needed, if the medium is busy or after every
	 * transmission.
	 * 
	 * @return <code>True</code> if the BackoffTimer could be started, otherwise
	 *         <code>false</code>, because the BackoffTimer is running.
	 */
	public boolean startBackoffTimer() {
		if (runningBackoffTimer) {
			return false;
		}
		backoffTimerLastRunningStart = Time.getCurrentTime()
				+ getTimeToIdleState();
		//
		// The Backoff-Timer must be running if:
		// * The Medium is busy
		// * and after every transmission
		//
		// After every transmission is not so easy in this structure!
		// So we use an approximation!
		//
		if (isMediumFree() && !hasSentInLastCW()) {
			backoffTimerEnd = 0 * slotTime + backoffTimerLastRunningStart;
		} else {
			backoffTimerEnd = rand.nextInt(cw) * slotTime
					+ backoffTimerLastRunningStart;
		}
		runningBackoffTimer = true;
		return true;
	}

	/**
	 * Checks for a free Medium. This is the case, if the WifiState is IDLE, and
	 * the Time to the Idle state is 0. The State of the wifi can be IDLE, but
	 * because of the DIFS, after every other state is the medium not released!
	 * 
	 * @return <code>True</code> if the medium is free, otherwise
	 *         <code>False</code>.
	 */
	private boolean isMediumFree() {
		return getWifiState().equals(WifiState.IDLE)
				&& getTimeToIdleState() == 0;
	}

	/**
	 * This is an approximation for the check of the last sent. The BackOffTimer
	 * should be run after the sending of message, but this is not so easy to
	 * implement in this structure. So we use this approximation.
	 * <p>
	 * The endTime of the last transmission + the CW should be bigger then the
	 * actually time.
	 * 
	 * @return <code>True</code> if the in the last CW the MAC has sent a
	 *         Message, otherwise <code>false</code>.
	 */
	private boolean hasSentInLastCW() {
		return txEndTime + cw * slotTime > Time.getCurrentTime();
	}

	/**
	 * Check for running of the BackoffTimer.
	 * 
	 * 
	 * @return <code>True</code> if the BackoffTimer is running, otherwise
	 *         <code>false</code>.
	 */
	public boolean isBackoffTimerRunning() {
		return runningBackoffTimer;
	}

	/**
	 * Gets the BackoffTimerEnd-Time. It is not fix, because it can be extended
	 * thru of state changes
	 * 
	 * @return The time of the end of the BackoffTimer.
	 */
	public long getBackoffTimeEnd() {
		if (!runningBackoffTimer) {
			throw new AssertionError(
					"The backoffTimer is not running. Please start the BackoffTimer before you call this Method.");
		}
		// TODO: can be removed, if not occured; or as assertion
		if (!ignoreWifiState && backoffTimerEnd == Time.getCurrentTime()
				&& getWifiState() != WifiState.IDLE) {
			throw new AssertionError(
					"try to get BackoffTime, but not in Idle state!");
		}
		return backoffTimerEnd;
	}

	/**
	 * Changes the state and updates the BackoffTimer
	 * 
	 * @param duration
	 *            The duration of the TX.
	 */
	public void notifyTxNow(long duration) {
		if (txEndTime > Time.getCurrentTime()) {
			throw new AssertionError("2 sends on the same time?");
		}
		txStartTime = Time.getCurrentTime();
		txEndTime = Time.getCurrentTime() + duration;

		// needed really? Maybe, if we have more then one BackoffTimer...
		updateBackoffTimer(duration);
	}

	/**
	 * Changes the state and updates the BackoffTimer
	 * 
	 * @param duration
	 *            The duration of the RX.
	 */
	public void notifyRxNow(long duration) {
		if (rxEndTime > Time.getCurrentTime()) {
			throw new AssertionError(
					"2 receives on the same time? (How to handle collisions for the second or more transfer?)");
		}
		rxStartTime = Time.getCurrentTime();
		rxEndTime = Time.getCurrentTime() + duration;
		updateBackoffTimer(duration);
	}

	/**
	 * Changes/extends the state and update the BackoffTimer
	 * 
	 * @param duration
	 *            The duration of the NAV.
	 */
	public void notifyNavNow(long duration) {

		long newEndTime = Time.getCurrentTime() + duration;
		this.navEndTime = Math.max(navEndTime, newEndTime);
		updateBackoffTimer(duration);
	}

	/**
	 * Changes/extends the state and updates the BackoffTimer
	 * 
	 * @param duration
	 *            The duration of the CCA.
	 */
	public void notifyCcaNow(long duration) {
		long newEndTime = Time.getCurrentTime() + duration;
		this.ccaEndTime = Math.max(ccaEndTime, newEndTime);
		updateBackoffTimer(duration);
	}

	/**
	 * Starts the CTS-Timeout and updates BackoffTimer
	 */
	public void notifiyCTSTimeout() {
		long duration = getCtsTimeout();
		ctsTimeoutEndTime = Time.getCurrentTime() + duration;
		updateBackoffTimer(duration);
	}

	/**
	 * Updates BackoffTimer with an EIFS.
	 */
	public void notifyRxDrop() {
		updateBackoffTimer(getEifs() - getDifs());
	}

	protected void setSifs(long sifs) {
		this.sifs = sifs;
	}

	protected void setDifs(long difs) {
		this.difs = difs;
	}

	protected void setEifs(long eifs) {
		this.eifs = eifs;
	}

	public long getSifs() {
		return sifs;
	}

	public long getDifs() {
		return difs;
	}

	public long getEifs() {
		return eifs;
	}

	public long getCtsTimeout() {
		return ctsTimeout;
	}

	protected void setSlotTime(long slotTime) {
		this.slotTime = slotTime;
	}

	protected void setCwMax(int cwMax) {
		this.cwMax = cwMax;
	}

	protected void setCwMin(int cwMin) {
		this.cwMin = cwMin;
	}

	protected void setCtsTimeout(long ctsTimeout) {
		this.ctsTimeout = ctsTimeout;
	}

	/**
	 * This flag is used to start a collision. If two BackoffTimer ends to the
	 * same time, we can only handle one of them. The first event for the end of
	 * the BackoffTimer will be change the {@link WifiState} of other.<br>
	 * This flag will be set, if the BackoffTimer will be extended, but the
	 * BackoffTimer was 0. So we know, that the MAC can send, because two or
	 * more BackoffTimer was 0. (it's a hack, because we have multiple events on
	 * the same time).
	 * 
	 * @return <code>True</code> if the {@link WifiState} should be ignored,
	 *         otherwise <code>false</code>
	 */
	public boolean isIgnoreWifiState() {
		return ignoreWifiState;
	}

}
