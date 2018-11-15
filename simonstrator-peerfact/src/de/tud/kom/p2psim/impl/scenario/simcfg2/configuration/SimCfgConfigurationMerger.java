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

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Variation;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgExpressionExecutor;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.SimCfgUtil;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This class is used to create a single SimCfgConfiguration
 * based on a given ConfigurationContext.
 */
public class SimCfgConfigurationMerger {

    SimCfgExpressionExecutor exprExecutor = new SimCfgExpressionExecutor();

    /**
     * Applies all given variations to the root configuration and merges
     * all imported configurations into the root configuration.
     *
     * @param context The configuration context as provided by the SimCfgFileParser
     * @param variations A list of variation names that should be applied to the configuration
     * @return A new configuration with all configurations of the context
     *         merged into one as well as all variation applied
     */
    public SimCfgConfiguration process(ConfigurationContext context, List<String> variations) {
        SimCfgConfiguration configuration = context.getRootConfig().copy();

        for (String variation : variations) {
            Variation variationInfo = context.getVariation(variation);
            configuration.setFlags(variationInfo.flags); // Sets flags if not yet present
            configuration.addSettings(variationInfo.getSettings());
        }

        mergeConfigurationTree(configuration, context.getRootNode());

        return configuration;
    }

    /**
     * Starts merging all imported configurations into the given configuration by traversing
     * the configuration tree.
     *
     * @param configuration New configuration into which the configurations should be merged
     * @param rootNode The root node of the configuration tree which is to be merged
     */
    private void mergeConfigurationTree(SimCfgConfiguration configuration, ConfigurationNode rootNode) {
        for (ConfigurationNode node : rootNode.getChildren()) {
            String expression = node.getImportExpression();
            boolean includeConfig = true;
            if (expression != null) {
                includeConfig = exprExecutor.evaluate(configuration, expression);
            }

            if (includeConfig) {
                mergeFlags(node.getConfiguration(), configuration);
                mergeSettings(node.getConfiguration(), configuration);
                mergeComponents(node.getConfiguration(), configuration);
                mergeMappings(node.getConfiguration(), configuration);

                mergeConfigurationTree(configuration, node);
            }
        }
    }

    private void mergeFlags(SimCfgConfiguration source, SimCfgConfiguration target) {
        for (Map.Entry<String, Boolean> entry : source.getFlags().entrySet()) {
            Tuple<String, String> parts = SimCfgUtil.separateNamespace(entry.getKey());

            if (!parts.getA().equals("") && !target.containsNamespace(parts.getA()) && entry.getValue().booleanValue()) {
                // We only require the qualified flags that are activated. Since a namespace can only contain one
                // activated flags, all other flags must be deactivated.
                target.setFlag(entry.getKey(), true);
            } else if (parts.getA().equals("")) {
                // For unqualified flags only the first occurrence counts
                if (target.isKnownFlag(entry.getKey())) {
                    target.setFlag(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private void mergeSettings(SimCfgConfiguration source, SimCfgConfiguration target) {
        target.addSettings(source.getSettings());
    }

    private void mergeComponents(SimCfgConfiguration source, final SimCfgConfiguration target) {
        List<String> mergedComponents = Lists.newArrayList();

        for (Component sourceComponent : source.getComponents()) {
            decideAndMerge(sourceComponent, target.getComponents(), mergedComponents, new ComponentAdder() {
                @Override
                public void addComponent(Component component) {
                    target.addComponent(component);
                }
            });
        }
    }

    private void mergeMappings(SimCfgConfiguration source, final SimCfgConfiguration target) {
        target.addMappings(source.getMappings());
    }

    /**
     * This method merges the source component with the target component
     * by copying all attributes and sub components to the target, should
     * they not already exist.
     *
     * @param sourceComponent
     * @param targetComponent
     */
    private void mergeComponent(Component sourceComponent, Component targetComponent) {
        Multimap<String, Value> sourceAttributes = sourceComponent.getAttributes();
        Multimap<String, Value> targetAttributes = targetComponent.getAttributes();

        for (String key : sourceAttributes.keySet()) {
            if (!targetComponent.hasAttribute(key)) {
                targetAttributes.putAll(key, sourceAttributes.get(key));
            }
        }

        List<Component> sourceChildren = sourceComponent.getChildren();
        final List<Component> targetChildren = targetComponent.getChildren();

        List<String> mergedComponents = Lists.newArrayList();
        for (Component sourceChild : sourceChildren) {
            decideAndMerge(sourceChild, targetChildren, mergedComponents, new ComponentAdder() {
                @Override
                public void addComponent(Component component) {
                    targetChildren.add(component);
                }
            });
        }
    }

    /**
     * Merges the source component with the first component of the target component list that has the same alias.
     *
     * Merging is done using the following rules:
     * - Ignore the component if the corresponding target component has redefine set
     * - Merge attributes and sub component if the target component has merge set
     * - Simply add the component if a merge has already taken place or none of the above apply
     *
     * @param sourceComponent The component that shall be merged with one of the target components
     * @param targetComponents The components from which one is selected for the merge based on its alias
     * @param alreadyMergedComponents A list of already merged components to prevent multiple merges of the same type
     * @param adder Anonymous class that implements code for adding the component
     *              to the target configuration / component list
     */
    private void decideAndMerge(Component sourceComponent, List<Component> targetComponents, List<String> alreadyMergedComponents, ComponentAdder adder) {
        Component targetComponent = matchByAlias(sourceComponent, targetComponents);
        if (targetComponent == null || alreadyMergedComponents.contains(sourceComponent.getAlias())) {
            // No matching component found. Retain.
			Monitor.log(
					SimCfgConfigurationMerger.class,
					Level.DEBUG,
					"Adding component '"
							+ sourceComponent.getAlias()
							+ "' there is no such target component or already another target component configured");
            adder.addComponent(sourceComponent);
        } else {
            // Only the first occurrence is merged
            alreadyMergedComponents.add(sourceComponent.getAlias());
            // Merge attributes and sub components
            if (targetComponent.isRedefined()) {
				Monitor.log(
						SimCfgConfigurationMerger.class,
						Level.DEBUG,
						"Ignoring '"
								+ sourceComponent.getAlias()
								+ "' as higher level config redefines component");
                // The higher level config redefines the component. Ignore sub components.
                return;
            } else if (targetComponent.isMerged()) {
				Monitor.log(SimCfgConfigurationMerger.class, Level.DEBUG,
						"Merging component '" + sourceComponent.getAlias()
								+ "' as higher level config allows merge");
                // Higher level config allows this component to be merged with an existing one.
                mergeComponent(sourceComponent, targetComponent);
            } else {
				Monitor.log(
						SimCfgConfigurationMerger.class,
						Level.DEBUG,
						"Adding component '"
								+ sourceComponent.getAlias()
								+ "' as higher level config neither allows a merge or redefines the component");
                // No merging or redefine. The component is simply added as another instance
                adder.addComponent(sourceComponent);
            }
        }
    }

    /**
     * Searches the first components that matches the source components alias and returns it
     *
     * @param sourceComponent Component whose alias is to be found
     * @param components List of components in which the alias shall be searched
     * @return The component of the given components list that matches the source components alias
     */
    private Component matchByAlias(Component sourceComponent, List<Component> components) {
        for (Component component : components) {
            String sourceAlias = sourceComponent.getAlias();
            if (sourceAlias == null) sourceAlias = sourceComponent.getName();

            String targetAlias = component.getAlias();
            if (targetAlias == null) targetAlias = component.getName();

            if (sourceAlias.equals(targetAlias)) return component;
        }
        return null;
    }

    /**
     * Interface used by the decideAndMerge method to add components to the
     * target configuration / component lists.
     */
    private static interface ComponentAdder {
        public void addComponent(Component component);
    }

}
