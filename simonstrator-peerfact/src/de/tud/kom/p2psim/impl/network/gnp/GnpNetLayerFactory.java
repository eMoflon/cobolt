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


package de.tud.kom.p2psim.impl.network.gnp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.BandwidthImpl;
import de.tud.kom.p2psim.impl.network.AbstractNetLayerFactory;
import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.gnp.topology.CountryLookup;
import de.tud.kom.p2psim.impl.network.gnp.topology.GnpPosition;
import de.tud.kom.p2psim.impl.network.gnp.topology.PingErLookup;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class GnpNetLayerFactory extends AbstractNetLayerFactory {

	private final GnpSubnet subnet;

	private HashMap<IPv4NetID, GnpHostInfo> hostPool;

	private HashMap<String, ArrayList<IPv4NetID>> namedGroups;

	private PingErLookup pingErLookup;

	private CountryLookup countryLookup;

	public GnpNetLayerFactory() {
		super();
		subnet = new GnpSubnet();
	}

	public GnpNetLayer createComponent(Host pureHost) {
		SimHost host = (SimHost) pureHost;
		GnpNetLayer netLayer = newNetLayer(host, host.getProperties()
				.getGroupID());
		return netLayer;
	}

	/**
	 * random node form group
	 * 
	 * @param id
	 * @return
	 */
	public GnpNetLayer newNetLayer(SimHost host, String id) {
		if (this.namedGroups.containsKey(id)
				&& !this.namedGroups.get(id).isEmpty()) {
			int size = namedGroups.get(id).size();
			IPv4NetID netId = namedGroups.get(id).get(
					Randoms.getRandom(GnpNetLayerFactory.class).nextInt(size));
			namedGroups.get(id).remove(netId);
			return newNetLayer(host, netId);

		} else {
			throw new IllegalStateException(
					"No (more) Hosts are assigned to \"" + id + "\"");
		}
	}

	// general method for allocation of bandwidth capacities, which depends on
	// the provided class by the setBandwidthDetermination-method
	private GnpNetLayer newNetLayer(SimHost host, IPv4NetID netID) {
		GnpPosition gnpPos = this.hostPool.get(netID).getGnpPosition();
		GeoLocation geoLoc = this.hostPool.get(netID).getGeoLoc();
		BandwidthImpl bw = getBandwidth(netID);
		GnpNetLayer nw = new GnpNetLayer(host, this.subnet, netID, gnpPos,
				geoLoc, bw);
		// hostPool.remove(netID); //TODO: Why remove? This information is
		// needed if a host is in multiple groups
		return nw;
	}

	public void setGnpFile(String gnpFileName) {
		
		File gnpFile = new File(gnpFileName);
		hostPool = new HashMap<IPv4NetID, GnpHostInfo>();
		namedGroups = new HashMap<String, ArrayList<IPv4NetID>>();
		
		SAXReader reader = new SAXReader(false);

		Document configuration = null;
		try {
			configuration = reader.read(gnpFile);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
		
		Element root = configuration.getRootElement();
		assert root.getName().equals("gnp");

		for (Object obj : root.elements()) {
			Element elem = (Element) obj;
			if (elem.getName().equals("GroupLookup")) {
				for (Iterator iter = elem.elementIterator("Group"); iter
						.hasNext();) {
					Element variable = (Element) iter.next();
					String id = variable.attributeValue("id");

					ArrayList<IPv4NetID> group = new ArrayList<IPv4NetID>();
					for (Iterator ipIter = variable.elementIterator("IPs"); ipIter
							.hasNext();) {
						Element ipElement = (Element) ipIter.next();
						String[] ips = ipElement.attributeValue("value").split(
								",");
						for (int c = 0; c < ips.length; c++)
							group.add(new IPv4NetID(Long.parseLong(ips[c])));
					}
					if (namedGroups.containsKey(id)) {
						throw new IllegalStateException(
								"Multiple Group Definition in " + gnpFileName
										+ " ( Group: " + id + " )");
					} else {
						namedGroups.put(id, group);
					}
				}
			} else if (elem.getName().equals("Hosts")) {
				for (Iterator iter = elem.elementIterator("Host"); iter
						.hasNext();) {
					Element variable = (Element) iter.next();

					// IP-Address
					IPv4NetID hostID = new IPv4NetID(Long.parseLong(variable
							.attributeValue("ip")));

					// GNP-Coordinates
					String[] coordinatesS = variable.attributeValue(
							"coordinates").split(",");
					double[] coordinatesD = new double[coordinatesS.length];
					for (int c = 0; c < coordinatesD.length; c++)
						coordinatesD[c] = Double.parseDouble(coordinatesS[c]);
					GnpPosition gnpPos = new GnpPosition(coordinatesD);

					// GeoLocation
					String continentalArea = variable
							.attributeValue("continentalArea");
					String countryCode = variable.attributeValue("countryCode");
					String region = variable.attributeValue("region");
					String city = variable.attributeValue("city");
					String isp = variable.attributeValue("isp");
					double longitude = Double.parseDouble(variable
							.attributeValue("longitude"));
					double latitude = Double.parseDouble(variable
							.attributeValue("latitude"));
					GeoLocation geoLoc = new GeoLocation(continentalArea,
							countryCode, region, city, isp, latitude, longitude);

					GnpHostInfo hostInfo = new GnpHostInfo(geoLoc, gnpPos);
					hostPool.put(hostID, hostInfo);
				}
			} else if (elem.getName().equals("PingErLookup")) {
				pingErLookup = new PingErLookup();
				pingErLookup.loadFromXML(elem);

			} else if (elem.getName().equals("CountryLookup")) {
				countryLookup = new CountryLookup();
				countryLookup.importFromXML(elem);
			}
		}
	}

	public void setLatencyModel(GnpLatencyModel model) {
		model.init(pingErLookup, countryLookup);
		subnet.setLatencyModel(model);
	}

	public void setBandwidthManager(AbstractGnpNetBandwidthManager bm) {
		subnet.setBandwidthManager(bm);
	}

	public void setPbaPeriod(double seconds) {
		subnet.setPbaPeriod(Math.round(seconds * Time.SECOND));
	}

	private class GnpHostInfo {

		private GnpPosition gnpPosition;

		private GeoLocation geoLoc;

		public GnpHostInfo(GeoLocation geoLoc, GnpPosition gnpPos) {
			this.gnpPosition = gnpPos;
			this.geoLoc = geoLoc;
		}

		GnpPosition getGnpPosition() {
			return gnpPosition;
		}

		GeoLocation getGeoLoc() {
			return geoLoc;
		}

	}

}
