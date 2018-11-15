/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

package de.tud.kom.p2psim.api.topology.movement;

import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;

/**
 * All interested parties can register themselves as a {@link MovementListener}
 * with a {@link MovementModel}. In most cases it will be sufficient to use the
 * provided callback in the {@link MovementSupported}-component rather than to
 * implement this listener.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 03.03.2012
 * @deprecated replaced with {@link LocationListener}s
 */
@Deprecated
public interface MovementListener {

	/**
	 * Triggered, after the respective component moved.
	 * 
	 * @param comp
	 */
	public void afterComponentMoved(MovementSupported comp);

}
