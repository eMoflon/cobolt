/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api;

import java.util.LinkedList;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;

/**
 * Components have to register at the binder to be accessible from the other
 * side of the API.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class Binder {

	private static final List<GlobalComponent> components = new LinkedList<GlobalComponent>();

	/**
	 * Returns the given component (only global components!)
	 * 
	 * @param componentClass
	 * @return
	 */
	public static <T extends GlobalComponent> T getComponent(
			Class<T> componentClass) throws ComponentNotAvailableException {
		for (GlobalComponent component : components) {
			if (componentClass.isInstance(component)) {
				return componentClass.cast(component);
			}
		}
		throw new ComponentNotAvailableException();
	}

	/**
	 * If you can live with a null return and do not want to catch errors, this
	 * method is an alternative access to {@link GlobalComponent}s.
	 * 
	 * @param componentClass
	 * @return
	 */
	public static <T extends GlobalComponent> T getComponentOrNull(
			Class<T> componentClass) {
		for (GlobalComponent component : components) {
			if (componentClass.isInstance(component)) {
				return componentClass.cast(component);
			}
		}
		return null;
	}

	/**
	 * Returns all global components matching the interface
	 *
	 * @param componentClass
	 * @return
	 */
	public static <T extends GlobalComponent> List<T> getComponents(
			Class<T> componentClass) throws ComponentNotAvailableException {
		List<T> match = new LinkedList<T>();
		for (GlobalComponent component : components) {
			if (componentClass.isInstance(component)) {
				match.add(componentClass.cast(component));
			}
		}
		if (match.isEmpty()) {
			throw new ComponentNotAvailableException();
		} else {
			return match;
		}
	}

	/**
	 * Register a global component (i.e., a component that is not instantiated
	 * on a per-host basis). Do nothing if the component was already registered.
	 * 
	 * @param component
	 */
	public static <T extends GlobalComponent> void registerComponent(T component) {
		if (!components.contains(component)) {
			components.add(component);
		}
	}

}
