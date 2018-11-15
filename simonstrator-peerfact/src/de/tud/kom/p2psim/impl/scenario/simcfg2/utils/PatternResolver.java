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

package de.tud.kom.p2psim.impl.scenario.simcfg2.utils;

import de.tud.kom.p2psim.impl.simengine.Simulator;

/**
 * @author Fabio Zöllner
 * @version 1.0, 18.08.13
 */
public class PatternResolver {

    public static String resolvePattern(Class type, String string) {
        String oldString = string;
        string = string.toLowerCase();

        if (type.equals(Integer.class) || type.equals(Long.class) ||
                type.equals(Double.class) || type.equals(Short.class)) {

            if (string.endsWith("h") ||
                    string.endsWith("m") ||
                    string.endsWith("s")) {
                return resolveTime(string);
            } else if (string.endsWith("b") ||
                    string.endsWith("kb") ||
                    string.endsWith("mb") ||
                    string.endsWith("gb")) {
                return resolveBytes(string);
            }

        }

        return oldString;
    }

    private static String resolveBytes(String string) {
        if (string.endsWith("kb")) {
            long size = 1024 * Long.parseLong(string.substring(0, string.length()-2));
            return String.valueOf(size);
        } else if (string.endsWith("mb")) {
            long size = 1024 * 1204 * Long.parseLong(string.substring(0, string.length()-2));
            return String.valueOf(size);
        } else if (string.endsWith("gb")) {
            long size = 1024 * 1024 * 1024 * Long.parseLong(string.substring(0, string.length()-2));
            return String.valueOf(size);
        } else if (string.endsWith("b")) {
            long size = Long.parseLong(string.substring(0, string.length()-1));
            return String.valueOf(size);
        }
        return String.valueOf(Long.parseLong(string));
    }

    private static String resolveTime(String string) {
        if (string.endsWith("h")) {
            long time = Simulator.HOUR_UNIT * Long.parseLong(string.substring(0, string.length()-1));
            return String.valueOf(time);
        } else if (string.endsWith("m")) {
            long time = Simulator.MINUTE_UNIT * Long.parseLong(string.substring(0, string.length()-1));
            return String.valueOf(time);
        } else if (string.endsWith("ms")) {
            long time = Simulator.MILLISECOND_UNIT * Long.parseLong(string.substring(0, string.length()-2));
            return String.valueOf(time);
        } else if (string.endsWith("s")) {
            long time = Simulator.SECOND_UNIT * Long.parseLong(string.substring(0, string.length()-1));
            return String.valueOf(time);
        }
        return String.valueOf(Long.parseLong(string));
    }
}
