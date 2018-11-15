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
 * Convenience methods for operations
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class Operations {

	static class EmptyCallback<TResult> implements OperationCallback<TResult> {

		public void calledOperationFailed(Operation<TResult> op) {
			// do nothing
		}

		public void calledOperationSucceeded(Operation<TResult> op) {
			// do nothing
		}

	}

	/**
	 * Returns an empty OperationCallback, type-safe.
	 * @param <TResult>
	 * @return
	 */
	public static <TResult> OperationCallback<TResult> getEmptyCallback() {
		return new EmptyCallback<TResult>();
	}

	/**
	 * Invoking this method schedules immediately an empty operation without any
	 * functionality. In addition, the given callback will be informed that the
	 * operation has finished with success.
	 * 
	 * @param component
	 *            the owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the unique operation identifier
	 */
	public static <TResult> int scheduleEmptyOperation(HostComponent component,
			OperationCallback<TResult> callback) {
		Operation<TResult> op = createEmptyOperation(component, callback);
		op.scheduleImmediately();
		return op.getOperationID();
	}

	/**
	 * Invoking this method creates an empty operation withoun an functionality.
	 * The execution of this operation will finish immediately with success.
	 * 
	 * @param component
	 *            The owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the created empty operation
	 */
	public static <TResult> Operation<TResult> createEmptyOperation(
			HostComponent component, OperationCallback<TResult> callback) {
		Operation<TResult> op = new AbstractOperation<HostComponent, TResult>(
				component, callback) {
			@Override
			protected void execute() {
				operationFinished(true);
			}

			@Override
			public TResult getResult() {
				return null;
			}

		};
		return op;
	}

	/**
	 * Invoking this method creates an empty operation without an functionality.
	 * The execution of this operation will finish immediately <b>without</b>
	 * success.
	 * 
	 * @param component
	 *            The owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the created empty operation
	 */
	public static <TResult> Operation<TResult> createEmptyFailingOperation(
			HostComponent component, OperationCallback<TResult> callback) {
		Operation<TResult> op = new AbstractOperation<HostComponent, TResult>(
				component, callback) {
			@Override
			protected void execute() {
				operationFinished(false);
			}

			@Override
			public TResult getResult() {
				return null;
			}

		};
		return op;
	}

	/**
	 * Invoking this method creates an empty operation withoun an functionality.
	 * The execution of this operation will finish immediately with success.
	 * 
	 * @param component
	 *            The owner (component) of the operation
	 * @param callback
	 *            the given callback
	 * @return the created empty operation
	 */
	public static <TResult> Operation<TResult> createEmptyOperationResult(
			HostComponent component, OperationCallback<TResult> callback,
			final TResult result) {
		Operation<TResult> op = new AbstractOperation<HostComponent, TResult>(
				component, callback) {
			@Override
			protected void execute() {
				operationFinished(true);
			}

			@Override
			public TResult getResult() {
				return result;
			}

		};
		return op;
	}
}
