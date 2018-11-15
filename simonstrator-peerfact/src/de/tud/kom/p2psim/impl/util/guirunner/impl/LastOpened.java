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


package de.tud.kom.p2psim.impl.util.guirunner.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * 
 * The manager for recently opened files.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class LastOpened {

	public static final String LAST_OPENED_CONF = "./guiCfg/runnerLastOpened.files";

	public static final int LAST_OPENED_LENGTH = 5;

	List<ConfigFile> lastOpened = new ArrayList<ConfigFile>();

	public LastOpened() {
		this.loadFromFile();
	}

	/**
	 * Appends a file to the recently opened files.
	 * 
	 * @param f
	 */
	public void append(ConfigFile f) {

		removeOccurrences(f);

		lastOpened.add(0, f);

		correctSize();
	}

	/**
	 * Removes the occurrence from the last opened files.
	 * 
	 * Nötig, da equals und damit remove entsprechend bei Dateien nicht
	 * funktioniert!
	 * 
	 * @param f
	 */
	private void removeOccurrences(ConfigFile f) {
		for (ConfigFile f2 : lastOpened) {
			if (f2.getFile().getAbsolutePath().equals(f.getFile().getAbsolutePath())) {
				lastOpened.remove(f2);
				break;
			}
		}
	}

	/**
	 * Returns all last opened files that are saved.
	 * 
	 * @return
	 */
	public List<ConfigFile> getLastOpened() {
		return lastOpened;
	}

	/**
	 * Cuts the size to the maximum entries allowed.
	 */
	private void correctSize() {
		if (lastOpened.size() > LAST_OPENED_LENGTH)
			lastOpened = lastOpened.subList(0, LAST_OPENED_LENGTH - 1);
	}

	/**
	 * Loads the entries from the configuration file.
	 */
	public void loadFromFile() {

		lastOpened = new ArrayList<ConfigFile>();

		BufferedReader cfgFileStream;
		try {
			cfgFileStream = new BufferedReader(new FileReader(LAST_OPENED_CONF));

			String line;
			while ((line = cfgFileStream.readLine()) != null) {

				File openedFile = new File(line);
				if (openedFile.exists()) {
					if (openedFile.getName().endsWith(".xml")) {
						lastOpened.add(new XMLConfigFile(openedFile));
					} else if (openedFile.getName().endsWith(".simcfg")) {
						lastOpened.add(new SimCfgConfigFile(openedFile));
					}
				} else {
					System.err.println("Datei \"" + line
							+ "\" existiert nicht mehr. Wird herausgenommen.");
				}
			}

		} catch (FileNotFoundException e) {
			// einfach leere Liste übergeben.
		} catch (IOException e) {
			System.err
					.println("Fehler beim Einlesen der zuletzt geöffneten Dateien: ");
			e.printStackTrace();
		}

		correctSize();
	}

	/**
	 * Saves the entries to the configuration file
	 */
	public void saveToFile() {
		try {

			BufferedWriter w = new BufferedWriter(new FileWriter(
					LAST_OPENED_CONF));

			for (ConfigFile f : lastOpened) {
				w.write(f.getFile().getAbsolutePath() + "\r\n");
			}

			w.close();

		} catch (IOException e) {
			System.err
					.println("Fehler beim Schreiben der zuletzt geöffneten Dateien: ");
			e.printStackTrace();
		}
	}

}
