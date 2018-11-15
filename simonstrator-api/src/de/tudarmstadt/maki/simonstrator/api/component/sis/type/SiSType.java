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
import java.util.LinkedHashSet;
import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.AggregationNotPossibleException;

/**
 * Taxonomy definition for the SiS. Right now, we use a flat identifier space
 * and derivation-relations+functions in between those identifiers.
 * 
 * @author Bjoern Richerzhagen
 *
 * @param <T>
 */
public class SiSType<T extends Object> {

	private final Set<SiSTypeDerivation<T>> derivations;

	private final SiSTypeAggregation<T> aggregationFunction;

	private final Class<T> dataType;

	private final String name;

	/**
	 * Singletons!
	 * 
	 * @param dataType
	 * @param derivation
	 */
	protected SiSType(String name, Class<T> dataType,
			SiSTypeAggregation<T> aggregationFunction) {
		this.name = name;
		this.dataType = dataType;
		this.derivations = new LinkedHashSet<>();
		this.aggregationFunction = aggregationFunction;
	}

	/**
	 * Enables non-API projects to add their own derivation functions for the
	 * given SiSType programmatically. Long-term, those Derivations should make
	 * it into the API package.
	 * 
	 * @param derivation
	 */
	public void addDerivation(SiSTypeDerivation<T> derivation) {
		derivations.add(derivation);
	}

	/**
	 * Data type. If this is a collection, this method returns the inner type of
	 * the collection.
	 * 
	 * @return
	 */
	public Class<T> getType() {
		return dataType;
	}

	/**
	 * Returns the name of this property
	 * 
	 * @return the name of this property
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Determines the "best" derivation for the given available types and
	 * returns it (or null, if no matching derivation is found...)
	 * 
	 * FIXME Please note: "best" is currently not defined, we just try one after
	 * the other in the order they were added until we find a matching one.
	 * Maybe, one wants to add a weight or utility later-on?
	 * 
	 * @param availableTypes
	 * @return matching derivation or null
	 */
	public SiSTypeDerivation<T> canDerive(Set<SiSType<?>> availableTypes) {
		for (SiSTypeDerivation<T> derivation : derivations) {
			if (derivation.canDerive(availableTypes)) {
				return derivation;
			}
		}
		return null;
	}

	/**
	 * Merges two values a and b of the given type.
	 * 
	 * @param data
	 * @param function
	 * @return
	 * @throws AggregationNotPossibleException
	 */
	public T aggregate(Collection<T> data, AggregationFunction function)
			throws AggregationNotPossibleException {
		return aggregationFunction.aggregate(data, function);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SiSType<?> other = (SiSType<?>) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SiS-" + name;
	}

}
