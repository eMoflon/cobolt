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


package de.tudarmstadt.maki.simonstrator.api.common;

import java.io.Serializable;

import de.tudarmstadt.maki.simonstrator.api.component.overlay.Serializer;

/**
 * Used for entities that might be transmitted as part of a message. This
 * enforces the implementation of {@link Serializable} to allow execution of
 * your code on real-world platforms.
 * 
 * If you want to implement more complex serilization for your overlay messages
 * and types, you should consider writing a {@link Serializer} for your overlay.
 * 
 * @author Julius Rueckert
 */
public interface Transmitable extends Serializable {

	/**
	 * This is required for simulations, where objects are not realy serialized
	 * (performance reasons). Instead, this method is called and should return
	 * the object size in bytes.
	 * 
	 * @return the transmission size of this entity in byte
	 */
	public int getTransmissionSize();
}
