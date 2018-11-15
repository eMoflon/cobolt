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


package de.tud.kom.p2psim.impl.network.gnp.topology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;

import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.gnp.geoip.IspLookupService;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.City;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Continent;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Country;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.PingErRegion;
import de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB.Region;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

/**
 * This class holds all informations needed to built an xml-Host File for the
 * simulations with the GnpNetLayer and GnpLatencyModel
 * 
 * @author Gerald Klunker
 * @version 0.1, 05.02.2008
 * 
 */
public class HostMap {

	private static final String GROUP_WORLD = "World";

	private static final String COUNTRY_UNLOCATABLE = "#UNLOCATABLE_COUNTRY";

	private static final String REGION_UNLOCATABLE = "#UNLOCATABLE_REGION";

	private static final String PINGER_REGION_UNLOCATABLE = "#PINGER_REGION_UNLOCATABLE";

	// importet Files
	private HashMap<String, HashSet<Host>> skitterFiles = new HashMap<String, HashSet<Host>>();

	private File geolocationFile;

	private IspLookupService ispService;

	// Host Index and Groups
	private HashMap<Integer, Host> monitorIndex = new HashMap<Integer, Host>();

	private HashMap<Integer, Host> hostIndex = new HashMap<Integer, Host>();

	private ArrayList<Host>[][][] quickLookup;

	private HashMap<String, Set<Host>> groups = new HashMap<String, Set<Host>>();

	// Country - Region - PingEr dictionary
	private PingErLookup pingErLookup = new PingErLookup();

	private CountryLookup countryLookup = new CountryLookup();

	private GnpSpace gnpRef;

	/**
	 * import Hosts and RTTs from a CAIDA skitter File
	 * 
	 * @param skitterFile
	 */
	public void importSkitterFile(File skitterFile, boolean oldFormat) {
		try {

			skitterFiles.put(skitterFile.getAbsolutePath(), new HashSet<Host>());
			FileReader inputFile = new FileReader(skitterFile);
			BufferedReader input = new BufferedReader(inputFile);

			int validLines = 0;
	
			String line = input.readLine();
			while (line != null) {
				
				if (line.length() < 1024) {
				
					int commentbegin = line.indexOf("#");
					String line2parse = commentbegin < 0?line:line.substring(0, commentbegin); //ignore comments
					
					//System.out.println("Line to parse is: " + line2parse);
					if (!"".equals(line2parse.trim())) {
						if(oldFormat?parseLineOldFormat(line2parse, skitterFile):parseLineNewFormat(line2parse, skitterFile)) validLines++;
					}
					
				} else {
					//sometimes weird long lines occur when the file is corrupted:
					Monitor.log(HostMap.class, Level.ERROR,
							"The weird long line with length " + line.length()
									+ "' could not be parsed in skitter file "
									+ skitterFile);
				}
				line = input.readLine();
			}
			System.gc();
			System.out.println("Imported " + validLines + " valid entries from skitter file " + skitterFile + ".");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean parseLineOldFormat(String line, File skitterFile) {
		
		int monitorIP;
		int peerIP;
		double rtt;
		Host currentMonitor;
		Host currentPeer;
		
		String[] tokens = line.split("\\s+");
		if (tokens[0].equals("C") || tokens[0].equals("I")) {
			
			if (tokens.length >= 5) {
			
				String mo = tokens[1];
				String pe = tokens[2];
				String rt = tokens[4];

				monitorIP = IPv4NetID.ipToInt(mo);
				peerIP = IPv4NetID.ipToInt(pe);
				rtt = Double.valueOf(rt);

				currentMonitor = hostIndex.get(monitorIP);
				if (currentMonitor == null) {
					currentMonitor = new Host(monitorIP, this);
					monitorIndex.put(monitorIP, currentMonitor);
					hostIndex.put(monitorIP, currentMonitor);
				}
				if (!monitorIndex.containsValue(currentMonitor)) {
					monitorIndex.put(monitorIP, currentMonitor);
				}
				currentMonitor.setHostType(Host.MONITOR);
				currentPeer = hostIndex.get(peerIP);
				if (currentPeer == null) {
					currentPeer = new Host(peerIP, this);
					currentPeer.setHostType(Host.HOST);
					hostIndex.put(peerIP, currentPeer);
				}
				currentPeer.addRtt(currentMonitor, rtt);
				skitterFiles.get(skitterFile.getAbsolutePath()).add(currentPeer);
				return true;
			} else
				Monitor.log(HostMap.class, Level.ERROR, "The line '" + line
						+ "' did not contain enough elements. in skitter file "
						+ skitterFile);
		}
		return false;
	}
	
	private boolean parseLineNewFormat(String line, File skitterFile) {
		
		int monitorIP;
		int peerIP;
		double rtt;
		Host currentMonitor;
		Host currentPeer;
		
		String[] tokens = line.split("\\s+");
		//System.out.println(StringToolkit.arrayToString(tokens, "|"));
		if (tokens[0].equals("T") && tokens.length >= 7) {
			
			if (tokens[6].equals("R") && tokens.length >= 8) {
			
				String mo = tokens[1];
				String pe = tokens[2];
				String rt = tokens[7];

				monitorIP = IPv4NetID.ipToInt(mo);
				peerIP = IPv4NetID.ipToInt(pe);
				rtt = Double.valueOf(rt);

				currentMonitor = hostIndex.get(monitorIP);
				if (currentMonitor == null) {
					currentMonitor = new Host(monitorIP, this);
					monitorIndex.put(monitorIP, currentMonitor);
					hostIndex.put(monitorIP, currentMonitor);
				}
				if (!monitorIndex.containsValue(currentMonitor)) {
					monitorIndex.put(monitorIP, currentMonitor);
				}
				currentMonitor.setHostType(Host.MONITOR);
				currentPeer = hostIndex.get(peerIP);
				if (currentPeer == null) {
					currentPeer = new Host(peerIP, this);
					currentPeer.setHostType(Host.HOST);
					hostIndex.put(peerIP, currentPeer);
				}
				currentPeer.addRtt(currentMonitor, rtt);
				skitterFiles.get(skitterFile.getAbsolutePath()).add(currentPeer);
				//System.out.println("Successfully loaded value.");
				return true;
			} else
				Monitor.log(HostMap.class, Level.ERROR, "The line '" + line
						+ "' did not contain enough elements. in skitter file "
						+ skitterFile);
		}
		return false;
	}

	/**
	 * import PingER summary report File
	 * 
	 * @param file
	 */
	public void importPingErMinimumRtt(File file) {
		pingErLookup.loadFromTSV(file, PingErLookup.DataType.MIN_RTT);
	}

	/**
	 * import PingER summary report File
	 * 
	 * @param file
	 */
	public void importPingErAverageRtt(File file) {
		pingErLookup.loadFromTSV(file, PingErLookup.DataType.AVERAGE_RTT);
	}

	/**
	 * import PingER summary report File
	 * 
	 * @param file
	 */
	public void importPingErDelayVariation(File file) {
		pingErLookup.loadFromTSV(file, PingErLookup.DataType.VARIATION_RTT);
	}

	/**
	 * import PingER summary report File
	 * 
	 * @param file
	 */
	public void importPingErPacketLoss(File file) {
		pingErLookup.loadFromTSV(file, PingErLookup.DataType.PACKET_LOSS);
	}

	/**
	 * 
	 * @return country dictionary
	 */
	public CountryLookup getCountryLookup() {
		return countryLookup;
	}

	/**
	 * 
	 * @return pingerEr lookup table
	 */
	public PingErLookup getPingErLookup() {
		return pingErLookup;
	}

	/**
	 * 
	 * @param longitude
	 * @param latitude
	 * @return set of the nearest hosts to longitude/latitude position
	 */
	public Set<Host> getNearestHosts(double longitude, double latitude) {
		HashSet<Host> result = new HashSet<Host>();
		int index0 = (int) Math.floor(HostMap.getGeographicalDistance(90, 0, latitude, longitude) / 1000.0);
		int index1 = (int) Math.floor(HostMap.getGeographicalDistance(0, 0, latitude, longitude) / 1000.0);
		int index2 = (int) Math.floor(HostMap.getGeographicalDistance(0, 90, latitude, longitude) / 1000.0);
		PeerComparatorDistance comparator = new PeerComparatorDistance();
		comparator.setPosition(latitude, longitude);
		Host peer = null;
		if (quickLookup != null && quickLookup[index0][index1][index2] != null) {
			for (Host p : quickLookup[index0][index1][index2]) {
				if (peer == null) {
					peer = p;
				} else if (comparator.compare(peer, p) == 1) {
					peer = p;
				}
			}
			for (Host p : quickLookup[index0][index1][index2]) {
				if (comparator.compare(peer, p) == 0)
					result.add(p);
			}
		}
		return result;
	}

	/**
	 * rebuilt an array of hosts for fast finding of nearest hosts
	 */
	private void builtQuickLookup() {
		quickLookup = new ArrayList[21][21][21];
		int index0 = 0;
		int index1 = 0;
		int index2 = 0;
		Set<Integer> ips = this.hostIndex.keySet();
		for (Integer ip : ips) {
			index0 = (int) Math.floor(HostMap.getGeographicalDistance(90, 0, hostIndex.get(ip).getLatitude(), hostIndex.get(ip).getLongitude()) / 1000.0);
			index1 = (int) Math.floor(HostMap.getGeographicalDistance(0, 0, hostIndex.get(ip).getLatitude(), hostIndex.get(ip).getLongitude()) / 1000.0);
			index2 = (int) Math.floor(HostMap.getGeographicalDistance(0, 90, hostIndex.get(ip).getLatitude(), hostIndex.get(ip).getLongitude()) / 1000.0);
			if (quickLookup[index0][index1][index2] == null)
				quickLookup[index0][index1][index2] = new ArrayList<Host>();
			quickLookup[index0][index1][index2].add(hostIndex.get(ip));
		}
	}

	/**
	 * 
	 * @return reference to the related GNP Space object
	 */
	public GnpSpace getGnpRef() {
		return gnpRef;
	}

	/**
	 * 
	 * @param gnp
	 *            reference to the related GNP Space object
	 */
	public void setGnpRef(GnpSpace gnp) {
		gnpRef = gnp;
	}

	/**
	 * 
	 * @return map of importet skitter files and hosts
	 */
	public HashMap<String, HashSet<Host>> getImportedSkitterFiles() {
		return skitterFiles;
	}

	/**
	 * counts the number of hosts with a certain number of measured RTTs [0]:
	 * number of Hosts with 0 measured RTTs, [1] number of Hosts with 1 measured
	 * RTTs ...
	 * 
	 * @return map from number of measured RTTs to number of related hosts
	 */
	public int[] getConnectivityOfHosts() {
		int[] counter = new int[getNoOfMonitors() + 1];
		for (Host host : hostIndex.values()) {
			if (host.getHostType() == Host.HOST)
				counter[host.getMeasuredMonitors().size()]++;
		}
		return counter;
	}

	/**
	 * 
	 * @param groupName
	 * @return Set of Host related to the Group
	 */
	public Collection<Host> getGroup(String groupName) {
		if (groups.containsKey(groupName))
			return groups.get(groupName);
		else
			return new HashSet<Host>();
	}

	/**
	 * 
	 * @return map of groupnames to related hosts
	 */
	public HashMap<String, Set<Host>> getGroups() {
		return groups;
	}

	/**
	 * 
	 * @param groupName
	 * @param host
	 */
	public void addHostToGroup(String groupName, Host host) {
		String name = groupName.replace(" ", "");
		if (!groups.containsKey(name))
			groups.put(name, new HashSet<Host>());
		groups.get(name).add(host);
	}

	/**
	 * 
	 * @param groupName
	 * @param hosts
	 *            set of hosts
	 */
	public void addHostToGroup(String groupName, Collection<Host> hosts) {
		String name = groupName.replace(" ", "");
		if (!groups.containsKey(name))
			groups.put(name, new HashSet<Host>());
		groups.get(name).addAll(hosts);
	}

	/**
	 * 
	 * @param groupName
	 * @param grid
	 */
	public void addHostToGroup(String groupName, boolean grid[][]) {
		Set<Host>[][] peerGrid = getHostGrid(grid.length, grid[0].length);
		for (int x = 0; x < grid.length; x++) {
			for (int y = 0; y < grid[x].length; y++) {
				if (grid[x][y]) {
					addHostToGroup(groupName, peerGrid[x][y]);
				}
			}
		}
	}

	/**
	 * 
	 * @param resLon
	 *            number of horizontal divisions
	 * @param resLat
	 *            number of vertical divisions
	 * @return hosts grouped by their geographical position
	 */
	private Set<Host>[][] getHostGrid(int resLon, int resLat) {
		Set<Host>[][] peerGrid = new HashSet[resLon][resLat];
		int posLon;
		int posLat;
		double stepLon = 360 / (double) resLon;
		double stepLat = 180 / (double) resLat;
		for (Host host : hostIndex.values()) {
			posLon = (int) Math.floor((180 + host.getLongitude()) / stepLon);
			posLat = (int) Math.floor((90 + host.getLatitude()) / stepLat);
			if (peerGrid[posLon][posLat] == null)
				peerGrid[posLon][posLat] = new HashSet<Host>();
			peerGrid[posLon][posLat].add(host);
		}
		return peerGrid;
	}

	/**
	 * 
	 * @param groupName
	 */
	public void removeGroup(String groupName) {
		groups.remove(groupName);
	}

	/**
	 * 
	 * @param groupNames
	 *            set of group names
	 */
	public void removeGroup(Set<String> groupNames) {
		for (String name : groupNames)
			removeGroup(name);
	}

	/**
	 * reduce the number of hosts in a group
	 * 
	 * @param groupName
	 * @param noOfHosts
	 */
	public void scaleGroup(String groupName, int newSize) {
		Set<Host> oldGroup = groups.get(groupName);
		Set<Host> newGroup = new HashSet<Host>();
		while (newGroup.size() < newSize && oldGroup.size() > 0) {
			Host[] hosts = oldGroup.toArray(new Host[0]);
			for (int c = 0; c < newSize - newGroup.size(); c++) {
				int random = (int) Math.floor(Math.random() * hosts.length);
				newGroup.add(hosts[random]);
				oldGroup.remove(hosts[random]);
			}
		}
		oldGroup.clear();
		oldGroup.addAll(newGroup);
	}

	/**
	 * generate groups with the name of GeoIP country names
	 */
	public void builtCountryGroups() {
		for (Host host : hostIndex.values()) {
			String country = countryLookup.getGeoIpCountryName(host.getCountryCode());
			if (country == null)
				country = COUNTRY_UNLOCATABLE;
			this.addHostToGroup(country, host);
		}
	}

	/**
	 * generate groups with the name of GeoIP country names
	 */
	public void builtRegionGroups() {
		for (Host host : hostIndex.values()) {
			String region = host.getRegion();
			if (region == null)
				region = REGION_UNLOCATABLE;
			this.addHostToGroup(region, host);
		}
	}

	/**
	 * generate groups with the name of PingER region names
	 */
	public void builtPingerGroups() {
		for (Host host : hostIndex.values()) {
			String country = countryLookup.getPingErRegionName(host.getCountryCode());
			if (country == null)
				country = PINGER_REGION_UNLOCATABLE;
			this.addHostToGroup(country, host);
		}
	}

	/**
	 * built a group with all hosts
	 */
	public void builtWorldGroups() {
		this.addHostToGroup(GROUP_WORLD, hostIndex.values());
	}

	/**
	 * 
	 * @return number of hosts in this map
	 */
	public int getNoOfHosts() {
		return hostIndex.size();
	}

	/**
	 * 
	 * @return number of monitors in this map
	 */
	public int getNoOfMonitors() {
		return monitorIndex.size();
	}

	/**
	 * 
	 * @param file
	 *            filename of a GeoIP binary database
	 */
	public void setGeolocationDatabase(File file) {
		geolocationFile = file;
	}

	public void setIspLocationDatabase(String db) {
		ispService = new IspLookupService(db);
	}

	/**
	 * 
	 * @return filename of the current GeoIP database
	 */
	public File getGeolocationDatabase() {
		return geolocationFile;
	}

	/**
	 * position all hosts with the current GeoIP database
	 */
	public void setLocationOfHosts() {
		if (geolocationFile == null) {
			return;
		}
		Geolocator locator = new GeolocatorGeoIP(geolocationFile);
		for (Host host : hostIndex.values()) {
			host.setLocation(locator, ispService);
		}
		builtQuickLookup();
		// distanceVsRttPlot("test100", null, 100);
	}

	/**
	 * 
	 * @return map from 2-digits country code to the related hosts
	 */
	public HashMap<String, HashSet<Host>> getLocations() {
		HashMap<String, HashSet<Host>> locations = new HashMap<String, HashSet<Host>>();
		locations.put("# LOCATABLE", new HashSet<Host>());
		locations.put("# UNLOCATABLE", new HashSet<Host>());
		for (Host host : hostIndex.values()) {
			if (host.isLocatable()) {
				if (!locations.containsKey(host.getCountryCode()))
					locations.put(host.getCountryCode(), new HashSet<Host>());
				locations.get(host.getCountryCode()).add(host);
				locations.get("# LOCATABLE").add(host);
			} else {
				locations.get("# UNLOCATABLE").add(host);
			}
		}
		return locations;
	}

	/**
	 * 
	 * @return map from ip to related monitor Host object
	 */
	public HashMap<Integer, Host> getMonitorIndex() {
		return monitorIndex;
	}

	/**
	 * 
	 * @return map from ip to related Host object
	 */
	public HashMap<Integer, Host> getHostIndex() {
		return hostIndex;
	}

	/**
	 * make the measured inter-monitor RTTs adjacency matrix symmetrical. => RTT
	 * A->B = RTT B->A assumtion is needed within the GNP coordinate model.
	 */
	public void makeSymmetrical() {
		for (Host monitorA : monitorIndex.values()) {
			for (Host monitorB : monitorIndex.values()) {
				if (monitorA == monitorB) {
					monitorA.removeRTT(monitorB);
				} else {
					double rtt1 = monitorA.getRtt(monitorB);
					double rtt2 = monitorB.getRtt(monitorA);
					double newRtt = Double.NaN;
					if (rtt1 > 0 && rtt2 > 0)
						newRtt = (rtt1 + rtt2) / 2;
					else if (rtt1 > 0)
						newRtt = rtt1;
					else if (rtt2 > 0)
						newRtt = rtt2;
					monitorA.setRtt(monitorB, newRtt);
					monitorB.setRtt(monitorA, newRtt);
				}
			}
		}
	}

	/**
	 * 
	 * @param p1
	 *            host 1
	 * @param p2
	 *            host 2
	 * @return distance in km
	 */
	private static double getGeographicalDistance(Host p1, Host p2) {
		return HostMap.getGeographicalDistance(p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
	}

	/**
	 * 
	 * @param latitude1
	 *            host 1
	 * @param longitude1
	 *            host 1
	 * @param latitude2
	 *            host 2
	 * @param longitude2
	 *            host 2
	 * @return distance in km
	 */
	private static double getGeographicalDistance(double latitude1, double longitude1, double latitude2, double longitude2) {
		GeographicPosition pos1 = new GeographicPosition(longitude1, latitude1);
		GeographicPosition pos2 = new GeographicPosition(longitude2, latitude2);
		return pos1.distanceTo(pos2);
	}

	/**
	 * unlocatable on the globe (unkonw ip, proxy, satelite)
	 * 
	 * @return number of unlocatable hosts
	 */
	public int getNoOfUnlocatableHosts() {
		int unlocatedPeers = 0;
		int unlocatedMonitors = 0;
		Set<Integer> ips = this.hostIndex.keySet();
		for (Integer ip : ips) {
			unlocatedPeers += (hostIndex.get(ip).isLocatable()) ? 0 : 1;
			unlocatedMonitors += (hostIndex.get(ip).isLocatable() || hostIndex.get(ip).getHostType() == Host.HOST) ? 0 : 1;
		}
		return unlocatedPeers - unlocatedMonitors;
	}

	/**
	 * 
	 * @param monitor
	 */
	public void removeMonitor(Host monitor) {
		this.monitorIndex.remove(monitor.getIpAddress());
		this.hostIndex.remove(monitor.getIpAddress());
		for (Host host : hostIndex.values()) {
			host.removeRTT(monitor);
		}
		this.builtQuickLookup();
	}

	/**
	 * 
	 * Removes the given set of hosts from the map.
	 * 
	 * @param hosts
	 *            set of hosts
	 */
	public void removeHosts(Set<Host> hosts) {
		for (Host host : hosts) {
			// if (host.getHostType() == Host.HOST) {
			if (this.getGnpRef() != null)
				this.getGnpRef().removePosition(host.getIpAddress());
			this.hostIndex.remove(host.getIpAddress());
			// }
		}
		for (Set<Host> group : groups.values())
			group.removeAll(hosts);
		countryLookup.keepCountries(getLocations().keySet());
		this.builtQuickLookup();
	}

	/**
	 * SEEMS TO remove all hosts that were pinged by exactly noOfConnections
	 * distinct monitors.
	 * 
	 * removes all hosts that have noOfConnections measured RTTs
	 * 
	 * @param noOfConnections
	 */
	public void removeHosts(int noOfConnections) {
		Set<Host> hosts = new HashSet<Host>();
		for (Host host : hostIndex.values()) {
			if (host.getMeasuredMonitors().size() == noOfConnections)
				hosts.add(host);
		}
		removeHosts(hosts);
	}

	/**
	 * remove all hosts that have at least one measured RTT with a error bigger
	 * than the argument. Removing that errors will improve the quality of the
	 * Gnp Space.
	 * 
	 * @param error
	 *            relative error
	 */
	public void removeHostsWithMaximumRelativeError(double error) {
		HashSet<Host> delete = new HashSet<Host>();
		for (Host host : hostIndex.values()) {
			for (int c = 0; c < gnpRef.getNumberOfMonitors(); c++) {
				double relError = Math.abs(host.getGnpPositionReference().getDirectionalRelativError(gnpRef.getMonitorPosition(c)));
				if (relError >= error) {
					delete.add(host);
					break;
				}
			}
		}
		removeHosts(delete);
	}

	/**
	 * SEEMS TO only keep the 'noOfMonitorsToKeep' monitor hosts in the GNP space that have the 
	 * maximum distance between each other, the other ones are being deleted.
	 * 
	 * keeps the maximum separated monitors
	 * 
	 * @param noOfMonitorsToKeep
	 */
	public void removeMonitorsKeepMaximumSparation(int noOfMonitorsToKeep) {
		ArrayList<Host> maxSeperatedPeers = getMaximumSeparatedMonitors(noOfMonitorsToKeep);
		ArrayList<Host> deleteMonitors = new ArrayList<Host>();
		for (Host monitor : monitorIndex.values()) {
			if (!maxSeperatedPeers.contains(monitor))
				deleteMonitors.add(monitor);
		}
		for (Host monitor : deleteMonitors) {
			this.removeMonitor(monitor);
		}
	}

	/**
	 * 
	 * 
	 * @param noOfMonitors
	 * @return maximum separated monitors
	 */
	private ArrayList<Host> getMaximumSeparatedMonitors(int noOfMonitors) {
		ArrayList<ArrayList<Host>> allCombinations = getMonitorCombinations(noOfMonitors);
		int posWithMax = 0;
		double valueMax = 0.0;
		for (int c = 0; c < allCombinations.size(); c++) {
			double currentDistance = getInterMonitorDistance(allCombinations.get(c));
			if (currentDistance > valueMax) {
				valueMax = currentDistance;
				posWithMax = c;
			}
		}
		return allCombinations.get(posWithMax);
	}

	/**
	 * 
	 * @param monitors
	 * @return sum measured RTTs between monitors
	 */
	private double getInterMonitorDistance(ArrayList<Host> monitors) {
		double result = 0.0;
		for (int c = 0; c < monitors.size() - 1; c++)
			for (int d = c + 1; d < monitors.size(); d++)
				result += monitors.get(c).getRtt(monitors.get(d));
		return result;

	}

	/**
	 * 
	 * @param size
	 * @return all combinations of monitors with size "size"
	 */
	private ArrayList<ArrayList<Host>> getMonitorCombinations(int size) {
		ArrayList<Host> monitors = new ArrayList<Host>();
		monitors.addAll(monitorIndex.values());
		Collections.sort(monitors, new PeerComparatorNoOfConnections());
		return builtRecursive(new ArrayList<Host>(), size, monitors, 0);
	}

	/**
	 * recursive built of all combinations of monitors with size "size"
	 * 
	 * @param current
	 * @param max
	 * @param monitors
	 * @param posInMonitors
	 * @return array of combinations
	 */
	private ArrayList<ArrayList<Host>> builtRecursive(ArrayList<Host> current, int max, ArrayList<Host> monitors, int posInMonitors) {
		ArrayList<ArrayList<Host>> result = new ArrayList<ArrayList<Host>>();
		if (current.size() == max) {
			result.add(current);
			return result;
		} else {
			for (int c = posInMonitors; c < monitors.size() - (max - current.size()); c++) {
				ArrayList<Host> copy = (ArrayList<Host>) current.clone();
				copy.add(monitors.get(c));
				result.addAll(builtRecursive(copy, max, monitors, c + 1));
			}
			return result;
		}
	}

	/**
	 * generate two files with distance - rtt pairs, that can be used with
	 * gnuplot
	 * 
	 * filename.txt1 plots each measured host - monitor RTT filename.txt2
	 * aggregates distances within a range and calculates the average RTT
	 * 
	 * @param filename
	 * @param steps
	 *            number of divisions within the 0 - 20000km for aggregated plot
	 */
	public void distanceVsRttPlot(String filename, Host monitor, int steps) {
		try {
			double[] test1 = new double[steps];
			int[] test2 = new int[steps];

			FileWriter all = new FileWriter(filename + ".txt1");
			for (Host host : hostIndex.values()) {
				for (Host mon : host.getMeasuredMonitors()) {
					double distance = getGeographicalDistance(host, mon);
					double rtt = host.getRtt(mon);
					all.write(distance + " " + rtt + "\n");

					int pos = (int) Math.floor((distance / 20000.0) * steps);
					test1[pos] += rtt;
					test2[pos]++;
				}
			}
			all.close();
			all = new FileWriter(filename + ".txt2");
			for (int c = 0; c < steps; c++) {
				double x = (c * (20000.0 / steps)) + (20000.0 / (2 * steps));
				double y = test1[c] / test2[c];
				all.write(x + " " + y + "\n");
			}
			all.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * saves host postion location, GNP position, groups, GNP Space, PingER
	 * Lookup table in an xml file used within the simulation
	 * 
	 * @param file
	 */
	public void exportToXml(File file) {
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
			XMLWriter writer = new XMLWriter(out, format);
			writer.write(getDocument());
			writer.close();
		} catch (IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * 
	 * @return xml Document Object of relevant Class Attributes
	 */
	private Document getDocument() {
		Set<Host> hosts = new HashSet<Host>();

		// "GroupLookup" Element
		DefaultElement groups = new DefaultElement("GroupLookup");
		for (String group : this.groups.keySet()) {
			hosts.addAll(this.groups.get(group));
			DefaultElement peerXml = new DefaultElement("Group");
			peerXml.addAttribute("id", group);
			peerXml.addAttribute("maxsize", String.valueOf(this.groups.get(group).size()));
			String ip = "";
			int x = 0;
			int blockSize = 1000;
			// IP Block of 1000, too long blocks leads to hangUp ??
			for (Host host : this.groups.get(group)) {
				x++;
				ip += "," + host.getIpAddress();
				if (x % blockSize == 0) {
					ip = ip.substring(1);
					DefaultElement ips = new DefaultElement("IPs");
					ips.addAttribute("value", ip);
					peerXml.add(ips);
					ip = "";
				}
			}
			if (ip.length() > 0) {
				ip = ip.substring(1);
				DefaultElement ips = new DefaultElement("IPs");
				ips.addAttribute("value", ip);
				peerXml.add(ips);
			}
			groups.add(peerXml);
		}

		// "Hosts" Element
		DefaultElement peers = new DefaultElement("Hosts");
		for (Host host : hosts) {
			DefaultElement peer = new DefaultElement("Host");
			peer.addAttribute("ip", String.valueOf(host.getIpAddress()));

			String area = (host.getArea() != null) ? host.getArea() : "--";
			peer.addAttribute("continentalArea", area);

			String countryCode = (host.getCountryCode() != null) ? host.getCountryCode() : "--";
			peer.addAttribute("countryCode", countryCode);

			String region = (host.getRegion() != null) ? host.getRegion() : "--";
			peer.addAttribute("region", region);

			String city = (host.getCity() != null) ? host.getCity() : "--";
			peer.addAttribute("city", city);

			String isp = (host.getISP() != null) ? host.getISP() : "--";
			peer.addAttribute("isp", isp);

			peer.addAttribute("longitude", String.valueOf(host.getLongitude()));
			peer.addAttribute("latitude", String.valueOf(host.getLatitude()));
			String coordinates = (host.getGnpPositionReference() != null) ? host.getGnpPositionReference().getCoordinateString() : "0";
			peer.addAttribute("coordinates", coordinates);
			peers.add(peer);
		}

		// "PingErLookup" Elements
		Element pingEr = pingErLookup.exportToXML();

		// "CountryLookup" Element
		Element country = countryLookup.exportToXML();

		DefaultDocument document = new DefaultDocument(new DefaultElement("gnp"));
		document.getRootElement().add(groups);
		document.getRootElement().add(peers);
		document.getRootElement().add(pingEr);
		document.getRootElement().add(country);
		return document;

	}
	
	public void insertIntoRelationalDB(NetMeasurementDB db) {
		
		Map<Host, NetMeasurementDB.Host> hostsScanned = new HashMap<Host, NetMeasurementDB.Host>();
		
		for (String group : this.groups.keySet()) {
			
			Set<Host> hosts = this.groups.get(group);
			
			List<NetMeasurementDB.Host> dbHosts = new ArrayList<NetMeasurementDB.Host>(hosts.size());
			
			for (Host h : hosts) {
				
				NetMeasurementDB.Host host = hostsScanned.get(h);
				
				if (host == null) {
					
					String countryStr = h.getCountryName();
					String pingErRegionStr = countryLookup.getPingErRegionName(countryStr);
					assert pingErRegionStr != null : "No PingEr region found for country " + countryStr;
					City city = getCity(db, h.getCity(), h.getRegion(), countryStr, h.getCountryCode(), h.getArea(), pingErRegionStr);
					
					GnpPosition pos = h.getGnpPositionReference();
					List<Double> gnpCoords;
					if (pos != null) {
						int dims = pos.getNoOfDimensions();
						gnpCoords = new ArrayList<Double>(dims);
						for (int i= 0; i<dims; i++) gnpCoords.add(pos.getGnpCoordinates(i));
					} else {
						gnpCoords = Collections.emptyList();
					}
					host = db.new Host(h.getIpAddress(), city, h.getLatitude(), h.getLongitude(), gnpCoords);
					hostsScanned.put(h, host);
				}
			
				dbHosts.add(host);
				
			}
			
			db.new Group(group, dbHosts);
			
		}

		pingErLookup.insertIntoRelationalDB(db);
	}
	
	public static City getCity(NetMeasurementDB db, String cityStr, String regionStr, String countryStr, String countryCode, String continentStr, String pingErRgName) {
		//the host never occured in a group before. Putting it into the DB.
		//FIXME: some cities or regions occur multiple times but in different regions/countries, we will have to separate them! Infers problem of DB structure.
		City city = db.getStringAddrObjFromStr(City.class, cityStr);
		if (city == null) {
			Region region = db.getStringAddrObjFromStr(Region.class, regionStr);
			if (region == null) {
				Country country = db.getStringAddrObjFromStr(Country.class, countryStr);
				if (country == null) {
					Continent continent = db.getStringAddrObjFromStr(Continent.class, continentStr);
					if (continent == null) continent = db.new Continent(continentStr);
					
					PingErRegion pErRegion = db.getStringAddrObjFromStr(PingErRegion.class, pingErRgName);
					if (pErRegion == null) pErRegion = db.new PingErRegion(pingErRgName);
					country = db.new Country(countryStr, continent, pErRegion, countryCode);
				}
				region = db.new Region(regionStr, country);
			}
			city = db.new City(cityStr, region);
		}
		return city;
	}

	/**
	 * Comparator implementation for sorting of Host with the geographical
	 * distance to a given position
	 */
	private class PeerComparatorDistance implements Comparator<Host> {

		double latitude;

		double longitude;

		public void setPosition(double latitude, double longitude) {
			this.latitude = latitude;
			this.longitude = longitude;
		}

		public int compare(Host peer1, Host peer2) {
			double distance1 = HostMap.getGeographicalDistance(peer1.getLatitude(), peer1.getLongitude(), latitude, longitude);
			double distance2 = HostMap.getGeographicalDistance(peer2.getLatitude(), peer2.getLongitude(), latitude, longitude);
			if (distance1 < distance2)
				return -1;
			else if (distance1 > distance2)
				return 1;
			else
				return 0;
		}
	}

	/**
	 * Comparator implementation for sorting of Host with the number of measured
	 * RTTs per Host.
	 */
	private class PeerComparatorNoOfConnections implements Comparator<Host> {
		public int compare(Host peer1, Host peer2) {
			int coonections1 = peer1.getMeasuredMonitors().size();
			int coonections2 = peer2.getMeasuredMonitors().size();
			if (coonections1 < coonections2)
				return 1;
			else if (coonections1 > coonections2)
				return -1;
			else
				return 0;
		}
	}
}
