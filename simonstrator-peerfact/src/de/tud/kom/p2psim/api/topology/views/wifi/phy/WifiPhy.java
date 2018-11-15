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

package de.tud.kom.p2psim.api.topology.views.wifi.phy;

import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode.WifiCodeRate;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode.WifiModulationClass;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.AbstractRateManager;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.DcfManager;

/**
 * This class defines the possible {@link WifiMode}s. Additionally it defines
 * the {@link Standard_802_11} with all needed Information for the DCF.<br>
 * Hence it has methods to calculate the duration of a packet. This is in WiFi
 * tricky, because the preamble must be added and the packet must be filled up
 * to a multiple of the coding scheme.
 * 
 * <p>
 * 
 * This class based on NS3 (src/wifi/model/wifi-phy.cc).
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public abstract class WifiPhy {

	public static long _5000MHZ = 5000000000l;

	public static long _2407MHZ = 2407000000l;

	/**
	 * This contains the parameter for the {@link DcfManager} and the
	 * {@link AbstractRateManager} for the standard.
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 28.02.2013
	 */
	public enum Standard_802_11 {
		a(_5000MHZ),

		b(_2407MHZ),

		g(_2407MHZ);

		static {
			a.setCwMin(15);
			a.setCwMax(1023);
			a.setSifs(16);
			a.setSlotTime(9);
			a.setDifs(16 + 2 * 9);
			a.setEifs(16 + 44 + 34);
			a.setCtsTimeout(16 + 44 + 9);
			a.setDefaultMode(getOfdmRate6Mbps());

			b.setCwMin(31);
			b.setCwMax(1023);
			b.setSifs(10);
			b.setSlotTime(20);
			b.setDifs(10 + 2 * 20);
			b.setEifs(10 + 304 + 50);
			b.setCtsTimeout(10 + 304 + 20);
			b.setDefaultMode(getDsssRate1Mbps());

			g.setCwMin(15);
			g.setCwMax(1023);
			g.setSifs(10);
			g.setSlotTime(20); // Short and Long?
			g.setDifs(10 + 2 * 20);
			g.setEifs(10 + 304 + 50);
			g.setCtsTimeout(10 + 304 + 20);
			g.setDefaultMode(getDsssRate1Mbps());

			a.addWifiMode(WifiPhy.getOfdmRate6Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate9Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate12Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate18Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate24Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate36Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate48Mbps());
			a.addWifiMode(WifiPhy.getOfdmRate54Mbps());

			b.addWifiMode(WifiPhy.getDsssRate1Mbps());
			b.addWifiMode(WifiPhy.getDsssRate2Mbps());
			b.addWifiMode(WifiPhy.getDsssRate5_5Mbps());
			b.addWifiMode(WifiPhy.getDsssRate11Mbps());

			g.addWifiMode(WifiPhy.getDsssRate1Mbps());
			g.addWifiMode(WifiPhy.getDsssRate2Mbps());
			g.addWifiMode(WifiPhy.getDsssRate5_5Mbps());
			g.addWifiMode(WifiPhy.getDsssRate11Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate6Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate9Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate12Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate18Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate24Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate36Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate48Mbps());
			g.addWifiMode(WifiPhy.getErpOfdmRate54Mbps());

		}

		private long basicFrequency;

		private List<WifiMode> modes = new Vector<WifiMode>();

		private WifiMode defaultMode;

		private long ctsTimeout;

		private long slotTime;

		private long eifs;

		private long difs;

		private long sifs;

		private int cwMax;

		private int cwMin;

		private Standard_802_11(long frequency) {
			this.basicFrequency = frequency;
		}

		public long getBasicFrequency() {
			return basicFrequency;
		}

		public WifiMode getDefaultMode() {
			return defaultMode;
		}

		public List<WifiMode> getWifiModes() {
			return modes;
		}

		public long getCtsTimeout() {
			return ctsTimeout;
		}

		public long getSlotTime() {
			return slotTime;
		}

		public long getEifs() {
			return eifs;
		}

		public long getDifs() {
			return difs;
		}

		public long getSifs() {
			return sifs;
		}

		public int getCwMax() {
			return cwMax;
		}

		public int getCwMin() {
			return cwMin;
		}

		private void setCtsTimeout(long ctsTimeout) {
			this.ctsTimeout = ctsTimeout;
		}

		private void setSlotTime(long slotTime) {
			this.slotTime = slotTime;
		}

		private void setEifs(long eifs) {
			this.eifs = eifs;
		}

		private void setDifs(long difs) {
			this.difs = difs;
		}

		private void setSifs(long sifs) {
			this.sifs = sifs;
		}

		private void setCwMax(int cwMax) {
			this.cwMax = cwMax;
		}

		private void setCwMin(int cwMin) {
			this.cwMin = cwMin;
		}

		private void setDefaultMode(WifiMode defaultMode) {
			this.defaultMode = defaultMode;
		}

		private void addWifiMode(WifiMode mode) {
			this.modes.add(mode);
		}
	}

	/**
	 * The type of preamble to be used by an IEEE 802.11 transmission
	 */
	public enum WifiPreamble {

		/**
		 * Long Preamble
		 */
		WIFI_PREAMBLE_LONG,
		/**
		 * Short Preamble
		 */
		WIFI_PREAMBLE_SHORT;
	}

	/**
	 * Gets the PLCP Header Mode.
	 * 
	 * Copied from NS3 (src/wifi/model/wifi-phy.cc).
	 * 
	 * @param payloadMode
	 *            The payload {@link WifiMode}
	 * @param preamble
	 *            The used preamble (normal the WIFI_PREAMBLE_LONG).
	 * @return The {@link WifiMode} for the PLCP Header.
	 */
	public static WifiMode getPlcpHeaderMode(WifiMode payloadMode,
			WifiPreamble preamble) {
		switch (payloadMode.getModClass()) {
		case WIFI_MOD_CLASS_OFDM: {
			switch (payloadMode.getBandwidth()) {
			case 5000000:
				return getOfdmRate1_5MbpsBW5MHz();
			case 10000000:
				return getOfdmRate3MbpsBW10MHz();
			default:
				// IEEE Std 802.11-2007, 17.3.2
				// actually this is only the first part of the PlcpHeader,
				// because the last 16 bits of the PlcpHeader are using the
				// same mode of the payload
				return getOfdmRate6Mbps();
			}
		}

		case WIFI_MOD_CLASS_ERP_OFDM:
			return getErpOfdmRate6Mbps();

		case WIFI_MOD_CLASS_DSSS:
			if (preamble == WifiPreamble.WIFI_PREAMBLE_LONG) {
				// IEEE Std 802.11-2007, sections 15.2.3 and 18.2.2.1
				return getDsssRate1Mbps();
			} else // WIFI_PREAMBLE_SHORT
			{
				// IEEE Std 802.11-2007, section 18.2.2.2
				return getDsssRate2Mbps();
			}

		default:
			throw new AssertionError("Unsupported Mod");
		}
	}

	/**
	 * Gets the PLCP Header duration in microseconds.
	 * 
	 * Copied from NS3 (src/wifi/model/wifi-phy.cc).
	 * 
	 * @param payloadMode
	 *            The payload {@link WifiMode}
	 * @param preamble
	 *            The used preamble (normal the WIFI_PREAMBLE_LONG).
	 * @return The PLCP Header duration in microseconds.
	 */
	public static long getPlcpHeaderDuration(WifiMode payloadMode,
			WifiPreamble preamble) {
		switch (payloadMode.getModClass()) {
		case WIFI_MOD_CLASS_OFDM: {
			switch (payloadMode.getBandwidth()) {
			case 20000000:
			default:
				// IEEE Std 802.11-2007, section 17.3.3 and figure 17-4
				// also section 17.3.2.3, table 17-4
				// We return the duration of the SIGNAL field only, since the
				// SERVICE field (which strictly speaking belongs to the PLCP
				// header, see section 17.3.2 and figure 17-1) is sent using the
				// payload mode.
				return 4;
			case 10000000:
				// IEEE Std 802.11-2007, section 17.3.2.3, table 17-4
				return 8;
			case 5000000:
				// IEEE Std 802.11-2007, section 17.3.2.3, table 17-4
				return 16;
			}
		}

		case WIFI_MOD_CLASS_ERP_OFDM:
			return 16;

		case WIFI_MOD_CLASS_DSSS:
			if (preamble == WifiPreamble.WIFI_PREAMBLE_SHORT) {
				// IEEE Std 802.11-2007, section 18.2.2.2 and figure 18-2
				return 24;
			} else // WIFI_PREAMBLE_LONG
			{
				// IEEE Std 802.11-2007, sections 18.2.2.1 and figure 18-1
				return 48;
			}

		default:
			throw new AssertionError("Unsupported Modulation");
		}
	}

	/**
	 * Gets the PLCP Preamble Duration in microseconds.
	 * 
	 * Copied from NS3 (src/wifi/model/wifi-phy.cc).
	 * 
	 * @param payloadMode
	 *            The payload {@link WifiMode}
	 * @param preamble
	 *            The used preamble (normal the WIFI_PREAMBLE_LONG).
	 * @return The PLCP preabmle duration in microseconds.
	 */
	public static long getPlcpPreambleDuration(WifiMode payloadMode,
			WifiPreamble preamble) {
		switch (payloadMode.getModClass()) {
		case WIFI_MOD_CLASS_OFDM: {
			switch (payloadMode.getBandwidth()) {
			case 20000000:
			default:
				// IEEE Std 802.11-2007, section 17.3.3, figure 17-4
				// also section 17.3.2.3, table 17-4
				return 16;
			case 10000000:
				// IEEE Std 802.11-2007, section 17.3.3, table 17-4
				// also section 17.3.2.3, table 17-4
				return 32;
			case 5000000:
				// IEEE Std 802.11-2007, section 17.3.3
				// also section 17.3.2.3, table 17-4
				return 64;
			}
		}

		case WIFI_MOD_CLASS_ERP_OFDM:
			return 4;

		case WIFI_MOD_CLASS_DSSS:
			if (preamble == WifiPreamble.WIFI_PREAMBLE_SHORT) {
				// IEEE Std 802.11-2007, section 18.2.2.2 and figure 18-2
				return 72;
			} else // WIFI_PREAMBLE_LONG
			{
				// IEEE Std 802.11-2007, sections 18.2.2.1 and figure 18-1
				return 144;
			}

		default:
			throw new AssertionError("Unsupported Modulation");
		}
	}

	/**
	 * Gets the Payload Duration for the size and the used payloadMode.
	 * 
	 * Copied from NS3 (src/wifi/model/wifi-phy.cc).
	 * 
	 * @param size
	 *            The size of the payload.
	 * @param payloadMode
	 *            The payload mode.
	 * @return The payload duration in mircoseconds.
	 */
	public static long getPayloadDuration(int size, WifiMode payloadMode) {

		switch (payloadMode.getModClass()) {
		case WIFI_MOD_CLASS_OFDM:
		case WIFI_MOD_CLASS_ERP_OFDM: {
			// IEEE Std 802.11-2007, section 17.3.2.3, table 17-4
			// corresponds to T_{SYM} in the table
			int symbolDurationUs;

			switch (payloadMode.getBandwidth()) {
			case 20000000:
			default:
				symbolDurationUs = 4;
				break;
			case 10000000:
				symbolDurationUs = 8;
				break;
			case 5000000:
				symbolDurationUs = 16;
				break;
			}

			// IEEE Std 802.11-2007, section 17.3.2.2, table 17-3
			// corresponds to N_{DBPS} in the table
			double numDataBitsPerSymbol = payloadMode.getDataRate()
					* symbolDurationUs / 1e6;

			// IEEE Std 802.11-2007, section 17.3.5.3, equation (17-11)
			int numSymbols = (int) Math.rint(Math.ceil((16 + size * 8.0 + 6.0)
					/ numDataBitsPerSymbol));

			// Add signal extension for ERP PHY
			if (payloadMode.getModClass() == WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM) {
				return numSymbols * symbolDurationUs + 6;
			} else {
				return numSymbols * symbolDurationUs;
			}
		}

		case WIFI_MOD_CLASS_DSSS:
			// IEEE Std 802.11-2007, section 18.2.3.5
			return (long) Math.rint(Math.ceil((size * 8.0)
					/ (payloadMode.getDataRate() / 1.0e6)));

		default:
			throw new AssertionError("Unsupported Modulation");
		}
	}

	/**
	 * Calculates the complete duration of the transmission. This includes the
	 * preamble duration, the Header duration and the payload duration.
	 * 
	 * @param size
	 *            The size of the payload.
	 * @param payloadMode
	 *            The payload {@link WifiMode}
	 * @param preamble
	 *            The used preamble (normal the WIFI_PREAMBLE_LONG).
	 * @return The duration of the transmission in microseconds.
	 */
	public static long calculateTxDuration(int size, WifiMode payloadMode,
			WifiPreamble preamble) {
		long duration = getPlcpPreambleDuration(payloadMode, preamble)
				+ getPlcpHeaderDuration(payloadMode, preamble)
				+ getPayloadDuration(size, payloadMode);
		return duration;
	}

	/**
	 * Clause 15 rates (DSSS)
	 */

	private static final WifiMode dsssRate1Mbps = new WifiMode("DsssRate1Mbps",
			WifiModulationClass.WIFI_MOD_CLASS_DSSS, true, 22000000, 1000000,
			WifiCodeRate.WIFI_CODE_RATE_UNDEFINED, 2);

	private static final WifiMode dsssRate2Mbps = new WifiMode("DsssRate2Mbps",
			WifiModulationClass.WIFI_MOD_CLASS_DSSS, true, 22000000, 2000000,
			WifiCodeRate.WIFI_CODE_RATE_UNDEFINED, 4);

	/**
	 * Clause 18 rates (HR/DSSS)
	 */
	private static final WifiMode dsssRate5_5Mbps = new WifiMode(
			"DsssRate5_5Mbps", WifiModulationClass.WIFI_MOD_CLASS_DSSS, true,
			22000000, 5500000, WifiCodeRate.WIFI_CODE_RATE_UNDEFINED, 4);

	private static final WifiMode dsssRate11Mbps = new WifiMode(
			"DsssRate11Mbps", WifiModulationClass.WIFI_MOD_CLASS_DSSS, true,
			22000000, 11000000, WifiCodeRate.WIFI_CODE_RATE_UNDEFINED, 4);

	/**
	 * Clause 19.5 rates (ERP-OFDM)
	 */
	private static final WifiMode erpOfdmRate6Mbps = new WifiMode(
			"ErpOfdmRate6Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			true, 20000000, 6000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 2);

	private static final WifiMode erpOfdmRate9Mbps = new WifiMode(
			"ErpOfdmRate9Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			false, 20000000, 9000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 2);

	private static final WifiMode erpOfdmRate12Mbps = new WifiMode(
			"ErpOfdmRate12Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			true, 20000000, 12000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 4);

	private static final WifiMode erpOfdmRate18Mbps = new WifiMode(
			"ErpOfdmRate18Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			false, 20000000, 18000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 4);

	private static final WifiMode erpOfdmRate24Mbps = new WifiMode(
			"ErpOfdmRate24Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			true, 20000000, 24000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 16);

	private static final WifiMode erpOfdmRate36Mbps = new WifiMode(
			"ErpOfdmRate36Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			false, 20000000, 36000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 16);

	private static final WifiMode erpOfdmRate48Mbps = new WifiMode(
			"ErpOfdmRate48Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			false, 20000000, 48000000, WifiCodeRate.WIFI_CODE_RATE_2_3, 64);

	private static final WifiMode erpOfdmRate54Mbps = new WifiMode(
			"ErpOfdmRate54Mbps", WifiModulationClass.WIFI_MOD_CLASS_ERP_OFDM,
			false, 20000000, 54000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 64);

	/**
	 * Clause 17 rates (OFDM)
	 */
	private static final WifiMode ofdmRate6Mbps = new WifiMode("OfdmRate6Mbps",
			WifiModulationClass.WIFI_MOD_CLASS_OFDM, true, 20000000, 6000000,
			WifiCodeRate.WIFI_CODE_RATE_1_2, 2);

	private static final WifiMode ofdmRate9Mbps = new WifiMode("OfdmRate9Mbps",
			WifiModulationClass.WIFI_MOD_CLASS_OFDM, false, 20000000, 9000000,
			WifiCodeRate.WIFI_CODE_RATE_3_4, 2);

	private static final WifiMode ofdmRate12Mbps = new WifiMode(
			"OfdmRate12Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, true,
			20000000, 12000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 4);

	private static final WifiMode ofdmRate18Mbps = new WifiMode(
			"OfdmRate18Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, false,
			20000000, 18000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 4);

	private static final WifiMode ofdmRate24Mbps = new WifiMode(
			"OfdmRate24Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, true,
			20000000, 24000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 16);

	private static final WifiMode ofdmRate36Mbps = new WifiMode(
			"OfdmRate36Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, false,
			20000000, 36000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 16);

	private static final WifiMode ofdmRate48Mbps = new WifiMode(
			"OfdmRate48Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, false,
			20000000, 48000000, WifiCodeRate.WIFI_CODE_RATE_2_3, 64);

	private static final WifiMode ofdmRate54Mbps = new WifiMode(
			"OfdmRate54Mbps", WifiModulationClass.WIFI_MOD_CLASS_OFDM, false,
			20000000, 54000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 64);

	/* 10 MHz channel rates */
	private static final WifiMode ofdmRate3MbpsBW10MHz = new WifiMode(
			"OfdmRate3MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 10000000, 3000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 2);

	private static final WifiMode ofdmRate4_5MbpsBW10MHz = new WifiMode(
			"OfdmRate4_5MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 10000000, 4500000, WifiCodeRate.WIFI_CODE_RATE_3_4, 2);

	private static final WifiMode ofdmRate6MbpsBW10MHz = new WifiMode(
			"OfdmRate6MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 10000000, 6000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 4);

	private static final WifiMode ofdmRate9MbpsBW10MHz = new WifiMode(
			"OfdmRate9MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 10000000, 9000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 4);

	private static final WifiMode ofdmRate12MbpsBW10MHz = new WifiMode(
			"OfdmRate12MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 10000000, 12000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 16);

	private static final WifiMode ofdmRate18MbpsBW10MHz = new WifiMode(
			"OfdmRate18MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 10000000, 18000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 16);

	private static final WifiMode ofdmRate24MbpsBW10MHz = new WifiMode(
			"OfdmRate24MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 10000000, 24000000, WifiCodeRate.WIFI_CODE_RATE_2_3, 64);

	private static final WifiMode ofdmRate27MbpsBW10MHz = new WifiMode(
			"OfdmRate27MbpsBW10MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 10000000, 27000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 64);

	/* 5 MHz channel rates */
	private static final WifiMode ofdmRate1_5MbpsBW5MHz = new WifiMode(
			"OfdmRate1_5MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 5000000, 1500000, WifiCodeRate.WIFI_CODE_RATE_1_2, 2);

	private static final WifiMode ofdmRate2_25MbpsBW5MHz = new WifiMode(
			"OfdmRate2_25MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 5000000, 2250000, WifiCodeRate.WIFI_CODE_RATE_3_4, 2);

	private static final WifiMode ofdmRate3MbpsBW5MHz = new WifiMode(
			"OfdmRate3MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 5000000, 3000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 4);

	private static final WifiMode ofdmRate4_5MbpsBW5MHz = new WifiMode(
			"OfdmRate4_5MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 5000000, 4500000, WifiCodeRate.WIFI_CODE_RATE_3_4, 4);

	private static final WifiMode ofdmRate6MbpsBW5MHz = new WifiMode(
			"OfdmRate6MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			true, 5000000, 6000000, WifiCodeRate.WIFI_CODE_RATE_1_2, 16);

	private static final WifiMode ofdmRate9MbpsBW5MHz = new WifiMode(
			"OfdmRate9MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 5000000, 9000000, WifiCodeRate.WIFI_CODE_RATE_3_4, 16);

	private static final WifiMode ofdmRate12MbpsBW5MHz = new WifiMode(
			"OfdmRate12MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 5000000, 12000000, WifiCodeRate.WIFI_CODE_RATE_2_3, 64);

	private static final WifiMode ofdmRate13_5MbpsBW5MHz = new WifiMode(
			"OfdmRate13_5MbpsBW5MHz", WifiModulationClass.WIFI_MOD_CLASS_OFDM,
			false, 5000000, 13500000, WifiCodeRate.WIFI_CODE_RATE_3_4, 64);

	/**
	 * Clause 15 rates (DSSS)
	 */

	public static WifiMode getDsssRate1Mbps() {

		return dsssRate1Mbps;
	}

	public static WifiMode getDsssRate2Mbps() {

		return dsssRate2Mbps;
	}

	/**
	 * Clause 18 rates (HR/DSSS)
	 */
	public static WifiMode getDsssRate5_5Mbps() {

		return dsssRate5_5Mbps;
	}

	public static WifiMode getDsssRate11Mbps() {

		return dsssRate11Mbps;
	}

	/**
	 * Clause 19.5 rates (ERP-OFDM)
	 */
	public static WifiMode getErpOfdmRate6Mbps() {

		return erpOfdmRate6Mbps;
	}

	public static WifiMode getErpOfdmRate9Mbps() {

		return erpOfdmRate9Mbps;
	}

	public static WifiMode getErpOfdmRate12Mbps() {

		return erpOfdmRate12Mbps;
	}

	public static WifiMode getErpOfdmRate18Mbps() {

		return erpOfdmRate18Mbps;
	}

	public static WifiMode getErpOfdmRate24Mbps() {

		return erpOfdmRate24Mbps;
	}

	public static WifiMode getErpOfdmRate36Mbps() {

		return erpOfdmRate36Mbps;
	}

	public static WifiMode getErpOfdmRate48Mbps() {

		return erpOfdmRate48Mbps;
	}

	public static WifiMode getErpOfdmRate54Mbps() {

		return erpOfdmRate54Mbps;
	}

	/**
	 * Clause 17 rates (OFDM)
	 */
	public static WifiMode getOfdmRate6Mbps() {

		return ofdmRate6Mbps;
	}

	public static WifiMode getOfdmRate9Mbps() {

		return ofdmRate9Mbps;
	}

	public static WifiMode getOfdmRate12Mbps() {

		return ofdmRate12Mbps;
	}

	public static WifiMode getOfdmRate18Mbps() {

		return ofdmRate18Mbps;
	}

	public static WifiMode getOfdmRate24Mbps() {

		return ofdmRate24Mbps;
	}

	public static WifiMode getOfdmRate36Mbps() {

		return ofdmRate36Mbps;
	}

	public static WifiMode getOfdmRate48Mbps() {

		return ofdmRate48Mbps;
	}

	public static WifiMode getOfdmRate54Mbps() {

		return ofdmRate54Mbps;
	}

	/* 10 MHz channel rates */
	public static WifiMode getOfdmRate3MbpsBW10MHz() {

		return ofdmRate3MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate4_5MbpsBW10MHz() {

		return ofdmRate4_5MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate6MbpsBW10MHz() {

		return ofdmRate6MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate9MbpsBW10MHz() {

		return ofdmRate9MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate12MbpsBW10MHz() {

		return ofdmRate12MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate18MbpsBW10MHz() {

		return ofdmRate18MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate24MbpsBW10MHz() {

		return ofdmRate24MbpsBW10MHz;
	}

	public static WifiMode getOfdmRate27MbpsBW10MHz() {

		return ofdmRate27MbpsBW10MHz;
	}

	/* 5 MHz channel rates */
	public static WifiMode getOfdmRate1_5MbpsBW5MHz() {

		return ofdmRate1_5MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate2_25MbpsBW5MHz() {

		return ofdmRate2_25MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate3MbpsBW5MHz() {

		return ofdmRate3MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate4_5MbpsBW5MHz() {

		return ofdmRate4_5MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate6MbpsBW5MHz() {

		return ofdmRate6MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate9MbpsBW5MHz() {

		return ofdmRate9MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate12MbpsBW5MHz() {

		return ofdmRate12MbpsBW5MHz;
	}

	public static WifiMode getOfdmRate13_5MbpsBW5MHz() {

		return ofdmRate13_5MbpsBW5MHz;
	}
}
