package de.tudarmstadt.maki.simonstrator.peerfact.multirunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * Description of one simulation as required by the MultiRunner
 * 
 * @author Bjoern Richerzhagen
 * 
 */
@SuppressWarnings("serial")
public class SimulationDescription implements Serializable {

	private final String name;

	private final List<String> args;

	public SimulationDescription(String name, List<String> args) {
		this.name = name;
		this.args = args;
	}

	public SimulationDescription(String name, String... args) {
		this.name = name;
		this.args = Arrays.asList(args);
	}

	public String getName() {
		return name;
	}

	public List<String> getArgs() {
		return args;
	}

	@Override
	public String toString() {
		return "CMD: " + args.toString();
	}

}
