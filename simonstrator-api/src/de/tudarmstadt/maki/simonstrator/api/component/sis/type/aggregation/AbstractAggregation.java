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

package de.tudarmstadt.maki.simonstrator.api.component.sis.type.aggregation;

import java.util.Collection;

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationConsumer.AggregationFunction;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.AggregationNotPossibleException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypeAggregation;

/**
 * Aggregation for flat (non-array) types
 * 
 * @author Bjoern Richerzhagen
 *
 * @param <T>
 */
public class AbstractAggregation<T> implements SiSTypeAggregation<T> {

	@Override
	public T aggregate(Collection<T> data, AggregationFunction function)
			throws AggregationNotPossibleException {
		switch (function) {
		case SUM:
			return sum(data);

		case MAX:
			return max(data);

		case MIN:
			return min(data);

		case AVG:
			return avg(data);

		case COUNT:
			return count(data);

		default:
			throw new AggregationNotPossibleException();
		}
	}

	protected T sum(Collection<T> data) throws AggregationNotPossibleException {
		throw new AggregationNotPossibleException();
	}

	protected T min(Collection<T> data) throws AggregationNotPossibleException {
		throw new AggregationNotPossibleException();
	}

	protected T max(Collection<T> data) throws AggregationNotPossibleException {
		throw new AggregationNotPossibleException();
	}

	protected T avg(Collection<T> data) throws AggregationNotPossibleException {
		throw new AggregationNotPossibleException();
	}

	protected T count(Collection<T> data)
			throws AggregationNotPossibleException {
		throw new AggregationNotPossibleException();
	}

	public static class AggregationDouble extends AbstractAggregation<Double> {
		@Override
		protected Double sum(Collection<Double> data)
				throws AggregationNotPossibleException {
			if (data == null || data.isEmpty()) {
				return Double.NaN;
			}
			double result = 0;
			for (double val : data) {
				result += val;
			}
			return result;
		}

		@Override
		protected Double min(Collection<Double> data)
				throws AggregationNotPossibleException {
			if (data == null || data.isEmpty()) {
				return Double.NaN;
			}
			double result = Double.MAX_VALUE;
			for (double val : data) {
				result = Math.min(val, result);
			}
			return result;
		}

		@Override
		protected Double max(Collection<Double> data)
				throws AggregationNotPossibleException {
			if (data == null || data.isEmpty()) {
				return Double.NaN;
			}
			double result = Double.MIN_VALUE;
			for (double val : data) {
				result = Math.max(val, result);
			}
			return result;
		}

		@Override
		protected Double avg(Collection<Double> data)
				throws AggregationNotPossibleException {
			if (data == null || data.isEmpty()) {
				return Double.NaN;
			}
			return sum(data) / count(data);
		}

		@Override
		protected Double count(Collection<Double> data)
				throws AggregationNotPossibleException {
			if (data == null) {
				return Double.NaN;
			}
			return (double) data.size();
		}
	}

}
