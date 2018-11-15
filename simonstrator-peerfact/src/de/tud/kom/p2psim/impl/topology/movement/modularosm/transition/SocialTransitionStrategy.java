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

package de.tud.kom.p2psim.impl.topology.movement.modularosm.transition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.topology.Topology;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.api.topology.social.SocialView;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modular.transition.TransitionStrategy;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction.AttractionPoint;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * This is a {@link TransitionStrategy} for the Social Case. It will be try to
 * build groups based on the {@link SocialView} information. For this, it tries
 * to assignment the given objects to the given
 * {@link AttractionPoint}s. For the {@link SocialView}, it is required a
 * {@link #socialId}, to find the right {@link SocialView}.
 * 
 * <br>
 * 
 * The Strategy has the parameter of {@link #socialFactor},
 * {@link #minPauseTime} and {@link #maxPauseTime}. The socialFactor should be a
 * value between 0 and 1. It gives the probability for a social based transition
 * or the transition to a random {@link AttractionPoint}. If the social based
 * transition is selected, then will be used a scoring to find the right
 * {@link AttractionPoint}. For that, it will be used only the AttractionPoints,
 * of the hosts, which are in the same SocialCluster or are SocialNeighbors. For
 * this AttractionPoints it will be find the highest scoring, which is to found
 * in score.
 * .
 * 
 * <br>
 * 
 * After the finding of the next {@link AttractionPoint}, it will be scheduled
 * an Event, with a delay between min- and maxPauseTime. After this delay, it
 * will be tried to assign a new {@link AttractionPoint} like the described
 * above.
 * 
 * 
 * @author Christoph Muenker
 * @version 1.0, 02.07.2013
 */
public class SocialTransitionStrategy implements ITransitionStrategy,
		EventHandler {

	private String socialId = null;

	private SocialView socialView;

	private List<SimLocationActuator> comps = new Vector<SimLocationActuator>();

	private List<AttractionPoint> aPoints = new Vector<AttractionPoint>();

	private Map<SimLocationActuator, AttractionPoint> assignments = new HashMap<SimLocationActuator, AttractionPoint>();
	
	private Set<SimLocationActuator> arrivedAtAttractionPoint = new LinkedHashSet<>();

	private Map<SimLocationActuator, Set<AttractionPoint>> favoritePlaces = new HashMap<SimLocationActuator, Set<AttractionPoint>>();

	private Map<SimLocationActuator, SimHost> mapMsHost = new HashMap<SimLocationActuator, SimHost>();

	private Map<SimHost, SimLocationActuator> mapHostMs = new HashMap<SimHost, SimLocationActuator>();

	private double minPauseTime = Simulator.MINUTE_UNIT * 0.5;

	private double maxPauseTime = Simulator.MINUTE_UNIT * 100;

	private double socialFactor = 0.8;

	private long numberOfFavoritePlaces = 4;

	private Random rand;

	private PositionVector worldDimension;

	private boolean init = false;

	public SocialTransitionStrategy() {
		this.rand = Randoms.getRandom(SocialTransitionStrategy.class);
	}

	private void init() {
		if (!init) {
			if (socialId == null) {
				throw new ConfigurationException(
						"SocialId is not set, to find the needed SocialView!");
			}

			socialView = Binder.getComponentOrNull(Topology.class)
					.getSocialView(socialId);

			if (socialView == null) {
				throw new ConfigurationException(
						"Cannot find the right socialView. Is the socialId correct?");
			}

			if (minPauseTime > maxPauseTime) {
				throw new ConfigurationException(
						"MinPauseTime should be smaller then maxPauseTime.");
			}

			worldDimension = Binder.getComponentOrNull(Topology.class)
					.getWorldDimensions();
			init = true;
		}
	}

	public void setSocialId(String socialId) {
		this.socialId = socialId;
	}

	@Override
	public Map<SimLocationActuator, AttractionPoint> getAssignments() {
		// FIXME why new? return new HashMap<MovementSupported,
		// AttractionPoint>(assignments);
		return assignments;
	}

	@Override
	public void setAttractionPoints(List<AttractionPoint> attractionPoints) {
		init();
		aPoints.addAll(attractionPoints);
	}

	@Override
	public void addComponent(SimLocationActuator ms) {
		comps.add(ms);
		mappingHost(ms);
		// assign the ms to an attractionPoint, which is near to the ms
		// position.
		// TODO: needed? We do Transition as next, and this will delete the
		// assignment..
		AttractionPoint nearest = aPoints.iterator().next();
		for (AttractionPoint aPoint : aPoints) {
			if (nearest.getRealPosition()
					.distanceTo(ms.getRealPosition()) > aPoint.getRealPosition()
							.distanceTo(ms.getRealPosition())) {
				nearest = aPoint;
			}
		}
		assignments.put(ms, nearest);

		assignFavoritePlaces(ms);

		doTransition(ms);
	}
	
	@Override
	public void updateTargetAttractionPoint(SimLocationActuator comp,
			AttractionPoint attractionPoint) {
		arrivedAtAttractionPoint.remove(comp);
		assignments.put(comp, attractionPoint);
	}
	
	@Override
	public void reachedAttractionPoint(SimLocationActuator ms) {
		if (!arrivedAtAttractionPoint.contains(ms)) {
			Event.scheduleWithDelay(getPauseTime(), this, ms, 0);
			arrivedAtAttractionPoint.add(ms);
		}
	}

	public void doTransition(SimLocationActuator ms) {
		List<AttractionPoint> apFavorites = getFavoritePlaces(ms);
		List<AttractionPoint> apFriends = getFriendsPlaces(ms);
		List<AttractionPoint> apClusters = getClusterPlaces(ms);
		List<AttractionPoint> apRandom = getRandomPlaces(ms,
				(int) Math.max(aPoints.size() * 0.2, 5));

		AttractionPoint ap = null;
		if (rand.nextDouble() < socialFactor) {
			ap = findHighestScore(ms, apFavorites, apFriends, apClusters,
					apRandom);
		} else {
			List<AttractionPoint> aps = new ArrayList<AttractionPoint>();
			aps.addAll(apRandom);
			aps.addAll(apFavorites);
			ap = aps.get(rand.nextInt(apRandom.size()));
		}
		assignments.put(ms, ap);
		arrivedAtAttractionPoint.remove(ms);
	}

	private AttractionPoint findHighestScore(SimLocationActuator ms,
			List<AttractionPoint> apFavorites, List<AttractionPoint> apFriends,
			List<AttractionPoint> apClusters, List<AttractionPoint> apRandom) {
		Set<AttractionPoint> aps = new HashSet<AttractionPoint>();
		aps.addAll(apFavorites);
		aps.addAll(apFriends);
		aps.addAll(apClusters);
		aps.addAll(apRandom);

		double maxScore = 0;
		AttractionPoint maxAp = null;
		for (AttractionPoint ap : aps) {
			double score = score(ms, ap, apFavorites, apFriends, apClusters,
					apRandom);
			// System.out.println(score);
			if (score > maxScore) {
				maxScore = score;
				maxAp = ap;
			}
		}
		return maxAp;
	}

	/**
	 * Score the given AttractionPoint for the given SimLocationActuator. <br>
	 * (clusterScore/#NodesInAp + friendsScore + 1/#NodesInAp) * socialFactor +
	 * (distanceScore + penalty) + (1-socialFactor) <br>
	 * 
	 * clusterScore = 1 if one is in the same cluster in this AP<br>
	 * friendsScore = 1 if one friend is in the same AP<br>
	 * penalty = -1 if AP the actually AP is <br>
	 * distance = 1 - (distance / maxDistance)
	 * 
	 * @param ms
	 * @param ap
	 * @param apFavorites
	 * @param apFriends
	 * @param apClusters
	 * @param apRandom
	 * @return
	 */
	private double score(SimLocationActuator ms, AttractionPoint ap,
			List<AttractionPoint> apFavorites, List<AttractionPoint> apFriends,
			List<AttractionPoint> apClusters, List<AttractionPoint> apRandom) {
		double distance = ms.getRealPosition().distanceTo(ap.getRealPosition());
		double distanceScore = 1 - (distance / worldDimension.getLength());

		double clusterScore = 0;
		double friendsScore = 0;
		if (apClusters.contains(ap)) {
			if (occurence(ap, apClusters) > 3) {
				// occurence give the number of other peers in this AP
				clusterScore = 1.0 / (occurence(ap, apClusters) - 1);
			} else {
				clusterScore = 1.0;
			}
		}
		if (apFriends.contains(ap)) {
			if (occurence(ap, apFriends) > 3) {
				// occurence give the number of other peers in this AP
				friendsScore = 1.0 / (occurence(ap, apFriends) - 1);
			} else {
				friendsScore = 1.0;
			}
		}
		// penalty for distance
		double penalty = 0;
		if (ap.equals(assignments.get(ms))) {
			penalty = -1;
		}

		return (clusterScore / assignedToAp(ap) + friendsScore + 1.0 / assignedToAp(ap))
				* socialFactor

				+ (distanceScore + penalty) * (1 - socialFactor);
	}

	// counts the number of the AttractionPoint in the list
	private int occurence(AttractionPoint ap, List<AttractionPoint> aps) {
		int i = 0;
		for (AttractionPoint a : aps) {
			if (a.equals(ap)) {
				i++;
			}
		}
		return i;
	}

	private int assignedToAp(AttractionPoint ap) {
		int i = 1;
		for (Entry<SimLocationActuator, AttractionPoint> entry : assignments
				.entrySet()) {
			if (entry.getValue().equals(ap)) {
				i++;
			}
		}
		return i;
	}

	private List<AttractionPoint> getRandomPlaces(SimLocationActuator ms,
			int number) {
		Collections.shuffle(aPoints);
		List<AttractionPoint> result = new Vector<AttractionPoint>();
		Iterator<AttractionPoint> iAP = aPoints.iterator();
		for (int i = 0; i < number && iAP.hasNext(); i++) {
			result.add(iAP.next());
		}

		return result;
	}

	private List<AttractionPoint> getClusterPlaces(SimLocationActuator ms) {
		List<AttractionPoint> result = new Vector<AttractionPoint>();
		SimHost msHost = mapMsHost.get(ms);

		for (SimHost host : socialView.getCluster(msHost)) {
			SimLocationActuator temp = mapHostMs.get(host);
			if (assignments.get(temp) != null) {
				result.add(assignments.get(temp));
			}
		}

		return result;
	}

	private List<AttractionPoint> getFriendsPlaces(SimLocationActuator ms) {
		List<AttractionPoint> result = new Vector<AttractionPoint>();
		SimHost msHost = mapMsHost.get(ms);

		for (SimHost host : socialView.getNeighbors(msHost)) {
			SimLocationActuator temp = mapHostMs.get(host);
			if (assignments.get(temp) != null) {
				result.add(assignments.get(temp));
			}
		}

		return result;
	}

	private List<AttractionPoint> getFavoritePlaces(SimLocationActuator ms) {
		return new Vector<AttractionPoint>(favoritePlaces.get(ms));
	}

	private void assignFavoritePlaces(SimLocationActuator ms) {
		Set<AttractionPoint> msFavoritePlaces = new HashSet<AttractionPoint>();
		LinkedList<AttractionPoint> temp = new LinkedList<AttractionPoint>(
				aPoints);
		Collections.shuffle(temp, rand);
		for (int i = 0; i < numberOfFavoritePlaces; i++) {
			if (!temp.isEmpty()) {
				msFavoritePlaces.add(temp.removeFirst());
			}
		}
		favoritePlaces.put(ms, msFavoritePlaces);
	}

	private void mappingHost(SimLocationActuator ms) {
		SimHostComponent comp = (SimHostComponent) ms;
		SimHost host = comp.getHost();

		assert host != null;

		mapHostMs.put(host, ms);
		mapMsHost.put(ms, host);
	}

	protected long getPauseTime() {
		return (long) ((rand.nextDouble() * (maxPauseTime - minPauseTime)) + minPauseTime);
	}

	public void setMinPauseTime(long minPauseTime) {
		if (minPauseTime < 0) {
			throw new ConfigurationException(
					"MinPauseTime should be bigger then 0!");
		}
		this.minPauseTime = minPauseTime;
	}

	public void setMaxPauseTime(long maxPauseTime) {
		if (maxPauseTime < 0) {
			throw new ConfigurationException(
					"MaxPauseTime should be bigger then 0!");
		}
		this.maxPauseTime = maxPauseTime;
	}

	@Override
	public void eventOccurred(Object se, int type) {
		if (arrivedAtAttractionPoint.contains(se)) {
			doTransition((SimLocationActuator) se);
		}
	}

	public void setSocialFactor(double socialFactor) {
		if (socialFactor < 0 || socialFactor > 1) {
			throw new ConfigurationException(
					"socialFactor should be between 0 and 1!");
		}
		this.socialFactor = socialFactor;
	}

}
