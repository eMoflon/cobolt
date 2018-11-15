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

package de.tud.kom.p2psim.impl.topology.views.visualization.world;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector.MouseClickListener;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.VisualizationComponent;

/**
 * This component draw the Social connections.
 * 
 * @author Christoph Muenker
 * @version 1.0, 22.06.2013
 */
public class SocialViewComponentVis extends JComponent
		implements MouseClickListener, VisualizationComponent {

	private static Vector<Color> CLUSTER_COLOR = new Vector<Color>();
	static {
		CLUSTER_COLOR.add(Color.BLUE);
		CLUSTER_COLOR.add(Color.GREEN);
		CLUSTER_COLOR.add(new Color(1f, 0.27f, 0f)); // OrangeRed
		CLUSTER_COLOR.add(Color.CYAN);
		CLUSTER_COLOR.add(Color.RED);
		CLUSTER_COLOR.add(Color.YELLOW);
	}

	private SocialView view;

	private Map<SimHost, PositionVector> posVecs = new HashMap<SimHost, PositionVector>();

	private Map<Color, Set<SimHost>> clusterMap = new HashMap<Color, Set<SimHost>>();

	private JMenu customMenu;

	private SimHost selectedHost = null;

	private boolean showCluster = false;

	private boolean showRelationship = false;

	public SocialViewComponentVis(SocialView view) {
		this.view = view;

		int i = 0;
		for (Set<SimHost> cluster : view.getClusters()) {
			for (SimHost host : cluster) {
				posVecs.put(host,
						host.getTopologyComponent().getRealPosition());
			}
			if (i < CLUSTER_COLOR.size()) {
				clusterMap.put(CLUSTER_COLOR.get(i), cluster);
				i++;
			}
		}

		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(true);
		setVisible(true);

		VisualizationInjector.addMouseListener(this);

		customMenu = new JMenu("Social View");
		customMenu.add(createRelationshipCheckBox());
		customMenu.add(createClusterCheckBox());

	}

	private JCheckBoxMenuItem createRelationshipCheckBox() {
		final JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem("Connections",
				showRelationship);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				showRelationship = checkBox.isSelected();
				SocialViewComponentVis.this.repaint();
			}
		});
		return checkBox;
	}

	private JCheckBoxMenuItem createClusterCheckBox() {
		final JCheckBoxMenuItem checkBox = new JCheckBoxMenuItem("Cluster",
				showCluster);
		checkBox.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				showCluster = checkBox.isSelected();
				SocialViewComponentVis.this.repaint();
			}
		});
		return checkBox;
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		if (showRelationship) {
			for (Entry<SimHost, PositionVector> entry : posVecs.entrySet()) {
				SimHost source = entry.getKey();
				Point sourcePos = entry.getValue().asPoint();
				// neighbors
				for (SimHost dest : view.getNeighbors(source)) {
					if (selectedHost == null || source.equals(selectedHost)
							|| dest.equals(selectedHost)) {
						g2.setColor(new Color(0, 0, 0));
					} else {
						g2.setColor(new Color(0, 0, 0, 32));
					}
					Point destPos = posVecs.get(dest).asPoint();
					g2.drawLine(sourcePos.x, sourcePos.y, destPos.x, destPos.y);
				}
			}
		}

		if (showCluster) {
			for (Entry<Color, Set<SimHost>> clusters : clusterMap.entrySet()) {

				for (SimHost host : clusters.getValue()) {
					Point center = posVecs.get(host).asPoint();
					// for a black border around the circle
					g2.setColor(Color.BLACK);
					g2.setStroke(new BasicStroke(4.5f));
					g2.drawOval(center.x - 7, center.y - 7, 14, 14);

					// draw the circle
					g2.setColor(clusters.getKey());
					g2.setStroke(new BasicStroke(3.5f));
					g2.drawOval(center.x - 7, center.y - 7, 14, 14);
				}
			}
		}
	}

	@Override
	public void mouseClicked(int x, int y) {
		List<SimHost> inNear = new Vector<SimHost>();
		for (Entry<SimHost, PositionVector> e : posVecs.entrySet()) {
			if (e.getValue().distanceTo(new PositionVector(x, y)) < 4) {
				inNear.add(e.getKey());
			}
		}

		if (inNear.size() == 1) {
			selectedHost = inNear.iterator().next();
		} else if (inNear.size() > 1) {
			Collections.shuffle(inNear);
			selectedHost = inNear.iterator().next();
		} else {
			selectedHost = null;
		}
		this.repaint();

	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public JMenu getCustomMenu() {
		return customMenu;
	}

	@Override
	public boolean isHidden() {
		return false;
	}

	@Override
	public String getDisplayName() {
		return view.getIdentifier();
	}
}
