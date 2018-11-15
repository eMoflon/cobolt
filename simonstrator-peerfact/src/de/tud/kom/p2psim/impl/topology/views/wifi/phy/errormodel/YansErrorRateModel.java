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

import java.util.HashMap;

import org.apache.commons.math.MathException;
import org.apache.commons.math.special.Erf;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.impl.util.NotSupportedException;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This class is the YANS Error Rate Model from the NS3. This are only formulas
 * for the OFDM. For the DSSS, we use the {@link DsssErrorRateModel}. The DSSS
 * 
 * The formulas are copied from NS3 (src/wifi/model/yans-wifi-phy.cc).
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class YansErrorRateModel extends AbstractErrorRateModel {

	private DsssErrorRateModel dsssErroRateModel = new DsssErrorRateModel();

	private static HashMap<Integer, Long> factorialCache = new HashMap<Integer, Long>();

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
		return getFecBpskBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				10, // dFree
				11 // adFree
		);
	}

	@Override
	protected double getOfdmFecBpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecBpskBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				5, // dFree
				8 // adFree
		);
	}

	@Override
	protected double getOfdmFecQpskCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				4, // m
				10, // dFree
				11, // adFree
				0 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQpskCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				4, // m
				5, // dFree
				8, // adFree
				31 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQamCodeRate1_2SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				16, // m
				10, // dFree
				11, // adFree
				0 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQamCodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				16, // m
				5, // dFree
				8, // adFree
				31 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate2_3SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				64, // m
				6, // dFree
				1, // adFree
				16 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate3_4SuccessRate(WifiMode mode,
			double snr, int nbits) {
		return getFecQamBer(snr, nbits, mode.getBandwidth(), // signal spread
				mode.getPhyRate(), // phy rate
				64, // m
				5, // dFree
				8, // adFree
				31 // adFreePlusOne
		);
	}

	@Override
	protected double getOfdmFecQam64CodeRate5_6SuccessRate(WifiMode mode,
			double snr, int nbits) {
		throw new NotSupportedException(
				"OFDM QAM 64 with 5/6 Code Rate is not supported in this Error Model!");
	}

	private double getFecBpskBer(double snr, double nbits, int signalSpread,
			int phyRate, int dFree, int adFree) {
		double ber = getBpskBer(snr, signalSpread, phyRate);
		if (ber == 0.0) {
			return 1.0;
		}
		double pd = calculatePd(ber, dFree);
		double pmu = adFree * pd;
		pmu = Math.min(pmu, 1.0);
		double pms = Math.pow(1 - pmu, nbits);
		return pms;
	}

	private double getFecQamBer(double snr, int nbits, int signalSpread,
			int phyRate, int m, int dFree, int adFree, int adFreePlusOne) {
		double ber = getQamBer(snr, m, signalSpread, phyRate);
		if (ber == 0.0) {
			return 1.0;
		}
		/* first term */
		double pd = calculatePd(ber, dFree);
		double pmu = adFree * pd;
		/* second term */
		pd = calculatePd(ber, dFree + 1);
		pmu += adFreePlusOne * pd;
		pmu = Math.min(pmu, 1.0);
		double pms = Math.pow(1 - pmu, nbits);
		return pms;
	}

	private double getErfc(double z) {
		try {
			return Erf.erfc(z);
		} catch (MathException e) {
			Monitor.log(YansErrorRateModel.class, Level.ERROR,
					"Unable to compute the complimentary error function! Returning '0'...");
		}
		return 0;
	}

	private double getQamBer(double snr, int m, int signalSpread, int phyRate) {
		double EbNo = snr * signalSpread / phyRate;
		double z = Math.sqrt((1.5 * log2(m) * EbNo) / (m - 1.0));
		double z1 = ((1.0 - 1.0 / Math.sqrt(m)) * getErfc(z));
		double z2 = 1 - Math.pow((1 - z1), 2.0);
		double ber = z2 / log2(m);
		return ber;
	}

	private double getBpskBer(double snr, int signalSpread, int phyRate) {

		double EbNo = snr * signalSpread / phyRate;
		double z = Math.sqrt(EbNo);
		double ber = 0.5 * getErfc(z);
		return ber;
	}

	private double log2(double val) {
		return Math.log(val) / Math.log(2.0);
	}

	private long factorial(int n) {
		Long ret;
		if (n == 0) {
			return 1;
		}
		if (null != (ret = factorialCache.get(n))) {
			return ret;
		}
		ret = n * factorial(n - 1);
		factorialCache.put(n, ret);
		return ret;
	}

	private double binomial(int k, double p, int n) {
		double retval = factorial(n) / (factorial(k) * factorial(n - k))
				* Math.pow(p, k) * Math.pow(1 - p, n - k);
		return retval;
	}

	private double calculatePdOdd(double ber, int d) {
		assert ((d % 2) == 1);
		int dstart = (d + 1) / 2;
		int dend = d;
		double pd = 0;

		for (int i = dstart; i < dend; i++) {
			pd += binomial(i, ber, d);
		}
		return pd;
	}

	private double calculatePdEven(double ber, int d) {
		assert ((d % 2) == 0);
		int dstart = d / 2 + 1;
		int dend = d;
		double pd = 0;

		for (int i = dstart; i < dend; i++) {
			pd += binomial(i, ber, d);
		}
		// TODO: Formula say multiply!
		pd += 0.5 * binomial(d / 2, ber, d);

		return pd;
	}

	private double calculatePd(double ber, int d) {
		double pd;
		if ((d % 2) == 0) {
			pd = calculatePdEven(ber, d);
		} else {
			pd = calculatePdOdd(ber, d);
		}
		return pd;
	}

}
