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


package de.tud.kom.p2psim.impl.scenario;

/**
 * Implementations of this interface are used by configurator to parse parameter
 * values for scenario actions. E.g. if you would like your overlay node to
 * lookup a value with the OverlayKey "foo" you can specify a parser for
 * OverlayKeys (so getType() must return OverlayKey.class) and which is able to
 * parse string representations of your keys to concrete OverlayKeys supported
 * by your application. Typically, you would implement exactly one Parser for
 * each parameter type.
 * <p>
 * Note that it makes no sence to provide parsers for simple types, e.g int,
 * long etc. as configurator can parse them already.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 14.12.2007
 * 
 */
public interface Parser {
	/**
	 * Parse and return the desired value.
	 * 
	 * @param stringValue
	 *            - string as specified in the action file
	 * @return parsed value.
	 */
	public Object parse(String stringValue);

	/**
	 * 
	 * @return supported type, e.g. OverlayKey, OverlayID, Document or whatever
	 *         you need as parameters to your methods.
	 */
	public Class getType();
}
