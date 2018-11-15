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

import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.placement.PlacementModel;
import de.tud.kom.p2psim.impl.network.modular.common.GeoToolkit;
import de.tud.kom.p2psim.impl.topology.ExtendedPositionVector;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Read positions from a CSV-file
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 29.03.2012
 */
public class CsvPlacement implements PlacementModel {

	private List<PositionVector> positions;

	private final String SEP = ";";

	private int numberOfComponents = 0;

	private int positionIndex = 0;

	private PositionVector world;

	private String file;

	/**
	 * 
	 * @param filename
	 */
	@XMLConfigurableConstructor({ "file" })
	public CsvPlacement(String file) {
		positions = new Vector<PositionVector>();
		this.file = file;
	}

	@Override
	public void addComponent(TopologyComponent comp) {
		numberOfComponents++;
		if (world == null) {
			world = comp.getTopology().getWorldDimensions();
		}
	}

	@Override
	public PositionVector place(TopologyComponent comp) {
		if (positions.isEmpty() || positionIndex >= positions.size()) {
			readData();
			positionIndex = 0;
		}
		PositionVector vec = positions.get(positionIndex);
		positionIndex++;
		return vec;
	}

	private void readData() {
		positions.clear();
		boolean entrySuccessfullyRead = false;
		BufferedReader csv = null;
		try {
			csv = new BufferedReader(new FileReader(file));

			while (csv.ready()) {
				String line = csv.readLine();

				if (line.indexOf(SEP) > -1) {
					String[] parts = line.split(SEP);

					if (parts.length == 2) {
						try {
							Double lon = Double.parseDouble(parts[0]);
							Double lat = Double.parseDouble(parts[1]);

							// Reference Point Dijon in France
							Point2D.Double XY = GeoToolkit.transformToXY(
									GeoToolkit.STANDARD_REF_POINT_GERMANY,
									new Point2D.Double(lon, lat));
							
//							System.out.println(lon + "," + lat + "-->" + XY.getX() + "," +  XY.getY());

							if (XY.getX() > world.getX() || XY.getY() > world.getY() || XY.getX() < 0
									|| XY.getY() < 0) {
								// System.err.println("Skipped entry " + x + ";"
								// + y);
								continue;
							}

							positions.add(new ExtendedPositionVector(XY.getX(), XY.getY(), lon, lat));
							entrySuccessfullyRead = true;
						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								// System.err.println("CSV ParseError " + line);
							}
						}
					} else if (parts.length == 4) {
						try {
							Double x = Double.parseDouble(parts[0]);
							Double y = Double.parseDouble(parts[1]);
							// in case we need the lon und lat for later
							// plotting
							Double lon = Double.parseDouble(parts[2]);
							Double lat = Double.parseDouble(parts[3]);

							if (x > world.getX() || y > world.getY() || x < 0
									|| y < 0) {
								// System.err.println("Skipped entry " + x + ";"
								// + y);
								continue;
							}

							positions.add(new ExtendedPositionVector(x, y, lon,
									lat));
							entrySuccessfullyRead = true;
						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								// System.err.println("CSV ParseError " + line);
							}
						}
					} else {
						throw new AssertionError("To many columns in CSV.");
					}
				}
			}

		} catch (Exception e) {
			System.err.println(e.toString());
		} finally {
			if (csv != null) {
				try {
					csv.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}

}
