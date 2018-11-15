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


package de.tud.kom.p2psim.impl.util.guirunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

/**
 * Klasse dient zum einfachen Zugriff auf eine XML-Config-Datei.
 * 
 * Zusätzliche Info: Alle nötigen Methoden sind statisch. Es ist daher keine
 * Instanz der Klasse notwendig.
 * 
 * @author Julius Rückert
 * 
 */

public class Config {

	/**
	 * Pfad für die zu benutzende Config-Datei
	 */
	private static String configFile = "guiCfg/config.xml";

	/**
	 * Enthält den XML-Baum der Config-Datei
	 */
	private static Document config;

	/**
	 * Legt falls nötig eine neue Config-Datei mit Wurzelelement an
	 */
	private static void setupFile() {
		File file = new File(configFile);

		if (!file.exists()) { // falls Config-Datei noch nicht existiert
			try {
				file.createNewFile(); // neue Datei anlegen

			} catch (IOException e) {
				// TODO
				e.printStackTrace();
			}

			config = DocumentHelper.createDocument();
			config.addElement("config");

			writeXMLFile();
		}
	}

	/**
	 * Lädt den Inhalt der Config-Datei in den Speicher
	 */
	private static void loadXMLFile() {

		if (config == null) { // Lade Inhalt nur, wenn noch nicht geladen

			setupFile();

			try {
				config = new SAXReader().read(configFile);

			} catch (DocumentException e) {
				// TODO
				System.err.println("Config: DocumentException!");
			}
		}
	}

	/**
	 * Schreibt die bestehende XML-Struktur in die Config-Datei
	 */
	public static void writeXMLFile() {

		if (config != null) { // Schreibe Datei nur, wenn XML-Baum im Speicher
								// vorhanden
			try {
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer = new XMLWriter(new FileWriter(configFile),
						format);
				writer.write(Config.config);
				writer.close();

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Untersucht das XML-Dokumnet <code>config</code> ob Blatt-Element unter
	 * gegebenem Pfad existiert
	 * 
	 * @param leafPath
	 * @return true, falls Blatt mit angegebenem Pfad in XML-Datei existiert,
	 *         sonst false
	 */
	private static boolean leafForKeyExists(String leafPath) {

		ArrayList<String> path = setupPathArrayList(leafPath);
		Element current = config.getRootElement();

		for (int i = 0; i < path.size(); i++) { // durchlaufe Pfad Element weise

			if (current.element(path.get(i)) != null) { // wenn Kind mit Namen
														// path[i] existiert

				current = current.element(path.get(i)); // Kindknoten wird
														// aktueller Knoten

				if (i + 1 == path.size()) { // sind wir bereits beim verlangten
											// Blatt in angegebenem Pfad
											// angekommen?

					if (current.elements().size() == 0) { // ist das
															// verlangte
															// Blatt
															// wirklich ein
															// Blatt in dem
															// XML-Dokument?
						return true;
					} else { // Von Pfad verlangtes Blatt ist kein Blatt in dem
								// XML-Dokument
						return false;
					}
				}
			} else { // Kind mit verlangtem Namen existiert nicht
				return false;
			}
		}
		return false;
	}

	/**
	 * Hilfsfunktion um Wert aus XML-Dokument zu lesen.
	 * 
	 * Vorbedingung: Unbedingt sicher stellen, dass Element in XML-Dokument
	 * existiert!
	 * 
	 * @param leafPath
	 * @return
	 */
	private static String getValueForPath(String leafPath) {
		ArrayList<String> p = setupPathArrayList(leafPath);

		Element current = config.getRootElement();

		for (int i = 0; i < p.size(); i++) {
			current = current.element(p.get(i));
		}
		return current.getText();
	}

	/**
	 * Erzeugt aus dem übergebenen Pfad eine ArrayList mit den einzelnen Teilen.
	 * String wird anhand von "/" getrennt und leere Elemente entfernt.
	 * 
	 * @param path
	 * @return Arraylist der Pfadelemente
	 */
	private static ArrayList<String> setupPathArrayList(String path) {

		ArrayList<String> p = new ArrayList<String>();

		String[] splitted = path.split("/");

		for (String s : splitted) { // entferne alle leeren Felder des Pfades
									// (entstehen z.B. durch / am Anfang oder
									// Ende)
			if (s.length() > 0) {
				p.add(s);
			}
		}
		return p;
	}

	/**
	 * Liefert den Wert des angegebenen Pfads aus der Config-Datei. Existiert
	 * das angegebene Konfigurations-Elements nicht, so wird dieses mit dem
	 * übergebenen Standard-Wert eingefägt.
	 * 
	 * @param valuePath
	 *            der Pfad innerhalb der Config-Datei. Z.B. "gui/width"
	 * @param standardValue
	 * @return der Wert des Konfigurations-Elements aus der Config-Datei, oder
	 *         der Standardwert, wenn das Element noch nicht existiert
	 */
	public static String getValue(String valuePath, String standardValue) {

		loadXMLFile(); // Lade XML-Datei

		if (!leafForKeyExists(valuePath)) { //
			setValue(valuePath, standardValue);
			return standardValue;
		} else {
			return getValueForPath(valuePath);
		}
	}

	public static int getValue(String valuePath, int standardValue) {
		String value = getValue(valuePath, String.valueOf(standardValue));
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException e) {
			System.err
					.println("Config: XML config entry "
							+ valuePath
							+ "=\""
							+ value
							+ "\" cannot be parsed as an integer. Using default value of \""
							+ standardValue + "\" instead.");
			return standardValue;
		}
	}

	public static boolean getValue(String valuePath, boolean standardValue) {
		String value = getValue(valuePath, String.valueOf(standardValue));
		return Boolean.valueOf(value);
	}

	/**
	 * Setzt den Wert des angegebenen Pfads aus der Config-Datei. Existiert das
	 * angegebene Konfigurations-Elements nicht, so wird dieses mit dem
	 * übergebenen Wert eingefügt.
	 * 
	 * @param valuePath
	 *            der Pfad innerhalb der Config-Datei. Z.B. "gui/width"
	 * @param value
	 */
	public static void setValue(String valuePath, String value) {

		loadXMLFile(); // Lade XML-Datei

		ArrayList<String> path = setupPathArrayList(valuePath);
		Element current = config.getRootElement();

		for (int i = 0; i < path.size(); i++) { // durchlaufe Pfad Element weise

			if (current.element(path.get(i)) == null) { // Kind mit verlangtem
														// Namen existiert
														// nicht
				current.addElement(path.get(i)); // Kindknoten als
													// neues Element
													// einfügen
			}
			current = current.element(path.get(i)); // Kindknoten wird
													// aktueller Knoten

			if (i + 1 == path.size()) { // sind wir bereits beim verlangten
										// Blatt in angegebenem Pfad angekommen?

				if (current.elements().size() == 0) { // ist das verlangte
														// Blatt wirklich
														// ein Blatt in dem
														// XML-Dokument?
					current.setText(value); // Wert setzen
				} else { // Von Pfad verlangtes Blatt ist kein Blatt in dem
							// XML-Dokument
				}
			}
		}
		// writeXMLFile(); //von mir herausgenommen, wird explizit aufgerufen am
		// Ende.
	}

	/**
	 * Setzt den Wert des angegebenen Pfads aus der Config-Datei. Existiert das
	 * angegebene Konfigurations-Elements nicht, so wird dieses mit dem
	 * übergebenen Wert eingefügt.
	 * 
	 * @param valuePath
	 *            der Pfad innerhalb der Config-Datei. Z.B. "gui/width"
	 * @param value
	 */
	public static void setValue(String valuePath, int value) {
		setValue(valuePath, String.valueOf(value));
	}

	/**
	 * Setzt den Wert des angegebenen Pfads aus der Config-Datei. Existiert das
	 * angegebene Konfigurations-Elements nicht, so wird dieses mit dem
	 * übergebenen Wert eingefügt.
	 * 
	 * @param valuePath
	 *            der Pfad innerhalb der Config-Datei. Z.B. "gui/width"
	 * @param value
	 */
	public static void setValue(String valuePath, boolean value) {
		setValue(valuePath, String.valueOf(value));
	}

}