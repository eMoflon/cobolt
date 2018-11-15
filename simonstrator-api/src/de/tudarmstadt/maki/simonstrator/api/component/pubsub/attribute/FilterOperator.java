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
 * Operators that are supported to filter attributes.
 * 
 * TODO add filters for times and locations?
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public enum FilterOperator implements Transmitable {

	/**
	 * Equal To == (should work on all attribute types)
	 */
	EQ,
	/**
	 * Not Equal != (should work on all attribute types)
	 */
	NEQ,
	/**
	 * Less Than < (only numerical)
	 */
	LT,
	/**
	 * Greater Than > (only numerical)
	 */
	GT,
	/**
	 * Less Than or Equal <= (only numerical)
	 */
	LTE,
	/**
	 * Greater Than or Equal >= (only numerical)
	 */
	GTE,
	/**
	 * Prefix Matching (only strings)
	 */
	PREFIX,
	/**
	 * Postfix Matching (only strings)
	 */
	POSTFIX,
	/**
	 * Contains (only strings and regions)
	 */
	CONTAINS,
	/**
	 * Intersects (only regions)
	 */
	INTERSECTS;

	@Override
	public int getTransmissionSize() {
		return 1;
	}

}