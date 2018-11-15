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

package de.tud.kom.p2psim.impl.topology.views.wifi.phy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.ErrorRateModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.PropagationLossModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy.WifiPreamble;
import de.tud.kom.p2psim.impl.linklayer.mac.wifi.Ieee80211AdHocMac;
import de.tud.kom.p2psim.impl.topology.views.wifi.WifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.wifi.phy.errormodel.NistErrorRateModel;
import de.tud.kom.p2psim.impl.topology.views.wifi.phy.propagation.loss.LogDistancePropagationLossModel;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;

/**
 * This class is the Interference Helper, which process for one
 * frequency/channel all Interferences on the map. With the Interferences it is
 * possible to calculate the Drop Rate of a transmission. For this class is an
 * Interference or a Transfer the same. The different is, that for a Transfer it
 * is only possible to calculate the Drop Rate. <br>
 * It is possible to add Interferences, which are contain no message. This can
 * be used, if you not want to simulate all message like RTS, CTS, ACK, but the
 * transmission of data. In this case, the RTS, CTS and ACK Messages must be
 * added as Interferences for a good accurate result. But it is possible to
 * simulate all messages, but this will be cost performance.<br>
 * 
 * The Drop Rate or rather the Packer Error Rate (PER) will be calculate for the
 * transmission of data. For that, the Transfer will be splitted in chunks, for
 * every event, which change the Network Interface. This are the other
 * Interferences, which are starting or ending and consequently change the SNR.
 * For every changed SNR will be calculated a new PER, which is later aggregated
 * to the total PER. <br>
 * 
 * Additionally it is possible to request to every point the actually
 * interference noise on the map.
 * 
 * <p>
 * The {@link InterferenceHelper} is only for one frequency/channel. It doesn't
 * support multiple channels. You can create multiple {@link InterferenceHelper}
 * for different channels, but the overlapping channels like 5 and 6 will be
 * note considered.
 * 
 * This class based on NS3 (src/wifi/model/interference-helper.cc) by Mathieu
 * Lacage <mathieu.lacage@sophia.inria.fr> further extended by Christoph
 * Muenker.
 * 
 * @author Christoph Muenker
 * @version 1.0, 01.03.2013
 */
public class InterferenceHelper implements EventHandler {

	// thermal noise at 290K in J/s = W
	private static double BOLTZMANN = 1.3803e-23;

	/**
	 * This contains the current transfers. The transfers info are important for
	 * the receiver. With this information will be calculated the PER on the
	 * receiver. <br>
	 * Because of Broadcasts, it is not so easy to delete a transfer
	 * information. So we decide to use a "Cache", which remove the eldest
	 * entries. We make the assumption, that 1ms after a transfer, the
	 * information is not more needed.
	 */
	private Map<Message, Interference> transfers = new LinkedHashMap<Message, Interference>(
			1000, 0.75f, true) {

		private final int MAX_ENTRIES = 10000;

		@Override
		protected boolean removeEldestEntry(
				Map.Entry<Message, Interference> eldest) {
			/*
			 * FIXME here, we have a potential memory leak... the map size is
			 * not limited.
			 */
			if (this.size() >= MAX_ENTRIES) {
				return true;
			}
			return eldest.getValue().getEndTime() + Time.MILLISECOND < Time
					.getCurrentTime();
		}
	};

	/**
	 * Contains the actually interferences
	 */
	private List<Interference> interferences = new LinkedList<Interference>();

	private ErrorRateModel errorModel = new NistErrorRateModel();

	private PropagationLossModel lossModel = new LogDistancePropagationLossModel();

	/**
	 * Loss (dB) in the Signal-to-Noise-Ratio due to non-idealities in the
	 * receiver. According to Wikipedia
	 * (http://en.wikipedia.org/wiki/Noise_figure), this is "the difference in
	 * decibels (dB) between the noise output of the actual receiver to the
	 * noise output of an ideal receiver with the same overall gain and
	 * bandwidth when the receivers are connected to sources at the standard
	 * noise temperature T0 (usually 290 K)"
	 */
	private double noiseFigure = dbToRatio(7);

	/**
	 * Reception gain (dB).<br>
	 * Will be used in the {@link Interference}
	 */
	private double rxGainDbm = 1;

	/**
	 * Transmission gain (dB). <br>
	 * Will be used in the {@link Interference}
	 */
	private double txGainDbm = 1;

	// Signal Attention Threshold. This means the minimal signal power before
	// the signal is ignored for the calculation of the noise.
	private double satDbm = -120; // before we had the value -120 -80

	private double satW = PropagationLossModel.dbmToW(satDbm);

	/**
	 * Contains to the txPowerDbm the maximal distance with the actually
	 * lossModel
	 */
	private Map<Double, Double> satDistanceCache = new HashMap<Double, Double>();

	private WifiTopologyView topoView;

	public InterferenceHelper(WifiTopologyView topoView) {
		this.topoView = topoView;
	}

	/**
	 * Add an Interference to the medium. It will be added to the other running
	 * Interferences and to future Interferences, which are interested of this
	 * Interference.
	 * 
	 * @param start
	 *            The start time of this interference in microseconds.
	 * @param end
	 *            The end time of this interference in microseconds
	 * @param sourcePosition
	 *            The {@link Location} of the source of this interference
	 * @param txPowerDbm
	 *            The signal power of this interference in dBm.
	 */
	@Deprecated
	// because sourceAddress should be not null!
	public void addInterference(long start, long end, Location sourcePosition,
			double txPowerDbm) {
		addInterference(start, end, sourcePosition, txPowerDbm, null, null);
	}

	/**
	 * Add an Interference to the medium. It will be added to the other running
	 * Interferences and to future Interferences, which are interested of this
	 * Interference.<br>
	 * The neighborhood of the node with the sourceAddress will be informed
	 * about the interference.
	 * 
	 * @param start
	 *            The start time of this interference in microseconds.
	 * @param end
	 *            The end time of this interference in microseconds
	 * @param sourcePosition
	 *            The {@link Location} of the source of this interference
	 * @param txPowerDbm
	 *            The signal power of this interference in dBm.
	 * @param dataMode
	 *            The {@link WifiMode} of this interference.
	 * @param sourceAddress
	 *            The {@link MacAddress} of the node, which are the originator
	 *            of this interference.
	 */
	public void addInterference(long start, long end, Location sourcePosition,
			double txPowerDbm, WifiMode dataMode, MacAddress sourceAddress) {
		addTransfer(start, end, sourcePosition, txPowerDbm, null, dataMode,
				null, null, sourceAddress);
	}

	/**
	 * Add a transfer as an Interference to the medium. It does the same like
	 * the addInterference method. But this adds the interference additional as
	 * a transfer to the transfer's list. The message will be handled as a ID,
	 * for the later calculation of PER.
	 * 
	 * @param start
	 *            The start time of this transfer in microseconds.
	 * @param end
	 *            The end time of this transfer in microseconds
	 * @param sourcePosition
	 *            The {@link Location} of the source of this transfer
	 * @param txPowerDbm
	 *            The signal power of this transfer in dBm.
	 * @param id
	 *            The message which will be transmitted. This is the ID, for the
	 *            calculation of the PER.
	 * @param dataMode
	 *            The {@link WifiMode} which is used for this transmission
	 * @param preamble
	 *            The {@link WifiMode} of the preamble
	 * @param host
	 *            The host which start this transmission.
	 * @param sourceAddress
	 *            The {@link MacAddress} of the node, which are the originator
	 *            of this transfer.
	 */
	public void addTransfer(long start, long end, Location sourcePosition,
			double txPowerDbm, Message id, WifiMode dataMode,
			WifiPreamble preamble, SimHost host, MacAddress sourceAddress) {
		if (start < Time.getCurrentTime()) {
			throw new IllegalArgumentException(
					"The start time of the Transfer cannot be lie in the past!");
		}
		if (start >= end) {
			throw new IllegalArgumentException(
					"The start time is equal or greater than the end time!");
		}
		Interference interference = new Interference(start, end, sourcePosition,
				txPowerDbm, id, dataMode, preamble,
				host, sourceAddress);
		addInterference(interference);

		if (id != null) {
			transfers.put(id, interference);
		}
		// System.out.println(transfers.size() + "    " + interferences.size()
		// + "    duration " + (end - start));
	}

	/**
	 * Add the interference to the interferences and inform the neighborhood
	 * about a signal on the medium. Only the neighborhood in the carrier sense
	 * range will be called.
	 * 
	 * @param interference
	 *            The interference which should be added.
	 */
	private void addInterference(Interference interference) {
		removeExpiredInterferences();
		for (Interference i : interferences) {
			interference.addInterference(i);
			i.addInterference(interference);
		}

		// schedule the call for the Carrier Sense of peers in this range.
		if (interference.getStartTime() != Time.getCurrentTime()) {
			Event.scheduleWithDelay(
					interference.getStartTime() - Time.getCurrentTime(), this,
					interference, 0);
		} else {
			notifyCarrierSense(interference);
		}

		interferences.add(interference);

	}

	/**
	 * Remove expired interferences from the list of interferences.
	 */
	private void removeExpiredInterferences() {
		List<Interference> toDelete = new LinkedList<Interference>();
		long current = Time.getCurrentTime();
		for (Interference i : interferences) {
			if ((i.getEndTime() + 1) < current) {
				toDelete.add(i);
			}
		}

		for (Interference i : toDelete) {
			interferences.remove(i);
		}
	}

	/**
	 * Calculates the success rate for the chunk. The chunk is the duration for
	 * the same SNR. <br>
	 * With the duration and the {@link WifiMode}, it is possible to derive the
	 * number of bits. So the duration should be accurate.
	 * 
	 * @param snir
	 *            The actually signal to noise ratio.
	 * @param duration
	 *            The duration for this chunk
	 * @param mode
	 *            The {@link WifiMode} which is used for this chunk.
	 * @return The success rate for this chunk
	 */
	protected double calculateChunkSuccessRate(double snir, long duration,
			WifiMode mode) {
		if (duration == 0) {
			return 1.0;
		}
		int rate = mode.getPhyRate();
		int nbits = (int) ((rate * duration) / Time.SECOND);
		double csr = errorModel.getChunkSuccessRate(mode, snir, nbits);
		return csr;
	}

	/**
	 * Calculates the Packet Error Rate for this ID (the Message) and the given
	 * {@link Location}. The position is used to calculate the receive power and
	 * the resulting SNR (Signal to noise Ratio).<br>
	 * The transfer information will be got with the message. In this
	 * information will be stored all interferences, which has influence for the
	 * transmission. This are other Interferences which has influence of the
	 * SNR. So that we must split the transfer to chunks, for every change of
	 * the SNR. Further, the transfer must be splitted in different parts,
	 * because the {@link WifiMode} can be changed between Header and Payload.
	 * 
	 * @param id
	 *            The message for that the Packet Error Rate should be
	 *            calculated.
	 * @param rxPos
	 *            The receiver Position. This should be accurate, because the
	 *            distance to the sender is important.
	 * @return The Packet Error Rate for this Message at the given
	 *         {@link Position}.
	 */
	public double calculatePer(Message id, Location rxPos) {
		Interference transfer = transfers.get(id);
		if (transfer == null) {
			Monitor.log(
					InterferenceHelper.class,
					Level.WARN,
					"No Transfer found with the given Message ID. This should not happen. Returning '1' for PER.");
			return 1;
		}
		double rxPowerW = transfer.getRxPowerW(rxPos);
		double noiseInterferenceW = 0;

		long startTime = transfer.getStartTime();
		long endTime = transfer.getEndTime();

		long preambleTime = transfer.getPreambleTime();
		long headerTime = transfer.getHeaderTime();
		long payloadTime = transfer.getPayloadTime();
		long startHeaderTime = startTime + preambleTime;
		long startPayloadTime = startTime + preambleTime + headerTime;

		WifiMode headerMode = transfer.getHeaderMode();
		WifiMode payloadMode = transfer.getPayloadMode();

		long lastChangeTime = 0;
		double psr = 1; /* Packet Success Rate */
		for (NIChangeTime niChange : transfer.getNIChanges()) {
			if (startTime < niChange.getTime()
					&& lastChangeTime < niChange.getTime()) {
				long startSection = Math.max(startTime, lastChangeTime);
				long endSection = Math.min(endTime, niChange.getTime());
				long sectionTime = endSection - startSection; // debug

				assert sectionTime != 0 : "Try to derive a section, but the time has not changed";
				// Preamble will be ignored, because it is only for
				// synchronization
				if (startSection < startHeaderTime) {
					long duration = Math.min(startHeaderTime, endSection)
							- startSection;

					sectionTime -= duration; // debug
					// no PSR calculation! because it is only for
					// synchronization.
				}
				// for Phy Header
				if (endSection >= startHeaderTime
						&& startSection < startPayloadTime) {
					long duration = Math.min(startPayloadTime, endSection)
							- Math.max(startHeaderTime, startSection);

					sectionTime -= duration; // debug

					psr *= getPsr(rxPowerW, noiseInterferenceW, headerMode,
							duration);
				}
				// for Payload
				if (endSection >= startPayloadTime) {
					long duration = endSection
							- Math.max(startPayloadTime, startSection);

					sectionTime -= duration; // debug

					psr *= getPsr(rxPowerW, noiseInterferenceW, payloadMode,
							duration);

				}

				assert sectionTime == 0 : "The section time must be after this check 0";

			}
			noiseInterferenceW += niChange.getRxPowerWChange(rxPos);
			lastChangeTime = niChange.getTime();
			if (endTime <= niChange.getTime()) {
				break;
			}
		}
		// The case, that no interferences exists, or fill to the endTime!
		if (lastChangeTime < endTime) {
			// we take care, that the header is missing, but the case
			// that is needed is very low!
			long duration = endTime - Math.max(lastChangeTime, startTime);
			psr *= getPsr(rxPowerW, noiseInterferenceW, payloadMode, duration);
		}

		double per = 1.0 - psr;

		return per;
	}

	/**
	 * Gets the Packet Success Rate (PSR).
	 * 
	 * @param rxPowerW
	 *            The receiver Power in watt
	 * @param noiseInterferenceW
	 *            The cumulative interference on the medium at a specific
	 *            position.
	 * @param mode
	 *            The {@link WifiMode}, which should be used to calculate the
	 *            Success Rate.
	 * @param duration
	 *            The duration for the chunk.
	 * @return The Packet Success Rate for the duration of this chunk.
	 */
	private double getPsr(double rxPowerW, double noiseInterferenceW,
			WifiMode mode, long duration) {
		double snr = calculateSnr(rxPowerW, noiseInterferenceW, mode);
		double psr = calculateChunkSuccessRate(snr, duration, mode);
		return psr;
	}

	/**
	 * Calculates the Noise floor to the given {@link WifiMode}. The Bandwidth
	 * of the {@link WifiMode} is for this calculation important.
	 * 
	 * @param mode
	 *            The {@link WifiMode} which is used.
	 * @return The NoiseFloor to the given {@link WifiMode}
	 */
	protected double getNoiseFloor(WifiMode mode) {
		// Nt is the power of thermal noise in W at room temperature
		double Nt = BOLTZMANN * 290.0 * mode.getBandwidth();
		// receiver noise Floor (W) which accounts for thermal noise and
		// non-idealities of the receiver
		double noiseFloor = noiseFigure * Nt;
		return noiseFloor;
	}

	/**
	 * Gets the default Noise Floor for a WifiMode with 20Mhz Bandwidth.
	 * 
	 * @return The noise Floor at a Bandwidth with 20Mhz.
	 */
	protected double getDefaultNoiseFloor() {
		// WifiMode with a Bandwidth of 20MHz
		WifiMode mode = WifiPhy.getDsssRate1Mbps();
		return getNoiseFloor(mode);
	}

	/**
	 * Calculates the Signal to Noise Ratio (SNR). First, it derives the noise
	 * (noiseFloor + noiseInterferenceW). After this, it divides the signalW
	 * through the noise.<br>
	 * Formula: <br>
	 * snr = signalW / (noiseFloor + noiseInterferenceW)
	 * 
	 * @param signalW
	 *            The signal power in Watt of the received signal.
	 * @param noiseInterferenceW
	 *            The cumulative interferences in Watt on the medium at a
	 *            specific point.
	 * @param mode
	 *            The {@link WifiMode} (the NoiseFloor can be changed through
	 *            this parameter).
	 * @return The Signal to Noise Ratio to this signal.
	 */
	public double calculateSnr(double signalW, double noiseInterferenceW,
			WifiMode mode) {
		double noiseFloor = getNoiseFloor(mode);
		double noise = noiseFloor + noiseInterferenceW;
		double snr = signalW / noise;
		return snr;
	}

	/**
	 * Calculates the actually Interference noise in Watt for a host. The host
	 * will be used, to use his {@link Location}, and to ignore interferences
	 * from him.
	 * 
	 * @param host
	 *            The host for that the interference should be calculated and
	 *            which host should be ignored.
	 * @return The noise at the position of the host, without the interferences
	 *         of the host in Watt.
	 */
	public double calculateNoiseInterferenceW(SimHost host) {
		return calculateNoiseInterferenceW(host, host.getTopologyComponent()
				.getRealPosition());
	}

	/**
	 * Calculates the actually interference noise in Watt at the given
	 * {@link Location}. The output of can be different to the method
	 * {@link InterferenceHelper#calculateNoiseInterferenceW(SimHost)}, because
	 * no interference will be leave out.
	 * 
	 * @param point
	 *            The position for that the interference noise is requested
	 * @return The actually interference noise in Watt at the given Position.
	 */
	public double calculateNoiseInterferenceW(Location point) {
		return calculateNoiseInterferenceW(null, point);
	}

	/**
	 * Calculates the actually interference noise in Watt at the given Position.
	 * The Host can be null, if no interference should be leave out. If you are
	 * only interested in the interferences of other peers, around the host,
	 * then should you add the host.
	 * 
	 * @param host
	 *            The host, which added interferences should be ignored.
	 * @param point
	 *            The position for that the interference noise should be
	 *            calculated.
	 * @return The actually interference noise in Watt at the given Position
	 *         without the interferences of the host.
	 */
	public double calculateNoiseInterferenceW(SimHost host, Location point) {
		double noiseInterferenceW = 0;
		for (Interference i : interferences) {
			if (i.getStartTime() <= Time.getCurrentTime()
					&& i.getEndTime() > Time.getCurrentTime()) {
				if (host == null || !host.equals(i.getHost())) {
					noiseInterferenceW += i.getRxPowerW(point);
				}
			}
		}
		return noiseInterferenceW;
	}

	public void setErrorModel(ErrorRateModel errorModel) {
		this.errorModel = errorModel;
	}

	public void setLossModel(PropagationLossModel lossModel) {
		satDistanceCache.clear();
		this.lossModel = lossModel;
	}

	public PropagationLossModel getLossModel() {
		return lossModel;
	}

	public ErrorRateModel getErrorModel() {
		return errorModel;
	}

	public double getRxGainDbm() {
		return rxGainDbm;
	}

	public double getTxGainDbm() {
		return txGainDbm;
	}

	public void setRxGainDbm(double rxGainDbm) {
		this.rxGainDbm = rxGainDbm;
	}

	public void setTxGainDbm(double txGainDbm) {
		this.txGainDbm = txGainDbm;
	}

	public double getSatDbm() {
		return satDbm;
	}

	public double getSatW() {
		return satW;
	}

	public void setSatDbm(double satDbm) {
		satDistanceCache.clear();
		this.satDbm = satDbm;
		this.satW = PropagationLossModel.dbmToW(satDbm);
	}

	/**
	 * Calculate the maximal radius to the given transmission Power in dBm and
	 * the receiver Power in dBm. It use the reverse function of the loss Model
	 * to calculate this information.<br>
	 * To the txPower and rxPower will be added/subtracted the txGain and
	 * rxGain.
	 * 
	 * @param txPowerDbm
	 *            The power for the transmission in dBm.
	 * @param rxPowerDbm
	 *            The required receive power in dBm.
	 * @return The maximal range, which are possible to result the rxPower, if
	 *         you send with the txPower.
	 */
	public double calculateMaximalRadius(double txPowerDbm, double rxPowerDbm) {
		// add the gain to the TX Power and subtract the gain from the RX Power
		// to calculate the range. Subtract because the antenna can be refresh
		// the signal. We are interested on the maximal range!
		double distance = lossModel.getDistance(txPowerDbm + getTxGainDbm(),
				rxPowerDbm - getRxGainDbm());
		return distance;
	}

	/**
	 * Calculates the maximal SAT radius (Signal Attention Threshold). For the
	 * performance, we use a cache, which contains all previous calculated.
	 * 
	 * @param txPowerDbm
	 *            The used txPower.
	 * @return The maximal radius which can get a signal which is send with the
	 *         given TxPower, and is perceived with the SAT power.
	 */
	protected double calculateMaximalSatRadius(double txPowerDbm) {
		Double distance = satDistanceCache.get(txPowerDbm);
		if (distance != null && distance >= 0) {
			return distance;
		}
		distance = lossModel.getDistance(txPowerDbm, satDbm);
		satDistanceCache.put(txPowerDbm, distance);
		return distance;
	}

	/**
	 * This class stores the information for a Interference or rather a
	 * Transfer. A Transfer is the same how a interference but has only more
	 * information. (It would be better, if we inherit the interference as a
	 * transfer...) Further it has a list with all Interferences times of starts
	 * and ends. Which contains all interferences which influence the transfer.
	 * 
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 01.03.2013
	 */
	private class Interference {
		private final NIChangeTime niChangeStart;

		private final NIChangeTime niChangeEnd;

		private final Location sourcePosition;

		private final double txPowerDbm;

		private final Message id;

		private final WifiMode dataMode;

		private final SimHost host;

		private final WifiPreamble preamble;

		private final boolean unknown;

		private final MacAddress sourceAddress;

		private final long duration;

		private List<InterferenceHelper.NIChangeTime> niChangeList = new Vector<InterferenceHelper.NIChangeTime>();

		public Interference(long startTime, long endTime,
				Location sourcePosition, double txPowerDbm, Message id,
				WifiMode dataMode, WifiPreamble preamble, SimHost host,
				MacAddress sourceAddress) {
			this.niChangeStart = new NIChangeTime(startTime, false, this);
			this.niChangeEnd = new NIChangeTime(endTime, true, this);
			this.sourcePosition = sourcePosition;
			this.txPowerDbm = txPowerDbm + getTxGainDbm();
			this.id = id;
			this.dataMode = dataMode;
			this.preamble = preamble;
			this.host = host;
			this.sourceAddress = sourceAddress;
			this.duration = endTime - startTime;
			this.unknown = (id == null || dataMode == null || host == null);
		}

		public NIChangeTime getNiChangeStart() {
			return niChangeStart;
		}

		public NIChangeTime getNiChangeEnd() {
			return niChangeEnd;
		}

		public long getStartTime() {
			return niChangeStart.getTime();
		}

		public long getEndTime() {
			return niChangeEnd.getTime();
		}

		public Location getSourcePosition() {
			return sourcePosition;
		}

		public double getTxPowerDbm() {
			return txPowerDbm;
		}

		public Message getId() {
			return id;
		}

		public WifiMode getMode() {
			return dataMode;
		}

		public boolean isUnknown() {
			return unknown;
		}

		public SimHost getHost() {
			return host;
		}

		public double getRxPowerDbm(Location pos) {
			return getLossModel().getRxPowerDbm(getTxPowerDbm(),
					sourcePosition.distanceTo(pos))
					+ getRxGainDbm();
		}

		public double getRxPowerW(Location pos) {
			double rxPowerdbm = getRxPowerDbm(pos);
			return PropagationLossModel.dbmToW(rxPowerdbm);
		}

		public void addInterference(Interference interference) {
			if (!isUnknown()) {
				double distance = this.sourcePosition
						.distanceTo(interference
						.getSourcePosition());
				double maxRadius = calculateMaximalSatRadius(interference
						.getTxPowerDbm());
				if (distance < maxRadius) {
					niChangeList.add(interference.getNiChangeStart());
					niChangeList.add(interference.getNiChangeEnd());
				}
			}
		}

		/**
		 * Gets a sorted list of {@link NIChangeTime}s. This list contains all
		 * important network events in the right order.
		 * 
		 * @return An order list of network events, which can influence the
		 *         interference.
		 */
		public List<NIChangeTime> getNIChanges() {
			// the interferences must be sorted, because the
			Collections.sort(niChangeList);
			return niChangeList;
		}

		public long getPreambleTime() {
			return WifiPhy.getPlcpPreambleDuration(dataMode, preamble);
		}

		public long getHeaderTime() {
			return WifiPhy.getPlcpHeaderDuration(dataMode, preamble);
		}

		public long getPayloadTime() {
			return WifiPhy.getPayloadDuration((int) id.getSize(), dataMode);
		}

		public WifiMode getHeaderMode() {
			return WifiPhy.getPlcpHeaderMode(dataMode, preamble);
		}

		public WifiMode getPayloadMode() {
			return dataMode;
		}

		public MacAddress getSourceMacAddress() {
			return sourceAddress;
		}

		public long getTransmissionDuration() {
			return duration;
		}
	}

	/**
	 * This Class is a container for the time, when the Network Interface (NI)
	 * become a change, because a new Interference starts or ends.
	 * 
	 * This class can be ordered after the occurrence of the interference event.
	 * 
	 * 
	 * @author Christoph Muenker
	 * @version 1.0, 01.03.2013
	 */
	private class NIChangeTime implements Comparable<NIChangeTime> {
		private final long time;

		private final boolean endOfTransmission;

		private final Interference interference;

		public NIChangeTime(long time, boolean endOfTransmission,
				Interference interference) {
			if (interference == null) {
				throw new IllegalArgumentException(
						"Interference should not be null!");
			}
			this.time = time;
			this.endOfTransmission = endOfTransmission;
			this.interference = interference;
		}

		public boolean isEndOfTransmission() {
			return endOfTransmission;
		}

		public long getTime() {
			return time;
		}

		public Interference getInterference() {
			return interference;
		}

		/**
		 * Gets the change of the rxPower in Watt to the given Position. The
		 * change can be positive and negative. It is positive, if an
		 * interference start, and negative if the interference ends.
		 * 
		 * @param rxPos
		 *            The position for that the receive power should be
		 *            calculated
		 * @return It is positive, if an interference start, and negative if the
		 *         interference ends.
		 */
		public double getRxPowerWChange(Location rxPos) {
			double change = this.getInterference().getRxPowerW(rxPos);
			if (this.isEndOfTransmission()) {
				change *= -1;
			}
			return change;
		}

		@Override
		public int compareTo(NIChangeTime niChange) {
			long thisVal = this.time;
			long anotherVal = niChange.time;
			return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
		}
	}

	@Override
	public void eventOccurred(Object se, int type) {
		if (se instanceof Interference) {
			Interference interference = (Interference) se;
			notifyCarrierSense(interference);
		}
	}

	/**
	 * Notifies the neighbors MacLayers about an interference, or rather about
	 * the carrier sense.
	 * 
	 * @param interference
	 *            The interference which should be used to inform the
	 *            neighborhood.
	 */
	private void notifyCarrierSense(Interference interference) {
		assert interference.getStartTime() == Time.getCurrentTime() : "Wrong time to call this method with this interference!";
		MacAddress source = interference.getSourceMacAddress();

		long duration = interference.getTransmissionDuration();
		List<MacAddress> csNeighbors = topoView
				.getCarrierSenseNeighbors(source);
		for (MacAddress address : csNeighbors) {
			// TODO: Better use a Interface which is called to inform the
			// macLayer (Observer Pattern?)
			Ieee80211AdHocMac mac = (Ieee80211AdHocMac) topoView
					.getMac(address);
			mac.notifyCarrierSense(duration);
		}
	}

	/**
	 * Derive the dB to Ratio. (Copied from NS3).
	 * 
	 * @param dB
	 *            The dB
	 * @return The ratio
	 */
	private double dbToRatio(double dB) {
		double ratio = Math.pow(10.0, dB / 10.0);
		return ratio;
	}

	// public double calculateLinkRange(double txPowerDbm, WifiMode defaultMode)
	// {
	// double minimalSnr = errorModel.calculateSnrThreshold(defaultMode, 0.95,
	// 200); // 5% success for 200bits
	// double noiseW = getNoiseFloor(defaultMode); // without interferences!
	// // only default noise.
	// double rxPowerW = minimalSnr * noiseW;
	// double rxPowerDbm = lossModel.wToDbm(rxPowerW);
	//
	// System.out.println("minimalSnr: " + minimalSnr + "  rxPowerDbm: "
	// + rxPowerDbm + " noiseDbm " + lossModel.wToDbm(noiseW));
	// double distance = lossModel.getDistance(txPowerDbm, rxPowerDbm);
	// // small amount of space to the derived distance.
	// return distance + 10;
	// }
}
