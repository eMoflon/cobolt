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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.scenario.Composable;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;
import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.Component;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.ConfigurationContext;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * @author Fabio Zöllner
 * @version 1.0, 17.08.13
 */
public class InitializationContext {

    private SimCfgConfigurator configurator;
    private SimCfgConfiguration configuration;
    private ConfigurationContext configurationContext;
    private VariableResolver variableResolver;

    private List<HostComponentFactory> componentFactories = Lists.newArrayList();
    private Map<String, HostComponentFactory> componentFactoryMap = Maps.newLinkedHashMap();
    private List<Object> staticComponents = Lists.newArrayList();
    private List<Composable> composableComponents = Lists.newArrayList();
    private ScenarioFactory scenarioFactory;
    private List<ComponentConfigurationDescriptor> componentsExpectingConfiguration = Lists.newArrayList();
    private Map<String, Object> strangeComponentNameToObjectMap = Maps.newLinkedHashMap();

    public InitializationContext(SimCfgConfiguration configuration, ConfigurationContext context) {
        this.configuration = configuration;
        this.configurationContext = context;
        this.variableResolver = new VariableResolver(configuration);
    }

    public void addComponent(Component component, Object object) {
        if (component.getType().equals(Component.ComponentType.FACTORY)) {
            HostComponentFactory cf = (HostComponentFactory) object;
            componentFactories.add(cf);
            String alias = component.getAlias();
            componentFactoryMap.put(SimCfgUtil.convertToInternalSetterNameFormat(alias), cf);
        } else if (component.getType().equals(Component.ComponentType.STATIC)) {
            staticComponents.add(object);
        }
    }

    public SimCfgConfiguration getConfiguration() {
        return this.configuration;
    }

    public VariableResolver getVariableResolver() {
        return variableResolver;
    }

    public void addComposable(Composable composable) {
        composableComponents.add(composable);
    }

    public void setScenarioFactory(ScenarioFactory scenarioFactory) {
        this.scenarioFactory = scenarioFactory;
    }

    public ScenarioFactory getScenarioFactory() {
        return scenarioFactory;
    }

    public void checkAndAddConfigureMethods(Object instantiatedComponent, Class mapping) {
        List<Method> configureMethods = SimCfgUtil.findConfigureMethods(mapping);

        if (configureMethods == null || configureMethods.isEmpty()) {
            return;
        }

        for (Method method : configureMethods) {
            After after = SimCfgUtil.getConfigureAfterAnnotation(method);

            ComponentConfigurationDescriptor descriptor = new ComponentConfigurationDescriptor();
            descriptor.setComponent(instantiatedComponent);
            descriptor.setConfigureMethod(method);
            descriptor.setAfterAnnotation(after);


            componentsExpectingConfiguration.add(descriptor);
        }
    }

    public void register(String name, Object component) {
		Monitor.log(InitializationContext.class, Level.DEBUG,
				"Registering component '" + name + "'.");
        strangeComponentNameToObjectMap.put(name, component);
    }

    public Object getComponent(String name) {
        return strangeComponentNameToObjectMap.get(name);
    }

    public Collection<Object> getComponents() {
        return strangeComponentNameToObjectMap.values();
    }

    public List<Composable> getComposables() {
        return composableComponents;
    }

    public Map<String, Object> getComponentMap() {
        return strangeComponentNameToObjectMap;
    }

    public List<ComponentConfigurationDescriptor> getConfigurationDescriptors() {
        return componentsExpectingConfiguration;
    }

    public SimCfgConfigurator getConfigurator() {
        return configurator;
    }

    public void setConfigurator(SimCfgConfigurator configurator) {
        this.configurator = configurator;
    }

    public HostComponentFactory getFactory(String name) {
        return componentFactoryMap.get(name);
    }

    public void addComponents(Map<String, Object> components) {
        this.strangeComponentNameToObjectMap.putAll(components);
    }
}
