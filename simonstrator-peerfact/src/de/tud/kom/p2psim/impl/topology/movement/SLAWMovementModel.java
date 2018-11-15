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

package de.tud.kom.p2psim.impl.topology.movement;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.WeakHashMap;

import javax.swing.JComponent;

import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.waypoints.WaypointModel;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector;
import de.tud.kom.p2psim.impl.topology.views.VisualizationTopologyView.VisualizationInjector.DisplayString;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.StrongWaypoint;
import de.tud.kom.p2psim.impl.topology.waypoints.graph.Waypoint;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * The SLAW Movement Model is based on the implementation in
 * BonnMotion Copyright (C) 2002-2010 University of Bonn
 * by Zia-Ul-Huda, Gufron Atokhojaev and Florian Schmitt
 * 
 * @author Fabio Zöllner
 * @version 1.0, 09.04.2012
 */
public class SLAWMovementModel extends AbstractWaypointMovementModel {
	
	private static final int powerlaw_step = 1;
	private static int levy_scale_factor = 1;
	private static int powerlaw_mode = 1;

	protected Random rand = Randoms
			.getRandom(SLAWMovementModel.class);

	protected Random xorShiftRandom = new XorShiftRandom(rand.nextLong());

	protected double beta = 1;
	protected long minpause = 10;
	protected long maxpause = 50;
	protected double dist_alpha = 3;
	protected double cluster_range;
	protected int cluster_ratio = 3;
	protected int waypoint_ratio = 3;
	protected Cluster[] clusters;

	protected WeakHashMap<SimLocationActuator, SLAWMovementInformation> movementInformation;
	
	// TODO: minpause and maxpause are currently in microseconds... should be
	// changed to minutes
	@XMLConfigurableConstructor({"worldX", "worldY", "minpause", "maxpause", "beta", "distAlpha", "clusterRange", "clusterRatio", "waypointRatio" })
	public SLAWMovementModel(double worldX, double worldY, long minpause, long maxpause, double beta, double distAlpha, double clusterRange, int clusterRatio, int waypointRatio) {
		super(worldX, worldY);
		
		this.minpause = minpause;
		this.maxpause = maxpause;
		this.dist_alpha = distAlpha;
		this.cluster_range = clusterRange;
		this.cluster_ratio = clusterRatio;
		this.waypoint_ratio = waypointRatio;
		
		movementInformation = new WeakHashMap<SimLocationActuator, SLAWMovementModel.SLAWMovementInformation>();
	}

	@Override
	public void nextPosition(SimLocationActuator node) {
		// These variables have values same as in the matlab implementation of
		// SLAW model by Seongik Hong, NCSU, US (3/10/2009)
	
		
		if (waypointModel == null) {
			throw new ConfigurationException("SLAWMovementModel requires a valid waypoint model which hasn't been provided, cannot execute");
		}

		SLAWMovementInformation mi = movementInformation.get(node);

		if (mi == null) {
			//System.err.println("Creating new movement information for node " + node.toString());
			mi = new SLAWMovementInformation();
			movementInformation.put(node, mi);
		}

		// log.debug("Selecting new destination for node " + mi.nodeId);

		// get random clusters and waypoints
		if (mi.clts == null)
			mi.clts = make_selection(clusters, null, false);

		// get random clusters and waypoints
		if (mi.wlist == null)
			mi.wlist = get_waypoint_list(mi.clts);

		// random source node
		if (mi.srcIndex == -1) {
			int old = mi.srcIndex;
			mi.srcIndex = (int) Math
					.floor(randomNextDouble() * mi.wlist.length);
			
			//System.err.println("mi.srcIndex was " + old + " but is now " + mi.srcIndex);
			
			// Set the initial position to the selected waypoint
			nextDestination(node, mi.wlist[mi.srcIndex].pos, 0);
			//System.err.println("Default case? 0");
			return;
		}
		
		if (mi.dstIndex != -1) {
			mi.srcIndex = mi.dstIndex;
		}

		int count = 0;

		PositionVector source = new PositionVector(node.getRealPosition()
				.getX(), node.getRealPosition().getY());

		mi.wlist[mi.srcIndex].is_visited = true;

		// get list of not visited locations
		for (int i = 0; i < mi.wlist.length; i++) {
			if (!mi.wlist[i].is_visited) {
				count++;
			}
		}

		// if all waypoints are visited then select new clusters and
		// waypoints. Destructive mode of original SLAW matlab
		// implementation by Seongik Hong, NCSU, US (3/10/2009)
		while (count == 0) {
			mi.clts = make_selection(clusters, mi.clts, true);
			mi.wlist = get_waypoint_list(mi.clts);
			for (int i = 0; i < mi.wlist.length; i++) {
				if (!mi.wlist[i].is_visited) {
					if (source.distanceTo(mi.wlist[i].pos) != 0.0) {
						count++;
					} else {
						mi.wlist[i].is_visited = true;
					}
				}
			}
		}

		ClusterMember[] not_visited = new ClusterMember[count];
		count = 0;
		for (int i = 0; i < mi.wlist.length; i++) {
			if (!mi.wlist[i].is_visited) {
				not_visited[count++] = mi.wlist[i];
			}
		}

		// get distance from source to all remaining waypoints
		double[] dist = new double[not_visited.length];
		for (int i = 0; i < not_visited.length; i++) {
			dist[i] = source.distanceTo(not_visited[i].pos);
		}

		double[] weights = new double[not_visited.length];
		// cumulative sum of distance weights
		for (int i = 0; i < weights.length; i++) {
			weights[i] = 0;
			for (int j = 0; j <= i; j++) {
				weights[i] += 1 / Math.pow(dist[j], this.dist_alpha);
			}
		}

		for (int i = 0; i < weights.length; i++) {
			weights[i] /= weights[weights.length - 1];
		}

		double r = randomNextDouble();

		int index;
		for (index = 0; index < weights.length; index++) {
			if (r < weights[index]) {
				break;
			}
		}

		// select the next destination
		for (int i = 0; i < mi.wlist.length; i++) {
			if (mi.wlist[i].pos.equals(not_visited[index].pos)) {
				mi.dstIndex = i;
				double pauseTime = random_powerlaw(powerlaw_step,
						levy_scale_factor, powerlaw_mode)[0];
				//System.err.println("Next pause time is " + pauseTime);
				nextDestination(
						node,
						mi.wlist[mi.dstIndex].pos, (long) pauseTime
						);
				break;
			}
		}

		// change destination to next source
		// mi.srcIndex = mi.dstIndex;
	}

	/**
	 * returns aggregated list of cluster members from all passed clusters
	 * 
	 * @param clusters
	 *            clusters list
	 * @return array of ClusterMember type
	 */
	protected ClusterMember[] get_waypoint_list(Cluster[] clusters) {
		ArrayList<ClusterMember> result = new ArrayList<ClusterMember>();

		for (Cluster cluster : clusters) {
			for (ClusterMember clustermember : cluster.members) {
				result.add(clustermember);
			}
		}

		return result.toArray(new ClusterMember[0]);
	}

	/**
	 * makes the selection of new clusters and waypoints from passed clusters
	 * 
	 * @param clusters
	 *            array of clusters to make selection from
	 * @param change_one
	 *            changes only one of the clusters randomly and then selects new
	 *            waypoints from all clusters
	 * @return array of selected clusters
	 */
	protected Cluster[] make_selection(Cluster[] clusters, Cluster[] cur_list,
			boolean change_one) {
		ArrayList<Integer> cluster_selection;

		if (!change_one) {
			// Number of clusters to select
			int num_clusters = (int) Math.ceil((double) clusters.length
					/ (double) cluster_ratio);

			cluster_selection = new ArrayList<Integer>(num_clusters);
			for (int i = 0; i < num_clusters; i++) {
				cluster_selection.add(i, -1);
			}

			int[] total_list = new int[waypointModel.getNumberOfWaypoints(StrongWaypoint.class)];
			int counter = 0;
			// probability array
			for (int i = 0; i < clusters.length; i++) {
				for (int j = 0; j < clusters[i].members.length; j++) {
					total_list[counter++] = clusters[i].index;
				}
			}

			// select clusters randomly with weights
			int t = total_list[(int) Math.floor(this.randomNextDouble()
					* waypointModel.getNumberOfWaypoints(StrongWaypoint.class))];

			for (int i = 0; i < cluster_selection.size(); i++) {
				while (cluster_selection.contains(t)) {
					t = total_list[(int) Math.floor(this.randomNextDouble()
							* waypointModel.getNumberOfWaypoints(StrongWaypoint.class))];
				}
				cluster_selection.set(i, t);
			}
		} else {// just need to change one randomly
			cluster_selection = new ArrayList<Integer>(cur_list.length);
			for (Cluster cluster : cur_list) {
				cluster_selection.add(cluster.index);
			}
		}

		// change one cluster without weight consideration
		cluster_selection = change_one_random(cluster_selection,
				clusters.length);

		// select waypoints from selected clusters.
		Cluster[] result = new Cluster[cluster_selection.size()];
		double numberOfWaypoints;
		Cluster cluster_iterator = null;

		for (int i = 0; i < cluster_selection.size(); i++) {
			// find Cluster object in clusters array
			for (int j = 0; j < clusters.length; j++) {
				if (cluster_selection.get(i) == clusters[j].index) {
					cluster_iterator = clusters[j];
					break;
				}
			}

			result[i] = new Cluster(cluster_iterator.index);
			numberOfWaypoints = (double) cluster_iterator.members.length
					/ (double) this.waypoint_ratio;
			int[] waypoint_selection;

			if (numberOfWaypoints < 1) {
				waypoint_selection = select_uniformly(
						cluster_iterator.members.length, 1);
			} else {
				if (this.randomNextDouble() < numberOfWaypoints % 1) {
					waypoint_selection = select_uniformly(
							cluster_iterator.members.length,
							(int) (Math.floor(numberOfWaypoints) + 1));
				} else {
					waypoint_selection = select_uniformly(
							cluster_iterator.members.length,
							(int) Math.floor(numberOfWaypoints));
				}
			}

			result[i].members = new ClusterMember[waypoint_selection.length];
			for (int j = 0; j < waypoint_selection.length; j++) {
				result[i].members[j] = cluster_iterator.members[waypoint_selection[j]]
						.clone();
			}
		}
		return result;
	}

	/**
	 * changes one of the numbers randomly from the passed array. The new
	 * changed number is in range [1,n]
	 * 
	 * @param list
	 *            array of integers
	 * @param n
	 *            range of numbers
	 * @return array of integers with one element changed randomly
	 */
	protected ArrayList<Integer> change_one_random(ArrayList<Integer> list,
			int n) {
		int index = (int) Math.floor(this.randomNextDouble() * list.size());
		int value = (int) Math.floor(this.randomNextDouble() * n) + 1;

		while (list.contains(value)) {
			value = (int) Math.floor(this.randomNextDouble() * n) + 1;
		}
		list.set(index, value);

		return list;
	}

	/**
	 * selects k numbers out of n uniformly
	 * 
	 * @param n
	 * @param k
	 * @return array of k integers
	 */
	protected int[] select_uniformly(int n, int k) {
		if (k > n)
			throw new RuntimeException(
					"SLAW.select_uniformaly(): value of k must not be larger than n.");

		int t;
		int[] list = new int[k];
		for (int i = 0; i < k; i++) {
			list[i] = -1;
		}
		boolean is_in;
		int count = 0;
		while (count < k) {
			is_in = false;
			t = (int) Math.floor(this.randomNextDouble() * n);
			for (int i = 0; i < list.length; i++) {
				if (list[i] == t) {
					is_in = true;
					break;
				}
			}
			if (!is_in) {
				list[count++] = t;
			}
		}
		return list;
	}

	/**
	 * Generates random values from power-law distribution.
	 * 
	 * @param powerlaw_step
	 *            the total numbers to generate. Returns an array of this size.
	 * @param levy_scale_factor
	 *            levy scaling factor of distribution
	 * @param powerlaw_mode
	 *            1: stabrnd 2: reverse computation
	 * @return double array of powerlaw_step length
	 **/
	protected double[] random_powerlaw(int powerlaw_step,
			int levy_scale_factor, int powerlaw_mode) {
		double[] result = new double[powerlaw_step];

		for (int xi = 0; xi < powerlaw_step;) {
			if (powerlaw_mode == 1) { // stabrnd
				double[] stabrnd_result = stabrnd(0, levy_scale_factor, 0,
						powerlaw_step);

				ArrayList<Double> temp = new ArrayList<Double>();

				for (int i = 0; i < stabrnd_result.length; i++) {
					if (stabrnd_result[i] > this.minpause
							&& stabrnd_result[i] < this.maxpause) {
						temp.add(new Double(stabrnd_result[i]));
					}
				}

				if (temp.size() > 0) {
					for (Double d : temp) {
						result[xi++] = d;
						if (xi > powerlaw_step)
							break;
					}
				}
			} else if (powerlaw_mode == 2) { // reverse computation
				double temp = Math.pow(randomNextDouble(),
						1 / (1 - (this.beta + 1))) * this.minpause;
				if (temp < this.maxpause) {
					result[xi++] = temp;
				}
			}
		}
		return result;
	}

	/**
	 * Returns array of randomly generated n numbers based on the method of J.M.
	 * Chambers, C.L. Mallows and B.W. Stuck,
	 * "A Method for Simulating Stable Random Variables," JASA 71 (1976): 340-4.
	 * 
	 * @param b
	 *            beta factor
	 * @param levy_scale_factor
	 * @param delta
	 * @param n
	 *            count of random numbers to generate
	 * @return double array of n length
	 */
	private double[] stabrnd(double b, int levy_scale_factor, double delta,
			int n) {
		if (this.beta < .1 || this.beta > 2) {
			throw new RuntimeException(
					"SLAW.stabrnd(): Beta value must be in [.1,2]");
		}

		if (Math.abs(b) > 1) {
			throw new RuntimeException(
					"SLAW.stabrnd(): local beta value must be in [-1,1]");
		}

		// Generate exponential w and uniform phi
		double[] w = new double[n];
		double[] x = new double[n];
		double[] phi = new double[n];
		for (int i = 0; i < n; i++) {
			w[i] = -Math.log(randomNextDouble());
			phi[i] = (randomNextDouble() - 0.5) * Math.PI;
		}

		// Gaussian case (Box-Muller)
		if (this.beta == 2) {
			for (int i = 0; i < n; i++) {
				x[i] = 2 * Math.sqrt(w[i]) * Math.sin(phi[i]);
				x[i] = delta + levy_scale_factor * x[i];
			}
		} else if (b == 0) { // Symmetrical cases
			if (this.beta == 1) { // Cauchy case
				for (int i = 0; i < n; i++) {
					x[i] = Math.tan(phi[i]);
				}
			} else {
				for (int i = 0; i < n; i++) {
					x[i] = Math.pow(Math.cos((1 - this.beta) * phi[i]) / w[i],
							1 / this.beta - 1)
							* Math.sin(this.beta * phi[i])
							/ Math.pow(Math.cos(phi[i]), 1 / this.beta);
				}
			}
		} else { // General cases
			double cosphi, zeta, aphi, a1phi, bphi;

			if (Math.abs(this.beta - 1) > 0.00000001) {
				for (int i = 0; i < n; i++) {
					cosphi = Math.cos(phi[i]);
					zeta = b * Math.tan(Math.PI * this.beta / 2);
					aphi = this.beta * phi[i];
					a1phi = (1 - this.beta) * phi[i];
					x[i] = (Math.sin(aphi) + zeta * Math.cos(aphi))
							/ cosphi
							* Math.pow(
									(Math.cos(a1phi) + zeta * Math.sin(a1phi))
											/ (w[i] * cosphi), (1 - this.beta)
											/ this.beta);
				}
			} else {
				for (int i = 0; i < n; i++) {
					cosphi = Math.cos(phi[i]);
					bphi = Math.PI / 2 + b * phi[i];
					x[i] = 2
							/ Math.PI
							* (bphi * Math.tan(phi[i]) - b
									* Math.log((Math.PI / 2) * w[i] * cosphi
											/ bphi));
				}
				if (this.beta != 1) {
					for (int i = 0; i < n; i++) {
						x[i] += b * Math.tan(Math.PI * this.beta / 2);
					}
				}
			}

			for (int i = 0; i < n; i++) {
				x[i] = delta + levy_scale_factor * x[i];
			}
		}
		return x;
	}

	protected double randomNextDouble(final double value) {
		return (randomNextDouble() * value);
	}

	protected double randomNextDouble() {
		return xorShiftRandom.nextDouble();
		//return rand.nextDouble();
	}
	
	private static final class Cluster {
		public ClusterMember[] members;

		public int index = -1;

		public Cluster(int _index) {
			index = _index;
		}

		public Cluster(int _index, ClusterMember[] _members) {
			index = _index;
			members = _members;
		}
		
		public String toString() {
			return "Cluster[index: " + index + "]";
		}
	}

	private static final class ClusterMember {
		public int cluster_index = -1;

		public PositionVector pos;

		public boolean is_visited = false;

		public ClusterMember(int _cluster_index, PositionVector _pos,
				boolean _is_visited) {
			this.cluster_index = _cluster_index;
			this.is_visited = _is_visited;
			this.pos = _pos;
		}

		public ClusterMember clone() {
			return new ClusterMember(this.cluster_index, this.pos,
					this.is_visited);
		}
	}

	private static class SLAWMovementInformation {
		public SLAWMovementInformation() {
			//
		}

		public int srcIndex = -1;

		public int dstIndex = -1;

		public Cluster[] clts = null;

		public ClusterMember[] wlist = null;
	}
	
	@Configure()
	@After(required={WaypointModel.class})
	public void _verifyConfig() {
		/*
		 * FIXME: get rid of this method!
		 */
		Collection<Waypoint> strongWaypoints = waypointModel.getWaypoints(StrongWaypoint.class);
		
		if (strongWaypoints == null)
			throw new ConfigurationException("The configured waypoint model hasn't generated any strong waypoints that are required by the SLAWMovementModel.");
		
		clusters = generate_clusters(waypointModel);
		if (clusters.length <= 2) {
			throw new ConfigurationException("The SLAW movement model could only generate 1 cluster, please adjust the cluster range parameter");
		}
		
		VisualizationInjector.injectComponent("Clusters", -1, new SLAWClusterOverlay(clusters, waypointModel), false);

		VisualizationInjector.addDisplayString(new DisplayString() {
			@Override
			public String getDisplayString() {

				int clusterWps = 0;
				for (Cluster c : clusters) {
					clusterWps += c.members.length;
				}
				
				return "SLAW[Clusters: " + clusters.length + ", WPs in clusters: " + clusterWps + ", Avg per cluster: " + (clusterWps / clusters.length) + "]";
			}
		});
	}

	@Override
	public void setWaypointModel(WaypointModel waypointModel) {
		super.setWaypointModel(waypointModel);
	}

    /**
     * generates clusters
     * 
     * @param waypoints
     *            list of waypoint  s
     * @return array of clusters
     */
    protected Cluster[] generate_clusters(WaypointModel model) {
        Vector<PositionVector> all_points = new Vector<PositionVector>();
        Vector<PositionVector> new_points = new Vector<PositionVector>();
        Vector<PositionVector> members = new Vector<PositionVector>();

        Vector<Cluster> clusters = new Vector<Cluster>();
        Vector<ClusterMember> cluster_members = new Vector<ClusterMember>();

        Waypoint[] waypoints = new Waypoint[]{};
        Collection<Waypoint> waypointList = model.getWaypoints(StrongWaypoint.class);
        
        if (waypointList == null)
        	throw new ConfigurationException("The configured waypoint model didn't generate any strong waypoints");
        
        waypoints = waypointList.toArray(waypoints);
        
        for (int i = 0; i < waypoints.length; i++) {
            all_points.add(waypoints[i].getPosition());
        }

        PositionVector init_pos = null;
        int cluster_count = 0;

        while (!all_points.isEmpty()) {
            if (init_pos == null) {
                init_pos = all_points.firstElement();
                all_points.remove(0);
                members.add(init_pos);
            }

            for (int i = 0; i < all_points.size(); i++) {
                PositionVector new_pos = all_points.elementAt(i);

				if (init_pos.distanceTo(new_pos) <= this.cluster_range) {
                    new_points.add(new_pos);
                    members.add(new_pos);
                    all_points.remove(i--);
                }
            }// for all_points

            if (!new_points.isEmpty() && !all_points.isEmpty()) {
                init_pos = new_points.firstElement();
                new_points.remove(0);
            }
            else {
                for (int i = 0; i < members.size(); i++) {
                    cluster_members.add(new ClusterMember(cluster_count, members.elementAt(i), false));
                }

                clusters.add(new Cluster(++cluster_count, cluster_members.toArray(new ClusterMember[0])));

                cluster_members.clear();
                new_points.clear();
                members.clear();
                init_pos = null;
            }

        }// while all_points

        return clusters.toArray(new Cluster[0]);
    }
    
    private class SLAWClusterOverlay extends JComponent {
    	private Map<Cluster, Color> clusterColors = Maps.newHashMap();
		private Cluster[] clusters;
		private Random rnd = new Random(System.nanoTime());

		private WaypointModel model;

		public SLAWClusterOverlay(Cluster[] clusters, WaypointModel model) {
			super();

			this.clusters = clusters;
			this.model = model;

			PositionVector dimension = model.getMap().getDimensions();

			setBounds(0, 0, (int) dimension.getX(), (int) dimension.getY());
			setOpaque(false);
			setVisible(true);
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			Graphics2D g2 = (Graphics2D) g;

			g2.setFont(new Font("SansSerif", Font.PLAIN, 9));
			
			for (Cluster c : clusters) {
				PositionVector lastPos = null;
				
				Color randomColor = clusterColors.get(c);
				if (randomColor == null) {
					final float hue = rnd.nextFloat();
					// Saturation between 0.1 and 0.3
					final float saturation = (rnd.nextInt(2000) + 1000) / 10000f;
					final float luminance = 0.9f;
					randomColor = Color.getHSBColor(hue, saturation, luminance);
					randomColor = new Color(rnd.nextFloat(), rnd.nextFloat(), rnd.nextFloat());
					clusterColors.put(c, randomColor);
				}
				
				g2.setColor(randomColor);
				
				for (ClusterMember m : c.members) {
					if (lastPos == null) lastPos = m.pos;
					
					g2.drawLine((int)lastPos.getX(), (int)lastPos.getY(), (int)m.pos.getX(), (int)m.pos.getY());
					
					lastPos = m.pos;
				}
			}
		}
	}
}
