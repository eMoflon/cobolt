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

package de.tud.kom.p2psim.impl.network.fairshareng;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * The Class SchedulerList: Schedules flows according to their transfer-endtime.
 */
public class SchedulerList {

	private static Comparator<? super FairshareFlow> TRANSFERENDTIME_COMPERATOR;

	/** The sorted map. */
	private final DirectedGraph fullGraph;

	/** Recalculation needed: Set to yes once flow gets removed or added. */
	private boolean recalculationNeeded = false;

	/** The returned flows: Save all returned flows within one Simulator step in here to stop duplicates. */
	private final LinkedHashSet<FairshareFlow> returnedFlows = new LinkedHashSet<FairshareFlow>();

	/**
	 * Instantiates a new scheduler list.
	 * @param fullGraph
	 */
	public SchedulerList(DirectedGraph fullGraph) {
		this.fullGraph = fullGraph;
		this.TRANSFERENDTIME_COMPERATOR = new TransferEndTimeComp();
	}

	/**
	 * Adds the event.
	 *
	 * @param newFlow the fairshare flow
	 */
	public void addEvent(FairshareFlow newFlow) {

		assert (newFlow.getTransferEndTime() >= Time.getCurrentTime()) : "Flow too old: "
				+ newFlow.getTransferEndTime()
				+ " / "
				+ Time.getCurrentTime();

		this.recalculationNeeded = true;

	}

	/**
	 * Removes the given event/flow.
	 *
	 * @param toCancel the to cancel
	 */
	public void removeEvent(FairshareFlow toCancel) {

		this.recalculationNeeded = true;

	}

	/**
	 * Do recalculation.
	 */
	private void doRecalculation() {

		assert( this.fullGraph.getAllFlows() instanceof LinkedList );

		final LinkedList<FairshareFlow> graphToSort = (LinkedList<FairshareFlow>)this.fullGraph.getAllFlows();
		Collections.sort(graphToSort, this.TRANSFERENDTIME_COMPERATOR); 

		this.recalculationNeeded = false;
		this.returnedFlows.clear();

	}

	/**
	 * Gets the next arrival.
	 *
	 * @return the next arrival
	 */
	public long getNextArrival() {

		if( this.recalculationNeeded == true ) {
			doRecalculation();
		}

		return (this.fullGraph.getAllFlows().size() > 0) ? this.fullGraph.getAllFlows().iterator().next().getTransferEndTime()  : Long.MAX_VALUE;

	}

	/**
	 * Checks for currently arriving flow.
	 *
	 * @return true, if flow is currently arriving.
	 */
	public boolean hasCurrentlyArrivingFlow() {

		if( this.fullGraph.getAllFlows().isEmpty() ) {
			return false;
		}

		/* Return true, if a least one flow is arriving right now (meaning at Simulator.getCurrentTime() ) */
		return this.returnedFlows.contains(this.fullGraph.getAllFlows().iterator().next()) ? false : this.getNextArrival() == Time.getCurrentTime();

	}

	/**
	 * Gets the and remove currently arriving flow. If multiple flows are arriving, this
	 * function has to be called multiple times as well.
	 *
	 * @return the and remove current arrival
	 */
	public FairshareFlow getAndRemoveCurrentArrival() {

		final LinkedList<FairshareFlow> flowList = (LinkedList<FairshareFlow>) this.fullGraph.getAllFlows();

		final FairshareFlow nextArrival = flowList.remove(0);

		flowList.addLast(nextArrival);

		/* Save last seen to prevent returning flow twice. */
		this.returnedFlows.add(nextArrival);

		return nextArrival;

	}

	/**
	 * The Class TransferEndTimeComp, sort flows by TransferEndTime.
	 */
	class TransferEndTimeComp implements Comparator<FairshareFlow> {

		/* (non-Javadoc)
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		@Override
		public int compare(FairshareFlow f1, FairshareFlow f2) {

			if (f1.getTransferEndTime() < f2.getTransferEndTime()) {
				return -1;
			} else if (f1.getTransferEndTime() > f2.getTransferEndTime()) {
				return +1;
			} else {
				if (f1.equals(f2)) {
					return 0;
				} else {
					return (f1.hashCode() > f2.hashCode()) ? +1 : -1;
				}
			}
		}
	}
}
