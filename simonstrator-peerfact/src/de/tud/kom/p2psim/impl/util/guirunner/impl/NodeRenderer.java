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

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * Decides how to render a particular node.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class NodeRenderer extends DefaultTreeCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6968205085443692522L;

	static final ImageIcon DEFAULT_ICON = new ImageIcon(
			"images/icons/guiRunner/ScenarioConfigBlue.png");

	static final ImageIcon LAST_OPENED_ICON = new ImageIcon(
			"images/icons/guiRunner/ScenarioConfigRed.png");

	static final ImageIcon LAST_OPENED_DIR_ICON = new ImageIcon(
			"images/icons/guiRunner/recentlyOpened.png");

	static final ImageIcon AVAILABLE_DIR_ICON = new ImageIcon(
			"images/icons/guiRunner/availableConfigs.png");

	static final ImageIcon ROOT_ICON = new ImageIcon(
			"images/icons/guiRunner/RootNode.png");

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,
				row, hasFocus);

		if (value instanceof ConfigTreeNode.LastOpened) {
			setIcon(LAST_OPENED_ICON);
		} else if (value instanceof ConfigDirTreeNode.LastOpenedRoot) {
			setIcon(LAST_OPENED_DIR_ICON);
		} else if (value instanceof ConfigDirTreeNode.AvailableRoot) {
			setIcon(AVAILABLE_DIR_ICON);
		} else if (value instanceof ConfigDirTreeNode.Root) {
			setIcon(ROOT_ICON);
		} else if (value instanceof ConfigDirTreeNode) {
			// Standard-Knoten-Icon
		} else {
			setIcon(DEFAULT_ICON);
		}

		return this;
	}

}
