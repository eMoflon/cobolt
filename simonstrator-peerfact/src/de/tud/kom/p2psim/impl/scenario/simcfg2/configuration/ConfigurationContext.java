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


import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Variation;

public class ConfigurationContext {
    private ConfigurationNode root;
    private ConfigurationNode currentNode;
    private int loadedConfigurations = 1;

    private Map<String, Variation> variations = Maps.newLinkedHashMap();

    public ConfigurationContext() {

    }

    public ConfigurationContext(SimCfgConfiguration configuration) {
        root = new ConfigurationNode(configuration);
    }

    public Collection<String> getVariationNames() {
        return variations.keySet();
    }

    public void setRoot(SimCfgConfiguration configuration) {
        this.root = new ConfigurationNode(configuration);
    }

    public void setCurrentNode(ConfigurationNode node) {
        currentNode = node;
    }

    public ConfigurationNode getCurrentNode() {
        return currentNode;
    }

    public SimCfgConfiguration getCurrentConfiguration() {
        return currentNode.getConfiguration();
    }

    public ConfigurationNode getRootNode() {
        return root;
    }

    public SimCfgConfiguration getRootConfig() {
        return root.getConfiguration();
    }

    public void addVariation(Variation variation) {
        variations.put(variation.getName(), variation);
    }

    public void moveUp() {
        currentNode = currentNode.getParent();
    }

    public Variation getVariation(String variation) {
        return variations.get(variation);
    }

    public void addVariations(Collection<Variation> newVariations) {
        for (Variation variation : newVariations) {
            variations.put(variation.getName(), variation);
        }
    }

    public void newSubNode(SimCfgConfiguration config, String guard) {
        this.loadedConfigurations++;

        ConfigurationNode node = new ConfigurationNode(config);
        node.setImportExpression(guard);
        currentNode.addChild(node);
        node.setParent(currentNode);
        currentNode = node;
    }

    public int getNumberOfLoadedConfigurations() {
        return this.loadedConfigurations;
    }

    @Override
    public String toString() {
        return "ConfigurationContext[configs: " + loadedConfigurations + ", variations: " + variations.size() + "]";
    }
}
