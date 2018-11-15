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

package de.tud.kom.p2psim.impl.util.geo.maps;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public abstract class AbstractXMLHandlerMap extends AbstractMap implements EntityResolver, DTDHandler, ContentHandler, ErrorHandler {

	@Override
	public void error(SAXParseException e) throws SAXException {
		// no op
	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		// no op
	}

	@Override
	public void warning(SAXParseException e) throws SAXException {
		// no op
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// no op
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// no op
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {
		// no op
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// no op	
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// no op
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// no op
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// no op
	}

	@Override
	public void startDocument() throws SAXException {
		// no op
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// no op
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// no op
	}

	@Override
	public void notationDecl(String name, String publicId, String systemId)
			throws SAXException {
		// no op
	}

	@Override
	public void unparsedEntityDecl(String name, String publicId, String systemId,
			String notationName) throws SAXException {
		// no op
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		return null;
	}

}
