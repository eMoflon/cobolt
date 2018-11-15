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
import de.tudarmstadt.maki.simonstrator.tc.analyzer.writer.PropertyWriter;

/**
 * 
 * @author Michael Stein
 *
 */
public class UnderlayEdgeCount implements Metric
{

   private int edgeCount;

   @Override
   public void compute(final Graph initialUnderlay, final Graph resultUnderlay)
   {
      this.edgeCount = resultUnderlay.getEdges().size();
   }

   @Override
   public void writeResults(final PropertyWriter resultWriter)
   {
      resultWriter.writeProperty("UnderlayEdgeCount", edgeCount);
   }

   @Override
   public Iterable<JavaPlot> getPlots()
   {
      return null;
   }

}
