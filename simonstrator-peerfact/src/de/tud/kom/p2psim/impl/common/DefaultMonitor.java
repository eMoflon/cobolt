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

package de.tud.kom.p2psim.impl.common;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import de.tud.kom.p2psim.api.simengine.SimulatorObserver;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent;

/**
 * New Monitor-Component to work with the simonstrator-API (provides
 * overlay-access to analyzers)
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, Jul 8, 2013
 */
public class DefaultMonitor implements MonitorComponent, EventHandler,
		SimulatorObserver {

	private final static List<Analyzer> analyzers = new LinkedList<Analyzer>();

	private final static Map<Class<?>, Logger> loggers = new LinkedHashMap<Class<?>, Logger>();

	private static DefaultMonitor singletonInstance = null;

	private DefaultMonitor() {
		this.isMonitoring = false;
	}

	public static DefaultMonitor getInstance() {
		if (singletonInstance == null) {
			singletonInstance = new DefaultMonitor();
		}
		return singletonInstance;
	}

	@Override
	public <A extends Analyzer> List<A> getAnalyzers(Class<A> analyzerType)
			throws AnalyzerNotAvailableException {
		List<A> found = new LinkedList<A>();
		for (Analyzer a : analyzers) {
			if (analyzerType.isInstance(a)) {
				found.add(analyzerType.cast(a));
			}
		}
		if (found.isEmpty()) {
			throw new AnalyzerNotAvailableException();
		}
		return found;
	}

	@Override
	public <A extends Analyzer> void registerAnalyzer(A analyzer) {
		analyzers.add(analyzer);
	}

	@Override
	public void log(Class<?> subject, Level level, String msg, Object... data) {
		Logger log = loggers.get(subject);
		if (log == null) {
			log = Logger.getLogger(subject);
			loggers.put(subject, log);
		}
		switch (level) {
		case DEBUG:
			if (log.isDebugEnabled()) {
				log.debug(String.format(msg, data));
			}
			break;
		case ERROR:
			log.error(String.format(msg, data));
			break;
		case WARN:
			log.warn(String.format(msg, data));
			break;
		case INFO:
			if (log.isInfoEnabled()) {
				log.info(String.format(msg, data));
			}
			break;
		}
	}

	/**
	 * Called by the Configurator
	 * 
	 * @param analyzer
	 */
	public void setAnalyzer(Analyzer analyzer) {
		Monitor.registerAnalyzer(analyzer);
	}

	/**
	 * Specifies where to write the monitoring results to.
	 * 
	 * @param output
	 *            writer (e.g. FileWriter, StringWriter, ...)
	 */
	public void setResultWriter(Writer output) {
		this.output = new BufferedWriter(output);
	}

	/**
	 * @deprecated use the MetricOutputDAO instead!
	 * @param tableName
	 */
	public void setTableName(String tableName) {
		System.out.println("Table Name is set to: " + tableName);
		if (tableName != null && !tableName.equals("")) {
			DAO.database = tableName;
		}
	}

	/*
	 * FROM HERE ON: deprecated!
	 */

	private BufferedWriter output = new BufferedWriter(new OutputStreamWriter(
			System.out));

	private MonitorState state = MonitorState.INIT;

	private boolean isMonitoring = false;

	protected String experimentDescription = "IS NOT SET";

	public final int MONITOR_START = 1;

	public final int MONITOR_STOP = 2;

	public final int MONITOR_TEST = 3;

	public void setStart(long time) {
		if (state.equals(MonitorState.STOP_SET)) {
			state = MonitorState.READY;
		} else if (state.equals(MonitorState.INIT)) {
			state = MonitorState.START_SET;
		} else {
			return;
		}
		Event.scheduleWithDelay(Time.getCurrentTime() + time, this, null,
				MONITOR_START);
		Event.scheduleWithDelay(0, this, null, MONITOR_TEST);

		Simulator.getInstance().addObserver(this);
	}

	public void setStop(long time) {
		if (state.equals(MonitorState.START_SET)) {
			state = MonitorState.READY;
		} else if (state.equals(MonitorState.INIT)) {
			state = MonitorState.STOP_SET;
		} else {
			return;
		}
		Event.scheduleWithDelay(time, this, null, MONITOR_STOP);
	}

	@Override
	public void simulationFinished() {
		close();
	}

	public void setExperimentDescription(String description) {
		this.experimentDescription = description;
	}

	public String getExperimentDescription() {
		return experimentDescription;
	}

	public void close() {
		if (this.isMonitoring && analyzers.size() != 0) {
			try {
				output.write("*******************************************************\n");
				output.write("# Monitoring results \n");
				output.newLine();
				for (Analyzer analyzer : analyzers) {
					analyzer.stop(output);
				}
				output.write("*******************************************************\n");
				output.close();
			} catch (IOException e) {
				throw new AssertionError();
			}
		}
		this.isMonitoring = false;
	}

	@Override
	public void eventOccurred(Object se, int type) {
		if (type == MONITOR_START) {
			this.isMonitoring = true;
			for (Analyzer analyzer : analyzers) {
				analyzer.start();
			}
		} else if (type == MONITOR_TEST) {
			//
		} else if (type == MONITOR_STOP) {
			this.close();
		} else {
			throw new AssertionError("Unknown event type.");
		}
	}

	private enum MonitorState {
		INIT, START_SET, STOP_SET, READY;
	}

}
