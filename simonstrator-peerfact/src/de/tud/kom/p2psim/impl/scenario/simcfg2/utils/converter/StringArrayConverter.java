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

package de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

public class StringArrayConverter implements TypeConverter {

	@Override
	public Class<?> responsibleForType() {
		return String[].class;
	}

	@Override
	public Object convert(String name, Value rawValue) {
        if (!(rawValue instanceof Value)) {
            return null;
        }
        Value value = (Value)rawValue;

        if (value.isStringArray()) {
            return value.getStringArray();
        } else if (value.isString()) {
            return new String[] {value.getString()};
        }
		
		return null;
	}

}
