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

package de.tud.kom.p2psim.impl.topology.placement;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.GPSCalculation;

/**
 * Generates attraction points out of real data from OSM.
 * 
 * @author Nils Richerzhagen
 * @version 1.0, Feb 22, 2016
 */
public class JSONPlacement implements PlacementModel {

	private PositionVector worldDimensions;
	
	private List<PositionVector> placements;
	
	private String placementJsonFile;
	private double latLeft; //Values from -90 to 90; always smaller than latRight
	private double latRight; //Values from -90 to 90
	private double lonLeft; //Values from -180 to 180; Always smaller than lonRight
	private double lonRight; //Values from -180 to 180

	/**
	 * You have to set a json-file, which has set some POIs
	 * Sample-query for "bar"-POIs in Darmstadt (Bounding Box from [49.4813, 8.5590] to [49.9088, 8,7736]:
		http://overpass-api.de/api/interpreter?data=%5Bout:json%5D;node%5Bamenity=bar%5D%2849%2E4813%2C8%2E5590%2C49%2E9088%2C8%2E7736%29%3Bout%3B	 
	 */
	public JSONPlacement() {
		placements = new Vector<PositionVector>();
		
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
	
	private void getPlacements() {
		if(placements.size() == 0) {
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
						if(lat > latLeft && lat < latRight &&
								lon > lonLeft && lon < lonRight){
							placements.add(transformGPSWindowToOwnWorld(lat, lon));
						}
					}
					catch (JSONException e) {
						//This bar had no name defined, so there was an error. Not so bad
					}
				}
			}
		}				
	}
	
	public void setPlacementJsonFile(String placementJsonFile) {
		this.placementJsonFile = placementJsonFile;
	}
	
	
	@Override
	public void addComponent(TopologyComponent comp) {
		if(worldDimensions == null){
			worldDimensions = comp.getTopology().getWorldDimensions();
		}
		getPlacements();
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		return placements.remove(0);
	}

}
