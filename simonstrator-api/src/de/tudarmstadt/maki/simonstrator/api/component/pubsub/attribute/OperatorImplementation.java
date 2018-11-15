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

/**
 * Implementation of a {@link FilterOperator} for a given Type of attributes.
 * Default implementations for basic types (Strings, Numbers) are already
 * provided but can nevertheless be overwritten with this method. Has to be
 * passed to the pub/sub overlay.
 * 
 * @author Bjoern Richerzhagen
 */
public interface OperatorImplementation {

	/**
	 * Returns true, if the given candidate fulfills the operation contract with
	 * the provided filter. Example: if filter == int 3 and candidate == int 4,
	 * this method has to return false when implementing a LT-filter (4 < 3 ==
	 * false)
	 * 
	 * @param filter
	 * @param candidate
	 * @return
	 */
	public boolean matches(Attribute<?> filter, Attribute<?> candidate);

}
