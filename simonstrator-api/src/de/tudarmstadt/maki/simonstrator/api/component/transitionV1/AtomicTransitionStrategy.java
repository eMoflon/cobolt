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

package de.tudarmstadt.maki.simonstrator.api.component.transitionV1;

import java.io.Serializable;

/**
 * Classes implementing this interface define an AtomicTransition from F to T
 * (with the respective transition lifecycle).
 * 
 * @author Bjoern Richerzhagen, Alex Froemmgen
 * 
 *         TODO Annotation Flip/Flip vs. Run/Run
 * 
 * @param <F>
 *            From
 * @param <T>
 *            To
 */
public interface AtomicTransitionStrategy<F extends TransitionEnabled, T extends TransitionEnabled>
		extends Serializable {

	/**
	 * The Transition is in the startup-phase
	 * 
	 * @param from
	 * @param to
	 */
	public void inStartup(F from, T to);

	/**
	 * Now, both F and T are active, but T is preparing its shutdown.
	 * (F.runnning, T.shutdown)
	 * 
	 * FIXME: this callback is currently not triggered within the RunRun
	 * Transition. Do we require it at all?
	 * 
	 * @param from
	 * @param to
	 */
	@Deprecated
	public void inParallelActive(F from, T to);

	/**
	 * The Run/Run-Transition is currently in its rollback-state. Note the
	 * reversed meaning of from and to in this context!!
	 * 
	 * @param from
	 *            source of the transition (NOT source of the rollback!)
	 * @param to
	 *            target of the transition (NOT target of the rollback!)
	 */
	public void inRollback(F from, T to);

	/**
	 * Last call to the transition-object (after the components did their
	 * cleanup)
	 * 
	 * @param from
	 * @param to
	 */
	public void inCleanup(F from, T to);

}
