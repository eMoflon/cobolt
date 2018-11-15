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

package de.tudarmstadt.maki.simonstrator.api.component.transition;

import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;

/**
 * Interface for the local transition engine that is used to fetch component
 * proxies. This enables us to exchange the way proxies are created without
 * modifying tons of overlay code to replace static calls.
 * 
 * Furthermore, programmatic execution of transitions is eased to promote the
 * use of the {@link TransitionEnabled} interface even if control communication
 * is realized in-band with the application protocol.
 * 
 * This is the <strong>local</strong> transition engine.
 * 
 * @author Bjoern Richerzhagen
 *
 */
public interface TransitionEngine extends HostComponent {

	/**
	 * To create an register a new component proxy that can then be used in the
	 * respective application code.
	 * 
	 * @param proxyInterface
	 *            the interface (must extend {@link TransitionEnabled}) that is
	 *            exported by this proxy.
	 * @param defaultInstance
	 *            a default implementation of the {@link TransitionEnabled}
	 *            component to be used initially. Lifecycle-methods will be
	 *            invoked.
	 * @param proxyName
	 *            an identifier (string) for this proxy
	 * @return the proxy to be used in the application code or null, if the
	 *         proxy name is already taken. Check with proxyExists!
	 */
	public <T extends TransitionEnabled> T createMechanismProxy(
			Class<T> proxyInterface, T defaultInstance, String proxyName);

	/**
	 * Checks if a proxy with the given name exists in the engine.
	 * 
	 * @param proxyName
	 * @return
	 */
	public boolean proxyExists(String proxyName);

	/**
	 * Get the proxy facade with the given name.
	 * 
	 * @param proxyName
	 * @return
	 */
	public <T extends TransitionEnabled> T getProxy(String proxyName,
			Class<T> proxyInterface);

	/**
	 * Adds a transition listener to the given proxy.
	 * 
	 * @param proxyName
	 * @param listener
	 *            notified each time a transition on the given proxy is
	 *            executed.
	 */
	public void addTransitionListener(String proxyName,
			TransitionListener listener);

	/**
	 * Removes a transition listener.
	 * 
	 * @param proxyName
	 * @param listener
	 */
	public void removeTransitionListener(String proxyName,
			TransitionListener listener);

	/**
	 * A shortcut to alter fields that are annotated with {@link MechanismState}
	 * . Internally, a self-transition is created and executed.
	 * 
	 * @param proxyName
	 * @param targetClass
	 * @param fieldName
	 * @param value
	 */
	public <T extends TransitionEnabled> void alterLocalState(String proxyName,
			Class<T> targetClass, String fieldName, Object value);

	/**
	 * Executes a self-transition. No lifecycle-methods are invoked, but state
	 * can be altered through the {@link SelfTransition} object.
	 * 
	 * @param proxyName
	 * @param targetClass
	 * @param selfTransition
	 * @throws UnsupportedOperationException
	 */
	public <T extends TransitionEnabled> void executeSelfTransition(
			String proxyName, Class<T> targetClass,
			SelfTransition<T> selfTransition)
					throws UnsupportedOperationException;

	/**
	 * Executes an atomic transition. If an {@link AtomicTransition} instance
	 * for the respective types was previously registered, it is used to execute
	 * the transition. Otherwise, the default model is used, including the
	 * support for state transfers using the {@link TransferState} annotation.
	 * 
	 * @param proxyName
	 *            the previously chosen proxy identifier.
	 * @param targetClass
	 */
	public <T extends TransitionEnabled> void executeAtomicTransition(
			String proxyName, Class<T> targetClass);

	/**
	 * Register a transition with a custom transitionStrategy. This is optional
	 * - if you execute a previously not registered transition, a default model
	 * is used (including basic state transfer).
	 * 
	 * @param proxyName
	 * @param transitionStrategy
	 */
	public <F extends TransitionEnabled, T extends TransitionEnabled> void registerTransition(
			String proxyName, AtomicTransition<F, T> transitionStrategy);

}
