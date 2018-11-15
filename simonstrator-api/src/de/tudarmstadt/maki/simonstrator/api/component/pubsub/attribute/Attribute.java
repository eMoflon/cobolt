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

package de.tudarmstadt.maki.simonstrator.api.component.pubsub.attribute;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;

/**
 * An attribute is a "pub/sub-parseable" key-value combination that can be used
 * to filter content.
 * 
 * @author Bjoern Richerzhagen
 * @param <T>
 *            value type
 */
public interface Attribute<T> extends Transmitable, Cloneable {

	/**
	 * Type of the attribute
	 * 
	 * @return
	 */
	public Class<T> getType();

	/**
	 * Name of the attribute (identifier)
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * (typed) value of the attribute
	 * 
	 * @return
	 */
	public T getValue();

	/**
	 * Convenience method to create the same attribute (i.e., same name and same
	 * type) but with a new value.
	 * 
	 * @param value
	 * @return
	 */
	public Attribute<T> create(T value);

	/**
	 * Cloning of an attribute
	 * 
	 * @return
	 */
	public Attribute<T> clone();

}
