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

import java.util.Map;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;

/**
 * @author Fabio Zöllner
 * @version 1.0, 12.07.13
 */
public class SimCfgExpressionExecutor {
    private static final Logger log = Logger.getLogger(SimCfgExpressionExecutor.class);
    private ScriptEngine javascriptEngine = null;

    public SimCfgExpressionExecutor() {
        log.debug("Initializing JavaScript engine.");
        javascriptEngine = new ScriptEngineManager().getEngineByName("JavaScript");
        log.debug("JavaScript engine initialized.");
    }

    public boolean evaluate(SimCfgConfiguration configuration, String expression) {
        Map<String, Boolean> flags = configuration.getFlags();
        for (Map.Entry<String, Boolean> entry : flags.entrySet()) {
            javascriptEngine.put(entry.getKey().replace(".", "_"), entry.getValue());
        }

        boolean result = evaluate(expression);
        log.debug("Evaluated '" + expression + "' => " + result);
        javascriptEngine.getBindings(ScriptContext.ENGINE_SCOPE).clear();
        return result;
    }

    public boolean evaluate(String expression) {
        if (expression == null) return true;

        expression = expression.replace(".", "_");
        try {
            Object value = javascriptEngine.eval(expression);
            if (value instanceof Boolean) {
                return (Boolean) value;
            } else {
                return false;
            }
        } catch (ScriptException e) {
            String missingReference = findMissingReference(e);
            if (missingReference == null) {
                e.printStackTrace();
            } else {
                javascriptEngine.put(missingReference, false);
                return evaluate(expression);
            }
        }

        return false;
    }

    private String findMissingReference(Throwable e) {
        Throwable cause = e.getCause();
        if (cause == null) {
            return null;
        }

        if (cause.getMessage().contains("is not defined.")) {
            String message = cause.getMessage();
            String[] parts = message.split("\\\"");
            return parts[1];
        } else {
            if (cause.equals(e)) {
                return null;
            } else {
                return findMissingReference(cause);
            }
        }
    }
}
