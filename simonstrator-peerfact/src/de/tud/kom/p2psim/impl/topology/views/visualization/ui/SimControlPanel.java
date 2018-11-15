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

package de.tud.kom.p2psim.impl.topology.views.visualization.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager.VisInfo;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager.VisualizationListener;

/**
 * Menu-Bar containing means to alter the simulation speed, pause and un-pause
 * simulations and (new) configuration of plots and visualization layers.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.08.2012
 */
public class SimControlPanel extends JMenuBar
		implements ActionListener, ChangeListener, VisualizationListener {

	private static final long serialVersionUID = -914578954798611308L;

	private JToggleButton playpause = null;

	private JSlider speedslider = null;

	private JLabel speedlabel = null;

	private JMenu layerMenu = null;

	private boolean isSimulatorPaused = false;

	private ComponentVisManager visManager;

	public SimControlPanel(ComponentVisManager visManager) {
		// super("Control");
		this.visManager = visManager;
		visManager.addVisualizationListener(this);
		// this.setFloatable(false);
		this.setPreferredSize(
				new Dimension(VisualizationInjector.getWorldX(), 30));
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.add(Box.createHorizontalStrut(20));
		this.add(getLayerMenu());
		this.add(Box.createHorizontalGlue());
		this.add(new JLabel("Speed:"));
		this.add(getSpeedSlider());
		this.add(getSpeedLabel());
		this.add(Box.createHorizontalStrut(10));
		this.add(getPlayPauseButton());
		this.add(Box.createHorizontalGlue());
	}

	protected JToggleButton getPlayPauseButton() {
		if (playpause == null) {
			// playpause = new JCheckBox("pause simulation");
			// playpause.setActionCommand("playpause");
			// playpause.setEnabled(true);
			// playpause.addActionListener(this);
			Action a = new AbstractAction("Pause") {
				@Override
				public void actionPerformed(ActionEvent e) {
					togglePlayPause();
				}
			};
			playpause = new JToggleButton(a);
			playpause.setSelected(false);
		}
		return playpause;
	}

	protected JMenu getLayerMenu() {
		if (layerMenu == null) {
			layerMenu = new JMenu("Layers");
		}
		return layerMenu;
	}
	
	public ComponentVisManager getVisManager() {
		return visManager;
	}

	@Override
	public void visualizationAdded(VisInfo visInfo) {
		JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem(visInfo.getName());
		checkBox.setSelected(visInfo.isActiveByDefault());
		checkBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				getVisManager().toggleComponent(visInfo.getName());
			}
		});
		if (visInfo.getVisualizationComponent() != null && visInfo
				.getVisualizationComponent().getCustomMenu() != null) {
			// Custom Menu
			JMenu customMenu = visInfo.getVisualizationComponent().getCustomMenu();
			customMenu.insertSeparator(0);
			customMenu.add(checkBox, 0);
			layerMenu.add(customMenu);
		} else {
			layerMenu.add(checkBox);
		}
		revalidate();
	}

	@Override
	public void visualizationRemoved(VisInfo visInfo) {
		// don't care?
	}

	protected JSlider getSpeedSlider() {
		if (speedslider == null) {
			speedslider = new JSlider(SwingConstants.HORIZONTAL, 1, 30, 5);
			speedslider.addChangeListener(this);
			// speedslider.setValue(speedslider.getMaximum());
		}
		return speedslider;
	}

	protected JLabel getSpeedLabel() {
		if (speedlabel == null) {
			speedlabel = new JLabel("max");
		}
		return speedlabel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("playpause".equals(e.getActionCommand())) {
			togglePlayPause();
		}
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() instanceof JSlider) {
			JSlider source = (JSlider) e.getSource();
			if (!source.getValueIsAdjusting()) {
				int skew = source.getValue();
				changeSimulationSkew(skew);
			}
		}
	}

	public void togglePlayPause() {
		if (isSimulatorPaused) {
			unpauseSimulation();
		} else {
			pauseSimulation();
		}
	}

	protected boolean pauseSimulation() {
		if (!isSimulatorPaused) {
			isSimulatorPaused = true;
			Simulator.getScheduler().pause();
			getPlayPauseButton().setSelected(true);
			return true;
		}
		return false;
	}

	protected boolean unpauseSimulation() {
		if (isSimulatorPaused) {
			isSimulatorPaused = false;
			Simulator.getScheduler().unpause();
			getPlayPauseButton().setSelected(false);
			return true;
		}
		return false;
	}

	protected void changeSimulationSkew(double skew) {
		if (skew == 30) {
			getSpeedLabel().setText("max");
			Simulator.getScheduler().setTimeSkew(0);
		} else {
			if (skew <= 5) {
				skew = 0.02 * ((int) (skew * 10));
			} else {
				skew = skew - 4;
			}
			Simulator.getScheduler().setTimeSkew(skew);
			getSpeedLabel().setText(skew + "x");
		}
	}

}
