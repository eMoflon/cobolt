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

import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSType;

/**
 * Common base interface for an element in the graph
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface IElement {

	/**
	 * Get the value associated with the given property at this graph element.
	 * Null, if the property was not set.
	 * 
	 * @param property
	 * @return
	 */
	public <T> T getProperty(SiSType<T> property);

	/**
	 * Set (or overwrite) a property with the provided value. If the value ===
	 * null, the property is removed.
	 * 
	 * @param property
	 * @param value
	 * @return self for chaining
	 */
	public <T> IElement setProperty(SiSType<T> property, T value);

	/**
	 * Returns all {@link SiSType}s that are annotated to the graph element
	 * 
	 * @return
	 */
	public Map<SiSType<?>, Object> getProperties();
	
	/**
	 * Adds the properties from the other {@link IElement} to our current
	 * properties. If a property already exists locally, its value is replaced
	 * by the other value.
	 * 
	 * @param other
	 */
	public void addPropertiesFrom(IElement other);

	/**
	 * Deletes all associated properties from this IElement
	 */
	public void clearProperties();

}
