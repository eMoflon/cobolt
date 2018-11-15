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


package de.tud.kom.p2psim.impl.util.toolkits;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The DOM toolkit allows generic tools for DOM manipulation
 * 
 * @author Leo Nobach
 *
 */
public class DOMToolkit {

	/**
	 * Returns the first child element of a node that matches the given tag name.
	 * @param parent
	 * @param tagName
	 * @return
	 */
	public static Element getFirstChildElemMatching(Node parent, String tagName) {
		NodeList l = parent.getChildNodes();
		for (int i=0; i<l.getLength(); i++) {
			Node n = l.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(n.getNodeName())) return (Element)n;
		}
		return null;
	}
	
	/**
	 * Returns all child elements of a node matching the given tag name.
	 * @param parent
	 * @param tagName
	 * @return
	 */
	public static List<Element> getAllChildElemsMatching(Node parent, String tagName) {
		NodeList l = parent.getChildNodes();
		List<Element> result = new ArrayList<Element>(30);
		for (int i=0; i<l.getLength(); i++) {
			Node n = l.item(i);
			if (n.getNodeType() == Node.ELEMENT_NODE && tagName.equals(n.getNodeName()))
				result.add((Element)n);
		}
		return result;
	}
	
}
