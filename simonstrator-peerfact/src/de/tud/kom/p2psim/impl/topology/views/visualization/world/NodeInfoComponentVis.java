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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.VisualizationComponent;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.overlay.NodeInformation;

/**
 * Generic component that visualizes information from nodes implementing the
 * {@link NodeInformation} interface.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jul 9, 2015
 */
public class NodeInfoComponentVis extends JComponent
		implements VisualizationComponent {

	protected Collection<NodeVis> nodes = new LinkedList<>();

	private JMenu menu = new JMenu("Node Information");

	protected boolean[] activeLayers = null;

	public <T extends HostComponent> NodeInfoComponentVis(
			final Class<T> componentClass) {
		setBounds(0, 0, VisualizationInjector.getWorldX(),
				VisualizationInjector.getWorldY());
		setOpaque(true);
		setVisible(true);

		Event.scheduleWithDelay(1 * Time.MICROSECOND, new EventHandler() {
			@Override
			public void eventOccurred(Object content, int type) {
				for (Host host : Oracle.getAllHosts()) {
					try {
						HostComponent c = host.getComponent(componentClass);
						if (c instanceof NodeInformation) {
							nodes.add(new NodeVis(host, (NodeInformation) c));
						}
					} catch (ComponentNotAvailableException e) {
						// don't care
					}
				}
				initializeMenu();
			}
		}, null, 0);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		for (NodeVis vis : nodes) {
			vis.draw(g2);
		}
	}

	protected void initializeMenu() {
		for (NodeVis vis : nodes) {
			String[] dimensions = vis.nodeInfo
					.getNodeColorDimensionDescriptions();
			activeLayers = new boolean[dimensions.length];
			for (int dim = 0; dim < dimensions.length; dim++) {
				JCheckBoxMenuItem item = new JCheckBoxMenuItem(
						dimensions[dim]);
				item.setSelected(true);
				item.addChangeListener(new ChangeListenerImpl(dim));
				menu.add(item);
				activeLayers[dim] = true;
			}
			break;
		}
	}

	private class ChangeListenerImpl implements ChangeListener {

		private final int dim;

		public ChangeListenerImpl(int dim) {
			this.dim = dim;
		}

		@Override
		public void stateChanged(ChangeEvent e) {
			activeLayers[dim] = !activeLayers[dim];
		}

	}

	/**
	 * Visualization-fragments for Node-centric visualiation-information.
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, Sep 22, 2013
	 */
	private class NodeVis {

		public final NodeInformation nodeInfo;

		private final SimHost host;

		private final PositionVector loc;

		private final Color activeGreen = new Color(0, 200, 0);

		private final Color[] colors = { Color.ORANGE, Color.BLUE, Color.RED,
				Color.MAGENTA, Color.GRAY, Color.GREEN, Color.CYAN,
				Color.PINK };

		public NodeVis(Host host, NodeInformation nodeInfo) {
			this.nodeInfo = nodeInfo;
			this.host = (SimHost) host;
			this.loc = this.host.getTopologyComponent().getRealPosition();
		}

		/**
		 * Called on one of the nodes to draw global objects such as a legend.
		 * Called before draw.
		 * 
		 * @param g2
		 */
		public void drawLegend(Graphics2D g2) {
			String[] dimensions = nodeInfo.getNodeColorDimensionDescriptions();
			int radius = 4;
			for (int dim = 0; dim < dimensions.length; dim++) {
				radius += 2;
				g2.setColor(Color.DARK_GRAY);
				g2.drawOval(10, 20 * (dim + 1) - 10, radius * 2, radius * 2);
				g2.drawString(dimensions[dim], 30, 20 * (dim + 1));
				String[] colorDescs = nodeInfo.getNodeColorDescriptions(dim);
				for (int i = 0; i < colorDescs.length; i++) {
					g2.setColor(colors[i]);
					g2.fillRect(30 + (i + 1) * 90, 20 * (dim + 1) - 10, 8, 8);
					g2.setColor(Color.DARK_GRAY);
					g2.drawString(colorDescs[i], 40 + (i + 1) * 90,
							20 * (dim + 1));
				}
			}
		}

		public void draw(Graphics2D g2) {

			Point center = loc.asPoint();
			// Draw active (green) over underlay vis.
			g2.setColor(nodeInfo.isActive() ? activeGreen : Color.LIGHT_GRAY);
			int radius = 3;
			g2.drawOval(center.x - radius, center.y - radius, radius * 2,
					radius * 2);

			if (!nodeInfo.isActive()) {
				return;
			}

			/*
			 * TODO add offline/online info here as well (removes the need for a
			 * custom object that visualizes the underlay!)
			 */

			int numColors = nodeInfo.getNodeColorDimensions();
			radius = 4;
			for (int color = 0; color < numColors; color++) {
				int value = nodeInfo.getNodeColor(color);
				radius += 2;
				if (value < 0 || !activeLayers[color]) {
					continue;
				}
				g2.setColor(colors[value]);
				g2.drawOval(center.x - radius, center.y - radius, radius * 2,
						radius * 2);
			}
			String nodeDesc = nodeInfo.getNodeDescription();
			g2.drawString(nodeDesc, center.x + 4, center.y + 4);
		}

	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public JMenu getCustomMenu() {
		return menu;
	}

	@Override
	public boolean isHidden() {
		return false;
	}
	
	@Override
	public String getDisplayName() {
		return "Node Information";
	}

}
