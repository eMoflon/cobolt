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
package de.tud.kom.p2psim.impl.scenario.simcfg2.utils;

import java.util.HashMap;

import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.UnknownSetting;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;

/**
 * This class represents a group of hosts and can be created
 * like a normal component as part of the {@link SimCfgDefaultHostBuilder}
 * 
 * @author Fabio Zöllner
 * @version 1.0, 28.04.2012
 */
public class HostGroup {
	private String groupId = null;
	private int size = 0;
	private HashMap<String, Value> properties = new HashMap<String, Value>();
	private String[] layers = new String[0];
	
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public void setComponents(String[] componentNames) {
		String[] convertedNames = new String[componentNames.length];
		for (int i = 0; i < componentNames.length; i++) {
			convertedNames[i] = SimCfgUtil.convertToInternalSetterNameFormat(componentNames[i]);
		}
		this.layers = convertedNames;
	}
	
	public String[] getComponents() {
		return layers;
	}
	
	public String getGroupId() {
		return this.groupId;
	}
	
	public int getSize() {
		return this.size;
	}
	
	public HashMap<String, Value> getProperties() {
		return this.properties;
	}
	
	/**
	 * This method is called whenever the SimCfg configurator
	 * has an attribute for which it can't find the required setter.
	 * 
	 * The value will be converted to a string and is given to
	 * this method. This replaced the old <Properties /> tag of the
	 * XML configuration.
	 * 
	 * @param key
	 * @param value
	 */
	@UnknownSetting
	public void _unknownSetting(String key, Value value) {
		properties.put(key, value);
	}
}
