/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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



package de.tud.kom.p2psim.impl.scenario;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.dom4j.Element;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.scenario.Builder;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.HostBuilder;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.impl.common.DefaultHost;
import de.tud.kom.p2psim.impl.common.DefaultHostProperties;
import de.tud.kom.p2psim.impl.common.FakeHost;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;

/**
 * This builder will parse an XML subtree and create hosts as specified there.
 * It expects a tree which looks as follows: <code>
 * &lt;HostBuilder&gt;
 * 	  &lt;Host groupID="..."&gt;...
 *   &lt;Group size="..." groupID="..."&gt;...
 * &lt;HostBuilder/&gt;
 * </code>
 * 
 * The exact values for XML tags are specified as constants in this class (see
 * below).
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 29.11.2007
 * 
 */
public class DefaultHostBuilder implements HostBuilder, Builder {
	/**
	 * XML attribute with this name specifies the size of the group.
	 */
	public static final String GROUP_SIZE_TAG = "size";

	/**
	 * XML element with this name specifies a group of hosts.
	 */
	public static final String GROUP_TAG = "Group";

	/**
	 * XML element with this name specifies a single host and behaves equivalent
	 * to an element with the name = GROUP_TAG value and group size of 1.
	 */
	public static final String HOST_TAG = "Host";

	/**
	 * XML attribute with this name specifies the id of the group, which is used
	 * to refer to this group lateron, e.g. when you specify scenario actions.
	 */
	public static final String GROUP_ID_TAG = "groupID";

	/**
	 * Groups of hosts indexed by group ids.
	 */
	protected final Map<String, List<SimHost>> groups = new LinkedHashMap<>();

	protected int experimentSize;

	/**
	 * Flat list of all hosts.
	 */
	protected final List<SimHost> hosts = new LinkedList<SimHost>();

	/**
	 * Will be called by the configurator.
	 * 
	 * @param size
	 *            total number of hosts in the simulator TODO we could remove
	 *            this or force its correctness...
	 */
	public void setExperimentSize(int size) {
		this.experimentSize = size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.scenario.HostBuilder#getAllHostsWithGroupIDs()
	 */
	@Override
	public Map<String, List<SimHost>> getAllHostsWithGroupIDs() {
		Map<String, List<SimHost>> hosts = new LinkedHashMap<String, List<SimHost>>(
				groups);
		return hosts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.scenario.HostBuilder#getAllHosts()
	 */
	@Override
	public List<SimHost> getAllHosts() {
		return hosts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.scenario.HostBuilder#parse(org.dom4j.Element,
	 * de.tud.kom.p2psim.api.scenario.Configurator)
	 */
	@Override
	public void parse(Element elem, Configurator config) {
		DefaultConfigurator defaultConfigurator = (DefaultConfigurator) config;

		// create groups
		for (Iterator iter = elem.elementIterator(); iter.hasNext();) {
			Element groupElem = (Element) iter.next();
			String groupID = groupElem.attributeValue(GROUP_ID_TAG);
			if (groupID == null) {
				throw new IllegalArgumentException("Id of host/group "
						+ groupElem.asXML() + " must not be null");
			}

			// either a group of hosts or a single host (=group with size 1)
			int groupSize;
			if (groupElem.getName().equals(HOST_TAG)) {
				groupSize = 1;
			} else if (groupElem.getName().equals(GROUP_TAG)) {
				String attributeValue = config.parseValue(groupElem
						.attributeValue(GROUP_SIZE_TAG));
				groupSize = Integer.parseInt(attributeValue);
				if (groupSize == 0) {
					continue;
				}
			} else {
				throw new IllegalArgumentException("Unexpected tag "
						+ groupElem.getName());
			}
			List<SimHost> group = new ArrayList<SimHost>(groupSize);

			boolean firstSeen = true;
			List<HostComponentFactory> instanciatedLayers = new Vector<HostComponentFactory>();

			// create hosts and instances of specified components for each host
			for (int i = 0; i < groupSize; i++) {

				DefaultHost host = createNewDefaultHost();

				if (!(host instanceof FakeHost) && firstSeen) {
					firstSeen = false;
					/*
					 * Create instances ONLY ONCE for each group
					 */
					for (Iterator layers = groupElem.elementIterator(); layers
							.hasNext();) {
						Element layerElem = (Element) layers.next();
						if (!layerElem.getName().equals(
								Configurator.HOST_PROPERTIES_TAG)) {
							HostComponentFactory layer = (HostComponentFactory) defaultConfigurator
									.configureComponent(layerElem);
							if (layer == null) {
								Monitor.log(
										DefaultHostBuilder.class,
										Level.WARN,
										"Host group "
												+ groupElem.getName()
										+ ": An element of name '"
										+ layerElem.getName()
										+ "' is not configured. Ignoring.");
							} else {
								instanciatedLayers.add(layer);
							}
						}
					}
				}

				/* Skip this round if no new host was created.
				 * Might happen with RealNetworkingLayer.
				 */
				if( ! (host instanceof FakeHost) ) {
					// initialize properties
					DefaultHostProperties hostProperties = new DefaultHostProperties();
					host.setProperties(hostProperties);
					// minimal information for host properties is the group id
					hostProperties.setGroupID(groupID);
		
					// host properties
					for (Iterator layers = groupElem.elementIterator(); layers
							.hasNext();) {
						Element layerElem = (Element) layers.next();
						if (layerElem.getName().equals(
								Configurator.HOST_PROPERTIES_TAG)) {
							defaultConfigurator.configureAttributes(hostProperties,
									layerElem);
						}
					}
					for (HostComponentFactory cF : instanciatedLayers) {
						HostComponent comp = cF.createComponent(host);
						host.registerComponent(comp);
					}
				}
				group.add(host);
			}
			Monitor.log(DefaultHostBuilder.class, Level.DEBUG,
					"Created a group with " + group.size() + " hosts");
			hosts.addAll(group);
			groups.put(groupID, group);
		}
		Monitor.log(DefaultHostBuilder.class, Level.INFO,
				"CREATED " + hosts.size() + " hosts");

		// Populate Global Oracle
		GlobalOracle.populate(hosts);

		// initialize all hosts
		for (SimHost host : hosts) {
			host.initialize();
		}

		// initializeTopology (should be called after the initialize of all
		// hosts.
		/*
		 * FIXME (BR): is this really necessary?
		 */
		if (hosts.size() > 0) {
			TopologyComponent comp = hosts.iterator().next()
					.getTopologyComponent();
			if (comp != null) {
				Topology topo = comp.getTopology();
				if (topo != null) {
					topo.initializeSocial();
				}
			}
		}

		if (hosts.size() != experimentSize) {
			Monitor.log(
					DefaultHostBuilder.class,
					Level.WARN,
					"Only "
					+ hosts.size()
					+ " hosts were specified, though the experiment size was set to "
					+ experimentSize);
		}
	}

	/**
	 * Creates the a new default host.
	 * 	This is refactored as RealNetworkingHostBuilder needs to override this.
	 *
	 * @return the default host
	 */
	protected DefaultHost createNewDefaultHost() {
		return new DefaultHost();
	}

}
