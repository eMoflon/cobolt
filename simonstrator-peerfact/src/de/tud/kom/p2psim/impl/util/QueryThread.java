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


package de.tud.kom.p2psim.impl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.EventHandler;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Class which enables command line during execution of the simulation.
 * Currently there are the following commands:
 * <ul><li>quit (exit is an alias)</li>
 * <li>time prints the current simulation time</li>
 * <li>pause pauses the simulation</li>
 * <li>unpause resumes</li>
 * <li>gc executes Java's garbage collection</li>
 * <li>events prints the number of events in the queue</li></ul>
 * 
 * New commands can be added in the action file:
 * <br><code>&lt;QueryThread class="de.tud.kom.p2psim.impl.util.QueryThread" static="getInstance" stopOnExit="true"&gt;<br>
 *		&lt;Command class="de.tud.kom.p2psim.impl.overlay.dht.pastry.common.PastryCommand"/&gt;<br>
 *	&lt;/QueryThread&gt;
 * </code>
 * 
 * @author Robert Vock <rvock@rbg.informatik.tu-darmstadt.de>
 */
public class QueryThread extends Thread {
	private static QueryThread instance;
	
	boolean execute = true;

	public interface Command {
		public String getName();
		
		public void execute();
		public void execute(String[] parameters);
	}
	
	abstract protected class SystemCommand implements Command {
		public String getName() {
			return null;
		}
		
		public void execute(String[] parameters) {
			execute();
		}
	}
	
	private Map<String, Command> commands;
	
	private QueryThread() {
		// private constructor
		commands = new HashMap<String, Command>();
		commands.put("exit", new SystemCommand() {
			@Override
			public void execute() {
				System.exit(0);
			}
		});
		commands.put("quit", new SystemCommand() {
			@Override
			public void execute() {
				System.exit(0);
			}
		});
		commands.put("time", new SystemCommand() {
			@Override
			public void execute() {
				System.out.println("Current Time: " + Time.getFormattedTime());
			}
		});
		commands.put("pause", new SystemCommand() {
			@Override
			public void execute() {
				Simulator.getScheduler().pause();
				System.out.println("Scheduler paused");
			}
		});
		commands.put("unpause", new SystemCommand() {
			@Override
			public void execute() {
				Simulator.getScheduler().unpause();
				System.out.println("Scheduler unpaused");
			}
		});
		commands.put("gc", new SystemCommand() {
			@Override
			public void execute() {
				System.out.println("FreeMemory before gc: "+Runtime.getRuntime().freeMemory());
				System.gc();
				System.out.println("FreeMemory after gc : "+Runtime.getRuntime().freeMemory());
			}
		});
		commands.put("events", new SystemCommand() {
			@Override
			public void execute() {
				System.out.println(Simulator.getScheduler().getEventQueueSize()
						+ " events in the queue");
			}
		});
	}
	
	synchronized public static QueryThread getInstance() {
		if (instance == null) {
			instance = new QueryThread();
			instance.start();
		}
		return instance;
	}
	
	/**
	 * add a command to the list of commands.
	 * This does not delete any other commands, but adds the command.
	 */
	public void setCommand(Command command) {
		commands.put(command.getName(), command);
	}
	
	/**
	 * automatically stop the thread after the simulation has ended
	 */
	public void setStopOnExit(boolean stop) {
		if (stop) {
			Event.scheduleWithDelay(Simulator.getEndTime(), new EventHandler() {
				@Override
				public void eventOccurred(Object se, int type) {
					execute = false;
				}
			}, null, 0);
		}
	}
	
	@Override
	public void run() {
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Commandline started, waiting for commands");
		while (true) {
			String command;
			try {
				while (execute && !reader.ready()) {
					Thread.sleep(200);
				}
				if (!execute) {
					break;
				}
				command = reader.readLine().trim();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} catch (NullPointerException e) {
				// ignore, happens when process gets killed
				break;
			} catch (InterruptedException e) {
				// ignore
				break;
			}
			
			String[] params = command.split("\\s+");
			Command operation = commands.get(params[0]);
			if (operation != null) {
				if (params.length == 1) {
					// no parameters
					operation.execute();
				} else {
					// get the parameters
					String[] params2 = new String[params.length-1];
					System.arraycopy(params, 1, params2, 0, params.length-1);
					operation.execute(params2); 
				}
			} else {
				System.out.println("Unknown command: "+command);
			}
		}
	}
}
