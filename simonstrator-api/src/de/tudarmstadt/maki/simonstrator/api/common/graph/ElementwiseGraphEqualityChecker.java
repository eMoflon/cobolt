/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.common.graph;

/**
 * Equality checker for graphs that performs an element-wise comparison.
 * 
 * @author Roland Kluge - Initial Implementation
 *
 */
public class ElementwiseGraphEqualityChecker {

	/**
	 * Checks two {@link Graph}s for equality.
	 * 
	 * @param o1
	 *            first graph
	 * @param o2
	 *            second graph
	 * @return true if every {@link IElement} from o1 is contained in o2 and
	 *         vice versa
	 * 
	 * @see Graph#containsEdge(IEdge)
	 * @see Graph#containsNode(INode)
	 */
	public boolean equal(Graph o1, Graph o2) {
		if (o2.getNodeCount() != o1.getNodeCount() || o2.getEdgeCount() != o1.getEdgeCount())
			return false;

		for (final INode node : o1.getNodes()) {
			if (!o2.containsNode(node))
				return false;
		}

		for (final IEdge edge : o1.getEdges()) {
			if (!o2.containsEdge(edge))
				return false;
		}

		return false;
	}

	/**
	 * Utility method that negates the result of {@link #equal(Graph, Graph)}.
	 */
	public boolean unequal(Graph o1, Graph o2) {
		return !this.equal(o1, o2);
	};
}
