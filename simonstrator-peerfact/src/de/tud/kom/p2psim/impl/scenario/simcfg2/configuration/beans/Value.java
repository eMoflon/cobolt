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

/**
 * @author Fabio Zöllner
 * @version 1.0, 18.08.13
 */
public class Value {
    private ValueType type;
    private Object content;

    public Value(ValueType type, Object content) {
        this.type = type;
        this.content = content;
    }

    public String getString() {
        return (String) content;
    }

    public String[] getStringArray() {
        return (String[]) content;
    }

    public boolean isString() {
        return type.equals(ValueType.STRING);
    }

    public boolean isStringArray() {
        return type.equals(ValueType.STRING_ARRAY);
    }

    public ValueType getType() {
        return type;
    }

    public static enum ValueType {
        STRING,
        STRING_ARRAY
    }
}
