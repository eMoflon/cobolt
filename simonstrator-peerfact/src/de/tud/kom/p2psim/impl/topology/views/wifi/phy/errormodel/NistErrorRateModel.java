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

package de.tud.kom.p2psim.impl.topology.views.wifi.phy.errormodel;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.impl.util.NotSupportedException;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This class is the NIST Error Rate Model from the NS3. This are only formulas
 * for the OFDM. For the DSSS, we use the {@link DsssErrorRateModel}. The DSSS
 * 
 * The formulas are copied from NS3 (src/wifi/model/nist-error-rate-model.cc)
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class NistErrorRateModel extends AbstractErrorRateModel {

	private DsssErrorRateModel dsssErroRateModel = new DsssErrorRateModel();

	@Override
	protected double getDsssDbpskSuccessRate(WifiMode mode, double snr,
			int nbits) {
		return dsssErroRateModel.getDsssDbpskSuccessRate(mode, snr, nbits);
	}

	@Override
	protected double getDsssDqpskSuccessRate(WifiMode mode, double snr,
			int nbits) {
		return dsssErroRateModel.getDsssDqpskSuccessRate(mode, snr, nbits);
	}

	@Override
	protected double getDsssDqpskCck5_5SuccessRate(WifiMode mode, double snr,
			int nbits) {
		return dsssErroRateModel
				.getDsssDqpskCck5_5SuccessRate(mode, snr, nbits);
	}

	@Override
	protected double getDsssDqpskCck11SuccessRate(WifiMode mode, double snr,
			int nbits) {
		return dsssErroRateModel.getDsssDqpskCck11SuccessRate(mode, snr, nbits);
	}

	@Override
	protected double getOfdmFecBpskCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecBpskBer(snr, nbits, 1 // b value
		);
	}

	@Override
	protected double getOfdmFecBpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecBpskBer(snr, nbits, 3 // b value
		);
	}

	@Override
	protected double getOfdmFecQpskCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQpskBer(snr, nbits, 1 // b value
		);
	}

	@Override
	protected double getOfdmFecQpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQpskBer(snr, nbits, 3 // b value
		);
	}

	@Override
	protected double getOfdmFecQamCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFec16QamBer(snr, nbits, 1 // b value
		);
	}

	@Override
	protected double getOfdmFecQamCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFec16QamBer(snr, nbits, 3 // b value
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate2_3SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFec64QamBer(snr, nbits, 2 // b value
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFec64QamBer(snr, nbits, 3 // b value
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate5_6SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"OFDM QAM 64 with 5/6 Code Rate is not supported in this Error Model!");
	}

	private double getErfc(double z) {
		try {
			return Erf.erfc(z);
		} catch (MathException e) {
			Monitor.log(NistErrorRateModel.class, Level.ERROR,
					"Unable to compute the complimentary error function! Returning '0'...");
		}
		return 0;
	}

	private double getBpskBer(double snr) {
		double z = Math.sqrt(snr);
		double ber = 0.5 * getErfc(z);
		return ber;
	}

	private double getQpskBer(double snr) {
		double z = Math.sqrt(snr / 2.0);
		double ber = 0.5 * getErfc(z);
		return ber;
	}

	private double get16QamBer(double snr) {
		double z = Math.sqrt(snr / (5.0 * 2.0));
		double ber = 0.75 * 0.5 * getErfc(z);
		return ber;
	}

	double get64QamBer(double snr) {
		double z = Math.sqrt(snr / (21.0 * 2.0));
		double ber = 7.0 / 12.0 * 0.5 * getErfc(z);
		return ber;
	}

	double getFecBpskBer(double snr, int nbits, int bValue) {
		double ber = getBpskBer(snr);
		if (ber == 0.0) {
			return 1.0;
		}
		double pe = calculatePe(ber, bValue);
		pe = Math.min(pe, 1.0);
		double pms = Math.pow(1 - pe, nbits);
		return pms;
	}

	double getFecQpskBer(double snr, int nbits, int bValue) {
		double ber = getQpskBer(snr);
		if (ber == 0.0) {
			return 1.0;
		}
		double pe = calculatePe(ber, bValue);
		pe = Math.min(pe, 1.0);
		double pms = Math.pow(1 - pe, nbits);
		return pms;
	}

	double calculatePe(double p, int bValue) {
		double D = Math.sqrt(4.0 * p * (1.0 - p));
		double pe = 1.0;
		if (bValue == 1) {
			// code rate 1/2, use table 3.1.1
			pe = 0.5 * (36.0 * Math.pow(D, 10.0) + 211.0 * Math.pow(D, 12.0)
					+ 1404.0 * Math.pow(D, 14.0) + 11633.0 * Math.pow(D, 16.0)
					+ 77433.0 * Math.pow(D, 18.0) + 502690.0
					* Math.pow(D, 20.0) + 3322763.0 * Math.pow(D, 22.0)
					+ 21292910.0 * Math.pow(D, 24.0) + 134365911.0 * Math.pow(
					D, 26.0));
		} else if (bValue == 2) {
			// code rate 2/3, use table 3.1.2
			pe = 1.0
					/ (2.0 * bValue)
					* (3.0 * Math.pow(D, 6.0) + 70.0 * Math.pow(D, 7.0) + 285.0
							* Math.pow(D, 8.0) + 1276.0 * Math.pow(D, 9.0)
							+ 6160.0 * Math.pow(D, 10.0) + 27128.0
							* Math.pow(D, 11.0) + 117019.0 * Math.pow(D, 12.0)
							+ 498860.0 * Math.pow(D, 13.0) + 2103891.0
							* Math.pow(D, 14.0) + 8784123.0 * Math.pow(D, 15.0));
		} else if (bValue == 3) {
			// code rate 3/4, use table 3.1.2
			pe = 1.0
					/ (2.0 * bValue)
					* (42.0 * Math.pow(D, 5.0) + 201.0 * Math.pow(D, 6.0)
							+ 1492.0 * Math.pow(D, 7.0) + 10469.0
							* Math.pow(D, 8.0) + 62935.0 * Math.pow(D, 9.0)
							+ 379644.0 * Math.pow(D, 10.0) + 2253373.0
							* Math.pow(D, 11.0) + 13073811.0
							* Math.pow(D, 12.0) + 75152755.0
							* Math.pow(D, 13.0) + 428005675.0 * Math.pow(D,
							14.0));
		} else {
			throw new NotSupportedException(
					"The value for bValue is not supported! [bValue = "
							+ bValue + "] You should be check your code!");
		}
		return pe;
	}

	double getFec16QamBer(double snr, int nbits, int bValue) {
		double ber = get16QamBer(snr);
		if (ber == 0.0) {
			return 1.0;
		}
		double pe = calculatePe(ber, bValue);
		pe = Math.min(pe, 1.0);
		double pms = Math.pow(1 - pe, nbits);
		return pms;
	}

	double getFec64QamBer(double snr, int nbits, int bValue) {
		double ber = get64QamBer(snr);
		if (ber == 0.0) {
			return 1.0;
		}
		double pe = calculatePe(ber, bValue);
		pe = Math.min(pe, 1.0);
		double pms = Math.pow(1 - pe, nbits);
		return pms;
	}
}
