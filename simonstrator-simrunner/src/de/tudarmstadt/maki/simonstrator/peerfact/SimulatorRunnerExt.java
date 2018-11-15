package de.tudarmstadt.maki.simonstrator.peerfact;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.peerfact.MultiRunner.SimShell.SimShellCommands;

/**
 * Extended version of the simulation runner that supports an interactive
 * console mode. Derived from {@link SimulationRunner}
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class SimulatorRunnerExt extends SimulatorRunner {

	/**
	 * Messaging-API for commands. Requests will be prefixed with CMD and
	 * replies with REP.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	public enum Command {

		HELP("help", "Shows the help page for the interactive shell."),
		
		QUIT("q", "Close this interactive shell (simulation will continue)."),

		PROGRESS("p", "Displays the current progress in Simulation Time."),

		REMOVE("rm",
				"Remove this Simulation from the queue or kill it, if it is already active.");

		private Command(String cmd, String help) {
			this.cmd = cmd;
			this.help = help;
		}

		public final String help;

		public final String cmd;

	}

	public static final String CMD = "--CMD ";

	public static final String REP = "--REP ";

	private static long startTimestamp;

	/**
	 * Set private to prevent instantiation.
	 * 
	 */
	protected SimulatorRunnerExt(String[] args) {
		super(args);
		startTimestamp = System.currentTimeMillis();
		new RunnerInputParser(this).start();
	}

	/**
	 * This method can be used to run a simulation. The expected arguments are:
	 * <code>config file</code> and an optional list of zero or many variable
	 * assignments<code>(variable=value)*")</code> or from the command line
	 * <code> java Scenario {config file} {variable=value}*</code>.
	 * 
	 * @param args
	 *            expect an array with the name of the configuration file and
	 *            optional variable assignments
	 */
	public static void main(String[] args) {
		new SimulatorRunnerExt(args).run();
	}


	/**
	 * Waiting for cmd-inputs for one Simulation Process. This Thread ensures,
	 * that individual simulation processes are terminated as soon as the main
	 * runner is terminated.
	 * 
	 * @author Bjoern Richerzhagen
	 * 
	 */
	private static class RunnerInputParser extends Thread {

		SimulatorRunnerExt runner;

		public RunnerInputParser(SimulatorRunnerExt runner) {
			setDaemon(true);
			this.runner = runner;
		}

		@Override
		public void run() {
			this.setName("RunnerInputParser");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					System.in));
			String s;
			try {
				while ((s = in.readLine()) != null && s.length() != 0) {
					if (s.startsWith(CMD)) {
						// A command
						s = s.substring(CMD.length()).trim();
						for (Command command : Command.values()) {
							if (s.startsWith(command.cmd)) {
								// Found
								s = s.substring(command.cmd.length()).trim();
								SimulatorRunnerExt.executeCommand(command, s);
							}
						}
					}
					// nothing
				}
			} catch (IOException e) {
				//
			}
			if (runner != null && runner.getSim() != null) {
				runner.getSim().shutdownSimulation(null);
			}
		}

	}

	/**
	 * Execute a {@link Command}
	 * 
	 * @param cmd
	 * @param arg
	 */
	public static void executeCommand(Command cmd, String arg) {
		System.out.println(REP + "Executing " + cmd + " with args {" + arg
				+ "}");
		switch (cmd) {
		case PROGRESS:
			System.out.println(REP
					+ "Simulation time: "
					+ Time.getFormattedTime()
					+ " Real duration: "
					+ prettyTimeInterval(System.currentTimeMillis()
							- startTimestamp));
			break;

		case REMOVE:
			System.out.println(REP + " Process forcefully killed. ");
			System.exit(1);
			break;

		case HELP:
			System.out.println(REP + " Help ");
			for (SimShellCommands command : SimShellCommands.values()) {
				System.out.println(REP + command.cmd);
				System.out.println(REP + "\t" + command.help);
			}
			System.out.println(REP + " - SimShell by BR, KOM");
			break;

		default:
			break;
		}
	}

	protected static String prettyTimeInterval(long ms) {
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

}
