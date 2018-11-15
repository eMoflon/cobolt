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


package de.tud.kom.p2psim.impl.util.guirunner.progress;

import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.WindowConstants;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.guirunner.Config;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class SimulationProgressView extends RichProgressView implements UncaughtExceptionHandler {

	public static SimulationProgressView inst = null;
	
	public static SimulationProgressView getInstance() {
		if (inst == null) inst = new SimulationProgressView();
		return inst;
	}
	
	/*
	 * Configuration paths
	 */
	static final String CONF_PATH = "GUIRunner/ProgressWindow/";
	static final String CONF_PATH_POSX = CONF_PATH + "PosX";
	static final String CONF_PATH_POSY = CONF_PATH + "PosY";
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1701832807987594850L;

	public static final String JOB_PREPARING = "Preparation...";

	public static final String JOB_SIMULATION = "Simulation running...";
	
	public static final String JOB_EXCEPTION_PREFIX = "Exception: ";

	public static final String JOB_FINISHED = "Simulation finished successfully";

	public static final Image frameIcon = new ImageIcon("images/icons/frame_icon.png").getImage();
	
	/**
	 * Angezeigter Fortschritt nach Vorbereitung
	 */
	public static final int PREPARING_PROGR = 100;

	static final long UPDATE_STEP_TIME = 100;
	
	String actualJobName = JOB_PREPARING;
	
	boolean simulationRunning = false;
	boolean simulationFinished = false;
	
	int snapshotQueueSize = 30;
	int snapshotInterval = 100;
	int snapshotIntervalCounter = 0;
	
	Queue<Snapshot> snaps = new LinkedBlockingQueue<>();
	private Snapshot snap2use = new Snapshot(System.currentTimeMillis(), 0);
	
	class Snapshot {
		Snapshot(long realTime, long virtTime) {
			this.realTime = realTime;
			this.virtTime = virtTime;
		}
		long realTime;
		long virtTime;
	}
	
	
	long lastUpdateTime = -1;

	int progress = 0;
	
	JButton pauseButton;
	boolean paused=false;
	//private String confName;
	private UpdateThread updThread = null;


	protected SimulationProgressView() {
		super();
		this.setLocation(new Point(Config.getValue(CONF_PATH_POSX, 0), Config.getValue(CONF_PATH_POSY, 0)));
		this.setIconImage(frameIcon);
		this.setTitle("Simulation Progress");
		this.setMaximum(1000 + PREPARING_PROGR);
		// We want to use the same handling as if the simulation would have been canceled 
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				onCancelWithConfirmation();
			}
		});
		update();
		
		pauseButton = new JButton();
		pauseButton.setEnabled(false);
		pauseButton.setText("Pause");
		pauseButton.setMnemonic('p');
		pauseButton.addActionListener(new PauseButtonController());
		
		buttonPanel.add(pauseButton);
	}

	public void setConfigurationName(String confName) {
		//this.confName = confName;
		this.setTitle(confName);
	}
	
	@Override
	public String getActualJobName() {
		return actualJobName;
	}

	@Override
	public int getProgress() {
		if (!simulationRunning)
			return 0;
		else if (simulationFinished)
			return 1000 + PREPARING_PROGR;
		else
			return PREPARING_PROGR + (int) ((double) getSimCurrentTime() / (double) getSimEndTime() * 1000);
	}

	@Override
	public void onCancel(boolean cancelled) {
		this.saveSettings();
		Config.writeXMLFile();
		System.exit(0);
	}

	public void notifySimulationRunning() {
		actualJobName = JOB_SIMULATION;
		simulationRunning = true;
		pauseButton.setEnabled(true);
		startUpdateThread();
	}

	void startUpdateThread() {
		if (updThread == null) {
			updThread = new UpdateThread();
			updThread.start();
		}
	}
	
	public void notifySimulationFinished() {
		actualJobName = JOB_FINISHED;
		simulationFinished = true;
		pauseButton.setVisible(false);
		this.setFinished();
		terminateUpdateThread();
	}

	void terminateUpdateThread() {
		if (updThread != null) {
			updThread.interrupt();
			updThread = null;
		}
	}
	
	class UpdateThread extends Thread {
		
		@Override
		public void run() {
			try {
				while(true) {
					Thread.sleep(UPDATE_STEP_TIME);
					update();
				}
			} catch (InterruptedException e) {
				//Nothing to do, run out.
			}
		}
		
	}

	@Override
	public long getEstimatedTime() {
		if (!simulationRunning)
			return -1;
		
		long currentRealTime = System.currentTimeMillis();
		long currentVirtTime = getSimCurrentTime();
		
		snapshotIntervalCounter++;
		
		if (snapshotIntervalCounter >= snapshotInterval) {
			snaps.add(new Snapshot (currentRealTime, currentVirtTime));
			snapshotIntervalCounter++;
		}
		if (snaps.size() >= snapshotQueueSize) {
			snap2use = snaps.remove();
		}
		
		
		
		//System.out.println("=========== " + snap2use.realTime + " === "+ snap2use.virtTime);
		
		double timePerVirt = (currentRealTime - snap2use.realTime) / (double)(currentVirtTime - snap2use.virtTime);
		long result = (long)((getSimEndTime() - getSimCurrentTime()) * timePerVirt);
		//System.out.println("Estimated Time: " + result);
		return result;

	}
	
	protected long getSimCurrentTime() {
		return Time.getCurrentTime() / Time.MILLISECOND;
	}
	
	protected long getSimEndTime() {
		return Simulator.getEndTime() / Time.MILLISECOND;
	}
	
	/**
	 * Saves settings, like window size etc.
	 */
	public void saveSettings() {
		
		Config.setValue(CONF_PATH_POSX, this.getX());
		Config.setValue(CONF_PATH_POSY, this.getY());
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		e.printStackTrace();
		actualJobName = JOB_EXCEPTION_PREFIX + e.getClass().getName();
		simulationFinished = true;
		pauseButton.setVisible(false);
		this.setFinished();
		terminateUpdateThread();
		this.update();
	}
	
	class PauseButtonController implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == pauseButton) {
				if (!paused) {
					Simulator.getScheduler().pause();
					pauseButton.setText("Continue");
					pauseButton.setMnemonic('c');
					paused = true;
					terminateUpdateThread();
				} else {
					Simulator.getScheduler().unpause();
					pauseButton.setText("Pause");
					pauseButton.setMnemonic('p');
					paused = false;
					startUpdateThread();
				}
			}
		}
		
	}

}













