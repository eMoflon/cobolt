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

package de.tudarmstadt.maki.simonstrator.api.component.core;

import java.io.Writer;
import java.util.List;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;

/**
 * This component provides global access to analyzers and also enables the
 * runtime to provide analyzers to the respective overlay. Overlays should
 * access the methods directly through {@link Monitor}, rather than calling the
 * binder.
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public interface MonitorComponent extends GlobalComponent {

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
	public void log(Class<?> subject, Level level, String message,
			Object... data);

	/**
	 * Retrieve all Analyzers implementing the respective interface.
	 * 
	 * @param analyzerType
	 * @return
	 */
	public <A extends Analyzer> List<A> getAnalyzers(Class<A> analyzerType)
			throws AnalyzerNotAvailableException;
	
	/**
	 * Register a new analyzer
	 * 
	 * @param analyzer
	 */
	public <A extends Analyzer> void registerAnalyzer(A analyzer);

	/**
	 * In general, analyzers are used to receive notifications about actions
	 * that took place on specific components, for instance the sending or
	 * receiving of messages. In particular, analyzers are able to collect data
	 * during a simulation run and prepare the results at the end of a
	 * simulation.
	 * 
	 * Note that analyzers must be registered by an implementation of the
	 * {@link Monitor} interface by using the xml configuration file before the
	 * simulation starts.
	 * 
	 * @author Sebastian Kaune
	 * @author Konstantin Pussep
	 * @version 4.0, 03/10/2011
	 * 
	 */
	public interface Analyzer {
		/**
		 * Invoking this method denotes start running analyzer
		 * 
		 */
		public void start();

		/**
		 * Invoking this method denotes stop running analyzer. The analyzer can
		 * write human readable data to the provided output stream, if wanted.
		 */
		public void stop(Writer out);

		/*
		 * Analyzer-Methods that are called by the overlays are not allowed to
		 * return data! I.e., the return type must be void!
		 */

	}
	
	/**
	 * Thrown, if the given analyzer is not available
	 * 
	 * @author Bjoern Richerzhagen
	 *
	 */
	public class AnalyzerNotAvailableException extends Exception {

		public AnalyzerNotAvailableException() {
			super("The Analyzer is not available!");
		}
		
		private static final long serialVersionUID = 1L;
		
	}

}
