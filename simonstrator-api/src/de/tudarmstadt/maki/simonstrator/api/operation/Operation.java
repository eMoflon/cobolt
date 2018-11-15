/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 * 
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.operation;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * In your overlay/app/service please extend the abstract classes
 * {@link PeriodicOperation} and {@link AbstractOperation}!
 * 
 * This class realizes the <i>command pattern</i> for common operations in a
 * distributed system. Typically, if some action should take place in a
 * component of a host, the action will be represented by an operation object
 * providing the required functionality. Examples of operations could be:
 * JoinOperation, SearchOperation, DisconnectOperation etc.
 * <p/>
 * The basic operation provides the knowledge about its state: finished or not
 * and if finished - successful or not. The main functionality of this class is
 * its ability to be <i>scheduled</i> in the simulator.
 * 
 * @author Sebastian Kaune <kaune@kom.tu-darmstadt.de>
 * @author Konstantin Pussep <pussep@kom.tu-darmstadt.de>
 * @version 3.0, 11/25/2007
 * @param <R>
 *            The result type of the operation. e.g. LookupOperation in a
 *            DHTNode should return a DHTValue.
 * 
 * @see OperationCallback
 */
public interface Operation<R> {

	/**
	 * This method returns information whether a given operation was successful
	 * or not. If the operation has not finished yet it must return false.
	 * 
	 * @return returns <code>true</code> if this operation was successful.
	 */
	public boolean isSuccessful();

	/**
	 * This method returns information whether a given operation is finished or
	 * not regardless of whether it finished successfully or failed.
	 * 
	 * @return returns <code>true</code> if this operation is finished.
	 */
	public boolean isFinished();

	/**
	 * By constructing a new operation instance, each operation is assigned a
	 * globally unique operation identifier. That is, each operation can be
	 * differentiated by another one using this identifier. For debugging
	 * purposes the operation id is even unique among all hosts in the
	 * simulator.
	 * 
	 * @return the globally unique operation identifier
	 */
	public int getOperationID();

	/**
	 * Schedules the operation with the time delay of <b>zero</b> into the
	 * scheduler. The sense of this method is that the operations should be
	 * executed outside of the callers call context.
	 * 
	 */
	public void scheduleImmediately();

	/**
	 * This method schedules the operation relatively to the current simulation
	 * time by using an additional delay. That is, the scheduling time t is
	 * given by <code>t = currentTime + delay</code>.
	 * 
	 * @param delay
	 *            - relative (virtual) time delay after which the operation will
	 *            be executed
	 */
	public void scheduleWithDelay(long delay);

	/**
	 * The result of the operation, which is only not null if the operation
	 * finished successfully.
	 * 
	 * @return operation result
	 */
	public R getResult();

	/**
	 * 
	 * @return
	 */
	public HostComponent getComponent();
}