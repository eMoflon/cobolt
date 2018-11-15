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

package de.tudarmstadt.maki.simonstrator.api.component.transition;

/**
 * Simplified version of an atomic transition providing a method to transfer
 * state between two mechanisms. This is only required if the
 * {@link TransferState} annotation is not sufficient in your use case.
 * 
 * This object is not supposed to maintain state between method invocations.
 * 
 * @author Bjoern Richerzhagen, Alexander Froemmgen
 *
 * @param <S>
 *            Source component type
 * @param <T>
 *            Target component type
 */
public interface AtomicTransition<S extends TransitionEnabled, T extends TransitionEnabled> {

	/**
	 * Returns the type of the source component of this transition
	 * 
	 * @return
	 */
	public Class<S> getSourceType();

	/**
	 * Returns the type of the target component of this transition
	 * 
	 * @return
	 */
	public Class<T> getTargetType();

	/**
	 * Implement custom state transformations here. For simple variable passing,
	 * is is sufficient to use the {@link TransferState} annotation.
	 * 
	 * @param sourceComponent
	 * @param targetComponent
	 */
	public void transferState(S sourceComponent, T targetComponent);

	/**
	 * Implement a custom failure recovery mechanism for this transition, if
	 * desired. This is called as soon as the transition finished (successful
	 * and unsuccessful).
	 * 
	 * @param sourceComponent
	 * @param targetComponent
	 * @param successful
	 *            true, if no error occured during the transition.
	 */
	public void transitionFinished(S sourceComponent, T targetComponent,
			boolean successful);

}
