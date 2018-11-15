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

package de.tud.kom.p2psim.impl.topology.views.wifi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.ErrorRateModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.PropagationLossModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.Standard_802_11;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.WifiPreamble;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.Ieee80211AdHocMac;
import de.tud.kom.p2psim.impl.topology.views.RangedLink;
import de.tud.kom.p2psim.impl.topology.views.RangedTopologyView;
import de.tud.kom.p2psim.impl.topology.views.wifi.phy.InterferenceHelper;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Extends the RangedTopologyView for a second range, which contains all
 * neighbors, who can sense the signal.<br>
 *
 * In the first range, it is possible to communicate with the other station
 * (Link.isConnected() should be true). <br>
 * In the second range, it is only a signal on the medium, which cannot be read
 * from the other station, but the other station can be sense, that the medium
 * is busy. Rather, the second range can be called CS (Carrier Sense) Range.
 * <p>
 * Additionally this class is the interface to the {@link InterferenceHelper}.
 * This mean, that this class can add transfers and interferences to the
 * InterferenceHelper. On the other hand, this class can get the PER to a
 * packet.
 * <p>
 * This WifiTopology supported only one channel for all peers and one TxPower
 * for all peers.<br>
 * The extending for multiple channels, the structure of neighbors must be
 * changed and for every channel an own {@link InterferenceHelper}. For the
 * TxPower, to all MACs, must be stored the txPower and the resulting ranges.
 *
 *
 * TODO: Add Interface for Carrier Sensing
 *
 * @author Christoph Muenker
 * @version 1.0, 15.09.2012
 */
public class WifiTopologyView extends RangedTopologyView {

	/**
	 * Stores the radius of the carrier sense range, which is computed based on
	 * the values for the two parameters maxTxPowerDbm and csDbm.
	 */
	private double csRange;

	/**
	 * The SAT denotes the Signal Attention Threshold and represents the mean
	 * minimal signal power, which should be used as noise for the calculation
	 * of the SNR. In other words, a signal lower as this level can be ignored
	 * and does not influence, for instance, the cs-range.<br>
	 * The Signal Attention Threshold is not specified in ns-3 but comes from
	 * OMNET++ and is specified in ChannelControl.ned and set to -110dBm.
	 */
	private double satDbm = -110;

	/**
	 * Specifies the Signal Attention Threshold in Watt.
	 */
	private double satW = PropagationLossModel.dbmToW(satDbm);

	/**
	 * rxSensitivityDbm denotes the receive sensitivity of a device and
	 * specifies down to which value a Wi-Fi card can still read the signal from
	 * the medium. In ns-3, this value is represented by the attribute
	 * "EnergyDetectionThreshold" in the class yans-wifi-phy.cc and set to
	 * -96dBm.
	 */
	private double rxSensitivityDbm = -96;

	/**
	 * Specifies the receive sensitivity in Watt.
	 */
	private double rxSensitivityW = PropagationLossModel
			.dbmToW(rxSensitivityDbm);

	/**
	 * csDbm denotes the carrier sense sensitivity of a device and specifies
	 * down to which value a Wi-Fi card can still sense (but not read) a signal.
	 * If signals above this level are received, the card knows that the medium
	 * is busy. In ns-3, this value is represented by the attribute
	 * "CcaMode1Threshold" in the class yans-wifi-phy.cc and set to -99dBm.
	 */
	private double csDbm = -99;

	/**
	 * Specifies the carrier sense sensitivity in Watt.
	 */
	private double csW = PropagationLossModel.dbmToW(csDbm);

	/**
	 * Stores the frequency of the utilized IEEE802.11 standard.
	 */
	private long frequency = 0;

	/**
	 * Specifies the transmission power of a all Wi-Fi chips, which simulated in
	 * a simulation.
	 */
	private double maxTxPowerDbm = 0;

	private Map<MacAddress, List<MacAddress>> cachedCSNeighbors = new HashMap<MacAddress, List<MacAddress>>();

	private InterferenceHelper interferenceHelper;

	private Standard_802_11 standard_802_11;

	public WifiTopologyView(PhyType phy) {
		super(phy, 100.0);
		interferenceHelper = new InterferenceHelper(this);
	}

	@XMLConfigurableConstructor({ "phy" })
	public WifiTopologyView(String phy) {
		this(PhyType.WIFI);
		setPhy(phy);

	}

	/**
	 * Updates the carrier sense neighborhood and cache the neighborhood.
	 * Additionally it executes the super method.
	 *
	 * @param source
	 * @return
	 */
	@Override
	protected List<MacAddress> updateNeighborhood(MacAddress source) {

		List<MacAddress> csNeighbors = new ArrayList<MacAddress>();
		for (MacAddress neighbor : allMacAddresses) {
			if (!source.equals(neighbor)) {
				RangedLink link = getLinkBetween(source, neighbor);
				if (link.getNodeDistance() < csRange) {
					assert !neighbor.equals(source);
					csNeighbors.add(neighbor);
				}
			}
		}

		cachedCSNeighbors.put(source, csNeighbors);

		return super.updateNeighborhood(source);
	}

	/**
	 * Gets all Neighbors which are in the carrier sense neighborhood of the
	 * given {@link MacAddress}.
	 *
	 * @param source
	 *            The MacAddress
	 * @return The unmodifable list of carrier sense neighborhood to the given
	 *         {@link MacAddress}.
	 */
	public List<MacAddress> getCarrierSenseNeighbors(MacAddress source) {
		return Collections.unmodifiableList(getCachedCSNeighbors(source));
	}

	private List<MacAddress> getCachedCSNeighbors(MacAddress source) {
		// updates the cachedCSNeighbors, if it is outdated
		getNeighbors(source);
		return cachedCSNeighbors.get(source);
	}

	public InterferenceHelper getInterferenceHelper() {
		return interferenceHelper;
	}

	protected void setCSRange(double csRange) {
		this.csRange = csRange;
	}

	public double getCSRange() {
		return csRange;
	}

	public PropagationLossModel getPropagationLossModel() {
		return interferenceHelper.getLossModel();
	}

	public ErrorRateModel getErrorRateModel() {
		return interferenceHelper.getErrorModel();
	}

	/**
	 * Sets the new propagationLossModel in {@link InterferenceHelper},
	 * additionally it sets the frequency for the lossModel which is used.
	 * Further, it executes a update of the ranges.
	 *
	 * @param lossModel
	 *            The new lossModel.
	 */
	public void setPropagationLossModel(PropagationLossModel lossModel) {
		lossModel.setFrequency(frequency);
		interferenceHelper.setLossModel(lossModel);
		updateRanges();
	}

	/**
	 * Sets the new ErrorRateModel in {@link InterferenceHelper}.Further, it
	 * executes a update of the ranges.
	 *
	 * @param errorRateModel
	 *            The new errorRateModel.
	 */
	public void setErrorRateModel(ErrorRateModel errorRateModel) {
		interferenceHelper.setErrorModel(errorRateModel);
		updateRanges();
	}

	/**
	 * Add a new transfer to the {@link InterferenceHelper}. This mean, that
	 * this will be added as Interference and it is possible to get to the given
	 * message a Packet Error Rate (PER), to check for a drop.
	 *
	 * @param start
	 *            The start time in microseconds
	 * @param end
	 *            The end time in microseconds
	 * @param sourcePosition
	 *            The position of the source
	 * @param txPowerDbm
	 *            The transmission power in dBm
	 * @param id
	 *            The message which is the identifier for the calculation of the
	 *            PER
	 * @param dataMode
	 *            The {@link WifiMode} for the data
	 * @param preamble
	 *            The {@link WifiMode} of the preamble
	 * @param host
	 *            The host which starts this transfer.
	 * @param sourceAddress
	 *            The {@link MacAddress} of the source, which stats this
	 *            transfer
	 */
	public void addTransfer(long start, long end, Location sourcePosition,
			double txPowerDbm, Message id, WifiMode dataMode,
			WifiPreamble preamble, SimHost host, MacAddress sourceAddress) {
		interferenceHelper.addTransfer(start, end, sourcePosition, txPowerDbm,
				id, dataMode, preamble, host, sourceAddress);
	}

	/**
	 * Add a new Interference to the {@link InterferenceHelper}. This should be
	 * used, if you want to simulate a transfer, but you are not interested
	 * about the Packet Error Rate of this message.
	 *
	 * @param start
	 *            The start time in microseconds
	 *
	 * @param end
	 *            The end time in microseconds
	 * @param sourcePosition
	 *            The position of the source
	 * @param txPowerDbm
	 *            The transmission power in dBm
	 * @param dataMode
	 *            The {@link WifiMode} of this interference
	 * @param sourceAddress
	 *            The sourceAddress of this interference (It will be used the
	 *            neighborhood of this peer, to inform about carrier sensing).
	 */
	public void addInterference(long start, long end, Location sourcePosition,
			double txPowerDbm, WifiMode dataMode, MacAddress sourceAddress) {
		interferenceHelper.addInterference(start, end, sourcePosition,
				txPowerDbm, dataMode, sourceAddress);
	}

	/**
	 * Calculates the Packer Error Rate (PER) for the message at the given
	 * Position.
	 *
	 * @param msg
	 *            The message which was added as interference (or rather as ID)
	 *            with
	 *            {@link WifiTopologyView#addInterference(long, long, Location, double, WifiMode, MacAddress)}
	 *            to the {@link InterferenceHelper}.
	 * @param pos
	 *            The position for that the PER should be calculated for this
	 *            message.
	 * @return The Packet Error Rate (PER) for the message at the given
	 *         Position.
	 */
	public double calculatePer(Message msg, Location pos) {
		return interferenceHelper.calculatePer(msg, pos);
	}

	/**
	 * Gets all {@link MacAddress} which are known by this {@link TopologyView}
	 *
	 * @return An unmodifiable list of all known mac addresses
	 */
	public List<MacAddress> getAllMacAddresses() {
		return Collections.unmodifiableList(allMacAddresses);
	}

	/**
	 * Sets the frequency, which should be used. This will be multiple called
	 * from the MAC-Layer, so we check for the condition, that all MACs have the
	 * same frequency.<br>
	 * Additionally the ranges will be updated
	 *
	 * @param frequency
	 *            The frequency, which will be used from the MAC-Layer.
	 */
	protected void setFrequency(long frequency) {
		if (this.frequency != 0 && this.frequency != frequency) {
			throw new ConfigurationException(
					"Using different Frequencies on the same Topology-View. This is not supported!");
		}
		getPropagationLossModel().setFrequency(frequency);
		this.frequency = frequency;
		updateRanges();
	}

	/**
	 * Sets the transmission power in dBm, which should be used to calculate the
	 * ranges. This will be multiple called from the MAC-Layer, so we check for
	 * maximal transmission power and use the maximal transmission power for the
	 * calculation of the ranges.<br>
	 * <br>
	 * Additionally the ranges will be updated
	 *
	 * @param txPowerDbm
	 *            the transmission power in dBm
	 */
	protected void setTxPowerDbm(double txPowerDbm) {
		// FIXME: Possible to store for every node, and for every node an own
		// range!
		this.maxTxPowerDbm = Math.max(maxTxPowerDbm, txPowerDbm);
		updateRanges();
	}

	/**
	 * Updates the ranges, which will be used to fill the caches of the
	 * neighborhood.
	 */
	private void updateRanges() {
		double csRange = interferenceHelper.calculateMaximalRadius(
				maxTxPowerDbm, csDbm);
		double range = interferenceHelper.calculateMaximalRadius(maxTxPowerDbm,
				rxSensitivityDbm);
		double satRange = interferenceHelper.calculateMaximalRadius(
				maxTxPowerDbm, satDbm);
		Monitor.log(WifiTopologyView.class, Level.INFO,
				"WiFi underlay ranges: satRange: " + satRange + " csRange: "
						+ csRange + " range: " + range);
		setRange(range);
		setCSRange(csRange);

	}

	/**
	 * Sets the used standard of the MAC-Layer. This will be called multiple,
	 * and we check for different standards. If we found different standards, we
	 * throw an exception, because we only support a homogeneous network of the
	 * same standard.
	 *
	 * @param standard
	 *            The standard which is used by the MAC-Layer.
	 */
	protected void setStandard_802_11(Standard_802_11 standard) {
		if (standard_802_11 != null && !standard_802_11.equals(standard)) {
			throw new ConfigurationException(
					"Using different IEEE 802.11 Standards on the same Topology-View. This is not supported!");
		}
		this.standard_802_11 = standard;
	}

	@Override
	protected void addedMac(MacLayer mac) {
		super.addedMac(mac);
		Ieee80211AdHocMac wifiMac = (Ieee80211AdHocMac) mac;
		// sets the standard, txPower and the frequency, which is using of the
		// mac.
		// Additionally it will be checked for a homogeneous network of this
		// parameter.
		setStandard_802_11(wifiMac.getStandard_802_11());
		setTxPowerDbm(wifiMac.getTxPowerDdm());
		setFrequency(wifiMac.getFrequency());
	}

	/**
	 * Sets the Signal Attention Threshold in dBm for the
	 * {@link InterferenceHelper}.
	 *
	 * @param satDbm
	 *            The Signal Attention Threshold in dBm.
	 */
	public void setSatDbm(double satDbm) {
		this.satDbm = satDbm;
		this.satW = PropagationLossModel.dbmToW(satDbm);
		this.interferenceHelper.setSatDbm(satDbm);
	}

	/**
	 * Sets the Signal Attention Threshold in watt for the
	 * {@link InterferenceHelper}
	 *
	 * @param satW
	 *            The Signal Attention Threshold in watt
	 */
	public void setSatW(double satW) {
		this.satW = satW;
		this.satDbm = PropagationLossModel.wToDbm(satW);
		this.interferenceHelper.setSatDbm(satDbm);
	}

	/**
	 * Sets the carrier sense threshold.
	 *
	 * @param csDbm
	 *            the carrier sense threshold in dBm
	 */
	public void setCsDbm(double csDbm) {
		this.csDbm = csDbm;
		this.csW = PropagationLossModel.dbmToW(csDbm);
	}

	/**
	 * Sets the carrier sense threshold.
	 *
	 * @param csW
	 *            the carrier sense threshold in watt
	 */
	public void setCsW(double csW) {
		this.csW = csW;
		this.csDbm = PropagationLossModel.wToDbm(csW);
	}

	/**
	 * Sets the receive sensitivity threshold.
	 *
	 * @param rxSensitivityDbm
	 *            the receive sensitivity threshold in dBm
	 */
	public void setRxSensitivityDbm(double rxSensitivityDbm) {
		this.rxSensitivityDbm = rxSensitivityDbm;
		this.rxSensitivityW = PropagationLossModel.dbmToW(rxSensitivityDbm);
	}

	/**
	 * Sets the receive sensitivity threshold.
	 *
	 * @param rxSensitivityW
	 *            the receive sensitivity threshold in watt
	 */
	public void setRxSensitivityW(double rxSensitivityW) {
		this.rxSensitivityW = rxSensitivityW;
		this.rxSensitivityDbm = PropagationLossModel.wToDbm(rxSensitivityW);
	}

	/**
	 * Sets the transmit gain of the antenna in the {@link InterferenceHelper}.
	 *
	 * @param gain
	 *            The transmit gain
	 */
	public void setTxGain(double gain) {
		this.interferenceHelper.setTxGainDbm(gain);
	}

	/**
	 * Sets the receive gain of the antenna in the {@link InterferenceHelper}.
	 *
	 * @param gain
	 *            The receive gain
	 */
	public void setRxGain(double gain) {
		this.interferenceHelper.setRxGainDbm(gain);
	}
}
