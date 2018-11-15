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

package de.tudarmstadt.maki.simonstrator.tc.analyzer.javaplot;

import java.util.Collection;
import java.util.Iterator;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.ArrayDataSet;
import com.panayotis.gnuplot.plot.DataSetPlot;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Smooth;
import com.panayotis.gnuplot.style.Style;

/**
 * 
 * @author Michael Stein
 *
 */
public class DefaultPlots {

	public static JavaPlot getCfgPlot(Collection<Double> values, String title,
			String xLabel, String yLabel) {

		double[] arr = new double[values.size()];

		Iterator<Double> it = values.iterator();
		int i = 0;
		while (it.hasNext()) {
			arr[i] = it.next();
			i++;
		}

		return getCfgPlot(arr, title, xLabel, yLabel);
	}

	public static JavaPlot getCfgPlot(double[] values, String title,
			String xLabel, String yLabel) {

		JavaPlot p = new JavaPlot();
		// JavaPlot.getDebugger().setLevel(Debug.VERBOSE);

		int valueCount = values.length;
		double yValue = 1.0 / valueCount;
		double maxValue = Double.MIN_VALUE;
		double[][] valueSet = new double[valueCount][2];
		for (int i = 0; i < valueCount; i++) {
			valueSet[i][0] = values[i];
			valueSet[i][1] = yValue;

			if (values[i] > maxValue)
				maxValue = values[i];
		}

		// labeling
		p.setTitle(title);
		p.getAxis("x").setLabel(xLabel);
		p.getAxis("y").setLabel(yLabel);

		p.setKey(JavaPlot.Key.OFF);

		// boundaries
		p.getAxis("x").setBoundaries(0.0, maxValue);
		p.getAxis("y").setBoundaries(0.0, 1.0);
		
		DataSetPlot s = new DataSetPlot(new ArrayDataSet(valueSet));
		s.setPlotStyle(new PlotStyle(Style.LINESPOINTS));
		// CDF (use this hack, as "cumulative" is not available in enumeration)
		s.set("smooth", "cumulative");

		p.addPlot(s);

		return p;
	}
	
	public static JavaPlot getHistogramPlot(Collection<Integer> values, String title,
			String xLabel, String yLabel) {

		int[] arr = new int[values.size()];

		Iterator<Integer> it = values.iterator();
		int i = 0;
		while (it.hasNext()) {
			arr[i] = it.next();
			i++;
		}

		return getHistogramPlot(arr, title, xLabel, yLabel);
	}
	
	
	

	public static JavaPlot getHistogramPlot(int[] values, String title,
			String xLabel, String yLabel) {

		// max value
		int max = 0;
		int[][] arrValues = new int[values.length][2];
		for (int i = 0; i < values.length; i++) {
			if (values[i] > max)
				max = values[i];
			arrValues[i][0] = values[i];

			// notwendig??
			arrValues[i][1] = 1;
		}

		JavaPlot p = new JavaPlot();
		//JavaPlot.getDebugger().setLevel(Debug.VERBOSE);
		
		// labeling
		p.setTitle(title);
		p.getAxis("x").setLabel(xLabel);
		p.getAxis("y").setLabel(yLabel);

		p.setKey(JavaPlot.Key.OFF);

		// boundaries
		p.getAxis("x").setBoundaries(0.0, max);
		// set(getName() + "range", "[" + from + ":" + to + "]");
		p.getAxis("y").set("yrange", "[0:]");
				
				


		DataSetPlot s = new DataSetPlot(new ArrayDataSet(arrValues));
		s.setPlotStyle(new PlotStyle(Style.BOXES));
		s.setSmooth(Smooth.FREQUENCY);

		p.addPlot(s);

		return p;
	}

	// test
	public static void main(String[] args) {
		int[] values = {1,1,2,2,2,3,3,3,3,10};
		JavaPlot plot = getHistogramPlot(values, "Simple Histogram", "My values", "My count");
		plot.plot();
	}
}
