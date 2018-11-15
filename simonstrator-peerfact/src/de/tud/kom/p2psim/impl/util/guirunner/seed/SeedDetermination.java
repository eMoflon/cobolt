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


package de.tud.kom.p2psim.impl.util.guirunner.seed;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import de.tud.kom.p2psim.impl.util.guirunner.impl.ConfigFile;

/**
 * Class that provides a seed for the simulation, depending on user options.
 * 
 * @author Leo Nobach
 *
 */
public class SeedDetermination {
	
	int newSeed;
	int lastUsedSeed;
	boolean foundLastUsedSeed = false;
	private SeedDeterminationChoice choice = SeedDeterminationChoice.newSeed;
	
	List<SeedDeterminationListener> listeners = new ArrayList<SeedDeterminationListener>();
	
	Map<String, Integer> seeds = new HashMap<String, Integer>();
	private int customSeed;
	private ConfigFile file;
	
	static final String SEEDS_FILE = "guiCfg/seeds.db";
	public static final int NULL_SEED = 286591039;
	
	public SeedDetermination() {
		readSeedsDB();
		newSeed = new Random().nextInt();
		customSeed = 0;
	}
	
	public void loadFile(ConfigFile f) {
		file = f;
		determineLastUsedSeed();
		fileChanged();
	}
	
	public ConfigFile getConfigFile() {
		return file;
	}
	
	private void readSeedsDB() {
	    BufferedReader input;
		try {
			input = new BufferedReader(new FileReader(SEEDS_FILE));
		} catch (FileNotFoundException e) {
			System.out.println("Seeds file not found, ignoring...");
			return;
		}
	      try {
	        String line = null;
	        while (( line = input.readLine()) != null){
	          String[] elements = line.split(" ");
	          if (elements.length >= 2) {
	        	  seeds.put(elements[0], Integer.parseInt(elements[1]));
	          }
	        }
	        input.close();
	      } catch (IOException e) {
	    	  throw new RuntimeException(e);
	      }
	}
	
	private void determineLastUsedSeed() {
		String hash = getFileNameHash();
		if (seeds.containsKey(hash)) {
			lastUsedSeed = seeds.get(hash);
			foundLastUsedSeed = true;
			return;
		}
		lastUsedSeed = NULL_SEED;
		foundLastUsedSeed = false;
	}

	protected boolean foundLastUsedSeed() {
		return foundLastUsedSeed;
	}
	
	protected int getLastUsedSeed() {
		return lastUsedSeed;
	}
	
	protected int getNewSeed() {
		return newSeed;
	}
	
	protected void choose(SeedDeterminationChoice c) {
		this.choice  = c;
	}
	
	protected String getFileNameHash() {
		if (file == null) {
			return null;
		} else {
			return MD5.hash(file.getFile().getAbsolutePath());
		}
	}
	
	public void saveSettings() {
		if (choice != SeedDeterminationChoice.fromLastRun) {
			
			seeds.put(getFileNameHash(), getChosenSeed());
			
			try{
			    FileWriter fstream = new FileWriter(SEEDS_FILE);
			        BufferedWriter out = new BufferedWriter(fstream);
			    for (Entry<String, Integer> seed : seeds.entrySet()) {
			    	out.write(seed.getKey() + " " + String.valueOf(seed.getValue()) + "\r\n");
			    }			    
			    out.close();
			    } catch (Exception e){
			       System.err.println("Could not write seed cache file. Error: " + e.getMessage());
			    }
		}
	}
	
	public void setCustomSeed(int customSeed) {
		this.customSeed = customSeed;
	}
	
	public int getConfigSeed() {
		String seed = file.getSeedInConfig();
		if (seed == null) return NULL_SEED;
		try {
			return Integer.parseInt(seed);
		} catch (NumberFormatException e) {
			return NULL_SEED;
		}
	}
	
	public int getChosenSeed() {
		if (choice == SeedDeterminationChoice.fromLastRun) return lastUsedSeed;
		if (choice == SeedDeterminationChoice.newSeed) return newSeed;
		if (choice == SeedDeterminationChoice.fromConfig) return getConfigSeed();
		else return customSeed;
	}

	protected enum SeedDeterminationChoice {
		fromLastRun,
		newSeed,
		fromConfig,
		customSeed;
	}

	public int getCustomSeed() {
		return customSeed;
	}
	
	public void addListener(SeedDeterminationListener l) {
		listeners.add(l);
	}
	
	void fileChanged() {
		for (SeedDeterminationListener l : listeners) l.fileChanged();
	}
	
	public interface SeedDeterminationListener {
		public void fileChanged();
	}
}
