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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.tud.kom.p2psim.api.scenario.simcfg.converter.SimCfgTypeConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.SimCfgConfigurator;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.ConfigurationContext;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfiguration;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.SimCfgConfigurationMerger;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.util.Tuple;

public class SimCfgConfigFile implements ConfigFile {
	private String description = "";
	private File configFile = null;
	private boolean hasBeenParsed = false;
	private ConfigurationContext configurationContext = null;
	private List<String> selectedVariations = Lists.newArrayList();
	private SimCfgConfigurator configurator;
	private List<Tuple<String, String>> variables = null;
	private Map<String, String> modifiedVariables = Maps.newLinkedHashMap();
    private Map<String, Value> settings = Maps.newLinkedHashMap();
    private String seed = "";

    public SimCfgConfigFile(File configFile) {
		this.configFile = configFile;
	}

	@Override
	public String getDesc() {
		if (!hasBeenParsed) {
			parseConfig();
		}
		
		return description;
	}

	@Override
	public void loadConfiguration(final ConfigLoadedCallback callback) {
		parseConfig();
		callback.loadingFinished();
	}

	private void parseConfig() {
		if (this.hasBeenParsed) return;
		
		BufferedReader reader = null;
		
		try {
			reader = new BufferedReader(new FileReader(configFile));
			
			String line = reader.readLine();
			while (line != null) {
				
				if (line.startsWith("//")) {
					description += line.substring(2) + "\n";
				} else {
					break;
				}
				
				line = reader.readLine();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) try { reader.close(); } catch (IOException e) { /* */ }
		}
		
		try {
			// Misuse the configurator to parse the configuration.
			configurator = new SimCfgConfigurator(configFile);
			configurationContext = configurator.getConfigurationContext();

            calculateFinalConfiguration();
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.hasBeenParsed = true;
	}

    private void calculateFinalConfiguration() {
        SimCfgConfigurationMerger merger = new SimCfgConfigurationMerger();
        SimCfgConfiguration finalConfiguration = merger.process(configurationContext, selectedVariations);

        this.settings = finalConfiguration.getSettings();
    }

    @Override
	public File getFile() {
		return configFile;
	}

	@Override
	public String getSeedInConfig() {
		if (!hasBeenParsed) {
			parseConfig();
		}

        return seed;
	}

	@Override
	public List<String> getVariations() {
		if (!hasBeenParsed) {
			parseConfig();
		}
		
		if (configurationContext == null || configurationContext.getVariationNames() == null)
			return null;

		return Lists.newArrayList(configurationContext.getVariationNames());
	}
	
	@Override
	public void setVariable(String name, Object aValue) {
		if (!(aValue instanceof String)) return;
		
		for (Tuple<String, String> variable : this.variables) {
			if (variable.getA().equals(name)) {
				variable.setB((String)aValue);
				break;
			}
		}

        modifiedVariables.put(name, (String)aValue);
	}

	@Override
	public List<Tuple<String, String>> getVariables() {
		if (!hasBeenParsed) {
			parseConfig();
		}

        if (variables == null) {
			variables = Lists.newArrayList();
        }

        if (settings != null) {
            variables.clear();
			for (String key : settings.keySet()) {
                if (modifiedVariables.containsKey(key)) {
                    variables.add(new Tuple<String, String>(key, (String) SimCfgTypeConverter.convertTo(key, new Value(Value.ValueType.STRING, modifiedVariables.get(key)), String.class)));
                } else {
                    variables.add(new Tuple<String, String>(key, (String) SimCfgTypeConverter.convertTo(key, settings.get(key), String.class)));
			    }
            }
            seed = settings.get("seed").getString();
		}

		return variables;
	}
	
	@Override
	public List<Tuple<String, String>> getModifiedVariables() {
		return Tuple.tupleListFromMap(this.modifiedVariables);
	}
	
	@Override
	public List<String> getSelectedVariations() {
		return selectedVariations;
	}

	@Override
	public void setSelectedVariations(List<String> selectedVariations) {
		this.selectedVariations = selectedVariations;

        calculateFinalConfiguration();
	}
}
