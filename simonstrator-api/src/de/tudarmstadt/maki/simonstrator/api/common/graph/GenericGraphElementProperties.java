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
 * This interface collects widely used graph element properties
 */
public interface GenericGraphElementProperties {

	/**
	 * Edge weight
	 */
	public static final GraphElementProperty<Double> WEIGHT = new GraphElementProperty<>("WEIGHT", Double.class);

	/**
	 * Hop count
	 */
	public static final GraphElementProperty<Double> HOP_COUNT = new GraphElementProperty<>("hopCount", Double.class);

	/**
	 * This property points to the reverse edge of an edge
	 * 
	 * @deprecated (since 2.5) Use {@link Graph#makeInverseEdges(IEdge, IEdge)}
	 *             etc. because this property is dangerous when edges are
	 *             copied!
	 */
	@Deprecated
	GraphElementProperty<IEdge> REVERSE_EDGE = new GraphElementProperty<IEdge>("reverseEdge", IEdge.class);
}
