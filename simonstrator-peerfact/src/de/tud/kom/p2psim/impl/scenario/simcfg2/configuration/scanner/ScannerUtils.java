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

package de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.scanner;

import java.io.File;
import java.util.List;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.Component;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Import;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

public class ScannerUtils {
    private SimCfgConfiguration config = null;

    private String temp = "";

    public ScannerUtils(File configFile) {
        config = new SimCfgConfiguration(configFile);
    }

    public void addFlag(String name) {
        config.setFlag(name);
    }

    public void addImport(String name) {
        config.addImport(name, null);
    }

    public void setLastImportGuard(String guard) {
        List<Import> imports = config.getImports();
        imports.get(imports.size()-1).setGuard(guard);
    }

    public void addMapping(String name, String cls) {
        Class clazz = null;
        try {
            clazz = Class.forName(cls);
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Error in mapping '" + name + "'. Could not find class '" + cls + "'", e);
        }

        config.addMapping(name, clazz);
    }

    public SimCfgConfiguration getConfig() {
        return config;
    }

    public void addSetting(String name, String value) {
        config.addSetting(name, new Value(Value.ValueType.STRING, value));
    }

    public void addSetting(String name, String[] values) {
        config.addSetting(name, new Value(Value.ValueType.STRING_ARRAY, values));
    }

    private List<String> stringList = null;

    public void newArray() {
        stringList = Lists.newArrayList();
    }

    public void addToArray(String str) {
        stringList.add(str);
    }

    public String[] getArray() {
        String[] array = new String[]{};
        return stringList.toArray(array);
    }

    private Component lastComponent = null;

    public void newComponent(Component.ComponentType type) {
        if (type.equals(Component.ComponentType.COMPONENT)) {
            Component newComponent = new Component(type);
            lastComponent.addChildComponent(newComponent);
            newComponent.setParent(lastComponent);
            lastComponent = newComponent;
        } else {
            lastComponent = new Component(type);
            config.addComponent(lastComponent);
        }
    }

    public void walkUpComponent() {
        lastComponent = lastComponent.getParent();
    }

    public void setComponentSetter(String name) {
        lastComponent.setSetter(name);
    }

    public void setComponentName(String name) {
        lastComponent.setName(name);
    }

    public void setComponentAlias(String name) {
        lastComponent.setAlias(name);
    }

    public void setComponentGuard(String guard) {
        lastComponent.setGuard(guard);
    }

    public void addAttribute(String name, Object value) {
        if (value instanceof String) {
            lastComponent.addAttribute(name,  new Value(Value.ValueType.STRING, value));
        } else if (value instanceof String[]) {
            lastComponent.addAttribute(name, new Value(Value.ValueType.STRING_ARRAY, value));
        }
    }

    public void setComponentRedefine() {
        lastComponent.setRedefine(true);
    }

    public void setComponentMerge() {
        lastComponent.setMerge(true);
    }

    public void addVariationFlag(String name, String flag) {
        config.setVariationFlag(name, flag);
    }

    public void addVariationSetting(String name, String key, Object value) {
        if (value instanceof String) {
            config.setVariationSetting(name, key, new Value(Value.ValueType.STRING, value));
        } else if (value instanceof String[]) {
            config.setVariationSetting(name, key, new Value(Value.ValueType.STRING_ARRAY, value));
        }
    }
}
