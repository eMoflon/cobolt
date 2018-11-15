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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.Component;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * @author Fabio Zöllner
 * @version 1.0, 17.08.13
 */
public class InitializationUtils {


    public static ConstructorMatchInfo findBestMatchingConstructor(Component component, InitializationContext context) {
        // A priority queue that orders the ConstructorMatchInfo objects by the
        // number of attributes that match
        PriorityQueue<ConstructorMatchInfo> matchInfo = new PriorityQueue<ConstructorMatchInfo>(
                1, new Comparator<ConstructorMatchInfo>() {
            @Override
            public int compare(ConstructorMatchInfo cmi1, ConstructorMatchInfo cmi2) {
                return cmi2.getNumerOfAttributesThatMatch()	- cmi1.getNumerOfAttributesThatMatch();
            }
        });

        Class mapping = context.getConfiguration().getMapping(component.getName());
        Constructor[] constructors = mapping.getConstructors();

        for (Constructor constructor : constructors) {
            XMLConfigurableConstructor xmlConfAnnotation = (XMLConfigurableConstructor) constructor.getAnnotation(XMLConfigurableConstructor.class);

            if (xmlConfAnnotation == null) {
                continue;
            }

            Tuple<Boolean, HashMap<String, Component>> annotationCheckReturn = annotationMatchesAttributesOrComponents(xmlConfAnnotation.value(), component.getAttributes(), component.getChildren());

            if (annotationCheckReturn.getA()) {
                matchInfo.add(new ConstructorMatchInfo(xmlConfAnnotation.value().length, constructor, xmlConfAnnotation, annotationCheckReturn.getB()));
            }
        }

        if (matchInfo.size() <= 0) {
            return null;
        }

        return matchInfo.poll();
    }

    private static Tuple<Boolean, HashMap<String, Component>> annotationMatchesAttributesOrComponents(String[] annotationValues, Multimap<String, Value> attributes, List<Component> components) {
        List<String> annotationValueList = Arrays.asList(annotationValues);
        List<String> componentNames = new ArrayList<String>();

        for (Component gc : components) {
            componentNames.add(SimCfgUtil.convertToInternalParameterNameFormat(gc.getSetter()));
        }

        HashMap<String, Component> componentsInConstructor = new HashMap<String, Component>();

        for (String key : annotationValueList) {
            if (!attributes.containsKey(key)) {
                if (componentNames.contains(key)) {
                    componentsInConstructor.put(key, components.get(componentNames.indexOf(key)));
                } else {
                    return new Tuple<Boolean, HashMap<String, Component>>(false, null);
                }
            }
        }

        return new Tuple<Boolean, HashMap<String, Component>>(true, componentsInConstructor);
    }

    public static void configureComponents(InitializationContext context) {
        boolean anotherRound = true;
        List<ComponentConfigurationDescriptor> descriptors = sortTopological(context.getConfigurationDescriptors());

        Set<Class> availableComponentTypes = getAvailableTypes(descriptors);
        List<Class> configuredTypes = Lists.newArrayList();
        Set<Class> loadedComponentTypes = getLoadedComponentTypes(context.getComponents());

        while (anotherRound) {
            anotherRound = false;

			Monitor.log(InitializationUtils.class, Level.DEBUG,
					"Components to configure this round: " + descriptors);

            Iterator iter = descriptors.iterator();
            while (iter.hasNext()) {
                ComponentConfigurationDescriptor descriptor = (ComponentConfigurationDescriptor)iter.next();

                Object component = descriptor.getComponent();
                Class[] optionalDependencies = descriptor.getOptionalDependencies();
                Class[] requiredDependencies = descriptor.getRequiredDependencies();

                if (descriptor.isFactory()) {
                    optionalDependencies = Arrays.copyOf(optionalDependencies, optionalDependencies.length+1);
                    optionalDependencies[optionalDependencies.length-1] = HostBuilder.class;
                }

                checkIfRequiredComponentsAreLoaded(component, requiredDependencies, loadedComponentTypes);

                boolean optionalStatus = checkDependencies(component, optionalDependencies, availableComponentTypes, configuredTypes);
                boolean requiredStatus = checkDependencies(component, requiredDependencies, availableComponentTypes, configuredTypes);
                boolean dependenciesAreConfigured = optionalStatus && requiredStatus;

                if (!dependenciesAreConfigured) {
					Monitor.log(InitializationUtils.class, Level.DEBUG,
							"Component: " + component.getClass().getName());
					Monitor.log(
							InitializationUtils.class,
							Level.DEBUG,
							"Optional dependencies configured: "
									+ optionalStatus + " "
									+ Lists.newArrayList(optionalDependencies));
					Monitor.log(
							InitializationUtils.class,
							Level.DEBUG,
							"Required dependencies configured: "
									+ requiredStatus + " "
									+ Lists.newArrayList(requiredDependencies));

                    anotherRound = true;
                    continue;
                }

                Method configureMethod = descriptor.getConfigureMethod();
				Monitor.log(InitializationUtils.class, Level.DEBUG,
						"Configuring " + configureMethod.getName() + " of "
								+ component.getClass().getSimpleName());
                try {
                    if (configureMethod.getReturnType().equals(boolean.class)) {
                        if (!(Boolean) configureMethod.invoke(component, buildConfigureParameterArray(component, configureMethod, context))) {
                            anotherRound = true;
                        } else {
                            iter.remove();

                            if (!getAvailableTypes(descriptors).contains(component.getClass())) { // TODO: getAvailableTypes is inefficient
								Monitor.log(InitializationUtils.class,
										Level.DEBUG, component.getClass()
												.getSimpleName()
												+ " is fully configured...");
                                configuredTypes.add(component.getClass());
                            }
                        }
                    } else {
						Monitor.log(
								InitializationUtils.class,
								Level.DEBUG,
								"The configuration methods return type of '"
										+ component.getClass().getSimpleName()
										+ "' isn't boolean and thus will only be called once.");
                        configureMethod.invoke(component, buildConfigureParameterArray(component, configureMethod, context));
                        iter.remove();

                        if (!getAvailableTypes(descriptors).contains(component.getClass())) { // TODO: getAvailableTypes is inefficient
							Monitor.log(InitializationUtils.class, Level.DEBUG,
									component.getClass().getSimpleName()
											+ " is fully configured...");
                            configuredTypes.add(component.getClass());
                        }
                    }

                } catch (Exception e) {
                    throw new ConfigurationException("Error: Error while invoking the method '" + configureMethod.getName() +
                            "' on '" + component.getClass().getSimpleName() + "'.", e);
                }
            }
        }
    }

    private static void checkIfRequiredComponentsAreLoaded(Object component, Class[] requiredDependencies, Set<Class> loadedComponents) {
        for (Class requiredCls : requiredDependencies) {
            Iterator<Class> iter = loadedComponents.iterator();

            boolean containsRequiredClass = false;
            while (iter.hasNext()) {
                Class loadedClass = iter.next();

                if (loadedClass.isAssignableFrom(loadedClass)) {
                    containsRequiredClass = true;
                    break;
                }
            }
            if (!containsRequiredClass) {
                throw new ConfigurationException("The component of type " + requiredCls.getName() + " hasn't been configured, but is required by " + component.getClass().getName());
            }
        }
    }

    private static boolean checkDependencies(Object component, Class[] dependencies, Set<Class> availableComponentTypes, List<Class> configuredTypes) {
        if (dependencies.length == 0) return true;

        for (Class cls : dependencies) {
            throwExceptionIfDependsOnSelf(cls, component);

            if (isAvailableConfigurationType(availableComponentTypes, cls) && !alreadyConfigured(configuredTypes, cls)) {
				Monitor.log(InitializationUtils.class, Level.DEBUG, "Skipping "
						+ component.getClass().getSimpleName() + ", since "
						+ cls.getSimpleName() + " isn't configured yet");
                return false;
            }
        }

        return true;
    }

    // TODO: Merge alreadyConfigured and isAvailableConfigurationType
    private static boolean alreadyConfigured(Collection<Class> configuredTypes, Class testCls) {
        for (Class cls : configuredTypes) {
			Monitor.log(
					InitializationUtils.class,
					Level.DEBUG,
					"Testing " + testCls.getSimpleName() + " against "
							+ cls.getSimpleName());
            if (testCls.isAssignableFrom(cls)) {
                return true;
            }
        }
		Monitor.log(InitializationUtils.class, Level.DEBUG,
				testCls.getSimpleName() + " is not yet configured.");

        return false;
    }

    private static boolean isAvailableConfigurationType(Collection<Class> availableComponentTypes, Class testCls) {
        return alreadyConfigured(availableComponentTypes, testCls);
    }

    private static void throwExceptionIfDependsOnSelf(Class cls, Object component) {
        if (dependendOnSelf(cls, component)) {
            throw new ConfigurationException("Yo dawg I herd you like " + cls.getSimpleName() + " so we made your " + cls.getSimpleName() + " depend on " + cls.getSimpleName() + "... ...");
        }
    }

    private static Set<Class> getAvailableTypes(List<ComponentConfigurationDescriptor> descriptors) {
        Set<Class> types = Sets.newHashSet();
        for (ComponentConfigurationDescriptor descriptor : descriptors) {
            types.add(descriptor.getComponent().getClass());
        }
        return types;
    }

    private static Set<Class> getLoadedComponentTypes(Collection<Object> components) {
        Set<Class> types = Sets.newHashSet();
        for (Object component : components) {
            types.add(component.getClass());
        }
        return types;
    }

    private static boolean dependendOnSelf(Class cls, Object component) {
        return cls.isAssignableFrom(component.getClass());
    }

    private static List<ComponentConfigurationDescriptor> sortTopological(
            List<ComponentConfigurationDescriptor> descriptor) {

        // TODO: Sort

        return Lists.newArrayList(descriptor);
    }

    /**
     * Builds a parameter array with the following possible parameters:
     * - HostBuilder
     * - ParsedConfiguration
     * - Configurator
     *
     * @param component
     * @param configureMethod
     * @return
     */
    private static Object[] buildConfigureParameterArray(Object component, Method configureMethod, InitializationContext context) {

        Class<?>[] parameterTypes = configureMethod.getParameterTypes();

        if (parameterTypes.length <= 0) {
            return new Object[0];
        }

        Object[] parameter = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            if (HostBuilder.class.isAssignableFrom(parameterTypes[i])) {
                parameter[i] = context.getComponent(Configurator.HOST_BUILDER);
            } else if (InitializationContext.class.isAssignableFrom(parameterTypes[i])) {
                parameter[i] = context;
            } else if (Configurator.class.isAssignableFrom(parameterTypes[i])) {
                parameter[i] = context.getConfigurator();
            } else {
                throw new ConfigurationException("The configuration method in '" + component.getClass().getSimpleName() + "' expected a parameter of type '" + parameterTypes[i].getSimpleName() + "' which isn't supported.");
            }
        }

        return parameter;
    }
}
