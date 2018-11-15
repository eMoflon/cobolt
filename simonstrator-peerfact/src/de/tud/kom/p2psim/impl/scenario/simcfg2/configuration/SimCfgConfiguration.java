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

package de.tud.kom.p2psim.impl.scenario.simcfg2.configuration;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Import;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Variation;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tud.kom.p2psim.impl.util.Tuple;

public class SimCfgConfiguration {
    private final File configFile;

    //private Map<String, Boolean> flags = Maps.newLinkedHashMap();

    // [namespace, flag, isActive]

    private Map<String, Boolean> flags = Maps.newLinkedHashMap();
    private Multimap<String, String> namespaces = ArrayListMultimap.create();

    private Map<String, Class> mappings = Maps.newLinkedHashMap();
    private Map<String, Value> settings = Maps.newLinkedHashMap();
    private Map<String, Variation> variations = Maps.newLinkedHashMap();
    private List<Import> imports = Lists.newArrayList();

    private List<Component> components = Lists.newArrayList();

    public SimCfgConfiguration(File configFile) {
        this.configFile = configFile;
    }

    public Collection<Variation> getVariations() {
        return variations.values();
    }

    public SimCfgConfiguration copy() {
        SimCfgConfiguration config = new SimCfgConfiguration(configFile);
        config.namespaces = ArrayListMultimap.create(namespaces);
        config.flags = Maps.newLinkedHashMap(flags);
        config.mappings = Maps.newLinkedHashMap(mappings);
        config.variations = Maps.newLinkedHashMap(variations);
        config.settings = Maps.newLinkedHashMap(settings);
        config.components = Lists.newArrayList();
        for (Component component : components) {
            config.components.add(component.deepCopy());
        }

        return config;
    }

    public void setFlag(String name) {
        if (name.startsWith("!")) {
            setFlag(name.substring(1), false);
        } else {
            setFlag(name, true);
        }
    }

    public boolean containsNamespace(String namespace) {
        return namespaces.containsKey(namespace);
    }

    public boolean containsNamespaceOfFlag(String name) {
        Tuple<String, String> parts = SimCfgUtil.separateNamespace(name);
        String namespace = parts.getA();

        return containsNamespace(namespace);
    }

    public void setFlag(String name, boolean active) {
        Tuple<String, String> parts = SimCfgUtil.separateNamespace(name);
        String namespace = parts.getA();
        String flag = parts.getB();

        String qualifiedFlag = (!namespace.equals("") ? namespace + "." : "") + flag;

        Collection<String> namespaceCollection = namespaces.get(namespace);

        if (namespace.equals("")) {
            // Special case for flags with up to two parts, no namespace is assigned
            flags.put(flag, active);
        } else if (!namespaceCollection.isEmpty()) { // In case there are already other flags with the same namespace
            boolean oldState = false;
            if (namespaceCollection.contains(flag)) {
                oldState = flags.get(qualifiedFlag);
            } else {
                namespaces.put(namespace, flag);
                flags.put(qualifiedFlag, active);
            }

            if (oldState != active) {
                if (active) { // Flag was previously deactivated, disable all other flags
                    for (Map.Entry<String, String> entry : namespaces.entries()) {
                        String qFs = (!entry.getKey().equals("") ? entry.getKey() + "." : "") + entry.getValue();
                        flags.put(qFs, false);
                    }
                    flags.put(qualifiedFlag, true);
                } else { // Flag was previously activated, one flag must be active... choosing a random one
                    Map.Entry<String, String> entry = Lists.newArrayList(namespaces.entries()).get(0);
                    String qFs = (!entry.getKey().equals("") ? entry.getKey() + "." : "") + entry.getValue();
                    flags.put(qFs, true);
                }
            }
        } else {
            namespaces.put(namespace, flag);
            String qfs = (!namespace.equals("") ? namespace + "." : "") + flag;
            flags.put(qfs, active);
        }
    }

    public void setFlags(Map<String, Boolean> flags) {
        for (String name : flags.keySet()) {
            if (!this.flags.containsKey(name)) {
                setFlag(name, flags.get(name));
            }
        }
    }

    public boolean isKnownFlag(String flag) {
        String cleanFlag = flag;
        if (flag.startsWith("!")) {
            cleanFlag = flag.substring(1);
        }

        return flags.containsKey(cleanFlag);
    }

    public boolean isActiveFlag(String flag) {
        return flags.get(flag);
    }

    public void addImport(String filename, String guard) {
        this.imports.add(new Import(filename, guard));
    }

    public List<Import> getImports() {
        return imports;
    }

    public boolean checkFlags(List<String> flags, boolean requireAll) {
        boolean flagActive = true;

        for (String flag : flags) {
            String cleanFlag = flag;
            if (flag.startsWith("!")) {
                flagActive = false;
                cleanFlag = flag.substring(1);
            } else {
                flagActive = true;
            }

            if (this.flags.get(cleanFlag) != flagActive && requireAll) {
                return false;
            } else if (this.flags.get(cleanFlag) == flagActive && !requireAll) {
                return true;
            }
        }

        return !requireAll;
    }

    public void addMapping(String name, Class cls) {
        mappings.put(name, cls);
    }

    public void addMappings(Map<String, Class> mappings) {
        for (String name : mappings.keySet()) {
            if (!this.mappings.containsKey(name)) {
                this.mappings.put(name, mappings.get(name));
            }
        }
    }

    public void addSetting(String key, Value value) {
        settings.put(key, value);
    }

    public void addSettings(Map<String, Value> settings) {
        for (String name : settings.keySet()) {
            if (!this.settings.containsKey(name)) {
                this.settings.put(name, settings.get(name));
            }
        }
    }

    public void setVariationFlag(String name, String flag) {
        Variation info = variations.get(name);
        if (info == null) {
            info = new Variation(name);
            variations.put(name, info);
        }

        boolean activate = !flag.startsWith("!");
        info.flags.put(activate ? flag : flag.substring(1), activate);
    }

    public void setVariationSetting(String name, String key, Value value) {
        Variation info = variations.get(name);
        if (info == null) {
            info = new Variation(name);
            variations.put(name, info);
        }

        info.settings.put(key, value);
    }

    public void addComponent(Component component) {
        this.components.add(component);
    }

    public List<Component> getComponents() {
        return Lists.newArrayList(components);
    }

    public File getConfigFile() {
        return configFile;
    }

    public Map<String,Boolean> getFlags() {
        return flags;
    }

    public Map<String, Value> getSettings() {
        return settings;
    }

    public void addFlags(Map<String, Boolean> flags) {
        for (String name : flags.keySet()) {
            if (!this.flags.containsKey(name)) {
                setFlag(name, flags.get(name));
                //this.flags.put(name, flags.get(name));
            }
        }
    }

    @Override
    public String toString() {
        return "SimCfgConfiguration[...]";
    }

    public int getNumberOfTopComponents() {
        return components.size();
    }

    public int getNumberOfComponents() {
        int count = 0;
        for (Component component : components) {
            count += component.getNumberOfSubtreeComponents();
        }
        return count;
    }

    public Class getMapping(String name) {
        return mappings.get(name);
    }

    public Map<String, Class> getMappings() {
        return mappings;
    }
}
