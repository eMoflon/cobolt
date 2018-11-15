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

package de.tudarmstadt.maki.simonstrator.api.component.network;

import de.tudarmstadt.maki.simonstrator.api.Rate;

/**
 * Bandwidth interface, in byte/s. See {@link Rate}.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface Bandwidth extends Cloneable {

	/**
	 * The downstream bandwidth in bit/s {@link Rate}.
	 * 
	 * @return
	 */
	public long getDownBW();

	/**
	 * The upstream bandwidth in bit/s {@link Rate}
	 * 
	 * @return
	 */
	public long getUpBW();

	/**
	 * 
	 * @param newDownBW
	 */
	public void setDownBW(long newDownBW);

	/**
	 * 
	 * @param newUpBW
	 */
	public void setUpBW(long newUpBW);

	/**
	 * 
	 * @return
	 */
	public Bandwidth clone();

}
