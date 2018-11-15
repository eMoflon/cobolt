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


package de.tud.kom.p2psim.impl.util.guirunner.progress;

import javax.swing.table.AbstractTableModel;

import de.tud.kom.p2psim.impl.util.LiveMonitoring;


public class ProgressValuesListModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4835422753977045344L;

	int oldSize = 0;

	public ProgressValuesListModel() {
		oldSize = LiveMonitoring.getProgressValues().size();
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		if (LiveMonitoring.getProgressValues().size() != oldSize) {
			oldSize = LiveMonitoring.getProgressValues().size();
			fireTableDataChanged();
		}
		return oldSize;
	}

	@Override
	public Object getValueAt(int row, int col) {
		if (col == 0)
			return LiveMonitoring.getProgressValues().get(row).getName();
		else
			return LiveMonitoring.getProgressValues().get(row).getValue();
	}

	@Override
	public String getColumnName(int col) {
		if (col == 0)
			return "";
		else
			return "Value";
	}

	@Override
	public Class<? extends Object> getColumnClass(int c) {
		return String.class;
	}
}
