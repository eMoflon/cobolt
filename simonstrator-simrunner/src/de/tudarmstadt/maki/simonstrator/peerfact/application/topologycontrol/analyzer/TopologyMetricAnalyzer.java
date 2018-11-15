package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView;
import de.tud.kom.p2psim.impl.topology.views.LogicalWifiTopologyView.LogicalWiFiTopology;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;
import de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.analyzer.TopologyControlAnalyzer;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.Metric;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.NodeCount;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.UnderlayDegree;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.UnderlayEdgeCount;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.UnderlayEdgeLength;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.HopSpanner;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.LengthSpanner;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics.spanner.PowerSpanner;

public class TopologyMetricAnalyzer implements Analyzer, EventHandler {

	ArrayList<Class<? extends Metric>> metrics = new ArrayList<Class<? extends Metric>>();

	{
		metrics.add(UnderlayDegree.class);
		metrics.add(UnderlayEdgeLength.class);
		metrics.add(NodeCount.class);
		metrics.add(UnderlayEdgeCount.class);
		metrics.add(HopSpanner.class);
		metrics.add(LengthSpanner.class);
		metrics.add(PowerSpanner.class);
	}

	private boolean shallPlotMetrics = false;

	/**
	 * Root folder which contains all simulation results
	 */
	private String rootFolder;
	private double eventOffsetInMinutes;
	private double measurementIntervalInMinutes;

	public TopologyMetricAnalyzer() {
	}

	@XMLConfigurableConstructor({ "rootFolder", "analyzerIntervalInMinutes", "measurementIntervalInMinutes" })
	public TopologyMetricAnalyzer(final String rootFolder, final double analyzerIntervalInMinutes,
			final double measurementIntervalInMinutes) {
		this.rootFolder = rootFolder;
		this.eventOffsetInMinutes = analyzerIntervalInMinutes;
		this.measurementIntervalInMinutes = measurementIntervalInMinutes;
	}

	public void setMetrics(final ArrayList<Class<? extends Metric>> metrics) {
		this.metrics = metrics;
	}

	public void setRootFolder(final String rootFolder) {
		this.rootFolder = rootFolder;
	}

	public void setEventOffsetInMinutes(final double eventOffsetInMinutes) {
		this.eventOffsetInMinutes = eventOffsetInMinutes;
	}

	public void setMeasurementIntervalInMinutes(final double measurementIntervalInMinutes) {
		this.measurementIntervalInMinutes = measurementIntervalInMinutes;
	}

	public void setShallPlotMetrics(final boolean shallPlotMetrics) {
		this.shallPlotMetrics = shallPlotMetrics;
	}

	@Override
	public void start() {
		Simulator.getScheduler().scheduleIn((long) (this.eventOffsetInMinutes * Simulator.MINUTE_UNIT), this, null, 0);
	}

	@Override
	public void stop(final Writer out) {
		//
	}

	@Override
	public void eventOccurred(final Object content, final int type) {
		writeAllMetrics();
		Event.scheduleWithDelay((long) (this.measurementIntervalInMinutes * Simulator.MINUTE_UNIT), this, null, 0);
	}

	private void writeAllMetrics() {

		Monitor.log(getClass(), Level.INFO, "Recording topology analysis data");

		// folder that contains all results form this simulation run
		final String thisSimulationPath = rootFolder + "/seed=" + Simulator.getSeed() + "/time="
				+ Simulator.getCurrentTime();

		// delete directory if it exists
		try {
			final File file = new File(thisSimulationPath);
			FileUtils.deleteDirectory(file);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}

		createDirectories(thisSimulationPath);

		final Graph udgTopology = GlobalOracle.getTopology(LogicalWiFiTopology.class,
				LogicalWifiTopologyView.getUDGTopologyID());
		final Graph modifiedTopology = GlobalOracle.getTopology(LogicalWiFiTopology.class,
				LogicalWifiTopologyView.getAdaptableTopologyID());

		// // top folder for initial topology
		// final String initialTopologyPath = thisSimulationPath + "/inital";
		// createDirectories(initialTopologyPath);
		// TopologyControlAnalyzer.analyze(initialTopologyPath, udgTopology,
		// null, udgTopology, null, this.metrics,
		// this.PLOT_METRICS);

		final String modifiedTopologyPath = thisSimulationPath; // + "/TC";
		createDirectories(modifiedTopologyPath);
		TopologyControlAnalyzer.analyze(modifiedTopologyPath, udgTopology, null, modifiedTopology, null, this.metrics,
				this.shallPlotMetrics);
	}

	/**
	 * Creates directory and parent directories if they don't exist
	 * 
	 * @param directory
	 */
	public void createDirectories(final String directory) {
		final File dir = new File(directory);
		if (!dir.exists()) {
			dir.mkdirs();
		}
	}

}
