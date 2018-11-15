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

package de.tudarmstadt.maki.simonstrator.api.component.sis.type;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.AggregationNotPossibleException;

public interface SiSTypeAggregation<T extends Object> {

	/**
	 * Merges two values. Definition of a merge is up to the implementing
	 * function.
	 * 
	 * @param data
	 *            to be aggregated
	 * @return
	 */
	public T aggregate(Collection<T> data, AggregationFunction function)
			throws AggregationNotPossibleException;

}
