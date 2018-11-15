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


package de.tudarmstadt.maki.simonstrator.peerfact;



/**
 * Starts a window to select a configuration file to run PFS from. Useful for
 * developers switching to different configuration files many times, as well as
 * for presentations etc.
 * 
 * See <a href="http://www.student.informatik.tu-darmstadt.de/~l_nobach/docs/howto-visualization.pdf"
 * >PeerfactSim.KOM Visualization HOWTO</a> on how to use it.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class GUIRunner {
	public static void main(String[] args) {
		System.setProperty("logfile.name", "simguilog.log");
		new de.tud.kom.p2psim.impl.util.guirunner.GUIRunner();
	}
}
