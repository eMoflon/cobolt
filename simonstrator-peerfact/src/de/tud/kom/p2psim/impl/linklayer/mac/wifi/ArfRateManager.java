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

import java.util.List;

import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;

/**
 * 
 * This class is based on NS3 (src/wifi/model/arf-wifi-manager.cc) by Mathieu
 * Lacage <mathieu.lacage@sophia.inria.fr>.
 * 
 * @version 1.0, 02.04.2013
 */
public class ArfRateManager extends AbstractRateManager {

	/**
	 * The 'timer' threshold in the ARF algorithm.
	 */
	private final static int TIMER_THRESHOLD = 15;

	/**
	 * The minimum number of successfull transmissions to try a new rate.
	 */
	private final static int SUCCESS_THRESHOLD = 10;

	public ArfRateManager(List<WifiMode> modes, WifiMode defaultMode,
			Ieee80211AdHocMac mac) {
		super(modes, defaultMode, mac);
	}

	@Override
	protected void doReportRtsFailed(IWifiRemoteStation station) {
		// Nothing to do
	}

	@Override
	protected void doReportDataFailed(IWifiRemoteStation station) {
		ArfRemoteStation aStation = (ArfRemoteStation) station;
		aStation.timer++;
		aStation.retry++;
		aStation.success = 0;

		if (aStation.recovery) {
			if (aStation.retry == 1) {
				// need recovery fallback
				if (aStation.rate != 0) {
					aStation.rate--;
				}
			}
			aStation.timer = 0;
		} else {
			if (((aStation.retry - 1) % 2) == 1) {
				// need normal fallback
				if (aStation.rate != 0) {
					aStation.rate--;
				}
			}
			if (aStation.retry >= 2) {
				aStation.timer = 0;
			}
		}
	}

	@Override
	protected void doReportRtsOk(IWifiRemoteStation station, double ctsSnr,
			WifiMode ctsMode) {
		// Nothing to do
	}

	@Override
	protected void doReportDataOk(IWifiRemoteStation station, double ackSnr,
			WifiMode ackMode) {
		ArfRemoteStation aStation = (ArfRemoteStation) station;
		aStation.timer++;
		aStation.success++;
		aStation.recovery = false;
		aStation.retry = 0;
		if ((aStation.success == SUCCESS_THRESHOLD || aStation.timer == TIMER_THRESHOLD)
				&& (aStation.rate < (getModes().size() - 1))) {
			aStation.rate++;
			aStation.timer = 0;
			aStation.success = 0;
			aStation.recovery = true;
		}
	}

	@Override
	protected void doReportFinalRtsFailed(IWifiRemoteStation station) {
		// Nothing to do
	}

	@Override
	protected void doReportFinalDataFailed(IWifiRemoteStation station) {
		// Nothing to do
	}

	@Override
	protected void doReportRxOk(IWifiRemoteStation station, double rxSnr,
			WifiMode txMode) {
		// Nothing to do
	}

	@Override
	protected IWifiRemoteStation doCreateWifiRemoteStation(MacAddress address) {
		ArfRemoteStation station = new ArfRemoteStation(address);
		return station;
	}

	@Override
	protected WifiMode doGetUnicastDataMode(IWifiRemoteStation station) {
		ArfRemoteStation aStation = (ArfRemoteStation) station;
		return getMode(aStation.rate);
	}

	@Override
	protected WifiMode doGetRtsMode(IWifiRemoteStation station) {
		WifiMode dataMode = doGetUnicastDataMode(station);
		WifiMode mode = getDefaultMode();

		// search a mode, which is not bigger as dataMode and in BasicMode.
		for (WifiMode bMode : getBasicModes()) {
			if (bMode.compareTo(dataMode) <= 0 && bMode.compareTo(mode) > 0) {
				mode = bMode;
			}
		}
		return mode;
	}

	private class ArfRemoteStation implements IWifiRemoteStation {
		private MacAddress address;

		public int timer = 0;

		public int success = 0;

		public boolean recovery = false;

		public int retry = 0;

		public int rate = 0;

		public ArfRemoteStation(MacAddress address) {
			this.address = address;
		}

		@Override
		public MacAddress getMacAddress() {
			return address;
		}

	}
}
