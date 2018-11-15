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

package de.tud.kom.p2psim.impl.common;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * The Class FakeHost:
 * 		Used with RealNetworkingLayer to simualate ranging events
 * 		like "10s-18s doSthn" from actions file in distributed
 * 		environments.
 * 
 * 		This class is thought of as a spaceholder and not actually
 * 		do anything.
 */
public class FakeHost extends DefaultHost {

	/* (non-Javadoc)
	 * @see de.tud.kom.p2psim.impl.common.DefaultHost#toString()
	 */
	@Override
	public String toString() {
		return "Fake" + super.toString();
	}
	
	@Override
	public <T extends HostComponent> void registerComponent(T component) {
		// do not do it.
	}

}
