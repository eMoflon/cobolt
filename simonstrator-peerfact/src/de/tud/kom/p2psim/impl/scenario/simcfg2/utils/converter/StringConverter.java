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

public class StringConverter implements TypeConverter {

	@Override
	public Class<?> responsibleForType() {
		return String.class;
	}

	@Override
	public Object convert(String name, Value value) {
        if (value.isString()) {
            return value.getString();
        } else if (value.isStringArray()) {
            StringBuilder strList = new StringBuilder();

            strList.append("[");
            for (String str : value.getStringArray()) {
                strList.append("'").append(str).append("' ");
            }
            strList.deleteCharAt(strList.length()-1);
            strList.append("]");

            return strList.toString();
        }
		
		return null;
	}

}
