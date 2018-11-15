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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 * A tree view that shows available and recently opened configuration files.
 * 
 * @author Leo Nobach
 * @version 3.0, 25.11.2008
 * 
 */
public class DirView extends JTree implements TreeSelectionListener,
		MouseListener {

	private RunnerController ctrl;

	private ConfigTreeNode selectedConfigNode = null;
	
	private FilterTreeModel treeModel;

	public DirView(RunnerController ctrl, LastOpened lastOpened) {
		super();

		this.ctrl = ctrl;

		NodeModel mdl = new NodeModel(lastOpened);

		treeModel = new FilterTreeModel(new DefaultTreeModel(mdl.getRoot()));
		
		this.setModel(treeModel);
		this.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		this.addTreeSelectionListener(this);
		this.expandAll(this);

		this.addMouseListener(this);

		this.setCellRenderer(new NodeRenderer());

		initialSelect();

	}

	public void filter(String text) {
		treeModel.setFilter(text);
	}

	/**
	 * Selects the row that should be selected on startup.
	 */
	private void initialSelect() {
		this.setSelectionRow(2);
	}

	/**
	 * Expands all entries in the tree view.
	 * 
	 * @param tree
	 */
	public void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object selectedNode = this.getLastSelectedPathComponent();

		if (selectedNode instanceof ConfigTreeNode) {
			selectedConfigNode = (ConfigTreeNode) selectedNode;
		} else {
			selectedConfigNode = null;
		}

		ctrl.selectFile(selectedConfigNode == null ? null : selectedConfigNode
				.getConfigFile());

		// System.out.println("Ausgewählte ConfigNode: " + selectedConfigNode);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		if (arg0.getClickCount() == 2 && selectedConfigNode != null)
			ctrl.invokeRunSimulator();
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
