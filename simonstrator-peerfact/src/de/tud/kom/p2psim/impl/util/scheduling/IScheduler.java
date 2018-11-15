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
 * 
 * ISchedulers schedule the delivery of arbitrary objects (TScheduleObject) on
 * an ISchedulableResource (which is given them e.g. through the constructor).
 * The IScheduler lets you create multiple scheduling queues (ISchedQueue),
 * where resources can arrive, are enqueued and scheduled on the
 * ISchedulableResource. Every queue has its own weight. The weight expresses
 * the ratio of the available time on the resource a queue will get from the
 * scheduler for its TScheduleObjects. E.g. a queue with a weight 2 will get
 * twice as much resources scheduled as a queue with weight 1 on average, if we
 * assume the queues never get empty. <br>
 * <br>
 * 
 * This scheduler is blocking. This means that if the job, when scheduled, can
 * not be done, e.g. because the given ISchedulableResource can't deliver it to
 * a further component (the method <b>service(obj)</b> of the given
 * ISchedulableResource returned false, for more on that, see
 * ISchedulableResource), this scheduler stops. Then, poke() has to be called to
 * redo the last operation and go on scheduling. If you don't call poke() after
 * this blocking, this scheduler will lock dead.
 * 
 * @author Leo Nobach
 * 
 * @param <TScheduleObject>
 */
public interface IScheduler<TScheduleObject> {

	/**
	 * Creates queues, where resources can arrive, are enqueued and scheduled on
	 * the ISchedulableResource. Every queue has its own weight. The weight
	 * expresses the ratio of the available time on the resource a queue will
	 * get from the scheduler for its TScheduleObjects. E.g. a queue with a
	 * weight 2 will get twice as much resources scheduled than a queue with
	 * weight 1 on average, if we assume the queues never get empty.
	 * 
	 * @param weight
	 * @return
	 */
	public ISchedQueue<TScheduleObject> createNewSchedQueue(double weight);

	/**
	 * If a scheduled operation could not be done (the method
	 * <b>service(obj)</b> of the given ISchedulableResource returned false),
	 * the scheduler is stopped. To reanimate it, you should call <b>poke()</b>.
	 * This lets the scheduler recall the method service() for the last object
	 * serviced, and if it succeeds (returns true), the scheduler will continue
	 * until the next call of the method service() returns false.
	 */
	public void poke();

}
