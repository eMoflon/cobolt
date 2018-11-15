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



package de.tud.kom.p2psim.api.common;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * Basic interface for all components of a host (=end node) in a distributed
 * system. Components can be single layers (network, transport etc.) or
 * subcomponents inside of layers (overlay components or similar). Instances of
 * the component interfaces should always be created via appropriate
 * <code>ComponentFactory</code> implementation
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 * @see de.tud.kom.p2psim.api.common#ComponentFactory
 * 
 */
public interface SimHostComponent extends HostComponent {

	/**
	 * 
	 * @return the host this component belongs to
	 */
	@Override
	public SimHost getHost();

}
