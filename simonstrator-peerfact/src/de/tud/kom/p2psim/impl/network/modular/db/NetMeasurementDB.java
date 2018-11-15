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


package de.tud.kom.p2psim.impl.network.modular.db;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.tud.kom.p2psim.impl.network.IPv4NetID;
import de.tud.kom.p2psim.impl.network.modular.common.PingErToolkit;
import de.tud.kom.p2psim.impl.util.BackToXMLWritable;
import de.tud.kom.p2psim.impl.util.SymmetricRelation;
import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.db.relational.DBInstantiationException;
import de.tud.kom.p2psim.impl.util.db.relational.RelationalDB;
import umontreal.iro.lecuyer.probdist.LognormalDist;

/**
 * Database format for measurements of network characteristics for the Modular Network Layer.
 * @author Leo Nobach
 *
 */
public class NetMeasurementDB extends RelationalDB implements BackToXMLWritable {
	
	boolean loadedFromFile = false;
	
	public boolean wasLoadedFromFile() {
		return loadedFromFile;
	}
	
	public class Continent extends StringAddressableDBIDObject {
		
		public Continent(String name) {
			super(name);
		}
		
		public Continent(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
		}
		
		public String toString() {
			return this.getName();
		}
		
	}
	
	/**
	 * A geographical region of hosts, defined by PingEr
	 *
	 */
	public class PingErRegion extends StringAddressableDBIDObject {
		
		public PingErRegion(String name) {
			super(name);
		}
		
		public PingErRegion(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
		}
		
		public String toString() {
			return this.getName();
		}
		
	}
	
	public class Country extends StringAddressableDBIDObject {
		
		public Continent getContinent() {
			return continent;
		}

		public void setContinent(Continent continent) {
			this.continent = continent;
		}

		private Continent continent;
		private PingErRegion pingErRegion;
		private String cc;
		
		public PingErRegion getPingErRegion() {
			return pingErRegion;
		}

		public void setPingErRegion(PingErRegion pingErRegion) {
			this.pingErRegion = pingErRegion;
		}

		public Country(String name, Continent continent, PingErRegion pingErRegion, String countryCode) {
			super(name);
			this.continent = continent;
			this.pingErRegion = pingErRegion;
			this.cc = countryCode;
		}
		
		public Country(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			this.continent = inst.getDBObj("continent", Continent.class);
			this.pingErRegion = inst.getDBObj("pingerregion", PingErRegion.class);
			this.cc = inst.getString("cc");
		}
		
		public String getCC() {
			return cc;
		}
		
		public String toString() {
			return this.getName();
		}
	}
	
	public class Region extends StringAddressableDBIDObject {
		
		public Country getCountry() {
			return country;
		}

		public void setCountry(Country country) {
			this.country = country;
		}

		private Country country;
		
		public Region(String name, Country country) {
			super(name);
			this.country = country;
		}
		
		public Region(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			this.country = inst.getDBObj("country", Country.class);
		}
		
		public String toString() {
			return this.getName();
		}
		
	}
	
	public class City extends StringAddressableDBIDObject {
		
		public Region getRegion() {
			return region;
		}

		public void setRegion(Region region) {
			this.region = region;
		}

		private Region region;
		
		public City(String name, Region region) {
			super(name);
			this.region = region;
		}
		
		public City(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			this.region = inst.getDBObj("region", Region.class);
		}
		
		public String toString() {
			return this.getName();
		}
		
	}
	
	/**
	 * Measurement information for a host
	 *
	 */
	public class Host extends DBObject {
		
		City city;
		double longitude;
		double latitude;
		List<Double> coordinates;
		
		public Host(int ip, City city, double longitude, double latitude, List<Double> coordinates) {
			super(ip);
			this.city = city;
			this.longitude = longitude;
			this.latitude = latitude;
			this.coordinates = coordinates;
		}
		
		public Host(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			this.city = inst.getDBObj("city", City.class);
			this.longitude = inst.getDouble("longitude");
			this.latitude = inst.getDouble("latitude");
			this.coordinates = inst.getList("coordinates", Double.class);
		}
		
		public City getCity() {
			return city;
		}

		public double getLongitude() {
			return longitude;
		}

		public double getLatitude() {
			return latitude;
		}

		
		public List<Double> getCoordinates() {		//TODO: Refactoring in getGNPCoordinates
			return coordinates;
		}
		
		public String toString() {
			return "Host(" + IPv4NetID.intToIP(this.getId()) + ")";
		}
		
	}
	
	/**
	 * A group of hosts.
	 *
	 */
	public class Group extends StringAddressableDBIDObject {

		private List<Host> members;
		private transient int nextMember = 0;

		public Group(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			members = inst.getList("members", Host.class);
		}
		
		public Group(String name, List<Host> members) {
			super(name);
			this.members = members;
		}
		
		public List<Host> getMembers() {
			return members;
		}
		
		public String toString() {
			return this.getName();
		}
		
		public Host tGetNextMember() {
			if (members.size() == 0) throw new IllegalStateException("The group named " + this.getName() + " exists, but is empty. Please change the group name or refresh data, or set 'useRegionGroups=\"false\" in the ModularNetLayerFactory'");
			if (nextMember >= members.size()) throw new IllegalStateException("You can not create another host. Already created " + members.size() + " hosts in group " + this.getName());
			Host result = members.get(nextMember);
			nextMember++;
			return result;
		}
		
	}
	
	Map<Tuple<Object, Object>, SummaryRelation> relations = new HashMap<Tuple<Object, Object>, SummaryRelation>();

	private String filename;

	public GlobalSummaryRelation globRel = null;
	
	public SummaryRelation getSummaryRelFrom(Object from, Object to) {
		return relations.get(new SymmetricRelation<Object, Object>(from, to));
	}
	
	/**
	 * A relation from a set of hosts to another set of hosts. Summary relations are symmetric.
	 * Contains measurement data applicable for this relation of hosts
	 * 
	 */
	public abstract class SummaryRelation extends DBObject {
		
		public double getMinRtt() {
			return minRtt;
		}

		public double getAvgRtt() {
			return avgRtt;
		}

		public double getdVar() {
			return dVar;
		}

		/**
		 * Returns the packet loss in PERCENT!
		 * @return
		 */
		public double getpLoss() {
			return pLoss;
		}
		
		public LognormalDist tGetJitterLnDist() {
			if (jitterLnDist == null) {
				jitterLnDist = PingErToolkit.getNewLnDistFrom(avgRtt, minRtt, dVar);
			}
			return jitterLnDist;
		}

		double minRtt;
		double avgRtt;
		double dVar;
		double pLoss;
		transient LognormalDist jitterLnDist = null;
		
		public SummaryRelation(IDBObjInstantiator inst) throws DBInstantiationException {
			super();
			this.minRtt = inst.getDouble("minrtt");
			this.avgRtt = inst.getDouble("avgrtt");
			this.dVar = inst.getDouble("dvar");
			this.pLoss = inst.getDouble("ploss");
		}

		public SummaryRelation(double minRtt, double avgRtt, double dVar,
				double pLoss) {
			super();
			this.minRtt = minRtt;
			this.avgRtt = avgRtt;
			this.dVar = dVar;
			this.pLoss = pLoss;
		}
		
	}
	
	/**
	 * A relation from all hosts in a country to all hosts in another country
	 */
	public class CountryCountrySummaryRelation extends SummaryRelation {

		private Country cntrA;
		private Country cntrB;

		public CountryCountrySummaryRelation(IDBObjInstantiator inst)
				throws DBInstantiationException {
			super(inst);
			this.cntrA = inst.getDBObj("cntra", Country.class);
			this.cntrB = inst.getDBObj("cntrb", Country.class);
			put();
		}

		public CountryCountrySummaryRelation(Country cntrA, Country cntrB, double minRtt, double avgRtt,
				double dVar, double pLoss) {
			super(minRtt, avgRtt, dVar, pLoss);
			this.cntrA = cntrA;
			this.cntrB = cntrB;
			put();
		}
		
		private void put() {
			relations.put(new Tuple<Object, Object>(cntrA, cntrB), this);
		}

		public Country getCntrA() {
			return cntrA;
		}

		public Country getCntrB() {
			return cntrB;
		}
		
		public String toString() {
			return "CC:" + cntrA.getName() + " <=> " + cntrB.getName();
		}
		
	}
	
	/**
	 * A relation from all hosts in a country to all hosts in a region
	 */
	public class CountryRegionSummaryRelation extends SummaryRelation {

		private Country cntrA;
		private PingErRegion regB;

		public CountryRegionSummaryRelation(IDBObjInstantiator inst)
				throws DBInstantiationException {
			super(inst);
			this.cntrA = inst.getDBObj("cntra", Country.class);
			this.regB = inst.getDBObj("regb", PingErRegion.class);
			put();
		}

		public CountryRegionSummaryRelation(Country cntrA, PingErRegion regB, double minRtt, double avgRtt,
				double dVar, double pLoss) {
			super(minRtt, avgRtt, dVar, pLoss);
			this.cntrA = cntrA;
			this.regB = regB;
			put();
		}
		
		private void put() {
			relations.put(new Tuple<Object, Object>(cntrA, regB), this);
		}

		public Country getCntrA() {
			return cntrA;
		}

		public PingErRegion getRegB() {
			return regB;
		}
		
		public String toString() {
			return "CR:" + cntrA.getName() + " <=> " + regB.getName();
		}
		
	}
	
	/**
	 * A relation from all hosts in a region to all hosts in a country
	 */
	public class RegionCountrySummaryRelation extends SummaryRelation {

		private PingErRegion regA;
		private Country cntrB;

		public RegionCountrySummaryRelation(IDBObjInstantiator inst)
				throws DBInstantiationException {
			super(inst);
			this.regA = inst.getDBObj("rega", PingErRegion.class);
			this.cntrB = inst.getDBObj("cntrb", Country.class);
			put();
		}

		public RegionCountrySummaryRelation(PingErRegion regA, Country cntrB, double minRtt, double avgRtt,
				double dVar, double pLoss) {
			super(minRtt, avgRtt, dVar, pLoss);
			this.regA = regA;
			this.cntrB = cntrB;
			put();
		}
		
		private void put() {
			relations.put(new Tuple<Object, Object>(regA, cntrB), this);
		}

		public PingErRegion getRegA() {
			return regA;
		}

		public Country getCntrB() {
			return cntrB;
		}
		
		public String toString() {
			return "RC:" + regA.getName() + " <=> " + cntrB.getName();
		}
		
	}
	
	/**
	 * A relation from all hosts in a region to all hosts in another region
	 */
	public class RegionRegionSummaryRelation extends SummaryRelation {

		private PingErRegion regA;
		private PingErRegion regB;

		public RegionRegionSummaryRelation(IDBObjInstantiator inst)
				throws DBInstantiationException {
			super(inst);
			this.regA = inst.getDBObj("rega", PingErRegion.class);
			this.regB = inst.getDBObj("regb", PingErRegion.class);
			put();
		}

		public RegionRegionSummaryRelation(PingErRegion regA, PingErRegion regB, double minRtt, double avgRtt,
				double dVar, double pLoss) {
			super(minRtt, avgRtt, dVar, pLoss);
			this.regA = regA;
			this.regB = regB;
			put();
		}
		
		private void put() {
			relations.put(new Tuple<Object, Object>(regA, regB), this);
		}

		public PingErRegion getRegA() {
			return regA;
		}

		public PingErRegion getRegB() {
			return regB;
		}
		
		public String toString() {
			return "RR:" + regA.getName() + " <=> " + regB.getName();
		}
		
	}
	
	/**
	 * To use if no summary relation could be found for the host.
	 * @author 
	 *
	 */
	public class GlobalSummaryRelation extends SummaryRelation {

		public GlobalSummaryRelation(double minRtt, double avgRtt, double dVar,
				double pLoss) {
			super(minRtt, avgRtt, dVar, pLoss);
			NetMeasurementDB.this.globRel = this;
		}
		
		public GlobalSummaryRelation(IDBObjInstantiator inst)
		throws DBInstantiationException {
			super(inst);
			NetMeasurementDB.this.globRel = this;
		}
		
	}
	
	
	@Override
	public List<Class<? extends DBObject>> getDependencySortedSerializationOrder() {
		List<Class<? extends DBObject>> result = new ArrayList<Class<? extends DBObject>>(4);
		
		result.add(Continent.class);
		result.add(PingErRegion.class);
		result.add(Country.class);
		result.add(Region.class);
		result.add(City.class);
		result.add(Host.class);
		result.add(Group.class);
		result.add(CountryCountrySummaryRelation.class);
		result.add(CountryRegionSummaryRelation.class);
		result.add(RegionCountrySummaryRelation.class);
		result.add(RegionRegionSummaryRelation.class);
		result.add(GlobalSummaryRelation.class);
		
		return result;
	}
	
	/**
	 * To be called by the PeerfactSim.KOM configurator when giving the File parameter.
	 * @param filename
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public void setFile(String filename) throws SAXException, IOException, ParserConfigurationException {
		loadedFromFile = true;

        InputStream input = null;

        if (!new File(filename).exists()) {
            if (new File(filename + ".zip").exists()) {
                input = new BufferedInputStream(new ZipInputStream(new FileInputStream(filename + ".zip")));
            }
        } else {
            input = new BufferedInputStream(new FileInputStream(filename));
        }

		this.readFromXMLFile(input);
		this.filename = filename;
	}

	public SummaryRelation getMostAccurateSummaryRelation(Host from,
			Host to) {
		Country host1Country = from.getCity().getRegion().getCountry();
		Country host2Country = to.getCity().getRegion().getCountry();
		
		SummaryRelation sumRel = getSummaryRelFrom(host1Country, host2Country);
		if (sumRel == null) sumRel = getSummaryRelFrom(host1Country.getPingErRegion(), host2Country);
		if (sumRel == null) sumRel = getSummaryRelFrom(host1Country, host2Country.getPingErRegion());
		if (sumRel == null) sumRel = getSummaryRelFrom(host1Country.getPingErRegion(), host2Country);
		if (sumRel == null) sumRel = getSummaryRelFrom(host1Country.getPingErRegion(), host2Country.getPingErRegion());
		if (sumRel == null) return globRel;
		
		return sumRel;
	}

	@Override
	public void writeBackToXML(BackWriter bw) {
		bw.writeSimpleType("file", filename);
	}

}


