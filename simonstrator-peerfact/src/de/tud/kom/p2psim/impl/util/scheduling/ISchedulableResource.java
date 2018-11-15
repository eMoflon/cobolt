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
 * A resource where scheduled objects arrive. Guarantees that the scheduled
 * object has the time to be serviced (the time in simulator time units given
 * along with the object in ISchedQueue.arrive(...)) until the next object
 * arrives.
 * 
 * @author Leo Nobach
 * 
 * @param <TScheduleObject>
 */
public interface ISchedulableResource<TScheduleObject> {

	/**
	 * Services the given object. Guarantees that the next call of this method
	 * is made after the time length given to the scheduler along with this
	 * object, when enqueueing it (ISchedQueue.arrive(...)).
	 * 
	 * <p>
	 * This method <b>must</b> return true to let the scheduler continue. If it
	 * returns false, the scheduler is stopped and waits for a call of
	 * <b>poke()</b> to continue with calling the method service() again with
	 * the same scheduled object again, that originally led to false return.
	 * </p>
	 * 
	 * @param obj
	 */
	public boolean service(TScheduleObject obj);

}
