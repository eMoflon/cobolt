package de.tudarmstadt.maki.simonstrator.peerfact.multirunner;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Some helpers to cope with args[] input
 * 
 * @author Bjoern Richerzhagen
 * 
 */
public class CmdArgsHelper {

	private final String[] args;

	private final Map<String, String> namedArgs = new LinkedHashMap<String, String>();

	private final Map<String, List<String>> namedLists = new LinkedHashMap<String, List<String>>();

	private final Set<String> flags = new LinkedHashSet<String>();

	public CmdArgsHelper(String[] args) {
		this.args = args;
		// Process named args and lists
		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if (arg.startsWith("-")) {
				// named arg. Flag or value?
				if (args.length > i + 1 && !args[i + 1].startsWith("-")) {
					/*
					 * It was a named argument. List or single value? Parse
					 * every token until the next one starts with "-"
					 */
					List<String> values = new LinkedList<String>();
					namedLists.put(arg, values);
					while (args.length > i + 1 && !args[i + 1].startsWith("-")) {
						values.add(args[i + 1]);
						i++;
					}
					assert !values.isEmpty();
					if (values.size() == 1) {
						namedArgs.put(arg, values.get(0));
					} else {
						namedArgs.put(arg, values.toString());
					}
				} else {
					// A Flag (next token starts with "-")
					flags.add(arg);
				}
			}
		}
	}

	/**
	 * 
	 * @param idx
	 * @param args
	 * @return
	 * @throws IllegalArgumentException
	 */
	public String getArgAt(int idx)
			throws IllegalArgumentException {
		if (args.length > idx) {
			return args[idx];
		}
		throw new IllegalArgumentException();
	}

	/**
	 * Named arg, in the form -name:value
	 * 
	 * @param args
	 * @param name
	 * @return
	 */
	public String getNamedArg(String name) {
		return namedArgs.get(name);
	}

	public List<String> getNamedArgAsList(String name) {
		if (namedLists.containsKey(name)) {
			return namedLists.get(name);
		}
		// Fallback, if only one list item
		if (namedArgs.containsKey(name)) {
			List<String> list = new LinkedList<String>();
			list.add(namedArgs.get(name));
			return list;
		}
		return null;
	}

	public boolean hasFlag(String flag) {
		return flags.contains(flag);
	}

	public boolean hasArg(String arg) {
		return namedArgs.containsKey(arg);
	}

	/**
	 * Just for testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String test = "-v1 /doc/var1/1.txt /doc/var1/2.txt -v2 /doc/var2/1.txt -p 2 -s 1 2 3 -start -test";
		CmdArgsHelper helper = new CmdArgsHelper(test.split(" "));
		assert helper.hasFlag("-start");
		assert helper.hasFlag("-test");
		assert !helper.hasFlag("-p");
		assert helper.hasArg("-v1");
		assert !helper.hasArg("-start");
	}

}
