/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
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


package de.tud.kom.p2psim.impl.util.toolkits;

/**
 * Interface for predicate checks on objects. The check itself has to be
 * implemented in the method {@link Predicate#isTrue(Object) isTrue}.
 * 
 * For example, that method could check whether passed objects are greater than
 * some value given in a constructor.
 * 
 * @author Tobias Lauinger <tl1003@rbg.informatik.tu-darmstadt.de>
 * 
 * @param <T>
 *            the type of the objects to be checked.
 */
public interface Predicate<T> {

	/**
	 * Decides whether a certain predicate, defined by the implementing class of
	 * this interface, holds for <code>object</code>.
	 * 
	 * @param object
	 *            the Object to be checked for the predicate.
	 * @return <code>true</code> if and only if the predicate holds for
	 *         <code>object</code>.
	 */
	public boolean isTrue(T object);
}
