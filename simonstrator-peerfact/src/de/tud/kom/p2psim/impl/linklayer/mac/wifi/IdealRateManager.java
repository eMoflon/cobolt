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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;

/**
 * The Ideal Rate Manager calculates an SNR-Threshold for all modes and search
 * the suitable {@link WifiMode} for the transmission from the allowed modes. As
 * reference SNR, it is using the last received Message from the station. The
 * reference SNR will be stored in the {@link IdealRemoteStation}.
 * 
 * 
 * This class based on NS3 (src/wifi/model/ideal-wifi-manager.cc).
 * 
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.02.2013
 */
public class IdealRateManager extends AbstractRateManager {

	/**
	 * The maximum Bit Error Rate acceptable at any transmission mode
	 */
	private final double berThreshold = 10e-6;

	private final Map<WifiMode, Double> snrThresholds = new HashMap<WifiMode, Double>();

	/**
	 * Calculates the SNR-Threshold-HashMap for every allowed {@link WifiMode}.
	 * 
	 * @param modes
	 *            The allowed {@link WifiMode}s
	 * @param defaultMode
	 *            The defaultMode for this RateManager.
	 * @param mac
	 *            The Mac, which creates this RateManager.
	 */
	public IdealRateManager(final List<WifiMode> modes,
			final WifiMode defaultMode, final Ieee80211AdHocMac mac) {
		super(modes, defaultMode, mac);
		for (final WifiMode mode : modes) {
			final double snrThreshold = mac.getWifiTopologyView()
					.getErrorRateModel()
					.calculateSnrThreshold(mode, berThreshold);
			this.snrThresholds.put(mode, snrThreshold);
			this.addBasicMode(mode);
		}
	}

	@Override
	protected void doReportRtsFailed(final IWifiRemoteStation station) {
		// nothing
	}

	@Override
	protected void doReportDataFailed(final IWifiRemoteStation station) {
		// nothing
	}

	@Override
	protected void doReportRtsOk(final IWifiRemoteStation station,
			final double ctsSnr, final WifiMode ctsMode) {
		final IdealRemoteStation s = (IdealRemoteStation) station;
		s.setLastSnr(ctsSnr);
	}

	@Override
	protected void doReportDataOk(final IWifiRemoteStation station,
			final double ackSnr, final WifiMode ackMode) {
		final IdealRemoteStation s = (IdealRemoteStation) station;
		s.setLastSnr(ackSnr);
	}

	@Override
	protected void doReportFinalRtsFailed(final IWifiRemoteStation station) {
		// nothing
	}

	@Override
	protected void doReportFinalDataFailed(final IWifiRemoteStation station) {
		// nothing
	}

	@Override
	protected void doReportRxOk(final IWifiRemoteStation station,
			final double rxSnr, final WifiMode txMode) {
		// nothing
	}

	@Override
	protected IWifiRemoteStation doCreateWifiRemoteStation(
			final MacAddress address) {
		return new IdealRemoteStation(address);
	}

	@Override
	protected WifiMode doGetUnicastDataMode(final IWifiRemoteStation station) {
		double maxThreshold = 0.0;
		final double snr = ((IdealRemoteStation) station).getLastSnr();
		WifiMode maxMode = getDefaultMode();
		for (final WifiMode mode : getModes()) {
			final double threshold = snrThresholds.get(mode);
			if (threshold > maxThreshold && threshold < snr) {
				maxThreshold = threshold;
				maxMode = mode;
			}
		}
		return maxMode;
	}

	@Override
	protected WifiMode doGetRtsMode(final IWifiRemoteStation station) {
		double maxThreshold = 0.0;
		final double snr = ((IdealRemoteStation) station).getLastSnr();
		WifiMode maxMode = getDefaultMode();
		// search within basicModes
		for (final WifiMode mode : getBasicModes()) {
			final double threshold = snrThresholds.get(mode);
			if (threshold > maxThreshold && threshold < snr) {
				maxThreshold = threshold;
				maxMode = mode;
			}
		}
		return maxMode;
	}

}
