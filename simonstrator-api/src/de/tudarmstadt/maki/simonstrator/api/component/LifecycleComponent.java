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

package de.tudarmstadt.maki.simonstrator.api.component;

/**
 * This component can be controlled by a churn generator. Could be a network
 * interface, an application, an overlay. Allows us to reuse network churn
 * generators with overlay components etc.
 * 
 * A churn generator should include means to specify on which kind of
 * {@link LifecycleComponent}s it wants to operate.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface LifecycleComponent extends HostComponent {

	/**
	 * Has to return true, if the {@link LifecycleComponent} is considered
	 * active.
	 * 
	 * @return
	 */
	public boolean isActive();

	/**
	 * Stops all activity of this {@link LifecycleComponent} - e.g., leaves
	 * an overlay, deactivates a network interface.
	 */
	public void stopComponent();

	/**
	 * Starts all activity of this {@link LifecycleComponent} - e.g., joins
	 * an overlay, activates a network interface.
	 */
	public void startComponent();

}
