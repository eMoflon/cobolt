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

package de.tud.kom.p2psim.impl.util.geo.maps.osm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.util.geo.maps.Node;
import de.tud.kom.p2psim.api.util.geo.maps.Way;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

public class OSMReader extends DefaultHandler {
	private static HashMap<String, OSMMap> loadedMaps = new HashMap<String, OSMMap>();
	
	private OSMMap map;
	private STATE state = STATE.INITIAL;
	private Vector<Long> idList = new Vector<Long>();
	private String name;
	private boolean boundsFound = false;
	private boolean firstNode = true;
	private HashMap<String, String> options = new HashMap<String, String>();
	private Long nodeId;
	
	public OSMReader() {
		map = new OSMMap();
	}
	
	public OSMReader(OSMMap osmMap) {
		this.map = osmMap;
	}

	public OSMMap getMap() {
		return map;
	}

	public static void loadMap(String filename, OSMMap map) {
		File osmFile = new File(filename);
		
		if (!osmFile.exists())
			throw new ConfigurationException("Couldn't find OSM file: " + filename);
		
		try {
	    	
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		    SAXParser parser;
			parser = parserFactory.newSAXParser();

		    OSMReader reader = new OSMReader(map);
		    
		    File mapFile = new File(filename);
		    InputStream mapIn = new FileInputStream(mapFile);
		    
		    if (filename.endsWith(".bz2")) {
		    	mapIn = new BZip2CompressorInputStream(mapIn);
		    }
		    
			parser.parse(mapIn, reader);
			
			reader.postProcessing();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void postProcessing() {
		removeOutOfBoundNodes(map);
	}
	
	public void removeOutOfBoundNodes(OSMMap map) {			
		Iterator<Way> iter = map.getWays().iterator();

		int size = map.getWays().size();
		
		OSMWay way;
		while (iter.hasNext()) {
			way = (OSMWay)iter.next();
			
			for (Node node : way.getNodes()) {
				if (node == null || !checkBounds(map, node.getPosition())) {
					iter.remove();
					break;
				}
			}
		}

		Monitor.log(OSMReader.class, Level.INFO, "Removed "
				+ (size - map.getWays().size()) + " of " + size + " ways.");
	}

    private boolean checkBounds(OSMMap map, PositionVector pos) {
    	if (pos.getX() > map.getMaxPosition().getX()) return false;
    	if (pos.getY() > map.getMaxPosition().getY()) return false;
    	
    	if (pos.getX() < map.getMinPosition().getX()) return false;
    	if (pos.getY() < map.getMinPosition().getY()) return false;
    	
    	return true;
    }
	
	private static enum STATE {
		INITIAL,
		NODES,
		WAYS;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		if (state.equals(STATE.WAYS)) {
    		if ("nd".equals(qName)) {
    			idList.add(Long.parseLong(attrs.getValue("ref")));
    		} else if ("tag".equals(qName)) {
    			if (attrs.getValue("k").equals("name")) {
    				name = attrs.getValue("v");
    			}

				options.put(attrs.getValue("k"), attrs.getValue("v"));
    		}
    	} else if (state.equals(STATE.NODES)) {
    		if ("tag".equals(qName)) {
				options.put(attrs.getValue("k"), attrs.getValue("v"));
    		}
    	} else {
	    	if ("node".equals(qName)) {
	    		UTM utmCoord = UTMConversion.latLon2UTM(Double.parseDouble(attrs.getValue("lat")),
	    				Double.parseDouble(attrs.getValue("lon")));
	    		
	    		PositionVector nodePosition = utmCoord.getPosition();
	    		
	    		if (!boundsFound) {
	    			PositionVector minLonLat = map.getMinPosition();
	    			PositionVector maxLonLat = map.getMaxPosition();
	    			
	    			if (firstNode) {
	    				firstNode = false;
	    				minLonLat.replace(nodePosition);
	    				maxLonLat.replace(nodePosition);
	    			} else {
		    			if (nodePosition.getX() < minLonLat.getX()) minLonLat.setEntry(0, nodePosition.getX());
		    			if (nodePosition.getY() < minLonLat.getY()) minLonLat.setEntry(1, nodePosition.getY());

		    			if (nodePosition.getX() > maxLonLat.getX()) maxLonLat.setEntry(0, nodePosition.getX());
		    			if (nodePosition.getY() > maxLonLat.getY()) maxLonLat.setEntry(1, nodePosition.getY());

		    			map.setMinPosition(minLonLat);
		    			map.setMaxPosition(maxLonLat);
	    			}
	    		}
	    		
	    		nodeId = Long.parseLong(attrs.getValue("id"));

	    		options = new HashMap<String, String>();
	    		
	    		map.addNode(nodeId, nodePosition);

	    		state = STATE.NODES;
	    		
	    	} else if ("way".equals(qName)) {
	    		if (!boundsFound) {
	    			// Assume that all nodes come in front of the way definitions and make
	    			// the bounds final at this point to get a consistent map
	    			boundsFound = true;
	    		}

	    		idList = new Vector<Long>();
	    		options = new HashMap<String, String>();
	    		state = STATE.WAYS;
	    	} else if ("bounds".equals(qName)) {
	    		boundsFound = true;
	    		
	    		//<bounds minlat="49.8634100" minlon="8.6321500" maxlat="49.8837700" maxlon="8.6746000"/>
	    		
	    		UTM minUtmCoord = UTMConversion.latLon2UTM(Double.parseDouble(attrs.getValue("minlat")),
	    				Double.parseDouble(attrs.getValue("minlon")));
	    		
	    		UTM maxUtmCoord = UTMConversion.latLon2UTM(Double.parseDouble(attrs.getValue("maxlat")),
	    				Double.parseDouble(attrs.getValue("maxlon")));
	    		
	    		map.setMinPosition(minUtmCoord.getPosition());
	    		map.setMaxPosition(maxUtmCoord.getPosition());
	    	}
    	}
	}

	@Override
    public void endElement(String uri, String localName, String qName) throws SAXException {		
		if ("way".equals(qName)) {
    		state = STATE.INITIAL;
    		map.addWay(name, idList, options);
    	} else if ("node".equals(qName)) {
    		state = STATE.INITIAL;

    		map.getNode(nodeId).setAttributes(options);

    	}
	}
	
	public static void main(String... args) {
		OSMMap map = null;
		
		File osmFile = new File(args[0]);
		
		if (!osmFile.exists())
			throw new ConfigurationException("Couldn't find OSM file: " + args[0]);
		
		try {
	    	
			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		    SAXParser parser;
			parser = parserFactory.newSAXParser();

		    OSMReader reader = new OSMReader();
			parser.parse(osmFile, reader);
			
			reader.postProcessing();

			map = reader.getMap();

			System.out.println("Filename: " + args[0]);
			System.out.println("maxlat: " + map.getMaxPosition().getY());
			System.out.println("minlon: " + map.getMinPosition().getY());
			System.out.println("maxlat: " + map.getMaxPosition().getX());
			System.out.println("minlon: " + map.getMinPosition().getX());

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
