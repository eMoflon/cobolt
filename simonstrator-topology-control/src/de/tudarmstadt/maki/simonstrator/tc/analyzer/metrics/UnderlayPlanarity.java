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

import com.panayotis.gnuplot.JavaPlot;

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.IEdge;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

/**
 * 
 * @author Michael Stein
 *
 */
public class UnderlayPlanarity implements Metric {

	private boolean planar;

	@Override
   public void compute(final Graph initialUnderlay, final Graph resultUnderlay) {

		IEdge[] edges = new IEdge[resultUnderlay.getEdges().size()];
		edges = resultUnderlay.getEdges().toArray(edges);
		for (int i = 0; i < edges.length - 1; i++) {
			for (int j = i + 1; j < edges.length; j++) {
				final IEdge e1 = edges[i];
				final INode node1 = resultUnderlay.getNode(e1.fromId());
				final double x1 = node1.getProperty(SiSTypes.PHY_LOCATION).getLongitude();
				final double y1 = node1.getProperty(SiSTypes.PHY_LOCATION).getLatitude();
				final INode node2 = resultUnderlay.getNode(e1.toId());
				final double x2 = node2.getProperty(SiSTypes.PHY_LOCATION).getLongitude();
				final double y2 = node2.getProperty(SiSTypes.PHY_LOCATION).getLatitude();

				final IEdge e2 = edges[j];
				final INode node3 = resultUnderlay.getNode(e2.fromId());
				final double x3 = node3.getProperty(SiSTypes.PHY_LOCATION).getLatitude();
				final double y3 = node3.getProperty(SiSTypes.PHY_LOCATION).getLatitude();
				final INode node4 = resultUnderlay.getNode(e2.toId());
				final double x4 = node4.getProperty(SiSTypes.PHY_LOCATION).getLongitude();
				final double y4 = node4.getProperty(SiSTypes.PHY_LOCATION).getLatitude();

				if (overlap(x1, y1, x2, y2, x3, y3, x4, y4)) {
					this.planar = false;
					return;
				}

			}
		}

		this.planar = true;
	}

	// private boolean overlap(double A_x1, double A_y1, double A_x2, double
	// A_y2,
	// double B_x1, double B_y1, double B_x2, double B_y2) {

	private boolean overlap(final double x1, final double y1, final double x2, final double y2, final double x3,
			final double y3, final double x4, final double y4) {

		// shared points. this is a special case. one line starts where another
		// line ends
		if ((x1 == x3 && y1 == y3) || (x2 == x3 && y2 == y3) || (x1 == x4 && y1 == y4) || (x2 == x4 && y2 == y4)) {
			return false;
		}

		// http://stackoverflow.com/questions/16314069/calculation-of-intersections-between-line-segments

		final double a1 = (y2 - y1) / (x2 - x1);
		final double b1 = y1 - a1 * x1;
		final double a2 = (y4 - y3) / (x4 - x3);
		final double b2 = y3 - a2 * x3;

		// are the lines parallel
		if (Math.abs((y2 - y1) * (x4 - x3) - (x2 - x1) * (y4 - y3)) < 1e-6) { // TODO tricky.
																// this has to
																// be modified
																// in order to
																// support
																// partial
																// overlappings
																// of parallel
																// line segments
			return false;
		} else {
			// intersection point x0
			final double x0 = -(b1 - b2) / (a1 - a2);
			return (Math.min(x1, x2) < x0 && x0 < Math.max(x1, x2)) && (Math.min(x3, x4) < x0 && x0 < Math.max(x3, x4));
		}
	}

	@Override
	public Iterable<JavaPlot> getPlots() {
		return null;
	}

   @Override
   public void writeResults(PropertyWriter resultWriter)
   {
      resultWriter.writeProperty("Planarity", this.planar);
   }
}
