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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Multimap;

import de.tud.kom.p2psim.api.scenario.Composable;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;
import de.tud.kom.p2psim.api.scenario.simcfg.converter.SimCfgTypeConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.Component;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.ConfigurationContext;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgExpressionExecutor;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * @author Fabio Zöllner
 * @version 1.0, 17.08.13
 */
public class ComponentInitializer {
    /**
     * The static getter method that is called on components marked with static
     * in the configuration. The method is expected to have the signature
     * 'public void methodName()'.
     */
    private static final String STATIC_GETTER_METHOD = "getInstance";

    private final SimCfgConfiguration configuration;
    private final ConfigurationContext configurationContext;
    private SimCfgExpressionExecutor expressionExecutor = new SimCfgExpressionExecutor();
    private InitializationContext context = null;

    public ComponentInitializer(SimCfgConfiguration configuration, ConfigurationContext configurationContext, SimCfgConfigurator configurator) {
        this.configuration = configuration;
        this.configurationContext = configurationContext;
        this.context = new InitializationContext(configuration, configurationContext);
        this.context.setConfigurator(configurator);
    }

    public InitializationContext process(Map<String, Object> preRegisteredComponents) {
        context.addComponents(preRegisteredComponents);

        for (Component component : configuration.getComponents()) {
            String name = component.getName();
            Class mapping = configuration.getMapping(name);

            if (mapping == null) {
                throw new ConfigurationException("Missing mapping for '" + name + "'");
            }

            boolean isActive = component.getGuard() != null ? expressionExecutor.evaluate(configuration, component.getGuard()) : true;
            if (!isActive) {
				Monitor.log(
						ComponentInitializer.class,
						Level.DEBUG,
						"Ignoring:   " + component.getName() + " [guard: "
								+ component.getGuard() + ", type: "
								+ mapping.getName() + "]");
                continue;
            }

			Monitor.log(ComponentInitializer.class, Level.DEBUG, "Processing: "
					+ component.getName() + " [guard: " + component.getGuard()
					+ ", type: " + mapping.getName() + "]");
            Object initializedComponent = createComponent(null, component);
            context.addComponent(component, initializedComponent);
        }

        return context;
    }

    private Object createComponent(Object parent, Component component) {
        SimCfgConfiguration configuration = context.getConfiguration();

        boolean isActive = component.getGuard() != null ? expressionExecutor.evaluate(configuration, component.getGuard()) : true;

        if (!isActive) {
			Monitor.log(ComponentInitializer.class, Level.DEBUG, "Ignoring:   "
					+ component.getName() + " [guard: " + component.getGuard()
					+ "]");
            return null;
        }

        String name = component.getName();
        Class mapping = configuration.getMapping(name);

        if (mapping == null) {
            throw new ConfigurationException("Error: Couldn't find a mapping for the " + component.getType() + " '" + component.getName() + "'");
        }
        if (component.getName().equals("weak_waypoint_strategy")) {
            System.out.println("test");
        }

		Monitor.log(ComponentInitializer.class, Level.DEBUG, "Creating "
				+ component.getName() + ": " + mapping.getCanonicalName());

        ConstructorMatchInfo constructor = InitializationUtils.findBestMatchingConstructor(component, context);
        Object componentObj = instantiateComponent(constructor, component);
        applyAttributes(component, componentObj, constructor);

        configureSubComponents(component, componentObj, constructor);

        if (componentObj instanceof Composable) {
            context.addComposable((Composable)componentObj);
        }

        if (componentObj instanceof ScenarioFactory) {
            if (context.getScenarioFactory() != null) {
                throw new ConfigurationException("Error: Multiple scenarios have been configured. Please only configure one.");
            } else {
                context.setScenarioFactory((ScenarioFactory) componentObj);
            }
        }

        context.checkAndAddConfigureMethods(componentObj, mapping);

        context.register(SimCfgUtil.convertToInternalSetterNameFormat(component.getName()), componentObj);

        String setterName = SimCfgUtil.convertToInternalSetterNameFormat(component.getSetter());
        if (setterName != null && parent != null) {
            giveComponentToParent(component, componentObj, parent);
        } else {
            return componentObj;
        }

        return componentObj;
    }

    private void giveComponentToParent(Component component, Object componentObj, Object parent) {
        if (component.getSetter() == null) {
            return;
        }

        String setterName = SimCfgUtil.convertToInternalSetterNameFormat(component.getSetter());

        String setter = "set" + setterName;
        Method setterMethod = SimCfgUtil.findSetter(parent.getClass(), setter);
        if (setterMethod == null)
            throw new ConfigurationException("Error: No setter for '" + setterName + "' on '" + parent.getClass().getSimpleName() + "' could be found.");

        try {
            setterMethod.invoke(parent, componentObj);

        } catch (Exception e) {
            throw new ConfigurationException(
                    "Error: Could not invoke the setter for '" + setterName + "' on " + component.getType() + " '" + component.getName() + "'.", e);
        }
    }

    private void configureSubComponents(Component component, Object componentObj, ConstructorMatchInfo constructor) {
        for (Component comp : component.getChildren()) {
            if (constructor != null && constructor.getComponentInConstructor() != null) {
                if (!constructor.getComponentInConstructor().values().contains(comp)) {
                    Object configurable = createComponent(componentObj, comp);
                }
            } else {
                Object configurable = createComponent(componentObj, comp);
            }
        }
    }


    private Object instantiateComponent(ConstructorMatchInfo constructor, Component component) {
        Class mapping = configuration.getMapping(component.getName());
        Object componentObj = null;

        if (component.getType().equals(Component.ComponentType.STATIC)) {
            try {
                Method getInstance = SimCfgUtil.findMethod(mapping, STATIC_GETTER_METHOD);
                componentObj = getInstance.invoke(null);
            } catch (Exception e) {
                throw new ConfigurationException("Could not create an instance of the " + component.getType() + " '" + component.getName() + "' using the default constructor", e);
            }
        } else {
            try {
                // Get the constructor with the highest number of matching attributes
                if (constructor == null) {
                    componentObj = constructWithDefaultConstructor(mapping);
                } else {
                    constructor.setComponentName(component.getName());
                    componentObj = constructWithConstructor(constructor, component);
                }

            } catch (Exception e) {
                throw new ConfigurationException("Error: Could not create an instance of the " + component.getType() + " '" + component.getName() + "' using the constructor: " + constructor, e);
            }
        }

        return componentObj;
    }

    public Object constructWithConstructor(ConstructorMatchInfo matchInfo, Component component) {
		Monitor.log(ComponentInitializer.class, Level.DEBUG, "Selected: "
				+ matchInfo);
        Object[] arguments = createTypedAttributes(matchInfo, component.getAttributes());
		Monitor.log(ComponentInitializer.class, Level.DEBUG, "With arguments: "
				+ Arrays.asList(arguments));
        try {
            return matchInfo.getConstructor().newInstance(arguments);
        } catch (Exception e) {
            throw new ConfigurationException("No matching constructor found; failed to initialize with constructor " + matchInfo, e);
        }
    }

    public Object constructWithDefaultConstructor(Class mapping) {
        try {
            return mapping.newInstance();
        } catch (Exception e) {
            throw new ConfigurationException("No matching constructor found; failed to initialize with default constructor", e);
        }
    }

    private Object[] createTypedAttributes(ConstructorMatchInfo matchInfo, Multimap<String, Value> attributes) {
        Object[] arguments = new Object[matchInfo.getNumerOfAttributesThatMatch()];

        XMLConfigurableConstructor constructorAnnotation = matchInfo.getAnnotation();
        List<String> annotationValueList = Arrays.asList(constructorAnnotation.value());

        // Assign attributes
        for (String key : attributes.keySet()) {
            int index = annotationValueList.indexOf(key);
            Collection<Value> values = attributes.get(key);
            if (values.isEmpty()) continue;
            Value value = values.iterator().next();
            if (index == -1) {
                continue;
            }
			Monitor.log(ComponentInitializer.class, Level.DEBUG, "kv: [" + key
					+ ", " + attributes.get(key) + "], index: " + index);
            Class<?> type = matchInfo.getParameterTypes()[index];
            arguments[index] = SimCfgTypeConverter.convertTo(key, context.getVariableResolver().resolveValue(value), type);
        }

        // Create components in the constructor
        for (String key : matchInfo.getComponentInConstructor().keySet()) {
            int index = annotationValueList.indexOf(key);
            if (index == -1) {
                continue;
            }
			Monitor.log(ComponentInitializer.class, Level.DEBUG, "kv: [" + key
					+ ", " + matchInfo.getComponentInConstructor().get(key)
					+ "], index: " + index);
            arguments[index] = createComponent(null, matchInfo.getComponentInConstructor().get(key));
        }

        return arguments;
    }

    private void applyAttributes(Component component, Object newComponent, ConstructorMatchInfo matchInfo) {
        Class componentClass = configuration.getMappings().get(component.getName());
        Set<String> attributesToApply = component.getAttributes().keySet();

        if (matchInfo != null) {
            substituteConstructorParameters(matchInfo, attributesToApply);
        }

        for (String parameter : attributesToApply) {
            String setter = "set" + SimCfgUtil.convertToInternalSetterNameFormat(parameter);

            Method setterMethod = SimCfgUtil.findSetter(componentClass, setter);
            Method magicMethod = null;

            if (setterMethod == null) {
                magicMethod = SimCfgUtil.findMagicMethod(componentClass);
                if (magicMethod == null) {
                    throw new ConfigurationException("Error: No setter for '" + parameter + "' on '" + component.getName() + "' could be found.");
                }
            }

            Collection<Value> values = component.getAttributes().get(parameter);

            for (Value value : values) {
                Object typedParameter = SimCfgTypeConverter.convertTo(parameter, context.getVariableResolver().resolveValue(value),
                        (setterMethod != null) ? setterMethod.getParameterTypes()[0] : String.class);

                try {
                    if (setterMethod != null) {
                        setterMethod.invoke(newComponent, typedParameter);
                    } else {
						Monitor.log(ComponentInitializer.class, Level.DEBUG,
								"Magic method has been invoked on '"
										+ component.getName() + "' for '"
										+ parameter + "' with value '"
										+ typedParameter + "'.");
                        if (magicMethod.getParameterTypes()[1].equals(Value.class)) {
                            magicMethod.invoke(newComponent, parameter, value);
                        } else {
                            magicMethod.invoke(newComponent, parameter, typedParameter);
                        }
                    }
                } catch (Exception e) {
                    throw new ConfigurationException("Error: Could not invoke the setter for '" + parameter + "' on " + component.getType() + " '" + component.getName() + "'.", e);
                }
            }
        }
    }

    private void substituteConstructorParameters(ConstructorMatchInfo matchInfo, Set<String> attributesToApply) {
        for (String parameter : matchInfo.getAnnotation().value()) {
            attributesToApply.remove(parameter);
        }
    }
}
