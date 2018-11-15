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
import java.util.List;

import de.tud.kom.p2psim.impl.util.Tuple;

public interface ConfigFile {
	
	public void loadConfiguration(ConfigLoadedCallback callback);

	public void setVariable(String name, Object value);
	
	public List<Tuple<String, String>> getVariables();

	public List<Tuple<String, String>> getModifiedVariables();
	
	public List<String> getVariations();

	public String getDesc();

	public File getFile();

	public String getSeedInConfig();

	public List<String> getSelectedVariations();

	public void setSelectedVariations(List<String> selectedVariation);
}
