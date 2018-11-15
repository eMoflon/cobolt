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
import de.tud.kom.p2psim.api.scenario.simcfg.converter.SimCfgTypeConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

public class ClassConverter implements TypeConverter {

	@Override
	public Class<?> responsibleForType() {
		return Class.class;
	}

	@Override
	public Object convert(String name, Value value) {
        String className = null;

        if (value.isString()) {
            className = value.getString();
        } else if (value.isStringArray()) {
            SimCfgTypeConverter.warnAboutConversion(name, value, responsibleForType(), "since only the first entry is processed.");
            className = value.getStringArray()[0];
        }

        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Error: Unable to find a required class", e);
        }
	}

}
