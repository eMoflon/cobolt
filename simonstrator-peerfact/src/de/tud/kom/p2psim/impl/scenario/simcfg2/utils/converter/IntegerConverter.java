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
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.PatternResolver;

public class IntegerConverter implements TypeConverter {

	@Override
	public Class<?> responsibleForType() {
		return int.class;
	}

	@Override
	public Object convert(String name, Value value) {
        if (value.isString()) {
            String resolved = PatternResolver.resolvePattern(Integer.class, value.getString());

            if (resolved.contains(".")) {
                return (int)Double.parseDouble(resolved);
            } else {
                return Integer.parseInt(resolved);
            }
        }

		return null;
	}

}
