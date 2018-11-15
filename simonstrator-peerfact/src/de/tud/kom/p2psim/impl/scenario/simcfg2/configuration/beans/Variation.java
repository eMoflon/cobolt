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

package de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

public class Variation {
    private String name;

    public HashMap<String, Value> settings = Maps.newHashMap();

    public Map<String, Boolean> flags = Maps.newLinkedHashMap();

    public Variation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFlag(String flag) {
        if (flag.startsWith("!")) {
            setFlag(flag.substring(1), false);
        } else {
            setFlag(flag, true);
        }
    }

    public void setFlag(String name, boolean active) {
        flags.put(name, active);
    }

    public boolean isActiveFlag(String name) {
        return flags.get(name);
    }

    public Map<String, Boolean> getFlags() {
        return this.flags;
    }

    public void setSetting(String name, Value value) {
        settings.put(name, value);
    }

    public Object getSetting(String name) {
        return settings.get(name);
    }

    public Map<String, Value> getSettings() {
        return settings;
    }
}
