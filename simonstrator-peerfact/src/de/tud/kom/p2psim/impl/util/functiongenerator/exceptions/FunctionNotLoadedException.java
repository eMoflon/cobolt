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

package de.tud.kom.p2psim.impl.util.functiongenerator.exceptions;

/**
 * This Runtime Exception is thrown if a value of a not loaded
 * function is requested from the Function Generator.
 *
 * @author Fabio Zöllner
 *
 */
public class FunctionNotLoadedException extends RuntimeException {

	public FunctionNotLoadedException() {
		/* empty */
	}
	
	public FunctionNotLoadedException(Throwable throwable) {
		super(throwable);
	}
	
	public FunctionNotLoadedException(String message, Throwable throwable) {
		super(message, throwable);
	}
	
	public FunctionNotLoadedException(String message) {
		super(message);
	}
}
