/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
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

import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FilterTreeModel implements TreeModel {

	private TreeModel treeModel;

	private String filter = "";

	public FilterTreeModel(DefaultTreeModel defaultTreeModel) {
		this.treeModel = defaultTreeModel;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	@Override
	public Object getRoot() {
		return treeModel.getRoot();
	}

	@Override
	public Object getChild(final Object parent, final int index) {
		int count = 0;
		int childCount = treeModel.getChildCount(parent);

		for (int i = 0; i < childCount; i++) {
			Object child = treeModel.getChild(parent, i);

			if (isMatching(child, filter)) {
				if (count == index) {
					return child;
				}

				count++;
			}
		}

		return null;
	}

	@Override
	public int getChildCount(final Object parent) {
		int count = 0;
		int childCount = treeModel.getChildCount(parent);

		for (int i = 0; i < childCount; i++) {
			Object child = treeModel.getChild(parent, i);

			if (isMatching(child, filter)) {
				count++;
			}
		}

		return count;
	}

	@Override
	public int getIndexOfChild(final Object parent, final Object childToFind) {
		int childCount = treeModel.getChildCount(parent);
		
		for (int i = 0; i < childCount; i++) {
			Object child = treeModel.getChild(parent, i);
			
			if (isMatching(child, filter)) {
				if (childToFind.equals(child)) {
					return i;
				}
			}
		}
		
		return -1;
	}

	private boolean isMatching(final Object node, final String filter) {
		boolean matches = node.toString().toLowerCase().contains(filter.toLowerCase());
		int childCount = treeModel.getChildCount(node);

		for (int i = 0; i < childCount; i++) {
			Object child = treeModel.getChild(node, i);

			matches |= isMatching(child, filter);
		}

		return matches;
	}

	@Override
	public boolean isLeaf(Object paramObject) {
		return treeModel.isLeaf(paramObject);
	}

	@Override
	public void valueForPathChanged(TreePath paramTreePath, Object paramObject) {
		treeModel.valueForPathChanged(paramTreePath, paramObject);
	}

	@Override
	public void addTreeModelListener(final TreeModelListener l) {
		treeModel.addTreeModelListener(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener paramTreeModelListener) {
		treeModel.removeTreeModelListener(paramTreeModelListener);
	}

}
