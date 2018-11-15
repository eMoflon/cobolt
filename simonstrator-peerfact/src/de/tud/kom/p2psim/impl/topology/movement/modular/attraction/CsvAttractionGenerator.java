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

package de.tud.kom.p2psim.impl.topology.movement.modular.attraction;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * Generates a given number of {@link AttractionPoint}s and sets the Position
 * according to a given CSV file.
 * 
 * @author Nils Richerzhagen
 * @version 1.0, 16.07.2014
 */
public class CsvAttractionGenerator implements AttractionGenerator {

	private PositionVector worldDimensions;

	private String file;

	private final String SEP = ";";

	private List<PositionVector> attractionPointsPositions;

	private double minSpeed = 2;

	private double maxSpeed = 2;

	/**
	 * 
	 * @param file
	 */
	@XMLConfigurableConstructor({ "placementFile" })
	public CsvAttractionGenerator(String placementFile) {
		this.worldDimensions = Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions();
		attractionPointsPositions = new LinkedList<PositionVector>();
		this.file = placementFile;
	}

	@Override
	public void setNumberOfAttractionPoints(int numberOfAttractionPoints) {
		// Number of AttractionPoints is set by the CSV file.
	}

	@Override
	public List<AttractionPoint> getAttractionPoints() {
		readData();

		List<AttractionPoint> result = new LinkedList<AttractionPoint>();
		for (PositionVector attractionPointPositionVector : attractionPointsPositions) {
			AttractionPoint aPoint = new AttractionPoint(
					attractionPointPositionVector, minSpeed, maxSpeed);
			result.add(aPoint);
		}
		return result;
	}

	private void readData() {
		attractionPointsPositions.clear();
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
							Double x = Double.parseDouble(parts[0]);
							Double y = Double.parseDouble(parts[1]);

							if (x > worldDimensions.getX()
									|| y > worldDimensions.getY() || x < 0
									|| y < 0) {
								System.err.println("Skipped entry " + x + ";"
										+ y);
								continue;
							}

							attractionPointsPositions.add(new PositionVector(x,
									y));
							entrySuccessfullyRead = true;
						} catch (NumberFormatException e) {
							// Ignore leading comments
							if (entrySuccessfullyRead) {
								// System.err.println("CSV ParseError " + line);
							}
						}
					}
				} else {
					throw new AssertionError("To many columns in CSV.");
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
