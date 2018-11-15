/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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
 *
 */

package de.tud.kom.p2psim.impl.topology.views.visualization;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.tud.kom.p2psim.impl.topology.views.visualization.ui.VisualizationComponent;

/**
 * This manager keeps track of all components that are added to the UI. It can
 * be used to add/remove named component to/from the UI and allows to change the
 * components visibility based on their name or component reference.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 12.07.2012
 */
public class ComponentVisManager {
	private Multimap<String, JComponent> nameToComponentMap = ArrayListMultimap
			.create();

	private Map<JComponent, VisInfo> componentToVisInfoMap = Maps
			.newConcurrentMap();

	private Map<JComponent, Boolean> componentToActivatedMap = Maps
			.newConcurrentMap();

	private JComponent worldPanel;

	private List<VisualizationListener> visListeners = Lists.newArrayList();

	public ComponentVisManager(JComponent worldPanel) {
		this.worldPanel = worldPanel;
	}

	/**
	 * Adds the given component to the visualization
	 * 
	 * @param name
	 * @param priority
	 * @param component
	 */
	public void addComponent(String name, int priority, JComponent component) {
		addComponent(new VisInfo(name, priority, component));
	}

	/**
	 * Adds the given component to the visualization
	 * 
	 * @param name
	 * @param priority
	 * @param component
	 */
	public void addComponent(VisualizationComponent comp) {
		addComponent(new VisInfo(comp));
	}

	/**
	 * Adds the component of the given VisInfo to the visualization
	 * 
	 * @param visInfo
	 */
	public void addComponent(VisInfo visInfo) {
		nameToComponentMap.putAll(visInfo.getName(), visInfo.getComponents());
		for (JComponent component : visInfo.getComponents()) {
			componentToVisInfoMap.put(component, visInfo);
			worldPanel.add(component, visInfo.getPriority());
			worldPanel.validate();
			if (visInfo.isActiveByDefault()) {
				componentToActivatedMap.put(component, true);
				activateComponent(component);
			} else {
				componentToActivatedMap.put(component, false);
				deactivateComponent(component);
			}
		}

		raiseVisualizationAdded(visInfo);
	}

	/**
	 * Removes the given component from the visualization
	 * 
	 * @param name
	 */
	public void removeComponent(String name) {
		if (!nameToComponentMap.containsKey(name))
			return;

		Collection<JComponent> components = nameToComponentMap.get(name);

		for (JComponent component : components) {
			removeComponent(component);
		}
	}

	/**
	 * Removes the given component from the visualization
	 * 
	 * @param component
	 */
	public void removeComponent(JComponent component) {
		if (!componentToVisInfoMap.containsKey(component))
			return;

		VisInfo visInfo = componentToVisInfoMap.remove(component);

		nameToComponentMap.remove(visInfo.getName(), component);

		componentToActivatedMap.remove(component);

		worldPanel.remove(component);

		raiseVisualizationRemoved(visInfo);
	}

	/**
	 * Toggles the visibility of a component.
	 * 
	 * @param component
	 */
	public void toggleComponent(String name) {
		for (JComponent component : nameToComponentMap.get(name)) {
			toggleComponent(component);
		}
	}

	/**
	 * Toggles the visibility of a component.
	 * 
	 * It's the same as component.setVisible(!component.isVisible());
	 * 
	 * @param component
	 */
	public void toggleComponent(JComponent component) {
		if (component == null)
			return;

		componentToActivatedMap.put(component,
				!componentToActivatedMap.get(component));
		component.setVisible(!component.isVisible());
	}

	public boolean isActivated(JComponent component) {
		if (!componentToActivatedMap.containsKey(component)) {
			return false;
		}
		return componentToActivatedMap.get(component);
	}

	/**
	 * Sets the given component invisible.
	 * 
	 * @param component
	 */
	public void deactivateComponent(String name) {
		for (JComponent component : nameToComponentMap.get(name)) {
			deactivateComponent(component);
		}
	}

	/**
	 * Sets the given component invisible.
	 * 
	 * It's the same as component.setVisible(false);
	 * 
	 * @param component
	 */
	public void deactivateComponent(JComponent component) {
		if (component == null)
			return;
		component.setVisible(false);
		componentToActivatedMap.put(component, false);
		component.setVisible(false);
	}

	/**
	 * Sets the given component visible.
	 * 
	 * @param component
	 */
	public void activateComponent(String name) {
		for (JComponent component : nameToComponentMap.get(name)) {
			activateComponent(component);
		}
	}

	/**
	 * Sets the given component visible.
	 * 
	 * It's the same as component.setVisible(true);
	 * 
	 * @param component
	 */
	public void activateComponent(JComponent component) {
		if (component == null)
			return;
		component.setVisible(true);
		componentToActivatedMap.put(component, true);
	}

	/**
	 * Adds a visualization listener to the manager
	 * 
	 * @param listener
	 */
	public void addVisualizationListener(VisualizationListener listener) {
		this.visListeners.add(listener);
	}

	/**
	 * Removes a visualization listener from the manager
	 * 
	 * @param listener
	 */
	public void removeVisualizationListener(VisualizationListener listener) {
		this.visListeners.remove(listener);
	}

	private void raiseVisualizationAdded(VisInfo visInfo) {
		for (VisualizationListener l : this.visListeners) {
			l.visualizationAdded(visInfo);
		}
	}

	private void raiseVisualizationRemoved(VisInfo visInfo) {
		for (VisualizationListener l : this.visListeners) {
			l.visualizationRemoved(visInfo);
		}
	}

	/**
	 * Bean that holds the name, priority and reference to the component.
	 * 
	 * Note: The equals and hashCode methods omit the priority.
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 12.07.2012
	 */
	public static class VisInfo {

		private String name;

		private int priority;

		private boolean activeByDefault = true;

		private boolean showInList = true;

		/**
		 * Recommended way of adding a custom visualization-layer.
		 */
		public VisualizationComponent visComp;

		private List<JComponent> components = Lists.newArrayList();

		public VisInfo(String name, int priority, JComponent... component) {
			this.setName(name);
			this.setPriority(priority);
			this.components.addAll(Arrays.asList(component));
		}

		/**
		 * Recommended way: use the {@link VisualizationComponent}
		 * 
		 * @param comp
		 */
		public VisInfo(VisualizationComponent comp) {
			this.visComp = comp;
			this.name = comp.getDisplayName();
			this.activeByDefault = !comp.isHidden();
			this.showInList = true;
			this.components.add(comp.getComponent());
		}

		public String getName() {
			return name;
		}

		/**
		 * Recommended container for custom visualizations.
		 * 
		 * @return may be null (legacy visualizations)
		 */
		public VisualizationComponent getVisualizationComponent() {
			return visComp;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getPriority() {
			return priority;
		}

		public void setPriority(int priority) {
			this.priority = priority;
		}

		public List<JComponent> getComponents() {
			return components;
		}

		public void addComponent(JComponent component) {
			this.components.add(component);
		}

		public void removeComponent(JComponent component) {
			this.components.remove(component);
		}

		public void setComponents(List<JComponent> components) {
			this.components = components;
		}

		public boolean isActiveByDefault() {
			return activeByDefault;
		}

		public void setActiveByDefault(boolean activeByDefault) {
			this.activeByDefault = activeByDefault;
		}

		public boolean isShowInList() {
			return showInList;
		}

		public void setShowInList(boolean showInList) {
			this.showInList = showInList;
		}
	}

	/**
	 * A listener interface that is used by the ComponentVisManager to notify
	 * interested parties about added or removed visualization components.
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 12.07.2012
	 */
	public static interface VisualizationListener {
		public void visualizationAdded(VisInfo visInfo);

		public void visualizationRemoved(VisInfo visInfo);
	}

	/**
	 * Returns a list of currently visible and invisible components.
	 * 
	 * @return
	 */
	public Collection<VisInfo> getVisualizations() {
		return this.componentToVisInfoMap.values();
	}
}
