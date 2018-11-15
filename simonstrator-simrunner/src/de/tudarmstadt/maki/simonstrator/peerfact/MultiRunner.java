package de.tudarmstadt.maki.simonstrator.peerfact;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import de.tudarmstadt.maki.simonstrator.peerfact.SimulatorRunnerExt.Command;
import de.tudarmstadt.maki.simonstrator.peerfact.multirunner.SimulationDescription;
import de.tudarmstadt.maki.simonstrator.peerfact.multirunner.SimulationLoader;

/**
 * This class seeks to replace the bash-script mania in that it supports
 * parallel execution of simulation tasks with more convenient run processing
 * and a basic interactive shell.
 * 
 * TODO add interface for post-processing toolchain
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class MultiRunner implements SimulationLoader.Callback {

	private enum Status {
		WAITING, RUNNING, FINISHED, ERROR, CANCELED
	}

	private static MultiRunner instance = null;

	public static final Queue<SimProcess> waiting = new LinkedList<SimProcess>();

	public static final List<SimProcess> running = new LinkedList<SimProcess>();

	public static final List<SimProcess> all = new LinkedList<SimProcess>();

	private long timeStarted = 0;

	public static boolean SIMS_RUNNING = false;

	public static boolean SIMS_LOADED = false;

	private final SimulationLoader simLoader;

	public static final Properties properties = new Properties();


	public MultiRunner(SimulationLoader simLoader) {
		assert instance == null;
		instance = this;
		this.simLoader = simLoader;
		loadSimulations();

		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				//
			}
		}
	}

	/**
	 * arg[0] name of the loader class (has to be located in subpackage
	 * multirunner). Omit the "SimLoader"-part -> for SimLoaderTest, just pass
	 * "Test"
	 * 
	 * - all other args are passed to the loader.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			// show help and exit
			System.err.println("Usage: MultiRunner <loader> [loader args]*");
			System.exit(1);
		} else {
			// Find, instantiate, and load loader.
			String loaderName = "SimLoader" + args[0];
			String classPath = "de.tudarmstadt.maki.simonstrator.peerfact.multirunner.";
			System.out.println("Loading SimLoader " + loaderName);
			try {
				Class<?> cls = Class.forName(classPath + loaderName);
				String[] loaderArgs = Arrays.copyOfRange(args, 1, args.length);
				SimulationLoader loaderClass = (SimulationLoader) cls
						.newInstance();
				loaderClass.initialize(loaderArgs);
				new MultiRunner(loaderClass);
			} catch (ClassNotFoundException e) {
				System.err.println("Class " + loaderName + " not found at "
						+ classPath);
				System.exit(1);
			} catch (InstantiationException e) {
				System.err.println("Class " + loaderName
						+ " instantiation failed.");
				System.exit(1);
			} catch (IllegalAccessException e) {
				System.err.println("Class " + loaderName
						+ " resulted in IllegalAccess.");
				System.exit(1);
			}
		}
	}

	private void loadSimulations() {
		if (SIMS_LOADED) {
			throw new IllegalStateException("Simulations were already loaded.");
		}
		// Load property-defaults
		try {
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream("assets/multiRunner.default.properties"));
			properties.load(stream);
			stream.close();
		} catch (IOException e) {
			System.err
					.println("Properties-file assets/multiRunner.default.properties not found!");
			System.exit(1);
		}
		// Load custom overwrites
		try {
			BufferedInputStream stream = new BufferedInputStream(
					new FileInputStream("assets/multiRunner.properties"));
			Properties overwrites = new Properties();
			overwrites.load(stream);
			stream.close();
			for (String key : overwrites.stringPropertyNames()) {
				properties.setProperty(key, overwrites.getProperty(key));
				System.out.println("Setting " + key
						+ " property to new value: "
						+ overwrites.getProperty(key));
			}
		} catch (IOException e) {
			System.out.println("No custom properties found...");
		}

		// Next statement is blocking.
		boolean autostart = simLoader.loadSimulations(this);
		SIMS_LOADED = true;
		if (autostart) {
			System.out.println(">> Autostarting Simulations");
			startSimulations();
		}
		// Start the shell
		System.out.println(">> Starting the SimShell");
		new SimShell().start();
	}

	@Override
	public void addSimulation(SimulationDescription simulation) {
		if (SIMS_LOADED) {
			throw new IllegalStateException("Simulations were already loaded.");
		}
		addProcess(new SimProcess(this, simulation));
	}

	private void startSimulations() {
		if (SIMS_RUNNING) {
			return;
		}
		SIMS_RUNNING = true;
		timeStarted = System.currentTimeMillis();
		for (int i = 0; i < simLoader.getNumberOfParallelInstances(); i++) {
			startNextProcess();
		}
	}

	public static void startAllSimulations() {
		instance.startSimulations();
	}

	private void addProcess(SimProcess p) {
		waiting.add(p);
		all.add(p);
	}

	public void onProcessStatusChanged(SimProcess process,
			Status newStatus) {
		if (newStatus == Status.FINISHED) {
			running.remove(process);
			startNextProcess();
		}
		if (newStatus == Status.ERROR) {
			System.out.println("Error messages: ");
			System.out.println(process.getErrorMessage());
			running.remove(process);
			startNextProcess();
		}
	}

	private void startNextProcess() {
		if (!waiting.isEmpty()) {
			System.out
					.println("=== Starting next Simulation... ("
							+ (all.size() - waiting.size()) + " of "
							+ all.size() + ")");
			SimProcess pr = waiting.poll();
			running.add(pr);
			pr.start();
		} else if (running.isEmpty()) {
			finished();
		}
	}

	private void finished() {
		System.out.println();
		System.out.println("====================================");
		System.out.println("=== Finished all Simulations!");
		System.out.println("=== Total time: "
				+ prettyTimeInterval(System.currentTimeMillis() - timeStarted));
		int ok = 0;
		int err = 0;
		for (SimProcess p : all) {
			if (p.getStatus() == Status.ERROR) {
				err++;
			} else {
				ok++;
			}
		}
		System.out.println("=== " + ok + " of " + (ok + err)
				+ " simulations were successful.");
		System.out.println();
		System.out.println("-- goodbye!");
		System.exit(0);
	}

	public static String prettyTimeInterval(long ms) {
		final long hr = TimeUnit.MILLISECONDS.toHours(ms);
		final long min = TimeUnit.MILLISECONDS.toMinutes(ms
				- TimeUnit.HOURS.toMillis(hr));
		final long sec = TimeUnit.MILLISECONDS.toSeconds(ms
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min));
		final long tms = TimeUnit.MILLISECONDS.toMillis(ms
				- TimeUnit.HOURS.toMillis(hr) - TimeUnit.MINUTES.toMillis(min)
				- TimeUnit.SECONDS.toMillis(sec));
		return String.format("%02d:%02d:%02d.%03d", hr, min, sec, tms);
	}

	/**
	 * Shamelessly derived from
	 * http://stackoverflow.com/questions/636367/executing
	 * -a-java-application-in-a-separate-process
	 */
	public static final class SimProcess extends Thread {
		
		public static int currentID = 0;
		
		private final int id;

		private Status ownStatus = Status.WAITING;

		private final SimulationDescription desc;

		private final StringBuilder errorBuilder = new StringBuilder();

		private final ProcessBuilder processBuilder;

		public Process process;

		private final MultiRunner runner;

		private SimProcessCommands simCmds;

		private long timestampStarted;

		private long timestampFinished;

		public SimProcess(MultiRunner runner, SimulationDescription desc) {
			this.id = ++currentID;
			this.runner = runner;
			this.desc = desc;

			// Environment
			String javaHome = System.getProperty("java.home");
			String javaBin = javaHome + File.separator + "bin" + File.separator
					+ "java";
			String classpath = System.getProperty("java.class.path");

			String className = SimulatorRunnerExt.class.getCanonicalName();

			List<String> cmds = new LinkedList<String>();
			cmds.add(javaBin);
			cmds.add("-Dsimprc=" + id);
			cmds.add("-Xms" + properties.getProperty("jvm.xms"));
			cmds.add("-Xmx" + properties.getProperty("jvm.xmx"));
			if (properties.getProperty("jvm.assertions").equals("true")) {
				cmds.add("-ea");
			}
			cmds.add("-cp");
			cmds.add(classpath);
			cmds.add(className);
			cmds.addAll(desc.getArgs());

			// processBuilder = new ProcessBuilder(javaBin, "-cp", classpath,
			// className, "-Xms" + properties.getProperty("jvm.xms"),
			// desc.getArgs());
			processBuilder = new ProcessBuilder(cmds);
			// This is very important - otherwise: blocking syserr-calls in
			// overlays!
			processBuilder.redirectErrorStream(true);
		}

		public Status getStatus() {
			return ownStatus;
		}

		@Override
		public void run() {
			this.setName("SimProcess " + id);
			try {
				process = processBuilder.start();
				statusChange(Status.RUNNING);
				// next statement blocks.
				try {
					timestampStarted = System.currentTimeMillis();
					new SimProcessLogger(SimProcess.this).start();
					simCmds = new SimProcessCommands(SimProcess.this);
					simCmds.start();
					process.waitFor();
					if (process.exitValue() == 0) {
						statusChange(Status.FINISHED);
					} else {
						statusChange(Status.ERROR);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
					statusChange(Status.ERROR);
				}
			} catch (IOException e) {
				e.printStackTrace();
				statusChange(Status.ERROR);
			}
			timestampFinished = System.currentTimeMillis();
		}

		private void statusChange(Status newStatus) {
			ownStatus = newStatus;
			runner.onProcessStatusChanged(this, ownStatus);
		}

		public boolean cancel() {
			if (waiting.remove(this)) {
				statusChange(Status.CANCELED);
				return true;
			}
			return false;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(desc.getName());
			switch (ownStatus) {
			case WAITING:
				sb.append(" waiting " + desc.getArgs());
				break;
				
			case RUNNING:
				sb.append(" running since "+prettyTimeInterval(System.currentTimeMillis()-timestampStarted));
				break;
				
			case CANCELED:
				sb.append(" CANCELED ");
				break;

			case ERROR:
			case FINISHED:
				sb.append(" "
						+ ownStatus.toString().toLowerCase()
						+ ", took "
						+ prettyTimeInterval(timestampFinished
								- timestampStarted));
				break;

			default:
				break;
			}
			return sb.toString();
		}

		public SimulationDescription getDescription() {
			return desc;
		}

		public void sendCommand(Command cmd, String args) {
			if (simCmds != null) {
				simCmds.sendCommand(cmd, args);
			} else {
				// Display some static information - enable users to delete the
				// Simulation here?
				System.out.println("-- currently inactive. " + this.toString());
			}
		}

		public String getErrorMessage() {
			return errorBuilder.toString();
		}

	}

	/**
	 * Allows to send commands to the stdin of the current simulation process.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public static final class SimProcessCommands extends Thread {

		private final SimProcess simProcess;
		
		private String nextArgs;
		
		private Command nextCmd;

		public SimProcessCommands(SimProcess simProcess) {
			this.simProcess = simProcess;
			setDaemon(true);
		}

		public void sendCommand(Command cmd, String args) {
			this.nextCmd = cmd;
			this.nextArgs = args;
			SimProcessCommands.this.interrupt();
		}

		@Override
		public void run() {
			this.setName("SimProcessCmds");
			OutputStream out = simProcess.process.getOutputStream();
			BufferedWriter bOut = new BufferedWriter(new OutputStreamWriter(out));

			while (true) {
				try {
					sleep(10000);
				} catch (InterruptedException e) {
					if (nextCmd != null) {
						try {
							bOut.write(SimulatorRunnerExt.CMD + nextCmd.cmd
									+ nextArgs);
							bOut.newLine();
							bOut.flush();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						nextCmd = null;
					}
				}
			}
		}
	}

	/**
	 * Interactive shell
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public static final class SimShell extends Thread {

		public enum SimShellCommands {

			HELP("help", "Shows the help page for the interactive shell."),

			GO("go", "Starts the currently configured batch of simulations."),

			CD("cd",
					"Switches to the shell of the simulation with the given <Name/ID>."),

			LS(
					"ls",
					"Lists all currently running simulations. -a shows all, -s <criteria> to search, -l live"),

			RM("rm", "Unshedules the simulation with the given <Name/ID>"),

			EXIT("exit", "Quits the shell and aborts all simulations.");

			private SimShellCommands(String cmd, String help) {
				this.cmd = cmd;
				this.help = help;
			}

			public final String help;

			public final String cmd;

		}

		private SimProcess activeP = null;

		public SimShell() {
			// empty
		}

		public void run() {
			this.setName("SimShell");

			BufferedReader br = new BufferedReader(new InputStreamReader(
					System.in));

			printShellHeader();

			String s;

			Command lastCmd = null;
			SimShellCommands lastGlobalCmd = null;

			while (true) {
				try {
					s = br.readLine();
					// check for valid commands. Show help otherwise.
					lastGlobalCmd = null;
					for (SimShellCommands cmd : SimShellCommands.values()) {
						// check for global return command
						if (s.startsWith(cmd.cmd)) {
							// cmd found -> "send" and wait for reply
							lastGlobalCmd = cmd;
							s = s.substring(cmd.cmd.length()).trim();
							executeCommand(cmd, s);
							try {
								sleep(100);
							} catch (InterruptedException e) {
								//
							}
						}
					}
					if (lastGlobalCmd == null) {
						System.out
								.println("=== Unsupported input. "+SimShellCommands.HELP.help);
					}

					if (activeP != null) {
						// Just switching into active shell of a single sim
						printSimulationShellHeader();
					}
					
					// Simulation-specific shell loop
					while (activeP != null) {
						s = br.readLine();
						// check for valid commands. Show help otherwise.
						lastCmd = null;
						for (Command cmd : Command.values()) {
							// check for global return command
							if (s.startsWith(cmd.cmd)) {
								// cmd found -> "send" and wait for reply
								lastCmd = cmd;
								s = s.substring(cmd.cmd.length());
								if (cmd == Command.QUIT) {
									// return to upper shell level
									printShellHeader();
									activeP = null;
									break;
								}
								activeP.sendCommand(cmd, s);
								try {
									sleep(100);
								} catch (InterruptedException e) {
									//
								}
							}
							}
						if (lastCmd == null) {
							System.out.println("!!! Unsupported input. Use "
									+ Command.HELP.cmd + " for help!");
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		private void printSimulationShellHeader() {
			System.out.println("## SimShell for "
					+ activeP.getDescription().getName());
			System.out.println("## " + Command.HELP.help);
			System.out.println("## " + Command.QUIT.help);
		}

		private void printShellHeader() {
			if (SIMS_RUNNING) {
				System.out.println("===");
				System.out.println("=== SimShell - interactive mode");
				System.out.println("===");
				System.out.println("=== Type " + SimShellCommands.HELP.cmd
						+ " for - you guessed it - help");
			} else {
				System.out.println("===");
				System.out.println("=== SimShell - pre-simulation mode");
				System.out.println("===");

				System.out.println("=== Your simulations are ready to <go>");
				System.out
						.println("=== You can check and alter them using <ls>");
				System.out.println("=== Type " + SimShellCommands.HELP.cmd
						+ " for - you guessed it - help");
			}
		}

		private void printHelp() {
			System.out.println("=== Help ");
			for (SimShellCommands command : SimShellCommands.values()) {
				System.out.println(command.cmd);
				System.out.println("\t" + command.help);
			}
			System.out.println(" - SimShell by BR, KOM");
		}

		private SimProcess findProcess(String nameOrId) {
			int id = 0;
			try {
				id = Integer.parseInt(nameOrId);
			} catch (NumberFormatException e) {
				//
			}
			for (SimProcess process : all) {
				if (process.id == id
						|| process.getDescription().getName().equals(nameOrId)) {
					return process;
				}
			}
			return null;
		}

		public void executeCommand(SimShellCommands cmd, String args) {
			switch (cmd) {
			case EXIT:
				System.out.println("== goodbye!");
				System.exit(0);
				break;

			case CD:
				// find ID/NAME
				activeP = findProcess(args);
				if (activeP == null) {
					System.out
							.println("!!! no simulation is matching your criteria!");
				} else {
					System.out.println(">> switching to Simulation "
							+ activeP.getDescription().getName());
				}
				break;

			case RM:
				SimProcess p = findProcess(args);
				if (p != null && waiting.contains(p)) {
					p.cancel();
					System.out.println(">> removed simulation " + p.toString());
				} else {
					System.out
							.println("!!! no waiting simulation is matching your criteria!");
				}
				break;

			case LS:
				System.out.println(">> " + waiting.size() + " waiting, "
						+ running.size()
						+ " running, "
						+ all.size() + " in total.");
				if (args.trim().startsWith("-a")) {
					for (SimProcess process : all) {
						System.out.println(">>\t[" + process.id + "]\t"
								+ process.toString());
					}
				} else if (args.trim().startsWith("-s")) {
					String search = args.substring(2).trim();
					int found = 0;
					for (SimProcess process : all) {
						if (process.getDescription().getName().contains(search)) {
							found++;
							System.out.println(">>\t[" + process.id + "]\t"
									+ process.toString());
						}
					}
					System.out.println(">> found " + found + " results.");
				} else if (args.trim().startsWith("-l")) {
					/*
					 * This will not work in an IDE
					 * http://stackoverflow.com/questions
					 * /14792478/how-to-get-carriage
					 * -return-without-line-feed-effect-in-eclipse-console)
					 */
					// TODO (single line updates)
				} else {
					System.out
							.println(">> Showing only running simulations. Use "
									+ SimShellCommands.LS.cmd
									+ " -a to show all simulations.");
					if (SIMS_RUNNING) {
						for (SimProcess process : running) {
							System.out.println(">>\t[" + process.id + "]\t"
									+ process.toString());
						}
					} else {
						System.out
								.println(">> Currently, no simulations are running.");
					}
				}

				break;

			case GO:
				if (SIMS_RUNNING) {
					System.out
							.println(">> Simulations are already running... ");
					break;
				}
				System.out.println(">> Starting simulations... ");
				startAllSimulations();
				break;

			case HELP:
				printHelp();
				break;

			default:
				break;
			}
		}
	}


	/**
	 * Retrieves the output of the current simulation process.
	 * 
	 * TODO unify reply handling.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public static final class SimProcessLogger extends Thread {

		private final SimProcess simProcess;

		public SimProcessLogger(SimProcess simProcess) {
			this.simProcess = simProcess;
			setDaemon(true);
		}

		@Override
		public void run() {
			this.setName("SimProcessLog");
			BufferedInputStream bIn = new BufferedInputStream(
					simProcess.process.getInputStream());
			Scanner sc = new Scanner(bIn);
			String s = "";
			while (sc.hasNextLine()) {
				s = sc.nextLine();
				System.out.println(s);
				if (s.startsWith(SimulatorRunnerExt.REP)) {
					s = s.substring(SimulatorRunnerExt.REP.length()).trim();
					System.out.println(s);
				}
			}
			sc.close();
		}

	}

}
