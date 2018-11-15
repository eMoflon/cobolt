/*
 * Copyright (c) 2005-2013 KOM - Multimedia Communications Lab
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
 */

package de.tud.kom.p2psim.impl.scenario.simcfg2.configuration;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import com.google.common.base.CharMatcher;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Import;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.scanner.Scanner;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

public class SimCfgFileParser {

    private File currentFile; // Contains the file of the currently parsed config (changes during parsing)


    public SimCfgFileParser() {

    }

    public ConfigurationContext parseConfig(File configFile) {
        ConfigurationContext context = loadConfig(configFile, null, new ConfigurationContext());
        return context;
    }

    private ConfigurationContext loadConfig(File configFile, String guard, ConfigurationContext context) {
        this.currentFile = configFile;
        SimCfgConfiguration config = Scanner.scanFile(configFile);

        if (context.getRootNode() == null) {
            context.setRoot(config);
            context.setCurrentNode(context.getRootNode());
        } else {
            context.newSubNode(config, guard);
        }

        context.addVariations(config.getVariations());

        for (Import anImport : config.getImports()) {
            File file = extractImportedFile(anImport.getFilename(), configFile);
            String importGuard = anImport.getGuard();

            String relative = new File("config").toURI().relativize(file.toURI()).getPath();

            logDebug("Stepping into import '" + relative + "'");
            loadConfig(file, importGuard, context);
            context.moveUp();
            currentFile = context.getCurrentNode().getFile();
        }

        return context;
    }

    private File extractImportedFile(String filename, File relativeFile) {
        String importedFile = CharMatcher.WHITESPACE.and(CharMatcher.JAVA_ISO_CONTROL).removeFrom(filename);

        if (importedFile.contains("~")) {
            return new File(importedFile.replace("~", "config"));
        } else {
            return getRelativeFile(importedFile, relativeFile);
        }
    }

    /**
     * Uses the URL class to create a new file object whos path is
     * the given filename relative to the existing file.
     *
     * @param filename The filename of the file to be loaded
     * @param relativeTo The base to which the file is relative
     *
     * @return A new File object with the new file + path
     */
    private File getRelativeFile(String filename, File relativeTo) {
        File newFile = null;
        URL url = null;
        try {
            url = new URL(relativeTo.toURI().toURL(), filename);

            newFile = new File(url.toURI());
        } catch (MalformedURLException e) {
            throw new ConfigurationException("Error: Couldn't find import " + filename, e);
        } catch (URISyntaxException e) {
            newFile = new File(url.getFile());
        }

        return newFile;
    }

    private void logDebug(String output) {
        String name = "unknown.log";
        if (currentFile != null) {
            name = currentFile.getName();
        }
		Monitor.log(SimCfgFileParser.class, Level.DEBUG, "[" + name + "] "
				+ output);
    }

    private void logInfo(String output) {
        String name = "unknown.log";
        if (currentFile != null) {
            name = currentFile.getName();
        }
		Monitor.log(SimCfgFileParser.class, Level.DEBUG, "[" + name + "] "
				+ output);
    }
}
