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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.common.SimHostComponent;
import de.tud.kom.p2psim.api.topology.movement.SimLocationActuator;
import de.tud.kom.p2psim.impl.topology.movement.modular.transition.TransitionStrategy;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.ModularMovementModel;
import de.tud.kom.p2psim.impl.topology.movement.modularosm.attraction.AttractionPoint;

/**
 * A {@link TransitionStrategy} for a case in which nodes are affiliated to an {@link AttractionPoint} only
 * in the beginning. No further transition will take place.
 * 
 * @author Martin Hellwig
 * @version 1.0, 07.07.2015
 */
public class FixedAssignmentStrategy implements ITransitionStrategy {
	
	private List<SimLocationActuator> comps = new LinkedList<SimLocationActuator>();

	private List<AttractionPoint> aPoints = new LinkedList<AttractionPoint>();

	private Map<SimLocationActuator, AttractionPoint> assignments = new LinkedHashMap<SimLocationActuator, AttractionPoint>();

	private Map<SimLocationActuator, SimHost> mappingMSHost = new LinkedHashMap<SimLocationActuator, SimHost>();

	private Map<SimHost, SimLocationActuator> mappingHostMS = new LinkedHashMap<SimHost, SimLocationActuator>();

	private Map<String, AttractionPoint> mappingGroupIdAP = new LinkedHashMap<String, AttractionPoint>();

	private Map<AttractionPoint, String> mappingAPGroupId = new LinkedHashMap<AttractionPoint, String>();
	
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
		}
		// GroupId is not mapped.
		else if (!mappingGroupIdAP.containsKey(mappingMSHost.get(ms)
				.getProperties().getGroupID())) {
			for (AttractionPoint actAP : aPoints) {
				if (!mappingAPGroupId.containsKey(actAP)) {
					assignments.put(ms, actAP);
					mappingGroupId(ms, actAP);
					break;
				}
			}
		}
		// GroupId is already mapped.
		else if (mappingGroupIdAP.containsKey(mappingMSHost.get(ms)
				.getProperties().getGroupID())) {
			AttractionPoint aPoint = mappingGroupIdAP.get(mappingMSHost.get(ms)
					.getProperties().getGroupID());
			assignments.put(ms, aPoint);
		} else {
			throw new Error("Should not happen.");
		}
		
		

	}
	
	@Override
	public void reachedAttractionPoint(SimLocationActuator ms) {
		// don't care, as no further assignment takes place.
	}
	
	@Override
	public void updateTargetAttractionPoint(SimLocationActuator comp,
			AttractionPoint attractionPoint) {
		assignments.put(comp, attractionPoint);
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

		mappingAPGroupId.put(AP, mappingMSHost.get(ms).getProperties()
				.getGroupID());
		mappingGroupIdAP.put(
				mappingMSHost.get(ms).getProperties().getGroupID(), AP);
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
