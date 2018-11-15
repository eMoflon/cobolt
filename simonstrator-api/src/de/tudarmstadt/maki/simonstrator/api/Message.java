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



package de.tudarmstadt.maki.simonstrator.api;

import java.io.Serializable;

/**
 * General message interface for all messages exchanged between inside of the
 * same layer in the simulator. Each layer and component will have their own
 * sub-interface or implementation of this class.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 */
public interface Message extends Serializable {

	/**
	 * Returns the message size in bytes
	 * 
	 * @return message size in bytes
	 */
	public long getSize();

	/**
	 * Returns the payload of a message
	 * 
	 * @return payload of message
	 */
	public Message getPayload();

}
