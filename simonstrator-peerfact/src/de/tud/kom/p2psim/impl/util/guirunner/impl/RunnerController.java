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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JButton;

import de.tud.kom.p2psim.impl.util.guirunner.GUIRunner;
import de.tud.kom.p2psim.impl.util.guirunner.seed.SeedDetermination;

/**
 * 
 * Runs the simulator or invokes operations on the view.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 *
 */
public class RunnerController implements ActionListener {

	LastOpened lastOpened = null;
	ConfigFile selectedFile = null;
	JButton launchButton = null;
	private GUIRunner runner;
	
	private List<IRunnerCtrlListener> listeners = new LinkedList<IRunnerCtrlListener>();
	
	SeedDetermination det = new SeedDetermination();
	
	/**
	 * Runs the simulator with the specified config file string.
	 * @param configFile
	 */
	private void runSimulator() {
		new SimulationThread(selectedFile, det.getChosenSeed()).start();
	}
	
	public void setLastOpened(LastOpened lastOpened) {
		this.lastOpened = lastOpened;
	}
	
	public SeedDetermination getDetermination() {
		return det;
	}
	
	/**
	 * Called when the user wants to start the simulation.
	 */
	public void invokeRunSimulator() {
		
		if (selectedFile == null) return;	//No file is selected!
		
		if (lastOpened != null) lastOpened.append(selectedFile);
		lastOpened.saveToFile();
		
		System.out.println("GUIRunner: Starting simulator with " + selectedFile.getFile().getAbsolutePath());
		runner.disposeRunner();
		
		det.saveSettings();
		
		runSimulator();
	}

	/**
	 * Called when the user has selected a file.
	 * @param file
	 */
	public void selectFile(final ConfigFile file) {
		selectedFile = file;
		
		if (selectedFile == null) {
			return;
		}
		
		selectedFile.loadConfiguration(new ConfigLoadedCallback() {
			@Override
			public void loadingFinished() {
				if (launchButton != null) {
					launchButton.setEnabled(selectedFile != null);
				}
				if (file != null) det.loadFile(selectedFile);
				newFileSelected(file);	
			}
		});
	}
	
	/**
	 * Sets the launch button that invokes the launch of the
	 * simulation
	 * @param b
	 */
	public void setLaunchButton(JButton b) {
		b.addActionListener(this);
		this.launchButton = b;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == launchButton) {
			invokeRunSimulator();
		}
	}
	
	public void addListener(IRunnerCtrlListener l) {
		listeners.add(l);
	}
	
	public void removeListener(IRunnerCtrlListener l) {
		listeners.remove(l);
	}
	
	void newFileSelected(ConfigFile f) {
		for (IRunnerCtrlListener l : listeners) l.newFileSelected(f);
	}

	/**
	 * Sets the main window of the GUIRunner.
	 * @param runner
	 */
	public void setMainWindow(GUIRunner runner) {
		this.runner=runner;
	}
	
	public static interface IRunnerCtrlListener {
		
		public void newFileSelected(ConfigFile f);
		
	}

	public ConfigFile getSelectedFile() {
		return selectedFile;
	}
	

}
