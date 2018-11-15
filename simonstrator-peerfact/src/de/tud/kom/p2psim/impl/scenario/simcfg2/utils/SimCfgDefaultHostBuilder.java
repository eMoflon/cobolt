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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.dom4j.Element;

import com.google.common.collect.Sets;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.api.scenario.simcfg.SimCfgHostBuilder;
import de.tud.kom.p2psim.api.scenario.simcfg.converter.SimCfgTypeConverter;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.common.DefaultHostProperties;
import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * This host builder uses the @Configure annotation of the SimCfgConfigurator to
 * configure its hosts. Groups are configured as simple group components in the
 * configuration.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 07.05.2012
 */
public class SimCfgDefaultHostBuilder implements SimCfgHostBuilder, HostBuilder {
	
	private int experimentSize = 0;
	private ArrayList<HostGroup> hostGroups = new ArrayList<HostGroup>();

	protected Map<String, List<SimHost>> groups = new HashMap<String, List<SimHost>>();
	protected final List<SimHost> hosts = new LinkedList<SimHost>();

	
	@Override
	public void parse(Element elem, Configurator config) {
		throw new ConfigurationException("The " + getClass().getSimpleName() + " doesn't support configuration via an XML subtree.");
	}

	@Configure
	public void _configure(SimCfgConfigurator configurator) {
		for (HostGroup hostGroup : hostGroups) {
			List<SimHost> group = new ArrayList<SimHost>(hostGroup.getSize());
			
			List<HostComponentFactory> instanciatedLayers = new Vector<HostComponentFactory>();
			
			for (String layer : hostGroup.getComponents()) {
				HostComponentFactory factory = configurator.getFactory(layer);
				if (factory == null) {
					Monitor.log(SimCfgDefaultHostBuilder.class, Level.DEBUG,
							"Host group " + hostGroup.getGroupId()
									+ ": Couldn't obtain factory '" + layer
									+ "', ignoring.");
				} else {
					Monitor.log(SimCfgDefaultHostBuilder.class, Level.DEBUG,
							"Found factory '"
									+ factory.getClass().getSimpleName() + "'.");
					instanciatedLayers.add(factory);
				}
			}
			
			Monitor.log(SimCfgDefaultHostBuilder.class, Level.INFO,
					"Configuring host group '" + hostGroup.getGroupId()
							+ "' with " + hostGroup.getSize() + " nodes.");
			
			// create hosts and instances of specified components for each host
            Set<String> warnedKeys = Sets.newHashSet();
            for (int i = 0; i < hostGroup.getSize(); i++) {
				DefaultHost host = new DefaultHost();

				// initialize properties
				DefaultHostProperties hostProperties = new DefaultHostProperties();
				host.setProperties(hostProperties);
				
				// minimal information for host properties is the group id
				hostProperties.setGroupID(hostGroup.getGroupId());

				// host properties
				for (String key : hostGroup.getProperties().keySet()) {
					try {
						configurator.configureAttribute(hostProperties, key, hostGroup.getProperties().get(key));
					} catch (ConfigurationException e) {
                        if (!warnedKeys.contains(key)) {
                            warnedKeys.add(key);
							Monitor.log(
									SimCfgDefaultHostBuilder.class,
									Level.WARN,
									"Loss of precession: Could not set the attribute '"
											+ key
											+ "' on host group '"
											+ hostGroup.getGroupId()
											+ "'. Adding a custom property with type String.");
                        }
						hostProperties.setProperty(key, (String) SimCfgTypeConverter.convertTo(key, hostGroup.getProperties().get(key), String.class));
					}
				}
				
				for (HostComponentFactory cF : instanciatedLayers) {
					//log.debug("Creating layer '" + cF.getClass().getSimpleName() + "'");
					HostComponent comp = cF.createComponent(host);
					host.registerComponent(comp);
				}
				group.add(host);
			}
			Monitor.log(SimCfgDefaultHostBuilder.class, Level.DEBUG,
					"Created a group with " + group.size() + " hosts");
			hosts.addAll(group);
			groups.put(hostGroup.getGroupId(), group);
		}
		

		// initialize all hosts
		for (SimHost host : hosts) {
			host.initialize();
		}

		if (hosts.size() != experimentSize) {
			Monitor.log(
					SimCfgDefaultHostBuilder.class,
					Level.WARN,
					"Only "
							+ hosts.size()
							+ " hosts were specified, though the experiment size was set to "
							+ experimentSize);
		}
	}
	
	@Override
	public Map<String, List<SimHost>> getAllHostsWithGroupIDs() {
		return this.groups;
	}

	@Override
	public List<SimHost> getAllHosts() {
		return this.hosts;
	}
	
	public void setExperimentSize(int size) {
		this.experimentSize = size;
	}
	
	public void setGroup(HostGroup group) {
		hostGroups.add(group);
	}
}
