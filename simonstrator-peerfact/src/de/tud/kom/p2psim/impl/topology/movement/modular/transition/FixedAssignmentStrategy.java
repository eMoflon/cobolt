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

package de.tud.kom.p2psim.impl.topology.movement.modular.transition;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.topology.movement.modular.ModularMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.modular.attraction.AttractionPoint;
import de.tudarmstadt.maki.simonstrator.api.Randoms;

/**
 * A {@link TransitionStrategy} for a case in which nodes are affiliated to an
 * {@link AttractionPoint} only in the beginning. No further transition will
 * take place.
 * 
 * @author Nils Richerzhagen
 * @version 1.0, 04.08.2014
 */
public class FixedAssignmentStrategy implements TransitionStrategy {

	private Random rnd;

	private List<SimLocationActuator> comps = new LinkedList<SimLocationActuator>();

	private List<AttractionPoint> aPoints = new LinkedList<AttractionPoint>();

	private Map<SimLocationActuator, AttractionPoint> assignments = new LinkedHashMap<SimLocationActuator, AttractionPoint>();

	private Map<SimLocationActuator, SimHost> mappingMSHost = new LinkedHashMap<SimLocationActuator, SimHost>();

	private Map<SimHost, SimLocationActuator> mappingHostMS = new LinkedHashMap<SimHost, SimLocationActuator>();

	private Map<String, AttractionPoint> mappingGroupIdAP = new LinkedHashMap<String, AttractionPoint>();

	private Map<AttractionPoint, String> mappingAPGroupId = new LinkedHashMap<AttractionPoint, String>();

	public FixedAssignmentStrategy() {
		rnd = Randoms.getRandom(FixedAssignmentStrategy.class);
	}

	@Override
	public Map<SimLocationActuator, AttractionPoint> getAssignments() {
		return new HashMap<SimLocationActuator, AttractionPoint>(assignments);
	}

	@Override
	public void setAttractionPoints(List<AttractionPoint> attractionPoints) {
		aPoints.addAll(attractionPoints);
	}

	@Override
	public void addComponent(SimLocationActuator ms) {
		comps.add(ms);
		mappingHost(ms);

		// No assignments been done before.
		if (assignments.isEmpty()) {
			AttractionPoint aPoint = aPoints.iterator().next();
			assignments.put(ms, aPoint);
			mappingGroupId(ms, aPoint);
			setStartPosition(ms, aPoint.getRealPosition());
		}
		// GroupId is not mapped.
		else if (!mappingGroupIdAP.containsKey(
				mappingMSHost.get(ms).getProperties().getGroupID())) {
			for (AttractionPoint actAP : aPoints) {
				if (!mappingAPGroupId.containsKey(actAP)) {
					assignments.put(ms, actAP);
					mappingGroupId(ms, actAP);
					setStartPosition(ms, actAP.getRealPosition());
					break;
				}
			}
		}
		// GroupId is already mapped.
		else if (mappingGroupIdAP.containsKey(
				mappingMSHost.get(ms).getProperties().getGroupID())) {
			AttractionPoint aPoint = mappingGroupIdAP
					.get(mappingMSHost.get(ms).getProperties().getGroupID());
			assignments.put(ms, aPoint);
			setStartPosition(ms, aPoint.getRealPosition());
		} else {
			throw new Error("Should not happen.");
		}

	}

	private void mappingHost(SimLocationActuator ms) {
		SimHostComponent comp = (SimHostComponent) ms;
		SimHost host = comp.getHost();

		assert host != null;

		mappingHostMS.put(host, ms);
		mappingMSHost.put(ms, host);
	}

	private void mappingGroupId(SimLocationActuator ms, AttractionPoint AP) {
		SimHostComponent comp = (SimHostComponent) ms;
		SimHost host = comp.getHost();

		assert host != null;

		mappingAPGroupId.put(AP,
				mappingMSHost.get(ms).getProperties().getGroupID());
		mappingGroupIdAP.put(mappingMSHost.get(ms).getProperties().getGroupID(),
				AP);
	}

	private void setStartPosition(SimLocationActuator ms,
			PositionVector aPointReferencePosition) {
		double minJitter = 50.0;
		double maxJitter = 100.0;

		double xJitter = (rnd.nextDouble() * (maxJitter - minJitter))
				+ minJitter;
		double yJitter = (rnd.nextDouble() * (maxJitter - minJitter))
				+ minJitter;
		PositionVector jitterVector = new PositionVector(xJitter, yJitter);
		PositionVector newPos = aPointReferencePosition.plus(jitterVector);
		ms.updateCurrentLocation(newPos.getLongitude(), newPos.getLatitude());
	}

	/**
	 * Used by the MobilityModel (M1) of the {@link ModularMovementModel} to get
	 * the groupId of the affiliated nodes to that {@link AttractionPoint}. Once
	 * the groupId is known nodes can be set <b>offline</b> or <b>online</b>.
	 * 
	 * @param attractionPoint
	 * @return
	 */
	public String getGroupIdOfAttractionPoint(AttractionPoint attractionPoint) {
		return mappingAPGroupId.get(attractionPoint);
	}

}
