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

package de.tud.kom.p2psim.impl.simengine;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math.random.JDKRandomGenerator;
import org.apache.commons.math.random.RandomGenerator;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.api.scenario.Scenario;
import de.tud.kom.p2psim.api.scenario.ScenarioFactory;
import de.tud.kom.p2psim.api.simengine.SimulatorObserver;
import de.tud.kom.p2psim.impl.common.DefaultMonitor;
import de.tud.kom.p2psim.impl.scenario.DefaultConfigurator;
import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;
import de.tud.kom.p2psim.impl.util.NTPClient;
import de.tud.kom.p2psim.impl.util.db.dao.DAO;
import de.tud.kom.p2psim.impl.util.db.dao.metric.ExperimentDAO;
import de.tud.kom.p2psim.impl.util.db.dao.metric.MeasurementDAO;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.component.core.RandomGeneratorComponent;

/**
 * Concrete implementation of a simulator which can be used to run a simulation
 * by calling the main method in the SimulatorRunner class.
 *
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 11/29/2007
 *
 */
public class Simulator implements RandomGeneratorComponent, GlobalComponent {

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static double NANOSECOND_UNIT = 1E-3d;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long MICROSECOND_UNIT = Time.MICROSECOND;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long MILLISECOND_UNIT = Time.MILLISECOND;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long SECOND_UNIT = Time.SECOND;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long MINUTE_UNIT = Time.MINUTE;

	/**
	 * These constant should be ALWAYS used for virtual time calculations.
	 */
	public final static long HOUR_UNIT = Time.HOUR;

	/**
	 * Scenario holding all the information about the current simulation run.
	 */
	private Scenario scenario;

	/**
	 * Singleton instance of default simulator.
	 */
	private static Simulator singleton;

	/**
	 * Configurator instance is used to initialize the scenario.
	 */
	private static Configurator defaultConfigurator;

	private boolean running;

	private static long seed;

	private static Scheduler scheduler;

	private static RandomGenerator randomGen = new JDKRandomGenerator();

	private static boolean finishedWithoutError = false;

	private final List<SimulatorObserver> observers = new LinkedList<SimulatorObserver>();

	private static Map<Object, Random> randomGenerators = new LinkedHashMap<Object, Random>();

	private long startTime;

	private Date realWorldStartTime = null;

	/**
	 * This class is singleton, so use getInstance() method to obtain a
	 * reference to it.
	 *
	 */
	private Simulator() {
		singleton = this;
		scheduler = new Scheduler(true);
		Binder.registerComponent(scheduler);
		Binder.registerComponent(this);
		Binder.registerComponent(GlobalOracle.getInstance());
		Binder.registerComponent(DefaultMonitor.getInstance());
		// eventBus = new EventBus("Global Simulator Event Bus");
		Locale.setDefault(Locale.ENGLISH);
	}

	/**
	 * Returns the single instance of the SimulationFramework
	 *
	 * @return the SimulationFramework
	 */
	public static Simulator getInstance() {
		if (singleton == null)
			singleton = new Simulator();
		return singleton;
	}

	public void reset() {
		scheduler.reset();
	}

	/**
	 * Set the scenario (protocol stack, network topology etc.) which will be
	 * used to run the simulation.
	 *
	 * @param scenario
	 *            simulation scenario to be used
	 */
	public void setScenario(final Scenario scenario) {
		checkRunning();
		this.scenario = scenario;
	}

	/**
	 * Returns the scenario used to run the simulation.
	 *
	 * @return
	 */
	public Scenario getScenario() {
		return scenario;
	}

	/**
	 * Only for internal use
	 *
	 * @return
	 */
	public static Scheduler getScheduler() {
		return scheduler;
	}

	public static DefaultMonitor getMonitor() {
		return DefaultMonitor.getInstance();
	}

	/**
	 * This method will run the simulation using the previously set scenario
	 * data.
	 *
	 */
	public void start(final boolean throwExceptions) {
		checkRunning();
		Monitor.log(Simulator.class, Level.INFO, "Prepare Scenario ...");
		this.scenario.prepare();

		Monitor.log(Simulator.class, Level.INFO,
				"Prepare Scenario ..." + getSeed());

		/*
		 * Real World Starting Time: Block till we're allowed to start.
		 */
		if (realWorldStartTime != null) {

			try {

				// final Date now = new Date();
				final Date now = NTPClient.getDate();
				final long waitFor = realWorldStartTime.getTime()
						- now.getTime();

				Thread.sleep(waitFor);

			} catch (final InterruptedException e) {
				//
			}

		}

		startTime = System.currentTimeMillis();
		Monitor.log(Simulator.class, Level.INFO, "Simulation started...");
		this.running = true;
		Exception reason = null;
		try {

			scheduler.start();
			finishedWithoutError = true;

		} catch (final Exception e) {
			finishedWithoutError = false;
			reason = e;
			throw e;
		} finally {
			this.running = false;
			// After a simulation start the mechanisms, which
			// finalize a simulation
			shutdownSimulation(reason);
		}
	}

	public void shutdownSimulation(final Exception reason) {
		this.running = false;
		if (finishedWithoutError) {
			Monitor.log(Simulator.class, Level.INFO,
					"Simulation successfully finished...");
		} else {
			Monitor.log(Simulator.class, Level.ERROR,
					"Simulation finished with errors...:\n%s", reason);
			if (reason != null) {
				final StringWriter stringWriter = new StringWriter();
				reason.printStackTrace(new PrintWriter(stringWriter));
				Monitor.log(getClass(), Level.ERROR, "Stacktrace: %s",
						stringWriter.toString());
			}
		}
		final long runTime = System.currentTimeMillis() - startTime;
		final long minutes = (long) Math.floor((runTime) / 60000);
		final long secs = (runTime % 60000) / 1000;
		Monitor.log(Simulator.class, Level.INFO,
				"Realtime Duration of experiment (m:s) " + minutes + ":"
						+ secs);

		for (final SimulatorObserver so : observers) {
			so.simulationFinished();
		}

		ExperimentDAO.simulationFinished();
	}

	/**
	 * Configure simulation from an XML file.
	 *
	 * @param configFile
	 *            XML file with the configuration data.
	 * @param variables
	 *            the variables which are specified in the XML file with the
	 *            configuration data.
	 */
	public void configure(final String configFile,
			final Map<String, String> variables,
			final Map<String, String> modifiedVariables,
			final List<String> variations) {
		// TODO create a class, that contains general informations of the
		// simulation, which can be accessed from every component during a
		// simulation. This can be seen as an alternative to implementing the
		// Composable interface
		if (configFile.endsWith(".xml")) {
			this.defaultConfigurator = new DefaultConfigurator(configFile);
			defaultConfigurator.setVariables(variables);
		} else {
			final SimCfgConfigurator simCfgConfigurator = new SimCfgConfigurator(
					configFile);
			simCfgConfigurator.applyVariations(variations);
			simCfgConfigurator.setVariables(modifiedVariables);

			this.defaultConfigurator = simCfgConfigurator;
		}

		this.defaultConfigurator.register(Configurator.CORE, this);

		this.defaultConfigurator.configureAll();

		// The next steps are only required for the xml version.
		// SimCfg will execute them in the configurator
		if (configFile.endsWith(".xml")) {
			final ScenarioFactory scenarioBuilder = (ScenarioFactory) this.defaultConfigurator
					.getConfigurable(Configurator.SCENARIO_TAG);

			if (scenarioBuilder == null)
				throw new ConfigurationException(
						"No scenario builder specified in the configuration file. Nothing to do.");

			final Scenario scenario = scenarioBuilder.createScenario();
			setScenario(scenario);
		}
	}

	/**
	 * Returns the seed used within a simulation run.
	 *
	 * @return the predefined seed
	 *
	 */
	public static long getSeed() {
		return seed;
	}

	/**
	 * This method sets the seed of the global random generator which can be
	 * obtained using the static getRandom()-method.
	 *
	 * @param seed
	 *            the seed to configure the global random generator
	 */
	public void setSeed(final long seed) {
		checkRunning();
		this.seed = seed;
		randomGen.setSeed(seed);
	}

	/**
	 * Assure that the set methods are not called after the simulation has
	 * started.
	 */
	private void checkRunning() {
		if (this.running)
			throw new IllegalStateException("Simulator is already running.");
	}

	/**
	 * This method provides a newly created random generator, configured with
	 * the specified seed of the simulator. This method should be preferred over
	 * the deprecated @{link getRandom}.
	 *
	 * <b>Notes:</b> Doing so will prevent components from influencing each
	 * other, thus creating reproducible results while allowing different
	 * compositions of components.
	 *
	 * The method allows the use of an object as source. This is to allow the
	 * use in different situations: 1. Random generators belonging to a single
	 * component should pass the class object of that component to this method.
	 * 2. Components that are always loaded during a simulation can use the
	 * Simulator.class object for a system wide random generator like
	 * {@link #getRandom()} provided. 3. Utility classes that are loaded by many
	 * different components should use a reference to themselves to avoid
	 * transitive dependencies on the same random generator throughout different
	 * components.
	 *
	 * This approach will provide way to to configure different seeds for
	 * specific class objects. This will allow component specific seeds and,
	 * e.g., the transfer of movement behavior from one configured scenario to
	 * another.
	 *
	 * @param source
	 *            The source that requires the random generator (component class
	 *            object is preferred)
	 * @param salt
	 *            Added to the default seed, should be bound
	 * @return The random generator for the given source
	 */
	@Override
	public Random getRandom(final Object source) {
		if (randomGenerators.containsKey(source)) {
			return randomGenerators.get(source);
		} else {
			final long thisSeed = source.toString().hashCode() + 31 * seed;
			Monitor.log(Simulator.class, Level.INFO,
					"Created a new Random Source for %s with seed %d", source,
					thisSeed);
			final Random randomGenerator = new Random(thisSeed);
			randomGenerators.put(source, randomGenerator);
			return randomGenerator;
		}
	}

	/**
	 * Returns the current simulation unit value.
	 *
	 * @return the current simulation unit value
	 */
	public static long getCurrentTime() {
		return Time.getCurrentTime();
	}

	/**
	 * Returns the start time of the simulation.
	 *
	 * @return
	 */
	public static long getStartTime() {
		return singleton.startTime;
	}

	/**
	 * Returns the end time of the simulation.
	 *
	 * @return
	 */
	public static long getEndTime() {
		return scheduler.getEndTime();
	}

	/**
	 * Sets the end time at which the simulation framework will finish at the
	 * latest the simulation , irrespective if there are still unprocessed
	 * events in the event queue.
	 *
	 * @param endTime
	 *            point in time at which the simular will finish at the latest
	 */
	public void setFinishAt(final long endTime) {
		checkRunning();
		this.scheduler.setFinishAt(endTime);
	}

	static boolean isFinishedWithoutError() {
		return finishedWithoutError;
	}

	/**
	 * This method delegates the request to the MeasurementDAO which in turn
	 * will save the state in a static variable. Is this flag set to true the
	 * MeasurementDAO will return from every method before doing any work.
	 *
	 * @param inactive
	 */
	public void setDatabaseInactive(final boolean inactive) {
		MeasurementDAO.setInactive(inactive);
		if (inactive) {
			Monitor.log(Simulator.class, Level.WARN,
					"Database output has been deactived. See the databaseInactive parameter of the Simulator for more information.");
		}
	}

	/**
	 * Overrides the database name provided in the persistence.xml.
	 *
	 * As the DAO class only provides static methods and is not a singleton it
	 * cannot be accessed through the config file otherwise.
	 *
	 * @param database
	 * @deprecated use the MetricOutputDAO instead to do all relevant
	 *             configuration!
	 */
	@Deprecated
	public void setDatabase(final String database) {
		DAO.database = database;
	}

	/**
	 * Can be used to format the absolute simulation time (current, past or
	 * future) into human-readable format: (h:m:s:ms).
	 *
	 * @param time
	 *            - absolute simulation time like the one obtained via
	 *            getCurrentTime();
	 * @return human-readable representation of the given simulation time
	 */
	public static String getFormattedTime(final long time) {
		return Time.getFormattedTime(time);
	}

	/**
	 * Specifies how often the scheduler will printout the current simulation
	 * time.
	 *
	 * @param time
	 */
	public void setStatusInterval(final long time) {
		scheduler.setStatusInterval(time);
	}

	public void setRealTime(final boolean realTime) {
		scheduler.setRealTime(realTime);
	}

	public void setTimeSkew(final double timeSkew) {
		scheduler.setTimeSkew(timeSkew);
	}

	public void setSimulationSpeedLocked(final boolean locked) {
		scheduler.setSimulationSpeedLocked(locked);
	}

	public static Configurator getConfigurator() {
		return defaultConfigurator;
	}

	/**
	 * Sets the real world start time.
	 *
	 * @param realWorldStartTime
	 *            the new real world start time
	 */
	public void setRealWorldStartTime(final String realWorldStartTime) {

		final Date now = new Date();
		final SimpleDateFormat formatter = new SimpleDateFormat(
				"HH:mm:ss z dd.MM.yyyy", Locale.GERMANY);

		try {

			final Date startTime = formatter.parse(realWorldStartTime);
			if (now.after(startTime)) {
				throw new AssertionError(
						"Simulator was supposed to run in the past. Check config for realWorldStartTime.");
			}

			this.realWorldStartTime = startTime;

		} catch (final ParseException e) {
			Monitor.log(Simulator.class, Level.WARN,
					"Could not parse realWorldStartTime. Please check configuration file. Starting *NOW*");
		}

	}

	public void addObserver(final SimulatorObserver observer) {
		observers.add(observer);
	}
}
