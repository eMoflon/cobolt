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
 * This is the Interface for the {@link ErrorRateModel}. It calculates the
 * success rate for a number of bits, which are transmitted. Additionally, it
 * can calculate the SNR-Treshold for a {@link WifiMode} with a specific Packet
 * Error Rate (PER).
 * 
 * This class based on NS3 (src/wifi/model/error-rate-model.h).
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public interface ErrorRateModel {
	/**
	 * 
	 * Calculate the SNR for the given Bit Error Rate (BER) for one bit.
	 * 
	 * @param txMode
	 *            the specific transmission mode
	 * @param ber
	 *            the target BER
	 * @return the SNR which corresponds to the requested BER
	 */
	public double calculateSnrThreshold(WifiMode txMode, double ber);

	/**
	 * Calculate the SNR Threshold for the given Packet Error Rate (PER) with a
	 * specific length of nbits.
	 * 
	 * @param txMode
	 *            The specific transmission mode.
	 * @param per
	 *            the target PER.
	 * @param nbits
	 *            The length of the packet in bits.
	 * @return The SNR which corresponds to the requested PER and packet length.
	 */
	public double calculateSnrThreshold(WifiMode txMode, double per, int nbits);

	/**
	 * Calculates the Success Rate for the {@link WifiMode} with the given SNR
	 * for the length of the chunk.
	 * 
	 * @param mode
	 * @param snr
	 *            The Snr
	 * @param nbits
	 *            The length of the chunk.
	 * @return
	 */
	public double getChunkSuccessRate(WifiMode mode, double snr, int nbits);
}
