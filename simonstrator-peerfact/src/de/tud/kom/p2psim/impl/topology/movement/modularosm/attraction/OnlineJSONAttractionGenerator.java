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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.GPSCalculation;
import de.tudarmstadt.maki.simonstrator.api.Binder;

/**
 * Generates attraction points out of real data from osm
 * The maximal number of attraction points can be set
 * 
 * @author Martin Hellwig
 * @version 1.0, 02.07.2015
 */
public class OnlineJSONAttractionGenerator implements IAttractionGenerator {

	private PositionVector worldDimensions;

	private List<AttractionPoint> attractionPoints;
	
	private int maxNumberOfAttractionPoints;
	
	private String placementJsonFile;
	private String placementJsonPath;
	
	private String amenity;
	private double latLeft; //Values from -90 to 90; always smaller than latRight
	private double latRight; //Values from -90 to 90
	private double lonLeft; //Values from -180 to 180; Always smaller than lonRight
	private double lonRight; //Values from -180 to 180

	public OnlineJSONAttractionGenerator() {
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		attractionPoints = new LinkedList<AttractionPoint>();
		
		latLeft = GPSCalculation.getLatLower();
		latRight = GPSCalculation.getLatUpper();
		lonLeft = GPSCalculation.getLonLeft();
		lonRight = GPSCalculation.getLonRight();
	}

	/**
	 * Projects the gps coordinates in the given gps window to the world-coordinates given in world-dimensions
	 * @param lat
	 * @param lon
	 * @return The projected position in world-dimensions
	 */
	private PositionVector transformGPSWindowToOwnWorld(double lat, double lon) {
		double x = worldDimensions.getX() * (lon - lonLeft)/(lonRight - lonLeft);
		//Invert the y value, because in Java Swing we start drawing in the upper left corner instead in the lower left one
		double y = worldDimensions.getY() - worldDimensions.getY() * (lat - latLeft)/(latRight - latLeft);
		return new PositionVector(x, y);
	}

	public void setNumberOfAttractionPoints(int numberOfAttractionPoints) {
		this.maxNumberOfAttractionPoints = numberOfAttractionPoints;
	}

	@Override
	public List<AttractionPoint> getAttractionPoints() {
		if(attractionPoints.size() == 0) {
			placementJsonFile = placementJsonPath + 
					"pois" + 
					GPSCalculation.getLatCenter() + 
					GPSCalculation.getLonCenter() + 
					GPSCalculation.getZoom() +
					amenity + ".json";

			//Check if the file with same properties (same location) already exists
			File f = new File(placementJsonFile);
			if(!f.exists()) {
				String poiString = "";
				JSONArray allPOI = null;
				InputStream in;
				try {
					in = new URL( "http://overpass-api.de/api/interpreter?data=%5Bout:json%5D;node%5Bamenity=" + amenity + "%5D%28" + latLeft + "%2C" + lonLeft + "%2C" + latRight + "%2C" + lonRight + "%29%3Bout%3B" ).openStream();
					poiString = IOUtils.toString(in);
					//Save the json data in file
					PrintWriter out = new PrintWriter(placementJsonFile);
					out.print(poiString);
					out.close();
					JSONObject poiData = new JSONObject(poiString);
					allPOI = poiData.getJSONArray("elements");
				} catch (JSONException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if(allPOI != null) {
					for(int i = 0; i < allPOI.length(); i++) {
						try {
							String barname = allPOI.getJSONObject(i).getJSONObject("tags").getString("name");
							double lat = allPOI.getJSONObject(i).getDouble("lat");
							double lon = allPOI.getJSONObject(i).getDouble("lon");
							if(lat > latLeft && lat < latRight &&
									lon > lonLeft && lon < lonRight) {
								attractionPoints.add(new AttractionPoint(transformGPSWindowToOwnWorld(lat, lon), barname));
							}
						}
						catch (JSONException e) {
							//This bar had no name defined, so there was an error. Not so bad
						}
					}
				}
			}
			else {
				//File already exists, now we have to parse this file
				String poiString = "";
				JSONArray allPOI = null;
				FileInputStream inputStream;
				try {
					inputStream = new FileInputStream(placementJsonFile);
					poiString = IOUtils.toString(inputStream);
					JSONObject poiData = new JSONObject(poiString);
					allPOI = poiData.getJSONArray("elements");
				} catch (FileNotFoundException e) {
					e.printStackTrace();
			    } catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				if(allPOI != null) {
					for(int i = 0; i < allPOI.length(); i++) {
						try {
							String barname = allPOI.getJSONObject(i).getJSONObject("tags").getString("name");
							double lat = allPOI.getJSONObject(i).getDouble("lat");
							double lon = allPOI.getJSONObject(i).getDouble("lon");
							attractionPoints.add(new AttractionPoint(transformGPSWindowToOwnWorld(lat, lon), barname));
						}
						catch (JSONException e) {
							//This bar had no name defined, so there was an error. Not so bad
						}
					}
				}
			}
		}				
		
		if(maxNumberOfAttractionPoints == 0) maxNumberOfAttractionPoints = Integer.MAX_VALUE;
		List<AttractionPoint> result = new LinkedList<AttractionPoint>();
		for (int i = 0; (i < attractionPoints.size() && i < maxNumberOfAttractionPoints); i++) {
			result.add(attractionPoints.get(i));
		}
		return result;
	}
	
	public void setAmenity(String amenity) {
		this.amenity = amenity;
	}
	
	public void setPlacementJsonPath(String placementJsonPath) {
		this.placementJsonPath = placementJsonPath;
	}
}
