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

import java.awt.Dimension;

import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import de.tud.kom.p2psim.impl.util.Tuple;
import de.tud.kom.p2psim.impl.util.guirunner.impl.RunnerController.IRunnerCtrlListener;

public class VariableFieldEditor {
	
	ConfigFile selectedFile;
	JTable t;

	public VariableFieldEditor(RunnerController ctrl) {
		selectedFile = ctrl.getSelectedFile();
		ctrl.addListener(this.new RunnerCtrlListenerImpl());
		t = new JTable(new TableModelImpl());
	}
	
	public JComponent getComponent() {
		JScrollPane p = new JScrollPane(t);
		p.setPreferredSize(new Dimension(230, 230));
		return p;
	}
	
	class RunnerCtrlListenerImpl implements IRunnerCtrlListener {

		@Override
		public void newFileSelected(ConfigFile f) {
			selectedFile = f;
			if (t == null) return;
			t.revalidate();
			t.repaint();
		}
		
	}
	
	class TableModelImpl extends AbstractTableModel {

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			if (columnIndex == 0) return "Name";
			else return "Value";
		}

		@Override
		public int getRowCount() {
			if (selectedFile == null || selectedFile.getVariables() == null)
				return 0;
			return selectedFile.getVariables().size();
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			Tuple<String, String> t = selectedFile.getVariables().get(rowIndex);
			if (columnIndex == 0) return t.getA();
			return t.getB();
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return columnIndex > 0;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			Tuple<String, String> t = selectedFile.getVariables().get(rowIndex);
			System.out.println("Changed value of " + t.getA() + " from " + t.getB() + " to " + aValue);
			
			selectedFile.setVariable(t.getA(), aValue);
		}
		
	}
	
}