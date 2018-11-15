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

import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * 
 * Abstract base class of an Operation. Extend this class rather than
 * implementing the Operation-Interface to ensure proper logging and control
 * flow within your operations.
 * 
 * @author Bjoern Richerzhagen
 * 
 * @param <T>
 *            Owner
 * @param <R>
 *            Return type
 */
public abstract class AbstractOperation<T extends HostComponent, R> implements
		Operation<R> {

	private boolean error = false;

	private boolean finished = false;

	private boolean alreadyScheduled = false;

	private OperationCallback<R> callback;

	/**
	 * The number of all operations constructed so far.
	 */
	private static int operationCounter = 0;

	/**
	 * The identifier of a single Operation.
	 */
	private int operationID;

	/**
	 * The owner component of this operation.
	 */
	private T owner;

	private final OperationEventHandler eventHandler = new OperationEventHandler();

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "(id=" + getOperationID()
				+ ")";
	}

	/**
	 * create an operation without an OperationCallback.
	 * 
	 * @param component
	 */
	protected AbstractOperation(T component) {
		this(component, null);
	}

	/**
	 * create an operation with an OperationCallback.
	 * 
	 * @param component
	 * @param callback
	 */
	protected AbstractOperation(T component, OperationCallback<R> callback) {
		this.operationID = ++operationCounter;
		this.owner = component;
		this.callback = callback;
	}

	/**
	 * Resets the internal state of the Operation, this is needed for periodic
	 * operations (Which should be Implemented by extending PeriodicOperation)
	 */
	protected void resetInternalState() {
		this.error = false;
		this.finished = false;
	}

	/**
	 * Starts the operation
	 * 
	 */
	protected abstract void execute();

	/**
	 * Marks the operation as finished. The caller will be informed.
	 * 
	 * @param success
	 *            whether it was successful
	 */
	protected void operationFinished(boolean success) {
		if (!finished) {
			this.finished = true;
			this.error = !success;
			/*
			 * TODO inform monitor?
			 */
			if (success) {
				if (callback != null)
					callback.calledOperationSucceeded(this);
			} else {
				if (callback != null)
					callback.calledOperationFailed(this);
			}
		}
	}

	public abstract R getResult();

	protected boolean isError() {
		return error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.application.Operation#isSuccessful()
	 */
	public boolean isSuccessful() {
		return isFinished() && !error;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.tud.kom.p2psim.api.application.Operation#isFinished()
	 */
	public boolean isFinished() {
		return finished;
	}

	/**
	 * Each operation should have an operation timeout to ensure the proper
	 * functionality of the simulation. If a timeout event occurs in
	 * {@link #eventOccurred(SimulationEvent)} this method is invoked to perform
	 * various actions. Default behavior is to break up the operation after the
	 * first timeout.
	 * 
	 */
	protected void operationTimeoutOccured() {
		operationFinished(false);
	}

	/**
	 * Schedules an operation timeout in <code>timeout</code> simulation time
	 * units. The timeout event will be scheduled relative to the current
	 * simulation time.
	 * 
	 * @param timeout
	 */
	protected void scheduleOperationTimeout(long timeout) {
		Event.scheduleWithDelay(timeout, eventHandler, null,
				eventHandler.TIMEOUT);
	}

	@Override
	public void scheduleImmediately() {
		scheduleWithDelay(0);
	}

	@Override
	public void scheduleWithDelay(long delay) {
		if (alreadyScheduled && !(this instanceof PeriodicOperation)) {
			System.err
					.println("AbstractOperation: WARNING - the operation "
							+ toString()
							+ " was scheduled multiple times! If this is intended, use a PeriodicOperation instead!");
		}
		alreadyScheduled = true;
		Event.scheduleWithDelay(delay, eventHandler, null, eventHandler.EXECUTE);
	}

	public T getComponent() {
		return owner;
	}

	@Override
	public int getOperationID() {
		return operationID;
	}

	/**
	 * Private event handler to allow an extending operation to implement
	 * EventHandler without interfering with this class.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	private class OperationEventHandler implements EventHandler {

		public final int TIMEOUT = 1;

		public final int EXECUTE = 2;

		@Override
		public void eventOccurred(Object content, int type) {
			if (!isFinished() && type == TIMEOUT) {
				operationTimeoutOccured();
			} else if (type == EXECUTE) {
				/*
				 * TODO: monitor operation initiated
				 */
				execute();
			}
		}

		@Override
		public String toString() {
			return AbstractOperation.this.getClass().getSimpleName();
		}
	}

}
