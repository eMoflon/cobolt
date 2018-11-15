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

import java.lang.reflect.Method;

import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

public class ComponentConfigurationDescriptor {
	private Object component;
	private Method method;
	private After after;

    public void setComponent(Object component) {
		this.component = component;
	}

	public void setConfigureMethod(Method method) {
		this.method = method;
	}

	public void setAfterAnnotation(After after) {
		this.after = after;
	}

	public Object getComponent() {
		return component;
	}
	
	public Method getConfigureMethod() {
		return method;
	}
	
	public After getAfterAnnotation() {
		return after;
	}
	
	public Class[] getOptionalDependencies() {
		if (after == null) return new Class[]{};
		return after.optional();
	}
	
	public Class[] getRequiredDependencies() {
		if (after == null) return new Class[]{};
		return after.required();
	}
	
	@Override
	public String toString() {
		return "CfgDescriptor[" + component.getClass().getSimpleName() + "]";
	}

    public boolean isFactory() {
        return component instanceof HostComponentFactory;
    }
}
