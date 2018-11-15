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
import javax.swing.tree.TreeNode;

import de.tud.kom.p2psim.impl.util.guirunner.GUIRunner;

/**
 * Creates the nodes and builds up the hierarchy.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 *
 */
public class NodeModel {

	DefaultMutableTreeNode root;
	private LastOpened lastOpened;
	
	public NodeModel(LastOpened lastOpened) {
		
		this.lastOpened = lastOpened;
		
		root = new ConfigDirTreeNode.Root();
		
		DefaultMutableTreeNode lastConfs = new ConfigDirTreeNode.LastOpenedRoot();
		populateWithLastOpened(lastConfs);
		root.add(lastConfs);
		
		DefaultMutableTreeNode dirs = new ConfigDirTreeNode.AvailableRoot();
		populateWithFileSystem(dirs);
		root.add(dirs);
		
		
	}
	
	/**
	 * Fills the given node dirs with the available configs on the file system.
	 * 
	 * @param dirs
	 */
	private void populateWithFileSystem(DefaultMutableTreeNode dirs) {
		FileToolkit.getConfigFiles(dirs, new File(GUIRunner.DEFAULT_CONFIG_DIR));
	}

	/**
	 * Fills the given node lastConfs with the recently opened files.
	 * @param lastConfs
	 */
	private void populateWithLastOpened(DefaultMutableTreeNode lastConfs) {
		for (ConfigFile f : lastOpened.getLastOpened()) {
			lastConfs.add(new ConfigTreeNode.LastOpened(f));
		}
	}

	/**
	 * Returns the root node of the created model.
	 * @return
	 */
	public TreeNode getRoot() {
		return root;
	}

}
