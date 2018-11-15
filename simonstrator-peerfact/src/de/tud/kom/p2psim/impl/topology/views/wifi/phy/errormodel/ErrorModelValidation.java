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

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.topology.views.wifi.phy.ErrorRateModel;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiMode;
import de.tud.kom.p2psim.api.topology.views.wifi.phy.WifiPhy;

/**
 * This is a checker for the ErrorRateModels. It does the same, how in NS3.
 * 
 * Based on NS3 (examples/wireless/ofdm-validation.cc)
 * 
 * @author Christoph Muenker
 * @version 1.0, 28.02.2013
 */
public class ErrorModelValidation {
	public static void main(String[] args) throws IOException {
		ErrorRateModel yans = new YansErrorRateModel();
		ErrorRateModel nist = new NistErrorRateModel();
		FileWriter fYans = new FileWriter("yans-frame-success-rate.plt");
		FileWriter fNist = new FileWriter("nist-frame-success-rate.plt");

		writeHeader("yans-frame-success-rate", fYans);
		writeHeader("nist-frame-success-rate", fNist);

		int frameSize = 2000;
		List<WifiMode> modes = new Vector<WifiMode>();
		modes.add(WifiPhy.getDsssRate1Mbps());
		modes.add(WifiPhy.getDsssRate2Mbps());
		modes.add(WifiPhy.getDsssRate5_5Mbps());
		modes.add(WifiPhy.getDsssRate11Mbps());
		modes.add(WifiPhy.getOfdmRate6Mbps());
		modes.add(WifiPhy.getOfdmRate9Mbps());
		modes.add(WifiPhy.getOfdmRate12Mbps());
		modes.add(WifiPhy.getOfdmRate18Mbps());
		modes.add(WifiPhy.getOfdmRate24Mbps());
		modes.add(WifiPhy.getOfdmRate36Mbps());
		modes.add(WifiPhy.getOfdmRate48Mbps());
		modes.add(WifiPhy.getOfdmRate54Mbps());

		for (WifiMode mode : modes) {
			System.out.println(mode.getUniqueUid());
			for (double snr = -5; snr < 30; snr += 0.01) {
				double successRate = yans.getChunkSuccessRate(mode,
						Math.pow(10.0, snr / 10.0), frameSize * 8);
				fYans.append(snr + " " + successRate + "\n");
				successRate = nist.getChunkSuccessRate(mode,
						Math.pow(10.0, snr / 10.0), frameSize * 8);
				fNist.append(snr + " " + successRate + "\n");
			}
			fYans.append("e\n");
			fNist.append("e\n");
		}
		fYans.flush();
		fYans.close();
		fNist.flush();
		fNist.close();
	}

	/**
	 * The Header is used from the NS3 created pls files.
	 * 
	 */
	private static void writeHeader(String description, FileWriter fw)
			throws IOException {
		fw.append("set terminal postscript eps color enh \"Times-BoldItalic\"\n");
		fw.append("set output '" + description + ".eps'\n");
		fw.append("set xlabel 'SNR(dB)'\n");
		fw.append("set ylabel 'Frame Success Rate'\n");
		fw.append("set xrange [-5:30]\n");
		fw.append("set yrange [0:1.2]\n");
		fw.append("set style line 1 linewidth 5\n");
		fw.append("set style line 2 linewidth 5\n");
		fw.append("set style line 3 linewidth 5\n");
		fw.append("set style line 4 linewidth 5\n");
		fw.append("set style line 5 linewidth 5\n");
		fw.append("set style line 6 linewidth 5\n");
		fw.append("set style line 7 linewidth 5\n");
		fw.append("set style line 8 linewidth 5\n");
		fw.append("set style line 9 linewidth 5\n");
		fw.append("set style line 10 linewidth 5\n");
		fw.append("set style line 11 linewidth 5\n");
		fw.append("set style line 12 linewidth 5\n");
		fw.append("set style increment user\n");
		fw.append("plot '-'  title 'DsssRate1Mbps' with lines, '-'  title 'DsssRate2Mbps' with lines, '-'  title 'DsssRate5.5Mbps' with lines, '-'  title 'DsssRate11Mbps' with lines, '-'  title 'OfdmRate6Mbps' with lines, '-'  title 'OfdmRate9Mbps' with lines, '-'  title 'OfdmRate12Mbps' with lines, '-'  title 'OfdmRate18Mbps' with lines, '-'  title 'OfdmRate24Mbps' with lines, '-'  title 'OfdmRate36Mbps' with lines, '-'  title 'OfdmRate48Mbps' with lines, '-'  title 'OfdmRate54Mbps' with lines\n");
	}
}
