/*
 * Copyright (c) 2005-2010 KOM - Multimedia Communications Lab
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PeerfactSim.KOM. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package de.tudarmstadt.maki.simonstrator.peerfact.application.sensor.util.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Helper class for simulation execution. Can execute multiple simulation tasks
 * in multiple processes and terminates when all tasks are finished. Executes
 * the simulations in parallel. The upper bound for parallelism can be defined.
 *
 * In case of failures the called processes need to terminate using a return
 * value != 0. This way {@link TaskExecutor} detects exceptions and marks the
 * task as failed.
 *
 * @author Michael Stein
 * @author Roland Kluge
 */
public class TaskExecutor {
	/**
	 * Log
	 */
	Logger log = Logger.getLogger(TaskExecutor.class);

	/**
	 * A string that may be appended to the JVM option -Xmx
	 *
	 * Valid inputs are, e.g., 1000m, 2g
	 *
	 * If null, no -XmX option is appended when creating processes
	 */
	private String jvmParameterXmx;

	/**
	 * Simulations that will be executed
	 */
	protected List<SimulationTask> simulations;

	/**
	 * Flag which indicates whether the output of sub processes will be written
	 * on the console of this process
	 */
	protected final boolean PRINT_CONSOLE_STREAMS = true;

	/**
	 * Number of parallel executed tasks
	 */
	protected final int PARALLEL_TASKS;

	/**
	 * Count completed tasks
	 */
	protected List<SimulationTask> completedTasks = new LinkedList<SimulationTask>();

	/**
	 * Failed processes
	 */
	protected List<SimulationTask> failedTasks = new LinkedList<SimulationTask>();

	private List<Process> startedProcesses = Collections.synchronizedList(new LinkedList<Process>());

	/**
	 * Execution service
	 */
	public ExecutorService execService;

	/**
	 * Remember time for statistics
	 */
	protected long calculationStartTime;

	/**
	 * If set to true the results are written to the working directory.
	 * Otherwise
	 */

	protected boolean changeWorkingDirectory = false;

	private List<TaskExecutorLifecycleHook> terminationHooks = new LinkedList<>();;

	/**
	 * Constructs a TaskExecutor.
	 *
	 * @param simulations
	 *            simulations tasks
	 * @param parallelTasks
	 *            the number of tasks that are executed in parallel
	 */
	public TaskExecutor(List<SimulationTask> simulations, int parallelTasks) {
		this.simulations = simulations;
		this.PARALLEL_TASKS = parallelTasks;
	}

	/**
	 * Constructs a TaskExecutor which constructs as many jobs in parallel as
	 * CPU cores are available.
	 *
	 * @param simulations
	 *            simulation tasks
	 */
	public TaskExecutor(List<SimulationTask> simulations) {
		this(simulations, Runtime.getRuntime().availableProcessors());
	}

	public void addTerminationHook(final TaskExecutorLifecycleHook hook) {
		if (null == hook)
			throw new IllegalArgumentException("Hook must not be null.");
		this.terminationHooks.add(hook);
	}

	public void removeTerminationHook(final TaskExecutorLifecycleHook hook) {
		this.terminationHooks.remove(hook);
	}

	public Collection<TaskExecutorLifecycleHook> getTerminationHooks() {
		return Collections.unmodifiableCollection(this.terminationHooks);
	}

	public Collection<Process> getStartedProcesses() {
		return startedProcesses;
	}

	public Collection<SimulationTask> getFailedTasks() {
		return Collections.unmodifiableList(failedTasks);
	}

	public void setJvmOptionXmx(final String memoryLimit) {
		this.jvmParameterXmx = memoryLimit;
	}

	/**
	 * Starts the execution of the specified simulations.
	 */
	public void start() {
		log.info("Simulation Task Processing starts." + simulations.size() + " Tasks. Executing " + PARALLEL_TASKS
				+ " tasks in parallel.");

		// remember start time
		this.calculationStartTime = System.currentTimeMillis();

		// enqueue all tasks in the Executor
		execService = Executors.newFixedThreadPool(PARALLEL_TASKS);
		for (SimulationTask task : simulations) {
			log.debug("Enqueuing task: " + task);
			execService.execute(new SimProcessRunner(task));
		}

		Thread closeChildProcesses = new Thread() {
			public void run() {
				destroyAllStartedProcesses();
			}
		};

		Runtime.getRuntime().addShutdownHook(closeChildProcesses);

		log.info("All tasks enqueued.");
	}

	/**
	 * Shuts the executor service down and kills all child processes
	 */
	public void stop() {
		execService.shutdownNow();
		destroyAllStartedProcesses();
	}

	private void destroyAllStartedProcesses() {
		synchronized (startedProcesses) {
			log.info(String.format("Destroying all remaining %d child processes.", startedProcesses.size()));
			ArrayList<Process> processList = new ArrayList<>(startedProcesses);
			for (final Process process : processList) {
				try {
					process.destroy();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Returns whether all tasks are finished (whether they failed or not)
	 */
	public boolean areAllTasksFinished() {
		return failedTasks.size() + completedTasks.size() == simulations.size();
	}

	/**
	 * Calculates the human readable time difference between start and end time.
	 *
	 * @param start
	 *            Time in Millis at the beginning
	 * @param end
	 *            Time in Millis at the end time
	 * @return
	 */
	protected static String getTimeDifference(long start, long end) {
		// http://www.raditha.com/blog/archives/552.html

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		long elapsed = end - start;

		return dateFormat.format(new Date(elapsed));
	}

	/**
	 * Encapsulates the process creation logic. It is important to run each
	 * simulation in its own Java VM to avoid shared state.
	 */
	private class SimProcessRunner implements Runnable {
		/**
		 * The task that will be executed
		 */
		private SimulationTask t;

		/**
		 * Constructs a SimProcessRunner
		 *
		 * @param t
		 *            corresponding task
		 */
		public SimProcessRunner(SimulationTask t) {
			this.t = t;
		}

		/**
		 * Converts the given relative path into an absolute path.
		 */
		private String getAbsolutePath(String relativePath) throws IOException {
			File f = new File(relativePath);
			return f.getCanonicalPath();
		}

		@Override
		public void run() {
			log.info("Now executing task: " + t);

			// remember for statistics
			long startTime = System.currentTimeMillis();

			try {
				try {
					Thread.sleep(300);
				} catch (Exception e) {
					// ignore
				}
				// use the same classpath and properties for the sub process as
				// for this process
				String separator = System.getProperty("file.separator");
				String classpath = System.getProperty("java.class.path");
				// convert relative classpath elements into absolute class path
				// elements
				String[] subPaths = classpath.split(";");
				LinkedList<String> cpList = new LinkedList<String>();
				for (String subPath : subPaths) {
					cpList.add(this.getAbsolutePath(subPath));
				}
				classpath = StringUtils.join(cpList, ";");

				String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
				Class<?> c = t.getSimulationClass();
				// peerfact parameters
				List<String> peerfactParams = new LinkedList<String>(t.getParams());
				// first parameter is the config file path. make sure that it is
				// absolute, since the working directory will be changed
				String relativeXmlConfigPath = peerfactParams.remove(0);
				String absoluteXmlConfigPath = this.getAbsolutePath(relativeXmlConfigPath);
				peerfactParams.add(0, absoluteXmlConfigPath);

				// construct execution command
				List<String> params = new LinkedList<String>();
				params.add(path); // set classpat
				if (jvmParameterXmx != null)
					params.add("-Xmx" + jvmParameterXmx);
				params.add("-cp");
				params.add(classpath);
				params.add(c.getName()); // set process entry point class
				params.addAll(peerfactParams); // set additional parameters
				log.debug("Process execution parameters: " + params);

				ProcessBuilder processBuilder = new ProcessBuilder(params);

				// mix both output streams
				processBuilder.redirectErrorStream(true);
				// change working directory
				String configFileDirectory = new File(absoluteXmlConfigPath).getParent();

				if (changeWorkingDirectory) {
					String resultDirectoryPath = configFileDirectory + "\\results\\"
							+ StringUtils.join("_", peerfactParams.subList(1, peerfactParams.size()));
					File resultDirectory = new File(resultDirectoryPath);
					System.out.println("trying to create directory: " + resultDirectory);
					resultDirectory.mkdirs();
					processBuilder.directory(resultDirectory);
				} else {
					// processBuilder.directory(new File(configFileDirectory));
				}

				// start the process
				Process process = processBuilder.start();
				startedProcesses.add(process);
				log.debug("Process started: " + process);

				// read the output stream. otherwise the process will block as
				// soon as the output stream is full (assumption)
				BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
				String line;
				while ((line = br.readLine()) != null) {
					// only print log if necessary
					if (PRINT_CONSOLE_STREAMS)
						System.out.println(line);
				}

				// wait for process to finish
				if (process.waitFor() == 0) {
					log.info("Task finished successfully: " + t);

					synchronized (TaskExecutor.this) {
						completedTasks.add(t);
					}
				} else {
					log.fatal("Task failed: " + t);
					synchronized (TaskExecutor.this) {
						failedTasks.add(t);
					}
				}
				for (final TaskExecutorLifecycleHook terminationHook : TaskExecutor.this.getTerminationHooks()) {
					terminationHook.run();
				}

				startedProcesses.remove(process);
				log.debug("Task execution time was: " + getTimeDifference(startTime, System.currentTimeMillis())
						+ " for task: " + t);

			} catch (Throwable e) {
				log.fatal("Execution fail", e);

				synchronized (TaskExecutor.this) {
					failedTasks.add(t);
				}
			}

			/**
			 * All simulations finished?
			 */
			synchronized (TaskExecutor.this) {
				log.debug("Check for complete execution");
				final boolean allSimulationsFinished = TaskExecutor.this.areAllTasksFinished();
				if (allSimulationsFinished) {
					String simulationDuration = getTimeDifference(calculationStartTime, System.currentTimeMillis());
					log.debug("Will shutdown executor");

					// allow this process to terminate
					execService.shutdown();

					log.info("Simulation calculations complete! Executed " + simulations.size() + " tasks.\n"
							+ +completedTasks.size() + " tasks successful: " + completedTasks + "\n"
							+ failedTasks.size() + " tasks failed: " + failedTasks + "\nCalculation duration: "
							+ simulationDuration);

				} else {
					log.info("Current progress: " + completedTasks.size() + " successful, " + failedTasks.size()
							+ " failed of a total of " + simulations.size());
				}
			}
		}

	}
}