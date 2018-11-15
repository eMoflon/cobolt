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

import java.util.List;
import java.util.Map;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;

public class Component {
    private ComponentType type;
    private String guard;
    private String name;
    private String setter;
    private Multimap<String, Value> attributes = ArrayListMultimap.create();
    private List<Component> children = Lists.newArrayList();
    private String alias;
    private Object sourceComponent = null;
    private boolean redefine = false;
    private boolean merge = false;
    private Component parent;

    public Component(ComponentType type) {
        this.type = type;
    }

    public Component deepCopy() {
        return recursiveCopy(this);
    }

    private Component recursiveCopy(Component component) {
        Component newComponent = new Component();
        newComponent.type = component.type;
        newComponent.guard = component.guard;
        newComponent.name = component.name;
        newComponent.setter = component.setter;
        newComponent.attributes = ArrayListMultimap.create(component.attributes);
        newComponent.alias = component.alias;
        newComponent.sourceComponent = component.sourceComponent;
        newComponent.redefine = component.redefine;
        newComponent.merge = component.merge;
        newComponent.children = Lists.newArrayList();

        for (Component child : component.children) {
            newComponent.children.add(recursiveCopy(child));
        }

        return newComponent;
    }

    public Component() {

    }

    public void addAttribute(String key, Value value) {
        attributes.put(SimCfgUtil.convertToInternalParameterNameFormat(key), value);
    }

    public boolean hasAttribute(String name) {
        return attributes.containsKey(SimCfgUtil.convertToInternalParameterNameFormat(name));
    }

    private Multimap<String, Value> convertToParameterFormatHashMap(Map<String, Value> attributes) {
        Multimap<String, Value> attributeMap = ArrayListMultimap.create();

        for (String key : attributes.keySet()) {
            attributeMap.put(SimCfgUtil.convertToInternalParameterNameFormat(key), attributes.get(key));
        }

        return attributeMap;
    }

    public ComponentType getType() {
        return type;
    }

    public void setType(ComponentType type) {
        this.type = type;
    }

    public String getGuard() {
        return guard;
    }

    public void setGuard(String guard) {
        this.guard = guard;
    }

    public boolean isRedefined() {
        return redefine;
    }

    public boolean isMerged() {
        return merge;
    }

    public void addChildComponent(Component newComponent) {
        this.children.add(newComponent);
        newComponent.setParent(this);
    }

    public void setParent(Component parent) {
        this.parent = parent;
    }

    public Component getParent() {
        return parent;
    }

    public void setRedefine(boolean active) {
        this.redefine = active;
    }

    public void setMerge(boolean active) {
        this.merge = active;
    }

    public static enum ComponentType {
        STATIC,
        FACTORY,
        COMPONENT,
        HELPER;

        public String toString() {
            return name().toLowerCase();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (this.alias == null) {
            this.alias = name;
        }
    }

    public String getSetter() {
        return setter;
    }

    public void setSetter(String setter) {
        this.setter = setter;
    }

    public Multimap<String, Value> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Value> attributes) {
        this.attributes = convertToParameterFormatHashMap(attributes);
    }

    public void setAttributes(Multimap<String, Value> attributes) {
        this.attributes = attributes;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public List<Component> getChildren() {
        return children;
    }

    public String getAlias() {
        return alias;
    }

    public String toString() {
        return "Component[Type: " + type + ", Name: " + name + ", Attributes: " + attributes.keySet() + "]";
    }

    public int getNumberOfChildren() {
        return children.size();
    }

    public int getNumberOfSubtreeComponents() {
        int count = 1;
        for (Component child : children) {
            count += child.getNumberOfSubtreeComponents();
        }
        return count;
    }
}
