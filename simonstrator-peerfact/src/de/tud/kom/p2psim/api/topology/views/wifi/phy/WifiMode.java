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

/**
 * This class contains the WifiMode with all needed information about the Mode.
 * It is {@link Comparable} over the dataRate.
 * 
 * 
 * This class based on NS3 (src/wifi/model/wifi-mode.cc and
 * src/wifi/model/wifi-mode.h) by Mathieu Lacage
 * <mathieu.lacage@sophia.inria.fr> further extended by Christoph Muenker
 * 
 * @author Christoph Muenker
 * @version 1.0, 14.08.2012
 */
public class WifiMode implements Comparable<WifiMode> {

	/**
	 * This enumeration defines the modulation classes per IEEE 802.11-2012,
	 * Section 9.7.8, Table 9-4.
	 */
	public enum WifiModulationClass {
		/**
		 * Modulation class unknown or unspecified. A WifiMode with this
		 * WifiModulationClass has not been properly initialized.
		 */
		WIFI_MOD_CLASS_UNKNOWN,
		/** Infrared (IR) (Clause 15) */
		WIFI_MOD_CLASS_IR,
		/** Frequency-hopping spread spectrum (FHSS) PHY (Clause 14) */
		WIFI_MOD_CLASS_FHSS,
		/** DSSS PHY (Clause 16) and HR/DSSS PHY (Clause 17) */
		WIFI_MOD_CLASS_DSSS,
		/** ERP-PBCC PHY (19.6) */
		WIFI_MOD_CLASS_ERP_PBCC,
		/**
		 * DSSS-OFDM PHY (19.7) <br>
		 * The use of the DSSS-OFDM option is deprecated, and this option may be
		 * removed in a later revision of the standard.
		 * 
		 * */
		WIFI_MOD_CLASS_DSSS_OFDM,
		/** ERP-OFDM PHY (19.5) */
		WIFI_MOD_CLASS_ERP_OFDM,
		/** OFDM PHY (Clause 18) */
		WIFI_MOD_CLASS_OFDM,
		/** HT PHY (Clause 20) */
		WIFI_MOD_CLASS_HT
	}

	/**
	 * The Coding Rate for OFDM. If the Modulation has no coding rate, then will
	 * be used WIFI_CODE_RATE_UNDEFINED.
	 */
	public enum WifiCodeRate {
		/** No explicit coding (for example, DSSS rates) */
		WIFI_CODE_RATE_UNDEFINED(1),
		/** Rate 3/4 */
		WIFI_CODE_RATE_3_4(3.0 / 4.0),
		/** Rate 2/3 */
		WIFI_CODE_RATE_2_3(2.0 / 3.0),
		/** Rate 1/2 */
		WIFI_CODE_RATE_1_2(1.0 / 2.0),
		/** Rate 5/6 (for HT) */
		WIFI_CODE_RATE_5_6(5.0 / 6.0);

		private double value;

		WifiCodeRate(double value) {
			this.value = value;
		}

		public double getValue() {
			return value;
		}
	}

	private int bandwidth;

	private String uniqueUid;

	private int dataRate;

	private int phyRate;

	private WifiModulationClass modClass;

	private int constellationSize;

	private WifiCodeRate codingRate;

	private boolean isMandatory;

	public WifiMode(String uniqueUid, WifiModulationClass modClass,
			boolean isMandatory, int bandwidth, int dataRate,
			WifiCodeRate codingRate, int constellationSize) {
		this.bandwidth = bandwidth;
		this.uniqueUid = uniqueUid;
		this.dataRate = dataRate;
		this.modClass = modClass;
		this.constellationSize = constellationSize;
		this.codingRate = codingRate;
		this.isMandatory = isMandatory;

		// calculate the phy Rate
		this.phyRate = (int) (dataRate / codingRate.getValue());

		// check for the Coding rate is undefined for the modClass DSSS. For
		// other modClasses it should be not undefined.
		assert isDSSS(modClass) == isCodingRateUndefined(codingRate) : "Error in Creation of "
				+ uniqueUid + ". only for DSSS is the coding Rate undefined.";

	}

	private boolean isCodingRateUndefined(WifiCodeRate codingRate) {
		return codingRate.equals(WifiCodeRate.WIFI_CODE_RATE_UNDEFINED);
	}

	private boolean isDSSS(WifiModulationClass modClass) {
		return modClass.equals(WifiModulationClass.WIFI_MOD_CLASS_DSSS);
	}

	/**
	 * @return the number of Hz used by this signal
	 */
	public int getBandwidth() {
		return bandwidth;
	}

	/**
	 * @return An human-readable representation of this WifiMode.
	 */
	public String getUniqueUid() {
		return uniqueUid;
	}

	/**
	 * @return the data bit rate of this signal.
	 */
	public int getDataRate() {
		return dataRate;
	}

	/**
	 * @return the physical bit rate of this signal.
	 */
	public int getPhyRate() {
		return phyRate;
	}

	/**
	 * @return the {@link WifiModulationClass} to which this WifiMode belongs.
	 */
	public WifiModulationClass getModClass() {
		return modClass;
	}

	/**
	 * @return the size of the modulation constellation.
	 */
	public int getConstellationSize() {
		return constellationSize;
	}

	/**
	 * @return the {@link WifiCodeRate} for the transmission.
	 */
	public WifiCodeRate getCodingRate() {
		return codingRate;
	}

	/**
	 * @return <code>true</code> if this mode is a mandatory mode, false
	 *         otherwise.
	 */
	public boolean isMandatory() {
		return isMandatory;
	}

	@Override
	public String toString() {
		return getUniqueUid();
	}

	@Override
	public int compareTo(WifiMode o) {
		int oRate = o.getDataRate();
		int tRate = this.getDataRate();
		return (tRate < oRate ? -1 : (tRate == oRate ? 0 : 1));
	}
}
