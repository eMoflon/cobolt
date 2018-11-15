/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.terminal.ImageTerminal;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.Metric;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertySerializer;

/**
 * 
 * @author Michael Stein
 *
 */
public class TopologyControlAnalyzer {

	/**
	 * 
	 * @param resultFilePath
	 *            the analyzer result path
	 * @param initialUnderlay
	 * @param initialOverlay
	 * @param resultUnderlay
	 * @param resultOverlay
	 * @param metrics
	 *            the list of metrics to be used
	 */
	public static void analyze(final String resultFolder, final Graph initialUnderlay, final Graph initialOverlay,
			final Graph resultUnderlay, final Graph resultOverlay, final Iterable<Class<? extends Metric>> metrics,
			final boolean plotMetrics) {

		final String resultFilePath = resultFolder + System.getProperty("file.separator") + "metrics.txt";

		final PropertySerializer propertyWriter = new PropertySerializer(resultFilePath);
		for (final Class<? extends Metric> metricClass : metrics) {
			Metric metric = null;
			try {
				metric = metricClass.newInstance();
			} catch (final Throwable e) {
				throw new ConfigurationException("unable to create Metric instance", e);
			}
			propertyWriter.writeComment("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			propertyWriter.writeComment(metricClass.getSimpleName());
			propertyWriter.writeComment("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			metric.compute(initialUnderlay, resultUnderlay);
			metric.writeResults(propertyWriter);
			propertyWriter.writeComment("");
			propertyWriter.writeComment("");

			if (plotMetrics) {
				plotMetric(resultFolder, metricClass, metric);
			}
		}
		propertyWriter.close();
	}

	private static void plotMetric(final String resultFolder, final Class<? extends Metric> metricClass,
			final Metric metric) {
		// plot metric
		final Iterable<JavaPlot> plots = metric.getPlots();
		if (plots != null) {
			int i = 1;
			for (final JavaPlot javaPlot : plots) {

				final ImageTerminal png = new ImageTerminal();
				final File file = new File(resultFolder + System.getProperty("file.separator") + "plot_"
						+ metricClass.getSimpleName() + i + ".png");
				try {
					file.createNewFile();
					png.processOutput(new FileInputStream(file));
				} catch (final FileNotFoundException ex) {
					System.err.print(ex);
				} catch (final IOException ex) {
					System.err.print(ex);
				}

				javaPlot.setTerminal(png);
				javaPlot.setPersist(false);

				try {
					javaPlot.plot();

					ImageIO.write(png.getImage(), "png", file);
				} catch (final Exception ex) {
					System.err.println("Java Plot Exeption for metric " + metricClass.getName());
					ex.printStackTrace();
				}

				i++;
			}
		}
	}
}
