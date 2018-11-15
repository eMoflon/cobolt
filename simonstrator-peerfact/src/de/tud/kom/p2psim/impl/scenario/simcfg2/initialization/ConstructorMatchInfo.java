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

package de.tud.kom.p2psim.impl.scenario.simcfg2.initialization;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.HashMap;

import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.Component;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * This class is used by the {@link de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator} when
 * trying to find viable XMLConfigurableConstructor. It contains
 * all information required to find the constructor, convert
 * parameters and invoke it.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 28.04.2012
 */
public class ConstructorMatchInfo {
	private String componentName;
	private int numerOfAttributesThatMatch = 0;
	private Constructor constructor;
	private XMLConfigurableConstructor annotation;
	private HashMap<String, Component> componentsInConstructor;

	public ConstructorMatchInfo(int numerOfAttributesThatMatch,
			Constructor constructor, XMLConfigurableConstructor annotation, HashMap<String, Component> componentsInConstructor) {
		super();
		this.numerOfAttributesThatMatch = numerOfAttributesThatMatch;
		this.constructor = constructor;
		this.annotation = annotation;
		this.componentsInConstructor = componentsInConstructor;
	}
	
	public HashMap<String, Component> getComponentInConstructor() {
		return componentsInConstructor;
	}
	
	public Class<?>[] getParameterTypes() {
		return constructor.getParameterTypes();
	}

	public int getNumerOfAttributesThatMatch() {
		return numerOfAttributesThatMatch;
	}

	public void setNumerOfAttributesThatMatch(int numerOfAttributesThatMatch) {
		this.numerOfAttributesThatMatch = numerOfAttributesThatMatch;
	}

	public Constructor getConstructor() {
		return constructor;
	}

	public void setConstructor(Constructor constructor) {
		this.constructor = constructor;
	}

	public XMLConfigurableConstructor getAnnotation() {
		return annotation;
	}

	public void setAnnotation(XMLConfigurableConstructor annotation) {
		this.annotation = annotation;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}
	
	public String toString() {
		return "ConstructorMatchInfo[componentName: " + componentName + ", Parameters: " + Arrays.asList(annotation.value()) + "]";
	}
}
