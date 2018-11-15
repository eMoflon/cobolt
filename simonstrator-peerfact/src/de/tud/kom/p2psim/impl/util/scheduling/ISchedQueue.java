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


package de.tud.kom.p2psim.impl.util.scheduling;

/**
 * A queue of an IScheduler, where objects can arrive. Every queue has its own
 * weight. The weight expresses the ratio of the available time on the resource
 * a queue will get from the scheduler for its TScheduleObjects. E.g. a queue
 * with a weight 2 will get twice as much resources scheduled than a queue with
 * weight 1 on average, if we assume the queues never get empty.
 * 
 * @author Leo Nobach
 * 
 * @param <TScheduleObject>
 */
public interface ISchedQueue<TScheduleObject> {

	/**
	 * Returns the weight of this queue. The weight expresses the ratio of the
	 * available time on the resource a queue will get from the scheduler for
	 * its TScheduleObjects.E.g. a queue with a weight 2 will get twice as much
	 * resources scheduled than a queue with weight 1 on average, if we assume
	 * the queues never get empty.
	 * 
	 * @return
	 */
	public double getWeight();

	/**
	 * Sets the weight of this queue to a new value. The weight expresses the
	 * ratio of the available time on the resource a queue will get from the
	 * scheduler for its TScheduleObjects. E.g. a queue with a weight 2 will get
	 * twice as much resources scheduled than a queue with weight 1 on average,
	 * if we assume the queues never get empty.
	 * 
	 * @param weightOfOwner
	 */
	public void setWeight(double weightOfOwner);

	/**
	 * Lets a new object arrive at this queue. When scheduled, the object will
	 * get serviceTime time (in simulation time units) on the
	 * ISchedulableResource.
	 * 
	 * @param obj
	 * @param serviceTime
	 */
	public void arrive(TScheduleObject obj, long serviceTime);

	/**
	 * Disconnects this queue from the corresponding scheduler. All elements
	 * currently in this queue will get dropped and will never be scheduled
	 * anymore.
	 */
	public void disconnect();

	/**
	 * Returns the current size of this queue. Contains all the objects in it
	 * except the one that is currently scheduled, if any.
	 * 
	 * @return
	 */
	public int getSize();

}
