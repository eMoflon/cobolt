/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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

import java.util.Comparator;

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * This comparator compares {@link IElement}s based on the configured numeric
 * {@link GraphElementProperty} (see also
 * {@link #GraphElementPropertyBasedComparator(GraphElementProperty)}).
 * 
 * @author Roland Kluge Original implementation
 */
public class GraphElementPropertyBasedComparator implements Comparator<IElement> {

	private final SiSType<? extends Number> property;

	/**
	 * Initializes the {@link GraphElementProperty} to use for comparing
	 * {@link IElement}s
	 * 
	 * @param property
	 *            the property to use
	 */
	public GraphElementPropertyBasedComparator(final SiSType<? extends Number> property) {
		this.property = property;
	}

	/**
	 * Returns the configured property
	 */
	public SiSType<? extends Number> getProperty() {
		return property;
	}

	/**
	 * Compares the two elements based on the configured
	 * {@link GraphElementProperty}.
	 * 
	 * Both elements need to provide the property, otherwise, an exception is
	 * thrown.
	 * 
	 * The result is equivalent to comparing the double values of both elements
	 * using {@link Double#compare(double, double)}.
	 * 
	 * @param o1
	 *            the first element
	 * @param o2
	 *            the second element
	 * @return the comparison result
	 * 
	 * @throws MissingPropertyException
	 *             if one of the elements does not expose the property
	 */
	@Override
	public int compare(final IElement o1, final IElement o2) {

		GraphElementProperties.validateThatPropertyIsPresent(o1, property);
		GraphElementProperties.validateThatPropertyIsPresent(o2, property);
		return Double.compare(o1.getProperty(property).doubleValue(), o2.getProperty(property).doubleValue());
	}

}
