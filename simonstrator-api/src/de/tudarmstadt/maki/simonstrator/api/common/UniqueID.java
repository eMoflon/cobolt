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

package de.tudarmstadt.maki.simonstrator.api.common;



/**
 * An ID. The concept of these IDs is as follows: they are SOLELY a container
 * for a double representation of the given ID. All arithmetic operations that
 * can be executed on top of such an ID have to be implemented in an ID-Space,
 * which might, thus, be overlay-specific. A UniqueID is NEVER overlay-specific!
 * 
 * This concept eases the creation of globally unified workload apps and
 * evaluation setups, where new IDs need to be guessed or created from within
 * the app. Creation of OverlayIDs should always be done via the respective
 * IDSpace.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 06/18/2011
 */
public interface UniqueID extends Transmitable, Comparable<UniqueID> {

	/**
	 * Representation as a long. Usually, there should be no need to actually
	 * use this method. All you need to to with IDs in the overlay should be
	 * sorting (as provided by the IDSpace) and comparison/equality checks.
	 */
	public long value();

	public String valueAsString();

}
