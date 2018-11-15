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

import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.javaplot.DefaultPlots;
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

/**
 * 
 * @author Michael Stein
 *
 */
public class UnderlayDegree implements Metric
{

   private double averageDegree;

   private int maxDegree = Integer.MIN_VALUE;

   private final List<Integer> underlayDegreeValues = new ArrayList<Integer>();

   @Override
   public void compute(final Graph initialUnderlay, final Graph resultUnderlay)
   {

      this.averageDegree = ((double) (resultUnderlay.getEdges().size()) / resultUnderlay.getNodes().size()) * 2;
		for (final INodeID node : resultUnderlay.getNodeIds())
      {
         final int degree = resultUnderlay.getIncomingEdges(node).size() + resultUnderlay.getOutgoingEdges(node).size();
         underlayDegreeValues.add(degree);
         if (degree > this.maxDegree)
         {
            this.maxDegree = degree;
         }
      }
   }

   @Override
   public void writeResults(final PropertyWriter resultWriter)
   {
      resultWriter.writeComment("The following two metric refer to ALL nodes");
      resultWriter.writeProperty("MaxUnderlayDegree", maxDegree);
      resultWriter.writeProperty("AverageUnderlayDegree", averageDegree);

      resultWriter.writeComment("");
      resultWriter.writeComment("the degree values per node:");
//      resultWriter.writeProperty("PerNodeUnderlayDegreeValues", this.underlayDegreeValues);

   }

   @Override
   public Iterable<JavaPlot> getPlots()
   {

      final List<JavaPlot> plots = new LinkedList<JavaPlot>();
      final JavaPlot plot = DefaultPlots.getHistogramPlot(this.underlayDegreeValues, "Underlay Degree Histogram", "Degree", "Number of Nodes");
      plots.add(plot);
      return plots;
   }

}
