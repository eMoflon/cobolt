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

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

public class BooleanConverter implements TypeConverter {
	
	@Override
	public Object convert(String name, Value value) {
        if (value.isString()) {
            String string = value.getString().toLowerCase();
            if (string.equals("true") || string.equals("1") || string.equals("yes")) {
                return true;
            } else if (string.equals("false") || string.equals("0") || string.equals("no")) {
                return false;
            }
        } else if (value.isStringArray()) {
            String[] array = value.getStringArray();
            if (array.length < 3) {
                throw new ConfigurationException("Cannot convert array to boolean, needs format: ['type', 'value', 'value'] where type is either 'and' or 'or'");
            }

            if (array[0].equals("and")) {
                for (int i = 1; i < array.length; i++) {
                    if (!(Boolean)convert("", new Value(Value.ValueType.STRING, array[i]))) {
                        return false;
                    }
                }
                return true;
            } else if (array[0].equals("or")) {
                for (int i = 1; i < array.length; i++) {
                    if ((Boolean)convert("", new Value(Value.ValueType.STRING, array[i]))) {
                        return true;
                    }
                }
                return false;
            } else {
                throw new ConfigurationException("Cannot convert array to boolean, needs format: ['type', 'value', 'value'] where type is either 'and' or 'or'");
            }
        }
		
		return true;
	}

	@Override
	public Class<?> responsibleForType() {
		return boolean.class;
	}
}
