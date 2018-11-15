package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics;

import com.panayotis.gnuplot.JavaPlot;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;
import de.tudarmstadt.maki.simonstrator.tc.graph.algorithm.TarjanSCC;


public class ConnectedMetric implements Metric {
	
	boolean connectedInitial, connectedResult;

	@Override
	public void compute(Graph initialUnderlay, Graph resultUnderlay) {
		connectedInitial = isConnected(initialUnderlay);
		connectedResult = isConnected(resultUnderlay);
	}
	
	public static boolean isConnected(Graph graph) {
		return new TarjanSCC(graph).getNumberOfSccs() == 1;
	}
	
	private double getDoubleValue(boolean connected) {
		return connected ? 1.0 : -1.0;
	}

	@Override
	public void writeResults(PropertyWriter resultWriter) {
		resultWriter.writeProperty("connectedUdg", getDoubleValue(connectedInitial));
		resultWriter.writeProperty("connectedResult", getDoubleValue(connectedResult));
	}

	@Override
	public Iterable<JavaPlot> getPlots() {
		return null;
	}

}
