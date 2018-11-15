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

package de.tudarmstadt.maki.simonstrator.tc.analyzer.metrics;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.panayotis.gnuplot.JavaPlot;

import de.tudarmstadt.maki.simonstrator.api.common.graph.GenericGraphElementProperties;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.javaplot.DefaultPlots;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

/**
 * 
 * @author Michael Stein
 *
 */
public class UnderlayEdgeLength implements Metric {

	double maxMaxEdgeLength = -1.0;

	double allAverageEdgeLength = -1.0;

	List<Double> perNodeMaxEdgeLengthValues = new ArrayList<Double>();

	private double avgMaxEdgeLength = -1.0;

	// private double totalLinkLengthRelativeInitial = -1.0;

	// private static double getTotalLinkLength(final Graph underlay) {
	// double sum = 0.0;
	// for (final IEdge edge : underlay.getEdges()) {
	// sum += edge.getProperty(GenericGraphElementProperties.WEIGHT);
	// }
	// return sum;
	// }

	@Override
	public void compute(final Graph initialUnderlay, final Graph resultUnderlay) {

		double sum = 0.0;
		for (final IEdge edge : resultUnderlay.getEdges()) {
			sum += edge.getProperty(GenericGraphElementProperties.WEIGHT);
			if (edge.getProperty(GenericGraphElementProperties.WEIGHT) > maxMaxEdgeLength) {
				maxMaxEdgeLength = edge.getProperty(GenericGraphElementProperties.WEIGHT);
			}
		}
		this.allAverageEdgeLength = sum / resultUnderlay.getEdges().size();

		// identify longest edge for each node
		sum = 0.0;
		int count = 0;
		for (final INodeID node : resultUnderlay.getNodeIds()) {
			double max = -1.0;
			for (final IEdge edge : resultUnderlay.getOutgoingEdges(node)) {
				Double weight = edge.getProperty(GenericGraphElementProperties.WEIGHT);
				if (weight > max) {
					max = weight;
				}
			}
			this.perNodeMaxEdgeLengthValues.add(max);
			sum += max;
			count += 1;
		}
		this.avgMaxEdgeLength = sum / count;

		// this.totalLinkLengthRelativeInitial =
		// getTotalLinkLength(resultUnderlay) /
		// getTotalLinkLength(initialUnderlay);
	}

	public static double getAvgMaxUnderlayEdgeLength(Graph graph) {
		UnderlayEdgeLength metric = new UnderlayEdgeLength();
		metric.compute(null, graph);
		return metric.avgMaxEdgeLength;
	}

	@Override
	public void writeResults(final PropertyWriter resultWriter) {
		resultWriter.writeComment("The following two metrics refer to ALL nodes:");
		resultWriter.writeProperty("MaxMaxUnderlayEdgeLength", maxMaxEdgeLength);
		resultWriter.writeProperty("AvgMaxUnderlayEdgeLength", avgMaxEdgeLength);
		resultWriter.writeProperty("AvgUnderlayEdgeLength", allAverageEdgeLength);
		// resultWriter.writeProperty("TotalLinkLengthRelativeInitial",
		// totalLinkLengthRelativeInitial);

		resultWriter.writeComment("The following metric refers to single Nodes");
		// resultWriter.writeProperty("PerNodeMaxUnderlayEdgeLength",
		// this.perNodeMaxEdgeLengthValues);
	}

	@Override
	public Iterable<JavaPlot> getPlots() {
		final List<JavaPlot> plots = new LinkedList<JavaPlot>();
		plots.add(DefaultPlots.getCfgPlot(this.perNodeMaxEdgeLengthValues, "Max Edge Length CFG", "Max Edge Length",
				"Probability"));

		return plots;
	}

}
