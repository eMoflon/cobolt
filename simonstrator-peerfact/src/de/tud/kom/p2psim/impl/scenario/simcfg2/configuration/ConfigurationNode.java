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
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


public class ConfigurationNode {
    private SimCfgConfiguration configuration;
    private ConfigurationNode parent;
    private List<ConfigurationNode> children = Lists.newArrayList();
    private Map<String, Boolean> flags = Maps.newLinkedHashMap();
    private String expression;

    public ConfigurationNode(SimCfgConfiguration configuration) {
        this.configuration = configuration;
    }

    public SimCfgConfiguration getConfiguration() {
        return configuration;
    }

    public void addChild(ConfigurationNode node) {
        children.add(node);
    }

    public List<ConfigurationNode> getChildren() {
        return Lists.newArrayList(children);
    }

    public void removeChild(ConfigurationNode node) {
        children.remove(node);
    }

    public void setParent(ConfigurationNode node) {
        this.parent = node;
    }

    public ConfigurationNode getParent() {
        return this.parent;
    }

    public void setImportExpression(String expression) {
        this.expression = expression;
    }

    public Map<String, Boolean> getFlags() {
        return flags;
    }

    public String getImportExpression() {
        return expression;
    }

    public File getFile() {
        return configuration.getConfigFile();
    }

    @Override
    public String toString() {
        return "ConfigurationNode[file: " + configuration.getConfigFile().getName() + ", children: " + children.size() + "]";
    }
}
