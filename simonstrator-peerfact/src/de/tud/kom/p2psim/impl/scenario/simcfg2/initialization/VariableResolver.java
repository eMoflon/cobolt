/*
 * Copyright (c) 2005-2013 KOM - Multimedia Communications Lab
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
 */

package de.tud.kom.p2psim.impl.scenario.simcfg2.initialization;

import java.util.Map;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

/**
 * @author Fabio Zöllner
 * @version 1.0, 17.08.13
 */
public class VariableResolver {

	private SimCfgConfiguration configuration;

    public VariableResolver(SimCfgConfiguration configuration) {
        this.configuration = configuration;
    }

    public Value resolveValue(Value value) { // Can be String or String[]
        Map<String, Value> settings = configuration.getSettings();

        if (value.isString()) {
            String str = value.getString();

            if (str.startsWith("$") && !str.startsWith("${")) { // Could be a single variable assignment
                String settingName = str.substring(1);
                if (settings.containsKey(settingName)) {
                    Value resolvedValue = resolveValue(settings.get(settingName));
                    return new Value(resolvedValue.getType(), resolvedValue.getString());
                }
                throw new ConfigurationException("Could not resolve variable '" + value.getString() + "'");
            } else { // Test for inline variables
                for (Map.Entry<String, Value> entry : settings.entrySet()) {
                    String inlineString = "${" + entry.getKey() + "}";
                    if (str.contains(inlineString)) {
                        Value resolvedValue = resolveValue(entry.getValue());
                        if (resolvedValue.isStringArray()) {
                            throw new ConfigurationException("Cannot substitute inline variable '" + inlineString + "' with array of setting '" + entry.getKey() + "'");
                        }
                        str = str.replace(inlineString, resolvedValue.getString());
                    }
                }
                return new Value(Value.ValueType.STRING, str);
            }

        } else if (value.isStringArray()) {
            String[] array = value.getStringArray();

            for (int i = 0; i < array.length; i++) {
                Value resolvedValue = resolveValue(new Value(Value.ValueType.STRING, array[i]));
                if (resolvedValue.isStringArray()) {
                    throw new ConfigurationException("Cannot substitute variable '" + array[i] + "' in array with another array");
                }
                array[i] = resolvedValue.getString();
            }
            return new Value(Value.ValueType.STRING_ARRAY, array);
        }

        return null;
    }
}
