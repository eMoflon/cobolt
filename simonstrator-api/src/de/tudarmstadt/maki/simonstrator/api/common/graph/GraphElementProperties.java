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

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;
import de.tudarmstadt.maki.simonstrator.api.util.UtilityClassNotInstantiableException;

/**
 * Utility methods related to {@link GraphElementProperty}.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public final class GraphElementProperties {

	// Disabled constructor
	private GraphElementProperties() {
		throw new UtilityClassNotInstantiableException();
	}

	/**
	 * Checks whether the given element provides the given property.
	 * 
	 * If yes, nothing happens, if no an appropriate
	 * {@link MissingPropertyException} is thrown.
	 * 
	 * @param element
	 *            the element to be checked
	 * @param property
	 *            the property that should be available
	 * @throws MissingPropertyException
	 *             if the given element does not provide the given property
	 */
	public static <T> void validateThatPropertyIsPresent(final IElement element,
			final SiSType<T> property) {
		if (!hasProperty(element, property))
			throw new MissingPropertyException(element, property);

	}

	/**
	 * Returns whether the given element exposes the given property
	 * 
	 * @param element
	 *            the element to check
	 * @param property
	 *            the expected property
	 * @return true if element exposes property
	 */
	public static <T> boolean hasProperty(final IElement element, SiSType<T> property) {
		return element.getProperty(property) != null;
	}
}
