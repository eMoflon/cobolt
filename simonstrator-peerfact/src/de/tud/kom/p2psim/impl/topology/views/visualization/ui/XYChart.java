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

package de.tud.kom.p2psim.impl.topology.views.visualization.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DeviationRenderer;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.YIntervalSeries;
import org.jfree.data.xy.YIntervalSeriesCollection;

/**
 * Assists in creating a very simple XYSeries plot. It basically
 * just holds a reference to the JFreeChart, ChartPanel and the dataset.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 30.07.2012
 */
public class XYChart {
	private JFreeChart chart;
	private YIntervalSeriesCollection dataset;
	private ChartPanel chartPanel;
	
	public XYChart(Color plotBackgroundColor, String name) {
		this(plotBackgroundColor, name, name);
	}
	
	public XYChart(Color plotBackgroundColor, String name, String seriesName) {
		dataset = new YIntervalSeriesCollection();
		dataset.addSeries(new YIntervalSeries(seriesName));

		chart = ChartFactory.createXYLineChart(null, "Time [h:m:s]", null, dataset, PlotOrientation.VERTICAL, true, true, false);
		chart.setBackgroundPaint(plotBackgroundColor);
		
		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(plotBackgroundColor);
		
		NumberAxis axis = ((NumberAxis)plot.getRangeAxis());

		//axis.setLabelInsets(insets);
		//axis.setAutoRange(false);
		//axis.setRange(0, 900);
		
		//((NumberAxis)plot.getRangeAxis()).setNumberFormatOverride(new TruncatingNumberFormater());
		axis.setStandardTickUnits(createTickUnitSource());
		
		//NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		//rangeAxis.setStandardTickUnits(createTickUnitSource()); //NumberAxis.createIntegerTickUnits());
		//rangeAxis.setLabelFont(new Font("monospace", Font.PLAIN, 9));
		//plot.getRangeAxis(0).setUpperMargin(10);
		//plot.getRangeAxis(0).setLowerMargin(10);
		
		// LegendItemCollection lic = new LegendItemCollection();
		// lic.add(new LegendItem("Reference Value", Color.blue));
		// lic.add(new LegendItem("Avg. Monitored Value", Color.red));

		// plot.setFixedLegendItems(lic);
		//plot.setFixedLegendItems(new LegendItemCollection());
		
		DeviationRenderer errorRenderer = new DeviationRenderer(true, true);
		errorRenderer.setSeriesStroke(0, new BasicStroke(3F, 1, 1));
        errorRenderer.setSeriesStroke(1, new BasicStroke(3F, 1, 1));
		errorRenderer.setSeriesStroke(2, new BasicStroke(3F, 1, 1));
        errorRenderer.setSeriesFillPaint(0, new Color(255, 200, 200));
        errorRenderer.setSeriesFillPaint(1, new Color(200, 200, 255));
		errorRenderer.setSeriesFillPaint(2, new Color(200, 255, 200));
        errorRenderer.setSeriesPaint(0, new Color(255, 0, 0));
        errorRenderer.setSeriesPaint(1, new Color(0, 0, 255));
		errorRenderer.setSeriesPaint(2, new Color(0, 255, 0));
		//errorRenderer.setShapesVisible(false);
		//errorRenderer.setLinesVisible(true);
		//errorRenderer.setAlpha(0.0f);
		// errorRenderer.setDrawYError(false);
		// errorRenderer.setDrawXError(false);
		
		
		plot.setRenderer(errorRenderer);
		
		chartPanel = new ChartPanel(chart, false);
	}
	
	public void setGridVisible(boolean visible) {
		XYPlot plot = chart.getXYPlot();
		plot.setDomainGridlinesVisible(visible);
		plot.setRangeGridlinesVisible(visible);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinePaint(Color.GRAY);
	}
	
	public ValueAxis getRangeAxis() {
		XYPlot plot = (XYPlot) chart.getPlot();
		
		return plot.getRangeAxis();
	}
	
	public ValueAxis getDomainAxis() {
		XYPlot plot = (XYPlot) chart.getPlot();
		
		return plot.getDomainAxis();
	}
	
	private static class TruncatingNumberFormater extends NumberFormat {
		@Override
		public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Number parse(String source, ParsePosition pos) {
			return null;
		}
		
	}
	
    //http://www.jfree.org/jfreechart/api/gjdoc/org/jfree/chart/axis/NumberAxis-source.html
	private TickUnitSource createTickUnitSource() {
        TickUnits units = new TickUnits();
        DecimalFormat df0 = new DecimalFormat("0.00000000");
        DecimalFormat df1 = new DecimalFormat("0.0000000");
        DecimalFormat df2 = new DecimalFormat("0.000000");
        DecimalFormat df3 = new DecimalFormat("0.00000");
        DecimalFormat df4 = new DecimalFormat("0.0000");
        DecimalFormat df5 = new DecimalFormat("0.000");
        DecimalFormat df6 = new DecimalFormat("0.00");
        DecimalFormat df7 = new DecimalFormat("0.0");
        DecimalFormat df8 = new DecimalFormat("#,##0");
        DecimalFormat df9 = new DecimalFormat("#,###,##0");
        DecimalFormat df10 = new DecimalFormat("#,###,###,##0");
        
        // we can add the units in any order, the TickUnits collection will 
        // sort them...
        units.add(new NumberTickUnit(0.0000001, df1));
        units.add(new NumberTickUnit(0.000001, df2));
        units.add(new NumberTickUnit(0.00001, df3));
        units.add(new NumberTickUnit(0.0001, df4));
        units.add(new NumberTickUnit(0.001, df5));
        units.add(new NumberTickUnit(0.01, df6));
        units.add(new NumberTickUnit(0.1, df7));
        units.add(new NumberTickUnit(1, df8));
        units.add(new NumberTickUnit(10, df8));
        units.add(new NumberTickUnit(100, df8));
        units.add(new NumberTickUnit(1000, df8));
        units.add(new NumberTickUnit(10000, df8));
        units.add(new NumberTickUnit(100000, df8));
        units.add(new NumberTickUnit(1000000, df9));
        units.add(new NumberTickUnit(10000000, df9));
        units.add(new NumberTickUnit(100000000, df9));
        units.add(new NumberTickUnit(1000000000, df10));
        units.add(new NumberTickUnit(10000000000.0, df10));
        units.add(new NumberTickUnit(100000000000.0, df10));
        
        units.add(new NumberTickUnit(0.00000025, df0));
        units.add(new NumberTickUnit(0.0000025, df1));
        units.add(new NumberTickUnit(0.000025, df2));
        units.add(new NumberTickUnit(0.00025, df3));
        units.add(new NumberTickUnit(0.0025, df4));
        units.add(new NumberTickUnit(0.025, df5));
        units.add(new NumberTickUnit(0.25, df6));
        units.add(new NumberTickUnit(2.5, df7));
        units.add(new NumberTickUnit(25, df8));
        units.add(new NumberTickUnit(250, df8));
        units.add(new NumberTickUnit(2500, df8));
        units.add(new NumberTickUnit(25000, df8));
        units.add(new NumberTickUnit(250000, df8));
        units.add(new NumberTickUnit(2500000, df9));
        units.add(new NumberTickUnit(25000000, df9));
        units.add(new NumberTickUnit(250000000, df9));
        units.add(new NumberTickUnit(2500000000.0, df10));
        units.add(new NumberTickUnit(25000000000.0, df10));
        units.add(new NumberTickUnit(250000000000.0, df10));

        units.add(new NumberTickUnit(0.0000005, df1));
        units.add(new NumberTickUnit(0.000005, df2));
        units.add(new NumberTickUnit(0.00005, df3));
        units.add(new NumberTickUnit(0.0005, df4));
        units.add(new NumberTickUnit(0.005, df5));
        units.add(new NumberTickUnit(0.05, df6));
        units.add(new NumberTickUnit(0.5, df7));
        units.add(new NumberTickUnit(5L, df8));
        units.add(new NumberTickUnit(50L, df8));
        units.add(new NumberTickUnit(500L, df8));
        units.add(new NumberTickUnit(5000L, df8));
        units.add(new NumberTickUnit(50000L, df8));
        units.add(new NumberTickUnit(500000L, df8));
        units.add(new NumberTickUnit(5000000L, df9));
        units.add(new NumberTickUnit(50000000L, df9));
        units.add(new NumberTickUnit(500000000L, df9));
        units.add(new NumberTickUnit(5000000000L, df10));
        units.add(new NumberTickUnit(50000000000L, df10));
        units.add(new NumberTickUnit(500000000000L, df10));

        return units;
	}
	
	public ChartPanel getChartPanel() {
		return this.chartPanel;
	}
	
	public BufferedImage getChartImage(int width, int height) {
		ChartRenderingInfo info = new ChartRenderingInfo();
		return chart.createBufferedImage(width, height, info);
	}

	public YIntervalSeriesCollection getDataset() {
		return dataset;
	}

	public void addDatasetChangeListener(DatasetChangeListener listener) {
		dataset.addChangeListener(listener);
	}
	
	public void removePlotUpdateListener(DatasetChangeListener listener) {
		dataset.removeChangeListener(listener);
	}
}
