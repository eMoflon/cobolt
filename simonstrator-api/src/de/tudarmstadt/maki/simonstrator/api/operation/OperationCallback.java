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



package de.tudarmstadt.maki.simonstrator.api.operation;


/**
 * <code>OperationCallback</code> is used to inform the caller of an operation
 * about operation's finish status. In a <i>real-world<i/> java program you
 * would write something like
 * <code>Foo result = someObject.doSomething(params);</code> As this would not
 * work in a discrete event-based simulator (you have to pass the control to the
 * simulator in order to get messages sent through the network) you have to do
 * something like: <code> 
 * 	someObject.doSomething(params, new OperationCallback(){ 
 * 	
 * 		public void calledOperationFailed(Operation op){
 * 	        processResult((Foo) op.getResult());
 *  		}
 * 
 *  	public void calledOperationSucceeded(Operation op){
 * 			processFailure();
 *  	}
 *  }
 * </code> So what happens here is actually is making the operation call
 * asynchronous.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 03.12.2007
 * 
 * @param <T>
 *            The type of the operation result, which is specific for any
 *            operation.
 * 
 * @see Operation
 */
public interface OperationCallback<T> {

	/**
	 * Called if the operation failed.
	 * 
	 * @param op
	 *            failed operation
	 */
	public void calledOperationFailed(Operation<T> op);

	/**
	 * Called if the operation was finished successfully.
	 * 
	 * @param op
	 *            finished operation
	 */
	public void calledOperationSucceeded(Operation<T> op);

}
