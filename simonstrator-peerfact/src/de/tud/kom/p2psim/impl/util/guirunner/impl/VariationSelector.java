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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.impl.util.guirunner.impl.RunnerController.IRunnerCtrlListener;

public class VariationSelector {
	private ConfigFile selectedFile;
    private JScrollPane scrollPane;
    private JPanel panel;
    private List<String> selectedVariations = Lists.newArrayList();
    private List<VariationChangeListener> variationChangeListeners = Lists.newArrayList();

	public VariationSelector(RunnerController ctrl) {
		selectedFile = ctrl.getSelectedFile();
		
        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setSize(panel.getWidth(), 50);
        scrollPane = new JScrollPane(panel);

		ctrl.addListener(new IRunnerCtrlListener() {
			@Override
			public void newFileSelected(ConfigFile f) {
				selectedFile = f;

				updateVariationList(f);
			}
		});
	}

    private void updateVariationList(ConfigFile configFile) {
        if(panel != null) {
        	panel.removeAll();
        }

        for (final String name : configFile.getVariations()) {
            final JCheckBox checkBox = new JCheckBox(name);
            checkBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (checkBox.isSelected()) {
                        selectedVariations.add(name);
                    } else {
                        selectedVariations.remove(name);
                    }

                    raiseVariationStateChanged(checkBox.getText(), checkBox.isSelected());
                    updateVariations();
                }
            });

            panel.add(checkBox);
        }

        scrollPane.invalidate();
        scrollPane.revalidate();
        scrollPane.repaint();
    }

    private void updateVariations() {
        selectedFile.setSelectedVariations(selectedVariations);
    }

    public JComponent getComponent() {
		return scrollPane;
	}

    private void raiseVariationStateChanged(String name, boolean state) {
        for (VariationChangeListener listener : this.variationChangeListeners) {
            listener.variationToggled(name, state);
        }
    }

    public void addVariationChangeListener(VariationChangeListener listener) {
        this.variationChangeListeners.add(listener);
    }

    public void removeVariationChangeListener(VariationChangeListener listener) {
        this.variationChangeListeners.remove(listener);
    }

    static interface VariationChangeListener {
        public void variationToggled(String name, boolean state);
    }
}
