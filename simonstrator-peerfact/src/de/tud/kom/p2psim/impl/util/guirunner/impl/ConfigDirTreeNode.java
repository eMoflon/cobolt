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


package de.tud.kom.p2psim.impl.util.guirunner.impl;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * A tree node that represents a directory.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class ConfigDirTreeNode extends DefaultMutableTreeNode {

	public ConfigDirTreeNode(File configDir) {
		super(configDir.getName());
	}

	/**
	 * A tree node that is the root of all nodes
	 * 
	 * @author Leo Nobach
	 * @version 3.0, 25.11.2008
	 * 
	 */
	public static class Root extends DefaultMutableTreeNode {
		public Root() {
			super("PeerfactSim Configurations");
		}
	}

	/**
	 * A tree node that is the root of the last opened files
	 * 
	 * @author Leo Nobach
	 * @version 3.0, 25.11.2008
	 * 
	 */
	public static class LastOpenedRoot extends DefaultMutableTreeNode {
		public LastOpenedRoot() {
			super("Recently opened");
		}
	}

	/**
	 * A tree node that is the root of all available files
	 * 
	 * @author Leo Nobach
	 * @version 3.0, 25.11.2008
	 * 
	 */
	public static class AvailableRoot extends DefaultMutableTreeNode {
		public AvailableRoot() {
			super("Available configs");
		}
	}
}
