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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.gnp.geoip.IspLookupService;
import de.tud.kom.p2psim.impl.util.measurement.AvgAccumulator;

/**
 * Host objects holds and manage information about their location on earth and
 * gnp space and about measured rtts to monitors
 * 
 * @author Gerald Klunker
 * @version 0.1, 09.01.2008
 * 
 */
public class Host implements Serializable {

	private static final long serialVersionUID = 5135831087205329917L;

	/**
	 * Set to "true" to allow to ask for country names (leo@relevantmusic.de). Setting to "false" saves memory.
	 */
	private static boolean useCountryNames = false;

	public static int MONITOR = 1;

	public static int HOST = 2;

	private int ipAddress = 0;

	private int type;

	private String countryCode;

	private String region;

	private String city;

	private String isp;

	private String area;

	private boolean locatable = false;

	private double latitude = Double.NaN;

	private double longitude = Double.NaN;

	private HashMap<Host, List<Double>> rttToMonitors = new HashMap<Host, List<Double>>();
	private HashMap<Host, Double> cumulatedRTTs = null;
 
	private HostMap mapRef;

	public GnpPosition coordinateRef;

	/**
	 * Stays null unless USE_COUNTRY_NAMES is set to "true". Setting it to false saves memory
	 */
	private String countryName = null;
	
	public static void setUseCountryNames(boolean useThem) {
		useCountryNames = useThem;
	}
	
	public static boolean issetUseCountryNames() {
		return useCountryNames;
	}

	/**
	 * Returns the country name. Will throw an exception unless setUseCountryNames was set to true while the node was constructed.
	 * @return
	 */
	public String getCountryName() {
		if (!useCountryNames) throw new IllegalStateException("Country names are not set. Please call the static method 'Host.setUseCountryNames(true)' " +
				"before any Host is being constructed. Not calling it saves memory.");
		return countryName;
	}

	/**
	 * 
	 * @param ipAddress
	 *            of the host
	 * @param mapRef
	 *            reference to the related HostMap Object
	 */
	public Host(int ipAddress, HostMap mapRef) {
		super();
		this.ipAddress = ipAddress;
		this.mapRef = mapRef;
	}

	/**
	 * adds a measured rtt to monitor. if there is still a rtt, it will be
	 * replaced, if the new one is smaller
	 * 
	 * @param host
	 * @param rtt
	 *            measured rtt
	 */
	public void addRtt(Host monitor, double rtt) {
		if (cumulatedRTTs != null) throw new IllegalStateException("RTTs were already cumulated.");
		List<Double> rtts = rttToMonitors.get(monitor);
		if (rtts == null) {
			rtts = new ArrayList<Double>(10);
			rttToMonitors.put(monitor, rtts);
		}
		rtts.add(rtt);
	}

	protected void cumulateRTTsCond() {
		if (cumulatedRTTs != null) return; //already cumulated
		cumulatedRTTs = new HashMap<Host, Double>();
		for (Entry<Host, List<Double>> e : rttToMonitors.entrySet()) {
			List<Double> rtts = e.getValue();
			avgPingAttemptsPerNode.addToTotal(rtts.size());
			double cumulationResult = currentStrategy.cumulate(rtts);
			cumulatedRTTs.put(e.getKey(), cumulationResult);
		}
	}
	
	/**
	 * set a measured rtt to monitor.
	 * 
	 * @param host
	 * @param rtt
	 *            measured rtt
	 */
	public void setRtt(Host monitor, double rtt) {
		cumulateRTTsCond();
		cumulatedRTTs.put(monitor, rtt);
	}

	/**
	 * 
	 * @param monitor
	 * @return measured rtt from host to monitor
	 */
	public double getRtt(Host monitor) {
		cumulateRTTsCond();
		Double rtt = cumulatedRTTs.get(monitor);
		return (rtt == null)?Double.NaN:rtt;
	}

	/**
	 * remove them measured rtt from host to monitor
	 * 
	 * @param monitor
	 */
	protected void removeRTT(Host monitor) {
		rttToMonitors.remove(monitor);
	}

	/**
	 * 
	 * @return set of monitors with a measured rtt to host
	 */
	protected Set<Host> getMeasuredMonitors() {
		return rttToMonitors.keySet();
	}

	/**
	 * 
	 * @return latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * 
	 * @return longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * 
	 * @return longitude
	 */
	public String getArea() {
		return area;
	}

	/**
	 * 
	 * @return 2-digits country code
	 */
	public String getCountryCode() {
		return countryCode;
	}

	/**
	 * @return region name
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * @return city name
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @return isp name
	 */
	public String getISP() {
		return isp;
	}

	/**
	 * 
	 * @return ip-address as 32bit value
	 */
	public int getIpAddress() {
		return ipAddress;
	}

	/**
	 * 
	 * @param peerType
	 *            (1:monitor, 2:host)
	 */
	protected void setHostType(int peerType) {
		this.type = peerType;
	}

	/**
	 * 
	 * @return type of host (1:monitor, 2:host)
	 */
	public int getHostType() {
		return type;
	}

	/**
	 * 
	 * @param coordinateRef
	 *            reference to the related gnp position
	 */
	protected void setPositionReference(GnpPosition coordinateRef) {
		this.coordinateRef = coordinateRef;
	}

	/**
	 * 
	 * @return reference to the related gnp position, null, if no position was
	 *         calculated
	 */
	protected GnpPosition getGnpPositionReference() {
		return coordinateRef;
	}

	static int isNotLocatable = 0;
	static int ccNotFound = 0;
	static int cityNotFound = 0;
	static int regionNotFound = 0;
	
	public static void printLocatorStats() {
		System.out.println("Locator stats: Hosts not locatable: " + isNotLocatable + ", city not found: " + cityNotFound + ", region not found: " + regionNotFound);
	}
	
	/**
	 * Try to locate the host on earth using the geolocator
	 * 
	 * @param locator
	 */
	public void setLocation(Geolocator locator, IspLookupService service) {
		this.locatable = false;
		
		
		boolean ipFound = locator.search(ipAddress);
		boolean ccFound = locator.getCountryCode() != null;
		boolean cityFound = locator.getCityName() != null;
		boolean regionFound = locator.getRegionName() != null;
		
//		if (locator.search(ipAddress) && locator.getCountryCode() != null && locator.getCityName() != null && locator.getRegionName() != null && service.getISP(ipAddress) != null) {
		if (ipFound && ccFound && cityFound && regionFound) {
			this.countryCode = locator.getCountryCode();
			if (useCountryNames) this.countryName = locator.getCountryName();
			this.region = locator.getRegionName();
			this.latitude = locator.getLatitude();
			this.longitude = locator.getLongitude();
			this.city = locator.getCityName();
			if (service != null)
				this.isp = service.getISP(ipAddress);
			this.mapRef.getCountryLookup().addCountryFromGeoIP(countryCode, locator.getCountryName());
			this.area = this.mapRef.getCountryLookup().getPingErRegionName(countryCode);
			if (this.area != null)
				this.locatable = true;
		} else {
			this.latitude = Double.NaN;
			this.longitude = Double.NaN;
		}
		if (!this.locatable) {
			if (!ipFound) isNotLocatable++;
			if (!ccFound) ccNotFound++;
			if (!cityFound) cityNotFound++;
			if (!regionFound) regionNotFound++;
		}
	}

	/**
	 * 
	 * @return true, if host was located on a longitude/latitude
	 */
	public boolean isLocatable() {
		return locatable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String returnString = "";
		returnString += IPv4NetID.intToIP(ipAddress);
		returnString += "\t(" + this.getCountryCode() + ")";
		return returnString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ipAddress;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Host other = (Host) obj;
		if (ipAddress != other.ipAddress)
			return false;
		return true;
	}
	
	protected static CumulateRTTStrategy currentStrategy = new RTTCumulationStrategies.Minimum();
	
	static AvgAccumulator avgPingAttemptsPerNode = new AvgAccumulator();
	
	public interface CumulateRTTStrategy {
		
		/**
		 * Cumulates the given rtts from multiple ping attempts.
		 * @param rtts
		 * @return Double.NaN, if rtts is empty or not cumulatable, else the cumulated value
		 */
		public Double cumulate(List<Double> rtts);
		
	}

	public static void setRTTCumulationStrategy(
			CumulateRTTStrategy s) {
		currentStrategy = s;
	}
	
	public static double getAvgPingAttemptsPerNode() {
		return avgPingAttemptsPerNode.getAverage();
	}

}
