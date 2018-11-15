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

package de.tud.kom.p2psim.impl.topology.views;

import java.awt.AWTEvent;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.linklayer.mac.Link;
import de.tud.kom.p2psim.api.linklayer.mac.MacAddress;
import de.tud.kom.p2psim.api.linklayer.mac.MacLayer;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.TopologyComponent;
import de.tud.kom.p2psim.api.topology.obstacles.ObstacleModel;
import de.tud.kom.p2psim.api.topology.views.TopologyView;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager.VisInfo;
import de.tud.kom.p2psim.impl.topology.views.visualization.ComponentVisManager.VisualizationListener;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.InteractiveVisualizationComponent;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.PlottingView;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.SimControlPanel;
import de.tud.kom.p2psim.impl.topology.views.visualization.ui.VisualizationComponent;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.NodeVisInteractionListener;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.ObstacleComponentVis;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.StrongWaypointComponentVis;
import de.tud.kom.p2psim.impl.topology.views.visualization.world.WeakWaypointComponentVis;
import de.tud.kom.p2psim.impl.util.NotSupportedException;
import de.tud.kom.p2psim.impl.util.guirunner.progress.SimulationProgressView;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.LocationListener;

/**
 * A very basic visualization of a Topology (ie. positioning and movement), just
 * to ease further debugging and development of movement and obstacle-related
 * classes.
 * 
 * It is added to the Topology as a TopologyView with no PHY-Type (and therefore
 * it is never used inside a MAC). Lateron an API will be provided that allows
 * other components (analyzers) to display information using this basic
 * Visualization.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 19.03.2012
 */
public class VisualizationTopologyView extends JFrame
		implements TopologyView, Runnable, VisualizationListener {

	protected final int WORLD_X;

	protected final int WORLD_Y;

	protected final WorldPanel worldPanel;

	protected SimControlPanel simControls;

	protected JSplitPane splitPane;

	public static final Stroke STROKE_BASIC = new BasicStroke(0.8f);

	public static final Stroke STROKE_THICK = new BasicStroke(2f);

	public static final Font FONT_TINY = new Font("SansSerif", Font.PLAIN, 7);

	public static final Font FONT_MEDIUM = new Font("SansSerif", Font.PLAIN, 9);

	private ComponentVisManager visManager;

	private WeakWaypointComponentVis weakWaypointComponentVis;

	private ObstacleComponentVis obstacleComponentVis;

	private StrongWaypointComponentVis strongWaypointComponentVis;

	/**
	 * 
	 */
	public VisualizationTopologyView() {
		WORLD_X = (int) Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions().getX();
		WORLD_Y = (int) Binder.getComponentOrNull(Topology.class)
				.getWorldDimensions().getY();
		worldPanel = new WorldPanel();
		visManager = new ComponentVisManager(worldPanel);

		VisualizationInjector.setVariables(this, visManager, worldPanel,
				WORLD_X, WORLD_Y);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setOneTouchExpandable(true);
		splitPane.setDividerLocation(1.0);
		splitPane.setResizeWeight(1.0);

		worldPanel.setPreferredSize(
				new Dimension(VisualizationInjector.getWorldX(),
						VisualizationInjector.getWorldY()));
		this.setPreferredSize(new Dimension(800, 600));
		this.setExtendedState(Frame.MAXIMIZED_BOTH);

		Thread t = new Thread(this);
		t.start();
	}

	@Override
	public void run() {

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				SimulationProgressView.getInstance().onCancelWithConfirmation();
			}
		});
		this.setLayout(new BorderLayout());
		this.setTitle("Topology Visualization");

		visManager.addVisualizationListener(this);

		/*
		 * World-view
		 */
		JScrollPane mapScrollPanel = new JScrollPane(worldPanel,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		mapScrollPanel.getVerticalScrollBar().setUnitIncrement(50);
		mapScrollPanel.getHorizontalScrollBar().setUnitIncrement(50);
		mapScrollPanel.setBackground(Color.DARK_GRAY);
		splitPane.setLeftComponent(mapScrollPanel);
		getContentPane().add(BorderLayout.CENTER, splitPane);

		buildSimControls();

		getRootPane().setDoubleBuffered(true);

		/*
		 * Finally, lights!
		 */
		this.pack();
		this.setVisible(true);

		// for the movement of the line
		final Timer timer = new Timer(250, null);
		timer.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				worldPanel.invalidate();
				timer.setDelay(250);
			}
		});
		timer.start();
	}

	@Override
	public void visualizationAdded(VisInfo visInfo) {
		/*
		 * Need demo controls?
		 */
		VisualizationComponent visComp = visInfo.getVisualizationComponent();
		if (visComp != null
				&& visComp instanceof InteractiveVisualizationComponent) {
			JComponent sidebar = ((InteractiveVisualizationComponent) visComp)
					.getSidebarComponent();
			if (sidebar != null) {
				splitPane.setRightComponent(sidebar);
				splitPane.setDividerLocation(0.8);
				splitPane.validate();
			}
		}
	}

	@Override
	public void visualizationRemoved(VisInfo visInfo) {
		// do not care
	}

	private void buildSimControls() {
		simControls = new SimControlPanel(visManager);
		getContentPane().add(BorderLayout.NORTH, simControls);
		getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_P, 0), "pause");
		Action pauseAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				getSimControls().togglePlayPause();
			}
		};
		getRootPane().getActionMap().put("pause", pauseAction);
	}

	public SimControlPanel getSimControls() {
		return simControls;
	}

	@Override
	public void addedComponent(TopologyComponent comp) {
		worldPanel.addTopologyComponent(comp);
	}

	@Override
	public void changedWaypointModel(WaypointModel model) {
		if (weakWaypointComponentVis != null) {
			this.visManager.removeComponent(weakWaypointComponentVis);
			this.visManager.removeComponent(strongWaypointComponentVis);
			// worldPanel.remove(waypointComponentVis);
		}

		if (model == null)
			return;
		weakWaypointComponentVis = new WeakWaypointComponentVis(model);
		strongWaypointComponentVis = new StrongWaypointComponentVis(model);
		this.visManager.addComponent("Streets", 0, weakWaypointComponentVis);
		this.visManager.addComponent("Waypoints", 0,
				strongWaypointComponentVis);
		// worldPanel.add(waypointComponentVis);
	}

	@Override
	public void changedObstacleModel(ObstacleModel model) {
		if (obstacleComponentVis != null)
			this.visManager.removeComponent(obstacleComponentVis);
		// worldPanel.remove(obstacleComponentVis);

		if (model == null)
			return;
		obstacleComponentVis = new ObstacleComponentVis(model);
		this.visManager.addComponent("Buildings", 0, obstacleComponentVis);
		// worldPanel.add(obstacleComponentVis);
	}

	/*
	 * @Override public void addedObstacle(Obstacle obstacle) { if (obstacle
	 * instanceof PolygonObstacle) { ObstacleComponentVis obsVis = new
	 * ObstacleComponentVis( ((PolygonObstacle) obstacle).getAwtPolygon(),
	 * obstacle); worldPanel.add(obsVis); } }
	 */

	@Override
	public void onLocationChanged(Host host, Location location) {
		// The NodeInformation objects are registered as listener.
	}

	@Override
	public Link getBestNextLink(MacAddress source, MacAddress lastHop,
			MacAddress currentHop, MacAddress destination) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Link getLinkBetween(MacAddress source, MacAddress destination) {
		throw new UnsupportedOperationException();
	}

	@Override
	public MacLayer getMac(MacAddress address) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<MacLayer> getAllMacs() {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<MacAddress> getNeighbors(MacAddress address) {
		throw new UnsupportedOperationException();
	}

	@Override
	public PhyType getPhyType() {
		return null;
	}

	/**
	 * Mini-Container for per-node information
	 * 
	 * @author bjoern
	 * @version 1.0, Jul 5, 2016
	 */
	private static class NodeInformation implements LocationListener {

		public Point2D position;

		public boolean clicked = false;

		public final long hostId;

		public NodeInformation(TopologyComponent comp) {
			this.hostId = comp.getHost().getId().value();
			this.position = comp.getRealPosition().asPoint();
		}

		@Override
		public void onLocationChanged(Host host, Location location) {
			this.position.setLocation(
					VisualizationInjector.scaleValue(location.getLongitude()),
					VisualizationInjector.scaleValue(location.getLatitude()));
		}

	}

	/**
	 * The world
	 * 
	 * @author Bjoern Richerzhagen
	 * @version 1.0, 19.03.2012
	 */
	protected class WorldPanel extends JLayeredPane {

		protected HashMap<INodeID, NodeInformation> nodeInformation = new HashMap<>();

		protected final static int PADDING = 16;

		private static final long serialVersionUID = -3023020559483652110L;

		public WorldPanel() {
			this.setLayout(null);
			this.setDoubleBuffered(true);
			this.addMouseListener(new MouseAdapter() {

				/**
				 * Stores the mouse position, if the mouse button is pressed
				 */
				@Override
				public void mousePressed(MouseEvent e) {
					boolean turnedSomeoneOff = false;
					for (NodeInformation node : nodeInformation.values()) {
						// Make it easier to turn things off.
						if (node.clicked && node.position
								.distance(e.getPoint()) < PADDING + 2) {
							node.clicked = !node.clicked;
							VisualizationInjector
									.notifyInteractionListenersOnClick(
											node.hostId, node.clicked);
							turnedSomeoneOff = true;
						}
					}
					if (!turnedSomeoneOff) {
						// Turn sth on (limit to one node)
						for (NodeInformation node : nodeInformation.values()) {
							if (!node.clicked && node.position
									.distance(e.getPoint()) < PADDING) {
								node.clicked = !node.clicked;
								VisualizationInjector
										.notifyInteractionListenersOnClick(
												node.hostId, node.clicked);
								break;
							}
						}
					}
				}

			});
		}

		public void addTopologyComponent(TopologyComponent comp) {
			if (!nodeInformation.containsKey(comp.getHost().getId())) {
				NodeInformation tVis = new NodeInformation(comp);
				comp.requestLocationUpdates(null, tVis);
				nodeInformation.put(comp.getHost().getId(), tVis);
			}
		}

		@Override
		public void invalidate() {
			super.invalidate();
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
					RenderingHints.VALUE_ANTIALIAS_ON);
			paintPlayingFieldBorder(g2);
			paintNodes(g2);
			super.paintComponent(g);
		}

		private void paintNodes(Graphics2D g2) {
			for (NodeInformation node : nodeInformation.values()) {
				if (node.clicked) {
					g2.setColor(Color.MAGENTA);
					g2.fillOval((int) node.position.getX() - PADDING,
							(int) node.position.getY() - PADDING,
							PADDING * 2 + 1, PADDING * 2 + 1);
				}
			}
		}

		private void paintPlayingFieldBorder(Graphics2D g2) {
			g2.setColor(Color.DARK_GRAY);
			g2.fillRect(0, 0, getWidth(), getHeight());
			g2.setColor(Color.WHITE);
			g2.fillRect(0, 0, VisualizationInjector.getWorldX(),
					VisualizationInjector.getWorldY());
			g2.setPaint(Color.MAGENTA);
			g2.setStroke(new BasicStroke(2));
			g2.drawLine(0, 0, VisualizationInjector.scaleValue(100), 0);
		}

	}

	public void notifyInteractionListenersOnClick(long hostid,
			boolean isActive) {
		VisualizationInjector.notifyInteractionListenersOnClick(hostid,
				isActive);
	}

	@Deprecated
	public void setPhy(String phy) {
		// legacy
		System.err.println(
				"The VisualizationTopologyView does no longer require PHY to be configured!");
	}

	/**
	 * This component allows the injection of JComponents into the visualization
	 * from anywhere in the simulator. It is marked deprecated due to its hacky
	 * nature. Integrate the visualization into the topology view instead.
	 * 
	 * TODO: Add checks for every component or plotting view if a graphical
	 * environment is present. This will allow calls to the
	 * VisualizationInjector on servers without failing. e.g. supply dummy a
	 * PlottingView or add additional checks to the plotting view for such
	 * cases.
	 * 
	 * @author Fabio Zöllner
	 * @version 1.0, 03.04.2012
	 */
	public static class VisualizationInjector {
		protected static WorldPanel worldPanel;

		public static ComponentVisManager visManager;

		protected static ConcurrentLinkedQueue<VisInfo> components = new ConcurrentLinkedQueue<>();

		protected static Map<String, VisInfo> nameToVisInfoMap = Maps
				.newConcurrentMap();

		protected static ConcurrentLinkedQueue<DisplayString> displayStrings = new ConcurrentLinkedQueue<>();

		protected static List<MouseClickListener> mouseListeners = new CopyOnWriteArrayList<>();

		protected static VisualizationTopologyView view;

		protected static ConcurrentLinkedQueue<NodeVisInteractionListener> interactionListeners = new ConcurrentLinkedQueue<>();

		protected static long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK
				+ AWTEvent.MOUSE_EVENT_MASK;

		private static int WORLD_X = 0;

		private static int WORLD_Y = 0;

		private static double SCALE = 1;

		protected static Map<String, PlottingView> plottingViews = Maps
				.newHashMap();

		public static PlottingView createPlottingView(String name) {
			PlottingView view = new PlottingView(name);
			plottingViews.put(name, view);
			return view;
		}

		public static PlottingView createPlottingView(String name, int x,
				int y) {
			PlottingView view = new PlottingView(name);
			plottingViews.put(name, view);

			view.setBounds(x, y, view.getWidth(), view.getHeight());
			view.setVisible(true);

			return view;
		}

		public static PlottingView getPlottingView(String name) {
			return plottingViews.get(name);
		}

		protected static void notifyInteractionListenersOnClick(long hostid,
				boolean isActive) {
			for (NodeVisInteractionListener listener : VisualizationInjector.interactionListeners) {
				listener.onHostClick(hostid, isActive);
			}
		}

		protected static void setVariables(VisualizationTopologyView view,
				ComponentVisManager visManager, WorldPanel worldPanel,
				int WORLD_X, int WORLD_Y) {
			VisualizationInjector.view = view;
			VisualizationInjector.worldPanel = worldPanel;
			VisualizationInjector.visManager = visManager;
			VisualizationInjector.WORLD_X = WORLD_X;
			VisualizationInjector.WORLD_Y = WORLD_Y;

			for (VisInfo visInfo : components) {
				visManager.addComponent(visInfo);
				// worldPanel.add(comp);
			}

			worldPanel.add(new DrawDisplayStrings(WORLD_X, WORLD_Y));

			setupAWTEventListener();
		}

		public static VisualizationTopologyView getTopologyView() {
			return view;
		}

		private static void setupAWTEventListener() {
			Toolkit tk = Toolkit.getDefaultToolkit();

			tk.addAWTEventListener(new AWTEventListener() {
				@Override
				public void eventDispatched(AWTEvent e) {
					if (e instanceof MouseEvent) {
						MouseEvent me = (MouseEvent) e;
						if (me.getID() == MouseEvent.MOUSE_CLICKED) {
							// Another dirty hack until we have another graphic
							// drawing system
							if (((JFrame) worldPanel.getTopLevelAncestor())
									.isActive()) {
								int x = me.getXOnScreen();
								int y = me.getYOnScreen();

								if (x > (int) worldPanel.getLocationOnScreen()
										.getX()
										&& y > (int) worldPanel
												.getLocationOnScreen().getY()
										&& x < (int) worldPanel
												.getLocationOnScreen().getX()
												+ worldPanel.getWidth()
										&& y < (int) worldPanel
												.getLocationOnScreen().getX()
												+ worldPanel.getHeight()) {

									x -= (int) worldPanel.getLocationOnScreen()
											.getX();
									y -= (int) worldPanel.getLocationOnScreen()
											.getY();

									for (MouseClickListener l : mouseListeners) {
										l.mouseClicked(x, y);
									}
								}

							}
						}
					}
				}
			}, eventMask);
		}

		public static interface MouseClickListener {
			public void mouseClicked(int x, int y);
		}

		public static JComponent getWorldPanel() {
			return worldPanel;
		}

		public static void addMouseListener(MouseClickListener listener) {
			mouseListeners.add(listener);
		}

		public static void removeMouseListener(MouseClickListener listener) {
			mouseListeners.remove(listener);
		}

		/**
		 * Listener is informed, whenever the user clicks on a node
		 * 
		 * @param listener
		 */
		public static void addInteractionListener(
				NodeVisInteractionListener listener) {
			interactionListeners.add(listener);
		}

		public static void removeInteractionListener(
				NodeVisInteractionListener listener) {
			interactionListeners.remove(listener);
		}

		protected static class DrawDisplayStrings extends JComponent {
			public DrawDisplayStrings(int WORLD_X, int WORLD_Y) {
				super();

				setBounds(0, 0, WORLD_X, WORLD_Y);
				setOpaque(false);
				setVisible(true);
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g;

				g2.setColor(Color.BLACK);
				g2.setFont(new Font("SansSerif", Font.PLAIN, 9));

				int height = 10;
				for (DisplayString str : displayStrings) {
					g2.drawString(str.getDisplayString(), 0, height);
					height += 10;
				}
			}
		}

		/**
		 * Returns a SCALED x-length of the world size, only to be used by
		 * visualizations.
		 * 
		 * @return
		 */
		public static int getWorldX() {
			// return WORLD_X;
			return (int) (WORLD_X * getScale());
		}

		/**
		 * Returns a SCALED y-length of the world size, only to be used by
		 * visualizations.
		 * 
		 * @return
		 */
		public static int getWorldY() {
			// return WORLD_Y;
			return (int) (WORLD_Y * getScale());
		}

		/**
		 * Still needed by OSM-loader (Fabio), however: should not be used
		 * anymore. The coordinates should be final.
		 * 
		 * @param x
		 * @param y
		 */
		@Deprecated
		public static void setWorldCoordinates(int x, int y) {
			WORLD_X = x;
			WORLD_Y = y;
		}

		public static double getScale() {
			return SCALE;
		}

		public static void setScale(double scale) {
			SCALE = scale;
		}

		public static int scaleValue(double value) {
			return (int) (value * getScale());
		}

		/**
		 * @deprecated This method was present in the previous API of the
		 *             {@link VisualizationTopologyView}. Use
		 *             {@link #injectComponent(VisualizationComponent)} instead
		 *             (for instance).
		 */
		@Deprecated
		public static void injectControl(JComponent component) {
			injectComponent(new VisualizationComponent() {

				@Override
				public boolean isHidden() {
					return false;
				}

				@Override
				public String getDisplayName() {
					return component.getName();
				}

				@Override
				public JMenu getCustomMenu() {
					return null;
				}

				@Override
				public JComponent getComponent() {
					return component;
				}
			});
		}

		/**
		 * Recommended way to add a custom visualization layer.
		 * 
		 * @param comp
		 */
		public static void injectComponent(VisualizationComponent comp) {
			visManager.addComponent(comp);
		}

		/**
		 * @deprecated use injectComponent with a {@link VisualizationComponent}
		 *             instead
		 */
		@Deprecated
		public static void injectComponent(String name, int priority,
				JComponent component) {
			injectComponent(name, priority, component, true);
		}

		/**
		 * @deprecated use injectComponent with a {@link VisualizationComponent}
		 *             instead
		 */
		@Deprecated
		public static void injectComponent(String name, int priority,
				JComponent component, boolean active) {
			injectComponent(name, priority, component, active, true);
		}

		/**
		 * @deprecated use injectComponent with a {@link VisualizationComponent}
		 *             instead
		 */
		@Deprecated
		public static void injectComponent(String name, int priority,
				JComponent component, boolean active, boolean showInList) {
			VisInfo visInfo = new VisInfo(name, priority, component);
			visInfo.setActiveByDefault(active);
			visInfo.setShowInList(showInList);

			components.add(visInfo);
			nameToVisInfoMap.put(name, visInfo);

			if (visManager != null) {
				visManager.addComponent(visInfo);
			}
		}

		/**
		 * @deprecated use removeInjectedComponent with a
		 *             {@link VisualizationComponent} instead
		 */
		@Deprecated
		public static void removeInjectedComponent(String name) {
			if (nameToVisInfoMap.containsKey(name)) {
				VisInfo visInfo = nameToVisInfoMap.get(name);
				nameToVisInfoMap.remove(name);
				components.remove(visInfo);
				visManager.removeComponent(name);
			}
		}

		public static void invalidate() {
			worldPanel.invalidate();
		}

		public static void addDisplayString(DisplayString str) {
			displayStrings.add(str);
		}

		public static void removeDisplayString(DisplayString str) {
			displayStrings.remove(str);
		}

		public static interface DisplayString {
			public String getDisplayString();
		}
	}

	@Override
	public Location getPosition(MacAddress address) {
		throw new NotSupportedException();
	}

	@Override
	public double getDistance(MacAddress addressA, MacAddress addressB) {
		throw new NotSupportedException();
	}

	@Override
	public boolean hasRealLinkLayer() {
		throw new NotSupportedException();
	}

}
