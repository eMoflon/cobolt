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


package de.tud.kom.p2psim;

import java.io.File;

/**
 * This interface contains global constants that required for simulations.
 * 
 * @author Dominik Stingl
 * 
 */
public interface Constants {

	/**
	 * Path for the tmp-directory, which shall be used to temporarily store
	 * data, that is required during or after a simulation (e.g. for online or
	 * post-processing)
	 */
	public static final String TMP_DIR = "tmp";

	/**
	 * Path for the output-directory, which shall be used for storing the
	 * results of a simulation, its post-processing or in general for data that
	 * must available for a longer period of time.
	 */
	public static final String OUTPUTS_DIR = "outputs";

	/**
	 * Path for the logging-directory, which contains the different
	 * configurations for log4j and is used to store the output of log4j
	 */
	public static final String LOGGING_DIR = "logging";

	/**
	 * Path for the directory which contains the scripts for generating graphics
	 * with GnuPlot
	 */
	public static final String GNUPLOT_SCRIPTS = "gnuplotScripts";

	// Variables for the automatic integration of GnuPlot within the simulator

	/**
	 * For completeness only, this variable defines the path to the binary for
	 * gnuplot. On Linux systems gnuplot can be started directly from the
	 * command line without the provisioning of a path for the binary.
	 */
	public static final String GNU_BIN_DIRECTORY_LINUX = File.listRoots()[0]
			.toString();

	/**
	 * Defines the command to run gnuplot out of the simulator on a Linux
	 * machine.
	 */
	public static final String GNU_EXECUTABLE_LINUX = "gnuplot";

	/**
	 * Defines the path where the binaries for gnuplot can be found on Windows
	 * machines. Please adapt the path to the current location of your gnuplot
	 * binaries.
	 */
	public static final String GNU_BIN_DIRECTORY_WINDOWS = "C:"
			+ File.separator 
			+ "gnuplot" + File.separator + "bin";

	/**
	 * Defines the command to run gnuplot out of the simulator on a Windows
	 * machine.
	 */
	public static final String GNU_EXECUTABLE_WINDOWS = "wgnuplot-pipes.exe";

}
