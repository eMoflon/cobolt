package de.tudarmstadt.maki.simonstrator.peerfact.multirunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * This SimLoader mimiks the behavior of the bash-script used to run multiple
 * variations of a configuration. It supports the following arguments
 *
 * config.xml -v1 Variations1[ Variations1b, ...] [-v2 Variations] -s 1,2,3 -p 1
 * [-start]
 *
 * with variations files containing lines of variable assignments, seed
 * specifying the different seeds and p specifying the number of parallel
 * process. File names <strong>must not</strong> contain blanks.
 *
 * If multiple variations are specified those variations are permutated pairwise
 * against each other. If a comma-separated list of variations for one -v switch
 * is specified, those variations are executed sequentially, as if they were all
 * specified in just one file.
 *
 * If the -start flag is set, simulations begin immediately (good for scripts)
 *
 * @author Bjoern Richerzhagen
 *
 */
public class SimLoaderVariations implements SimulationLoader {

	private final static String COMMENT_PREFIX = "#";

	private String configFile;

	private final List<List<String>> variations = new LinkedList<List<String>>();

	private final List<String> seeds = new LinkedList<String>();

	private int parallelProcesses = 1;

	private boolean autostart = false;

	@Override
	public void initialize(String[] argsRaw) {
		System.out.println(Arrays.toString(argsRaw));

		CmdArgsHelper args = new CmdArgsHelper(argsRaw);
		boolean error = false;
		try {
			configFile = args.getArgAt(0);
		} catch (IllegalArgumentException e) {
			System.err.println("Specify a config file!");
			error = true;
		}

		try (FileInputStream x = new FileInputStream(configFile)) {

		} catch (IOException e1) {
			System.err.println("Config file " + args.getArgAt(0) + " not found!");
			error = true;
		}

		if (args.hasArg("-s")) {
			seeds.addAll(args.getNamedArgAsList("-s"));
		} else {
			System.err.println("Specify the seed(s) using -s 1 2 3");
			error = true;
		}
		if (args.hasArg("-v1")) {
			List<String> filenames = args.getNamedArgAsList("-v1");
			List<String> var1 = new LinkedList<String>();
			for (String filename : filenames) {
				try {
					var1.addAll(readVariationsFile(filename));
				} catch (FileNotFoundException e) {
					System.err.println("File " + filename + " not found!");
					error = true;
					break;
				}
			}
			variations.add(var1);
		} else {
			System.err
					.println("Specify a variations.txt-file using -v1 filename");
		}
		if (args.hasArg("-v2")) {
			List<String> filenames = args.getNamedArgAsList("-v2");
			List<String> var2 = new LinkedList<String>();
			for (String filename : filenames) {
				try {
					var2.addAll(readVariationsFile(filename));
				} catch (FileNotFoundException e) {
					System.err.println("File " + filename + " not found!");
					error = true;
					break;
				}
			}
			variations.add(var2);
		}
		if (error) {
			System.err
					.println("Usage: <config> -v1 Variations1[ Variations1a Variations1b] [-v2 Variations2] -s 1 2 3 [-p 1] [-start]");
			System.exit(1);
		}


		autostart = args.hasFlag("-start");
		if (args.hasArg("-p")) {
			parallelProcesses = Integer.valueOf(args.getNamedArg("-p"));
		}
	}

	private List<String> readVariationsFile(String filename)
			throws FileNotFoundException {
		FileInputStream fIn = new FileInputStream(filename);
		Scanner sc = new Scanner(fIn);
		List<String> vars = new LinkedList<String>();
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (!line.startsWith(COMMENT_PREFIX)) {
				vars.add(line);
			}
		}
		sc.close();
		return vars;
	}

	@SuppressWarnings("unused")
	private void test() {
		configFile = "config/pubsub/batch/minimal/pubsub_mini.xml";
		List<String> var1 = new LinkedList<String>();
		var1.add("V1=a");
		var1.add("V1=b");
		var1.add("V1=c");
		List<String> var2 = new LinkedList<String>();
		var2.add("V2=a");
		var2.add("V2=b");
		var2.add("V2=c");
		variations.add(var1);
		variations.add(var2);
		seeds.add("1");
		seeds.add("2");
	}

	@Override
	public boolean loadSimulations(Callback callback) {
		/*
		 * Outer Loop: seeds (we want to have at least one run of each var as
		 * soon as possible, before doing repetitions.
		 */
		for (String seed : seeds) {
			for (List<String> currVariation : variations) {
				/*
				 * No duplicates: only permutate against not-yet-seen
				 * variations. Example: A, B, C leads to A x B, A x C; B x C
				 */
				List<List<String>> permutees = null;
				try {
					permutees = variations.subList(
						variations.indexOf(currVariation) + 1,
						variations.size());
				} catch (IndexOutOfBoundsException e) {
					// no inner vars
				}
				for (String cmdVariation : currVariation) {
					if (permutees.isEmpty()
							&& variations.indexOf(currVariation) == 0) {
						// just schedule the plain variation
						callback.addSimulation(getSimulationDescription(
								cmdVariation, seed));
					} else if (permutees.isEmpty()) {
						break;
					} else {
						for (List<String> permutee : permutees) {
							for (String cmdPermutee : permutee) {
								// schedule the variation
								callback.addSimulation(getSimulationDescription(
										cmdVariation + " " + cmdPermutee, seed));
							}
						}
					}
				}

			}
		}
		return autostart;
	}

	private SimulationDescription getSimulationDescription(String variations,
			String seed) {
		List<String> args = new LinkedList<String>();
		args.add(configFile);
		args.add("seed=" + seed);
		args.addAll(Arrays.asList(variations.split(" ")));
		return new SimulationDescription(variations + " S" + seed, args);
	}

	@Override
	public int getNumberOfParallelInstances() {
		return parallelProcesses;
	}

}
