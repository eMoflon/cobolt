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
import java.util.Set;
import java.util.TreeSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;


/**
 * 
 * Controls directory browsing and file type checking.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class FileToolkit {

	/**
	 * Appends all files and dirs in given directory rootDir to rootNode,
	 * recursively.
	 * 
	 * @param rootNode
	 * @param rootDir
	 * @return
	 */
	public static boolean getConfigFiles(DefaultMutableTreeNode rootNode,
			File rootDir) {

		File[] filesArray = rootDir.listFiles();
		Set<File> files = new TreeSet<File>();
		if (filesArray != null) {
			for (int i = 0; i < filesArray.length; i++)
				files.add(filesArray[i]);
		}

		boolean containsAConfig = false;

		for (File f : files) {

			if (f.isDirectory()) {
				DefaultMutableTreeNode subDir = new ConfigDirTreeNode(f);
				if (getConfigFiles(subDir, f)) {
					containsAConfig = true;
					rootNode.add(subDir);
				}

			} else if (fileIsConfig(f)) {
				MutableTreeNode configFile = null;
				if (f.getName().endsWith(".xml")) {
					configFile = new ConfigTreeNode(new XMLConfigFile(f));
				} else if (f.getName().endsWith(".simcfg")) {
					configFile = new ConfigTreeNode(new SimCfgConfigFile(f));
				}
				
				containsAConfig = true;
				rootNode.add(configFile);

			}
		}

		return containsAConfig;
	}

	/**
	 * Checks whether the given file is an XML config file or not.
	 * 
	 * @param f
	 * @return
	 */
	private static boolean fileIsConfig(File f) {
		return f.getName().endsWith(".xml") || f.getName().endsWith(".simcfg");
	}

}
