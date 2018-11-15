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

package de.tud.kom.p2psim.impl.scenario.simcfg2;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.scenario.Composable;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.Scenario;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;
import de.tud.kom.p2psim.api.scenario.simcfg.converter.SimCfgTypeConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.ConfigurationContext;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfigurationMerger;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgFileParser;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.initialization.ComponentInitializer;
import de.tud.kom.p2psim.impl.scenario.simcfg2.initialization.InitializationContext;
import de.tud.kom.p2psim.impl.scenario.simcfg2.initialization.InitializationUtils;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class SimCfgConfigurator implements Configurator {

    private File configFile;
    private ConfigurationContext configurationContext;
    private InitializationContext initializationContext;
    private SimCfgConfiguration configuration;
    private List<String> appliedVariations = Lists.newArrayList();
    private Map<String, String> uiVariables = Maps.newLinkedHashMap();
    private Map<String, Object> delayedRegistration = Maps.newLinkedHashMap();

    /**
     * Creates a new SimCfg configurator with the given config file as base
     *
     * @param configFile
     */
    public SimCfgConfigurator(File configFile) {
        this.configFile = configFile;

        loadConfig();
    }

    /**
     * Creates a new SimCfg configurator with the given filename but uses the
     * given XtextResourceSet for parsing.
     *
     * @param configFilename
     */
    public SimCfgConfigurator(String configFilename) {
        this(new File(configFilename));
    }

    private void loadConfig() {
        SimCfgFileParser parser = new SimCfgFileParser();
        this.configurationContext = parser.parseConfig(this.configFile);
        String relative = new File("config").toURI().relativize(configFile.toURI()).getPath();
    }

    @Override
    public Collection<Object> configureAll() {

        SimCfgConfigurationMerger merger = new SimCfgConfigurationMerger();
        configuration = merger.process(configurationContext, appliedVariations);

        for (String key : uiVariables.keySet()) {
            configuration.getSettings().put(key, new Value(Value.ValueType.STRING, uiVariables.get(key)));
        }

        processConfiguration(configuration, configurationContext);

        callComposableComponents();
        InitializationUtils.configureComponents(initializationContext);
        configureScenario();

        return initializationContext.getComponents();
    }

    private void configureScenario() {
        Simulator simCore = (Simulator) initializationContext.getComponentMap().get(Configurator.CORE);
        ScenarioFactory scenarioFactory = initializationContext.getScenarioFactory();

        if (scenarioFactory == null)
            throw new ConfigurationException("No scenario builder specified in the configuration file. Nothing to do.");

        Scenario scenario = scenarioFactory.createScenario();
        simCore.setScenario(scenario);
    }

    private void callComposableComponents() {
        for (Composable composable : initializationContext.getComposables()) {
            composable.compose(this);
        }
    }

    private void processConfiguration(SimCfgConfiguration configuration, ConfigurationContext context) {
        ComponentInitializer initializer = new ComponentInitializer(configuration, context, this);
        this.initializationContext = initializer.process(delayedRegistration);
    }

    @Override
    public de.tudarmstadt.maki.simonstrator.api.component.Component getConfigurable(String name) {
        // TODO: Lets hope they are really configurables...
        return (de.tudarmstadt.maki.simonstrator.api.component.Component) initializationContext.getComponent(name);
    }

    @Override
    public List<Object> getConfigurable(Class type) {
        List<Object> components = Lists.newArrayList();
        for (Object obj : initializationContext.getComponents()) {
            if (type.isAssignableFrom(obj.getClass())) {
                components.add(obj);
            }
        }
        return components;
    }

    @Override
    public String parseValue(String valueString) {
        return "";
    }

    @Override
    public void setVariables(Map<String, String> variables) {
        this.uiVariables = variables;
    }

    /**
     * Registers a configurable component under a specific name, but the old
     * configurator mentioned that there was some sort of problem with all
     * components implementing configurables.
     *
     * Also why should we limit the configurator to configurables? The
     * Configurable interface isn't used anywhere...
     *
     * @TODO Remove Configurable interface and add component annotations that
     *       can be processed by a utility which generates component list and
     *       documentations. In the best case even autocompleting information
     *       for SimCfg.
     *
     *       Also the only place where this is used is the simulator core who
     *       should be in the map already since we configured it!
     */
    @Override
    @Deprecated
    public void register(String name, de.tudarmstadt.maki.simonstrator.api.component.Component component) {
        delayedRegistration.put(name, component);
    }

    @Override
    public File getConfigFile() {
        return this.configFile;
    }

    @Override
    public Map<String, String> getVariables() {
        HashMap<String, String> variables = new HashMap<String, String>();

        for (String key : configuration.getSettings().keySet()) {
            variables.put(key, (String) SimCfgTypeConverter.convertTo(key, configuration.getSettings().get(key), String.class));
        }

        return variables;
    }

    @Override
    public String getResolvedConfiguration() {
        return "";
    }

    public void applyVariations(List<String> variation) {
        this.appliedVariations = variation;
    }

    public ConfigurationContext getConfigurationContext() {
        return configurationContext;
    }

    public HostComponentFactory getFactory(String name) {
        return initializationContext.getFactory(name);
    }

    public void configureAttribute(Object component, String name, Object rawValue) {
        Value value = null;
        if (rawValue instanceof Value) {
            value = (Value)rawValue;
        } else if (rawValue instanceof String) {
            value = new Value(Value.ValueType.STRING, rawValue);
        } else if (rawValue instanceof String[]) {
            value = new Value(Value.ValueType.STRING_ARRAY, rawValue);
        }

        String setter = "set" + SimCfgUtil.convertToInternalSetterNameFormat(name);

        Method method = SimCfgUtil.findSetter(component.getClass(), setter);

        if (method == null) {
            throw new ConfigurationException("Error: Tried to configure attribute '" + name +
                    "' with value '" + value + "' on component '" + component.getClass().getSimpleName() + "'.");
        }

        try {
            // TODO: Use the resolver in the parsed configuration to support
            // multiple levels of variable declarations?
            Value extractedValue = initializationContext.getVariableResolver().resolveValue(value);
            method.invoke(component, SimCfgTypeConverter.convertTo(name, extractedValue, method.getParameterTypes()[0]));
        } catch (Exception e) {
            throw new ConfigurationException("Error: Couldn't invoke setter '" + setter + "' on component '" + component.getClass().getSimpleName() + "'.", e);
        }
    }

    public void applyVariables(List<Tuple<String, String>> modifiedVariables) {
        // ...
    }
}
