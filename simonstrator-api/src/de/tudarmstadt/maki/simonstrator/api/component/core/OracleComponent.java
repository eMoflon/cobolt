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

package de.tudarmstadt.maki.simonstrator.api.component.core;

import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * If global knowledge to some extend is available, this component allows access
 * to other hosts and thereby their respective {@link HostComponent}s
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface OracleComponent extends GlobalComponent {

	/**
	 * Returns all known host objects. Usually, this method is only available on
	 * a simulation platform.
	 * 
	 * @return
	 */
	public List<Host> getAllHosts();

	/**
	 * Allows Overlays and Components to distinguish between real-world
	 * implementations and simulations - could be nifty for performance reasons
	 * or analyzing capabilities.
	 * 
	 * @return
	 */
	public boolean isSimulation();

}
