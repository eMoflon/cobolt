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

/**
 * This exception may be used to indicate that a necessary property of an
 * {@link INode} or {@link IEdge} is not set.
 * 
 * @author Roland Kluge - Initial Implementation
 */
public class MissingPropertyException extends RuntimeException {

	/**
	 * Creates an exception to indicate that the given {@link IElement} is
	 * lacking the given {@link GraphElementProperty}.
	 * 
	 * @param element
	 *            the element
	 * @param property
	 *            the property
	 */
	public <T> MissingPropertyException(final IElement element, final SiSType<T> property) {
		super(String.format("Element %s is missing required property %s", element.toString(), property));
	}

	private static final long serialVersionUID = 8622616464282511495L;
}
