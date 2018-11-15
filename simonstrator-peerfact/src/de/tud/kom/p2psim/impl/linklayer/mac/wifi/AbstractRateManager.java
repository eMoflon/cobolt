/*
 * Copyright(c) 2005-2010 KOM – Multimedia Communications Lab
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.PropagationLossModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.Standard_802_11;
import de.tud.kom.p2psim.impl.topology.views.wifi.phy.InterferenceHelper;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * This class is an interface for other implementation of Rate Managers. The
 * interface will be informed about fails and receives of MAC messages. So it is
 * possible to implement all available Rate Managers, which use the counting of
 * failing and receiving of Messages. It is <b>not</b> possible to implement
 * Rate Managers, which use a modified MAC-Header.<br>
 * The {@link IWifiRemoteStation} can be used to store state information for
 * every station.
 * <p>
 * If a new Rate Manager is implemented, it should be added to the
 * {@link RateManagerTypes} and in the create method in this class.
 * <p>
 * <p>
 * 
 * This class is based on NS3 (src/wifi/model/wifi-remote-station-manager.cc) by
 * Mathieu Lacage <mathieu.lacage@sophia.inria.fr> further extended by Christoph
 * Muenker.
 * 
 * @author Christoph Muenker
 * @version 1.0, 19.02.2013
 */
public abstract class AbstractRateManager {

	/**
	 * Defines the available Rate Managers.
	 */
	public enum RateManagerTypes {
		IDEAL, ARF
	}

	/**
	 * Creates a new RateManager.
	 * 
	 * @param mac
	 *            The Mac which need the RateManager.
	 * @param rateManagerType
	 *            The type of the RateManager.
	 * @param standard
	 *            The standard which use the MAC.
	 * @return A new RateManager of the type which is requested. It returns
	 *         <code>null</code> if the requested Type is not available.
	 */
	public static AbstractRateManager createRateManager(Ieee80211AdHocMac mac,
			RateManagerTypes rateManagerType, Standard_802_11 standard) {
		if (rateManagerType.equals(RateManagerTypes.IDEAL)) {
			return new IdealRateManager(standard.getWifiModes(),
					standard.getDefaultMode(), mac);
		} else if (rateManagerType.equals(RateManagerTypes.ARF)) {
			return new ArfRateManager(standard.getWifiModes(),
					standard.getDefaultMode(), mac);
		}
		return null;
	}

	/**
	 * A hashMap of information of {@link IWifiRemoteStation}s. To every
	 * {@link MacAddress} will be stored an own {@link IWifiRemoteStation}.
	 */
	private Map<MacAddress, IWifiRemoteStation> remoteStations = new HashMap<MacAddress, IWifiRemoteStation>();

	/**
	 * The modes are ordered after the dataRate.
	 */
	private List<WifiMode> modes = new ArrayList<WifiMode>();

	/**
	 * Should be the BSS Basic Rate Set, for the search of the rate for the
	 * answer message.
	 */
	private List<WifiMode> basicModes = new ArrayList<WifiMode>();

	private Ieee80211AdHocMac mac;

	private WifiMode defaultMode;

	/**
	 * can be set, if not set, it will be used the defaultMode.
	 */
	private WifiMode broadcastDataMode;

	/**
	 * Abstract Constructor of the {@link AbstractRateManager}. It stores the
	 * given values and sets the basicMode. The basicMode is the defaultMode.
	 * 
	 * @param modes
	 *            All modes, which should be handled by this Rate Manager.
	 * @param defaultMode
	 *            The default {@link WifiMode}. This is normal the lowest
	 *            {@link WifiMode} which is handled by this Rate Manager. After
	 *            this initialization of this class, the default mode is
	 *            additional one of the basicModes.
	 * @param mac
	 *            A reference to the MAC, which is owned by this RateManager.
	 */
	public AbstractRateManager(List<WifiMode> modes, WifiMode defaultMode,
			Ieee80211AdHocMac mac) {
		this.mac = mac;
		this.defaultMode = defaultMode;
		this.basicModes.add(this.defaultMode);
		for (WifiMode mode : modes) {
			this.modes.add(mode);
		}
		Collections.sort(modes);
	}

	/**
	 * Inform about the failing of an RTS, because the CTS-Timeout is fired.
	 * 
	 * @param station
	 *            station which not send the CTS back.
	 */
	protected abstract void doReportRtsFailed(IWifiRemoteStation station);

	/**
	 * Inform about the failing of the data, because the ACK-Timeout is fired.
	 * 
	 * @param station
	 *            station which not send the ACK back.
	 */
	protected abstract void doReportDataFailed(IWifiRemoteStation station);

	/**
	 * Inform about the receiving of the CTS, and so the RTS was OK.
	 * 
	 * @param station
	 *            station which send the CTS
	 * @param ctsSnr
	 *            The signal to noise ratio of the CTS
	 * @param ctsMode
	 *            The {@link WifiMode} of the CTS-Message.
	 */
	protected abstract void doReportRtsOk(IWifiRemoteStation station,
			double ctsSnr, WifiMode ctsMode);

	/**
	 * Inform about the receiving of the ACK, and so the Data was OK.
	 * 
	 * @param station
	 *            station which send the ACK
	 * @param ackSnr
	 *            The signal to noise ratio of the ACK
	 * @param ackMode
	 *            The {@link WifiMode} of the ACK-Message.
	 */
	protected abstract void doReportDataOk(IWifiRemoteStation station,
			double ackSnr, WifiMode ackMode);

	/**
	 * Inform about the failing of the RTS, and this was the last retry.
	 * 
	 * @param station
	 *            The station which not react to the RTS.
	 */
	protected abstract void doReportFinalRtsFailed(IWifiRemoteStation station);

	/**
	 * Inform about the failing of the Data, and this was the last retry.
	 * 
	 * @param station
	 *            The station which not received the data after x tries.
	 * 
	 */
	protected abstract void doReportFinalDataFailed(IWifiRemoteStation station);

	/**
	 * Inform about the receiving of an RTS or Data.
	 * 
	 * @param station
	 *            The station which send the RTS or the data
	 * @param rxSnr
	 *            The signal to noise ration of the sender
	 * @param txMode
	 *            The tx {@link WifiMode} of the sender.
	 */
	protected abstract void doReportRxOk(IWifiRemoteStation station,
			double rxSnr, WifiMode txMode);

	/**
	 * Create the own {@link IWifiRemoteStation}.
	 * 
	 * @param address
	 *            The identifier of the station
	 * @return The new created {@link IWifiRemoteStation}.
	 */
	protected abstract IWifiRemoteStation doCreateWifiRemoteStation(
			MacAddress address);

	/**
	 * Returns the {@link WifiMode} for a unicast transfer in depending of the
	 * given station.
	 * 
	 * @param station
	 *            The receiver of the unicast transfer
	 * @return The {@link WifiMode} for the unicast transfer to the given
	 *         station.
	 */
	protected abstract WifiMode doGetUnicastDataMode(IWifiRemoteStation station);

	/**
	 * Returns the {@link WifiMode} for the RTS-Message in depending of the
	 * given station.
	 * 
	 * @param station
	 *            The receiver of the RTS-Message.
	 * @return The {@link WifiMode} for the RTS-Message to the given station.
	 */
	protected abstract WifiMode doGetRtsMode(IWifiRemoteStation station);

	/**
	 * Should be invoked whenever the RTS is failed.
	 */
	final public void reportRtsFailed(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		doReportRtsFailed(station);
	}

	/**
	 * Should be invoked whenever the Data transmission failed!
	 */
	final public void reportDataFailed(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		doReportDataFailed(station);
	}

	/**
	 * Should be invoked whenever we receive the CTS associated to an RTS we
	 * just sent.
	 */
	final public void reportRtsOk(MacAddress address, double ctsSnr,
			WifiMode ctsMode) {
		IWifiRemoteStation station = getStation(address);
		doReportRtsOk(station, ctsSnr, ctsMode);
	}

	/**
	 * Should be invoked whenever we receive the Ack associated to a data packet
	 * we just sent. <br>
	 */
	final public void reportDataOk(MacAddress address, double ackSnr,
			WifiMode ackMode) {
		IWifiRemoteStation station = getStation(address);
		doReportDataOk(station, ackSnr, ackMode);
	}

	/**
	 * Should be invoked after calling ReportRtsFailed if no RTS should be more
	 * resent.
	 */
	final public void reportFinalRtsFailed(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		doReportFinalRtsFailed(station);
	}

	/**
	 * Should be invoked after calling ReportDataFailed if no resent of the data
	 * packet should be executed.
	 */
	final public void reportFinalDataFailed(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		doReportFinalDataFailed(station);
	}

	/**
	 * 
	 * Should be invoked whenever a packet is successfully received.
	 * 
	 * 
	 * @param address
	 *            remote address
	 * @param rxSnr
	 *            the snr of the packet received
	 * @param txMode
	 *            the transmission mode used for the packet received.
	 */
	final public void reportRxOk(MacAddress address, double rxSnr,
			WifiMode txMode) {
		IWifiRemoteStation station = getStation(address);
		doReportRxOk(station, rxSnr, txMode);
	}

	/**
	 * Returns a station to the given {@link MacAddress}. It does a lookup in a
	 * hashtable after the {@link IWifiRemoteStation}. If no
	 * {@link IWifiRemoteStation} found, then will be create a new
	 * {@link IWifiRemoteStation}.
	 * 
	 * @param address
	 *            The {@link MacAddress} to the station.
	 * @return A stored or new {@link IWifiRemoteStation}.
	 */
	private IWifiRemoteStation getStation(MacAddress address) {
		IWifiRemoteStation station = remoteStations.get(address);
		if (station == null) {
			station = doCreateWifiRemoteStation(address);
			remoteStations.put(address, station);
		}
		assert station != null : "Station should not be null!";
		return station;
	}

	/**
	 * Gets the DataMode for a unicast transfer to the given {@link MacAddress}.
	 * The answer is dependent on the used implementation of the RateManager. In
	 * normal case, the answer is {@link WifiMode} from all available Modes.
	 * 
	 * @param address
	 *            The {@link MacAddress} of the receiver.
	 * @return A {@link WifiMode} from {@link AbstractRateManager#modes}.
	 */
	public final WifiMode getUnicastDataMode(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		return doGetUnicastDataMode(station);
	}

	/**
	 * Gets {@link WifiMode} for the RTS Message for the given receiver. It is
	 * normal a {@link WifiMode}, which is declared as mandatory.
	 * 
	 * 
	 * @param address
	 *            The {@link MacAddress} of the receiver.
	 * @return The {@link WifiMode} for the RTS Message to the given receiver.
	 */
	public final WifiMode getRtsMode(MacAddress address) {
		IWifiRemoteStation station = getStation(address);
		return doGetRtsMode(station);
	}

	/**
	 * Chose the defaultMode if not an other {@link WifiMode} is declared as
	 * BroadcastMode. <br>
	 * The BroadcastMode can be set with
	 * {@link AbstractRateManager#setBroadcastDataMode(WifiMode)}.
	 * 
	 * @return The {@link WifiMode} for Broadcasts.
	 */
	public final WifiMode getBroadcastDataMode() {
		if (broadcastDataMode == null) {
			return defaultMode;
		}
		return broadcastDataMode;
	}

	/**
	 * Gets the {@link WifiMode} for the answer of a RTS Message. For the
	 * calculation of the WifiMode, it is used the algorithm of the IEEE
	 * Standard.
	 * 
	 * @param rtsMode
	 *            The {@link WifiMode} of the received RTS Message.
	 * @return The {@link WifiMode} for CTS Message.
	 */
	public final WifiMode getCtsMode(WifiMode rtsMode) {
		return getControlAnswerMode(rtsMode);
	}

	/**
	 * Gets the {@link WifiMode} for the acknowledgment of the data. For the
	 * calculation of the WifiMode, it is used the algorithm of the IEEE
	 * Standard.
	 * 
	 * @param dataMode
	 *            The {@link WifiMode} of the received Data.
	 * @return The {@link WifiMode} for the ACK Message.
	 */
	public final WifiMode getAckMode(WifiMode dataMode) {
		return getControlAnswerMode(dataMode);
	}

	/*
	 * copied from NS3::WifiRemoteStationManager
	 */
	protected WifiMode getControlAnswerMode(WifiMode reqMode) {
		/**
		 * The standard has relatively unambiguous rules for selecting a control
		 * response rate (the below is quoted from IEEE 802.11-2007, Section
		 * 9.6):
		 * 
		 * To allow the transmitting STA to calculate the contents of the
		 * Duration/ID field, a STA responding to a received frame shall
		 * transmit its Control Response frame (either CTS or ACK), other than
		 * the BlockAck control frame, at the highest rate in the
		 * BSSBasicRateSet parameter that is less than or equal to the rate of
		 * the immediately previous frame in the frame exchange sequence (as
		 * defined in 9.12) and that is of the same modulation class (see 9.6.1)
		 * as the received frame...
		 */
		WifiMode mode = getDefaultMode();
		boolean found = false;

		// First, search the BSS Basic Rate set
		for (WifiMode bss : getBSSBasicRateSet()) {
			if ((!found || bss.getPhyRate() > mode.getPhyRate())
					&& bss.getPhyRate() <= reqMode.getPhyRate()
					&& bss.getModClass() == reqMode.getModClass()) {
				mode = bss;
				// We've found a potentially-suitable transmit rate, but we
				// need to continue and consider all the basic rates before
				// we can be sure we've got the right one.
				found = true;
			}
		}

		// If we found a suitable rate in the BSSBasicRateSet, then we are
		// done and can return that mode.
		if (found) {
			return mode;
		}

		/**
		 * If no suitable basic rate was found, we search the mandatory rates.
		 * The standard (IEEE 802.11-2007, Section 9.6) says:
		 * 
		 * ...If no rate contained in the BSSBasicRateSet parameter meets these
		 * conditions, then the control frame sent in response to a received
		 * frame shall be transmitted at the highest mandatory rate of the PHY
		 * that is less than or equal to the rate of the received frame, and
		 * that is of the same modulation class as the received frame. In
		 * addition, the Control Response frame shall be sent using the same PHY
		 * options as the received frame, unless they conflict with the
		 * requirement to use the BSSBasicRateSet parameter.
		 * 
		 * Note that we're ignoring the last sentence for now, because there is
		 * not yet any manipulation here of PHY options.
		 */
		for (WifiMode thismode : modes) {

			/*
			 * If the rate:
			 * 
			 * - is a mandatory rate for the PHY, and - is equal to or faster
			 * than our current best choice, and - is less than or equal to the
			 * rate of the received frame, and - is of the same modulation class
			 * as the received frame
			 * 
			 * ...then it's our best choice so far.
			 */
			if (thismode.isMandatory()
					&& (!found || thismode.getPhyRate() > mode.getPhyRate())
					&& thismode.getPhyRate() <= reqMode.getPhyRate()
					&& thismode.getModClass() == reqMode.getModClass()) {
				mode = thismode;
				// As above; we've found a potentially-suitable transmit
				// rate, but we need to continue and consider all the
				// mandatory rates before we can be sure we've got the right
				// one.
				found = true;
			}
		}

		/**
		 * If we still haven't found a suitable rate for the response then
		 * someone has messed up the simulation config. This probably means that
		 * the WifiPhyStandard is not set correctly, or that a rate that is not
		 * supported by the PHY has been explicitly requested in a
		 * WifiRemoteStationManager (or descendant) configuration.
		 * 
		 * Either way, it is serious - we can either disobey the standard or
		 * fail, and I have chosen to do the latter...
		 */
		assert found : ("Can't find response rate for " + reqMode + ". Check standard and selected rates match.");

		return mode;
	}

	/**
	 * Gets the default Mode.
	 * 
	 * @return Returns the default Mode.
	 */
	protected WifiMode getDefaultMode() {
		return defaultMode;
	}

	/**
	 * Gets the BSS Basic Rate Set for this Rate Manager. Actually, it is only
	 * the default Mode.
	 * 
	 * @return Returns a Set of {@link WifiMode}s.
	 */
	protected Set<WifiMode> getBSSBasicRateSet() {
		// TODO: replace this with a BSS Basic Rate Set
		Set<WifiMode> bssbrs = new HashSet<WifiMode>();
		bssbrs.add(getDefaultMode());
		return bssbrs;
	}

	/**
	 * Gets the BasicModes.
	 * 
	 * @return
	 */
	public List<WifiMode> getBasicModes() {
		return basicModes;
	}

	/**
	 * Sets the Broadcast {@link WifiMode}. The default {@link WifiMode} is the
	 * defaultMode (the lowest WifiMode of this Rate Manager).
	 * <p>
	 * A higher BroadcastDataMode is not usually in the real world!
	 * 
	 * @param broadcastDataMode
	 *            The new {@link WifiMode} for the broadcast.
	 */
	public void setBroadcastDataMode(WifiMode broadcastDataMode) {
		this.broadcastDataMode = broadcastDataMode;
	}

	/**
	 * Adds a {@link WifiMode} to the basicModes. It can only added as basic
	 * mode, if it is registered as a normal mode.
	 * 
	 * @param mode
	 *            The {@link WifiMode} which should be added to the basicModes.
	 */
	public void addBasicMode(WifiMode mode) {
		if (modes.contains(mode)) {
			if (!basicModes.contains(mode)) {
				basicModes.add(mode);
			}
		} else {
			throw new AssertionError(
					"try to add a mode to the basicModes, which is not stored as a common mode");
		}
	}

	/**
	 * Gets all Modes.
	 * 
	 * @return All {@link WifiMode}s for this RateManager.
	 */
	protected List<WifiMode> getModes() {
		return modes;
	}

	/**
	 * Returns the {@link WifiMode} at place i. If WifiMode i not exists, it
	 * will throws a {@link IndexOutOfBoundsException}.
	 * 
	 * @param i
	 *            The place of the WifiMode.
	 * @return The {@link WifiMode} at position i.
	 */
	protected WifiMode getMode(int i) {
		return modes.get(i);
	}

	/**
	 * Gets the {@link MacLayer} which use this RateManager.
	 * 
	 * @return
	 */
	public Ieee80211AdHocMac getMac() {
		return mac;
	}

	/**
	 * Calculates the actually SNR between the start antenna and the target
	 * antenna. <br>
	 * This is only a helper to calculate the SNR in the AdHocMac.
	 */
	public double calculateActuallySNR(Location startPosition,
			Location targetPosition, WifiMode mode, double txPowerDbm) {
		InterferenceHelper helper = mac.getWifiTopologyView()
				.getInterferenceHelper();
		double noiseInterferenceW = helper
				.calculateNoiseInterferenceW(targetPosition);
		double rxPowerDbm = helper.getLossModel().getRxPowerDbm(
				txPowerDbm + helper.getTxGainDbm(), startPosition,
				targetPosition)
				+ helper.getRxGainDbm();
		return helper.calculateSnr(PropagationLossModel.dbmToW(rxPowerDbm),
				noiseInterferenceW, mode);
	}
}
