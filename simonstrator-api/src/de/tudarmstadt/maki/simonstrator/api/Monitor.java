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

package de.tudarmstadt.maki.simonstrator.api;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.Analyzer;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;

/**
 * Bridge to local Monitoring on each Runtime, providing Analyzers in a
 * Proxy-Fashion as well as logging capabilities.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public final class Monitor {

	public enum Level {
		INFO, WARN, ERROR, DEBUG
	}

	private static MonitorComponent monitor = null;

	private static Map<Class<?>, Analyzer> cachedProxies = new LinkedHashMap<>();

	private static Map<Class<?>, Boolean> cachedCheck = new LinkedHashMap<>();

	private static List<Delegator<?>> delegators = new LinkedList<>();

	private static MonitorComponent getMonitor() {
		if (monitor == null) {
			try {
				monitor = Binder.getComponent(MonitorComponent.class);
			} catch (ComponentNotAvailableException e) {
				monitor = new MonitorComponent() {

					@Override
					public <A extends Analyzer> void registerAnalyzer(A analyzer) {
						throw new AssertionError(
								"This environment does not support Analyzers (it needs to provide a MonitorComponent)!");
					}

					@Override
					public void log(Class<?> subject, Level level,
							String message, Object... data) {
						// fail silently
					}

					@Override
					public <A extends Analyzer> List<A> getAnalyzers(
							Class<A> analyzerType)
							throws AnalyzerNotAvailableException {
						throw new AnalyzerNotAvailableException();
					}
				};
			}
		}
		return monitor;
	}

	/**
	 * Logging, using printf semantics. <strong>For performance reasons: do
	 * never build a complete string but instead use the placeholder semantics.
	 * This way, strings are only built if logging is enabled.</strong>
	 * 
	 * @param subject
	 * @param level
	 * @param message
	 *            should be a single string using placeholders
	 * @param data
	 *            data objects (toString will be called on non-primitives)
	 */
	public static void log(Class<?> subject, Level level, String message,
			Object... data) {
		getMonitor().log(subject, level, message, data);
	}

	/**
	 * Retrieve an analyzing-Interface (transparent multiplexing is performed,
	 * if multiple analyzers with the same interface are registered).
	 * 
	 * @param analyzerType
	 * @return
	 * @throws AnalyzerNotAvailableException
	 */
	@SuppressWarnings("unchecked")
	public static <A extends Analyzer> A get(Class<A> analyzerType)
			throws AnalyzerNotAvailableException {
		// Caching
		if (cachedProxies.containsKey(analyzerType)) {
			return (A) cachedProxies.get(analyzerType);
		}
		/*
		 * Next call is needed to check if analyzers are available (throws
		 * exception otherwise!)
		 */
		getMonitor().getAnalyzers(analyzerType);

		cachedCheck.put(analyzerType, true);
		// create proxy
		Class<?>[] proxyInterfaces = new Class[]{analyzerType};
		Delegator<A> delegator = new Delegator<A>(analyzerType);
		A proxy = (A) Proxy.newProxyInstance(analyzerType.getClassLoader(),
				proxyInterfaces, delegator);
		cachedProxies.put(analyzerType, proxy);
		return proxy;
	}

	/**
	 * Get a proxy to the respective analyzers implementing the given type, or
	 * null, if no such analyzer is registered.
	 * 
	 * @param analyzerType
	 * @return
	 */
	public static <A extends Analyzer> A getOrNull(Class<A> analyzerType) {
		try {
			return get(analyzerType);
		} catch (AnalyzerNotAvailableException e) {
			return null;
		}
	}

	/**
	 * Quick (cached) call to check if analyzers for a given interface exist.
	 * 
	 * @param analyzerType
	 * @return
	 */
	public static <A extends Analyzer> boolean hasAnalyzer(
			Class<A> analyzerType) {
		if (!cachedCheck.containsKey(analyzerType)) {
			try {
				get(analyzerType);
			} catch (AnalyzerNotAvailableException e) {
				cachedCheck.put(analyzerType, false);
			}
		}
		assert cachedCheck.containsKey(analyzerType);
		return cachedCheck.get(analyzerType);
	}

	/**
	 * Register a new analyzer and invalidate caches.
	 * 
	 * @param analyzer
	 */
	public static <A extends Analyzer> void registerAnalyzer(A analyzer) {
		cachedCheck.clear();
		cachedProxies.clear();
		getMonitor().registerAnalyzer(analyzer);
		for (Delegator<?> delegator : delegators) {
			delegator.updateAnalyzers();
		}
	}

	/**
	 * Transparent Proxy for calls to the analyzers
	 * 
	 * TODO we should update the List of analyzers if a new analyzer is added
	 * that matches the requested interface.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	protected static class Delegator<A extends Analyzer> implements
			InvocationHandler {

		private final List<A> analyzers;

		public final Class<A> analyzerClass;

		public Delegator(Class<A> analyzerClass) {
			this.analyzerClass = analyzerClass;
			this.analyzers = new LinkedList<>();
			updateAnalyzers();
		}

		/**
		 * Update the proxy if a new analyzer is added.
		 * 
		 * @param analyzers
		 */
		public void updateAnalyzers() {
			this.analyzers.clear();
			try {
				this.analyzers.addAll(getMonitor().getAnalyzers(analyzerClass));
			} catch (AnalyzerNotAvailableException e) {
				throw new AssertionError("No analyzer available.");
			}
		}

		@Override
		public Object invoke(Object proxy, Method m, Object[] args)
				throws Throwable {
			for (A analyzer : analyzers) {
				m.invoke(analyzer, args);
			}
			return null;
		}

	}

}
