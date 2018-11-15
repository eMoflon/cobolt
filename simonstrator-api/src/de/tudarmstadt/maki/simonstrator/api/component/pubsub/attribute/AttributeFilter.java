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
 * Filter for a single {@link Attribute}, consisting of an attribute and a
 * {@link FilterOperator}.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface AttributeFilter<T> extends Transmitable, Cloneable {

	/**
	 * Operator of this filter
	 * 
	 * @return
	 */
	public FilterOperator getOperator();

	/**
	 * Attribute that the filter matches against
	 * 
	 * @return
	 */
	public Attribute<T> getAttribute();

	/**
	 * Clones the attribute filter
	 * 
	 * @return
	 */
	public AttributeFilter<T> clone();

}
