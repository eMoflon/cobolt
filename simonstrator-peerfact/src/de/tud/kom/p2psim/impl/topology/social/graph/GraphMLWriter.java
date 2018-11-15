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

package de.tud.kom.p2psim.impl.topology.social.graph;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.graph.DirectedGraph;

/**
 * Writes a given {@link DirectedGraph} to a file in the GraphML format!
 * 
 * @author Christoph Muenker
 * @version 1.0, 10.06.2013
 */
public class GraphMLWriter {
	public void write(DirectedGraph<SocialNode, SocialEdge> directedGraph,
			String filePath) throws IOException {

		edu.uci.ics.jung.io.GraphMLWriter<SocialNode, SocialEdge> graphWriter = new edu.uci.ics.jung.io.GraphMLWriter<SocialNode, SocialEdge>();

		PrintWriter pWriter = new PrintWriter(new BufferedWriter(
				new FileWriter(filePath)));
		graphWriter.setVertexIDs(new Transformer<SocialNode, String>() {

			@Override
			public String transform(SocialNode v) {
				return Integer.toString(v.getId());
			}
		});
		graphWriter.addVertexData("activity", null, "1",
				new Transformer<SocialNode, String>() {
					public String transform(SocialNode v) {
						return Double.toString(v.getActivity());
					}
				});

		graphWriter.addEdgeData("interaction", null, "1",
				new Transformer<SocialEdge, String>() {
					public String transform(SocialEdge e) {
						return Double.toString(e.getInteraction());
					}
				});
		graphWriter.save(directedGraph, pWriter);
	}
}
