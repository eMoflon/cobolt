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

import java.util.Set;

import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSLocalData;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.DerivationNotPossibleException;


/**
 * Derivation of a target type <strong>T</strong>.
 * 
 * @author Bjoern Richerzhagen
 *
 * @param <T>
 *            target type
 */
public interface SiSTypeDerivation<T extends Object> {
	
	/**
	 * True, if we can derive the target value from any of the available types
	 * 
	 * @param availableTypes
	 * @return
	 */
	public boolean canDerive(Set<SiSType<?>> availableTypes);

	/**
	 * "Magic" method deriving the target value <T> from a number of the input
	 * types (availableTypes). The actual value of the types has to be retrieved
	 * via the {@link SiSLocalData} interface provided upon method invocation.
	 * Ideally, this method implements a number of ways to derive the target
	 * value - sorted by complexity and expected cost for the host. If the
	 * locally available types are not sufficient to derive a value, an
	 * exception is to be thrown.
	 * 
	 * @param data
	 *            access to the actual data values
	 * @param availableTypes
	 *            try to derive the target value using only these types.
	 * @return derived value
	 */
	public T derive(SiSLocalData data, Set<SiSType<?>> availableTypes)
			throws DerivationNotPossibleException;

}
