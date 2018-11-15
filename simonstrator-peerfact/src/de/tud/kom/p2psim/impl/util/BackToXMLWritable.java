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


package de.tud.kom.p2psim.impl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import de.tudarmstadt.maki.simonstrator.api.Time;


/**
 * Understanding of the XML configurator of PeerfactSim.KOM may be required to understand
 * this interface.
 * 
 * An XML-configurable class in PeerfactSim.KOM that allows for writing back
 * an already configured object to an output stream or a string. The target is to dump default configurations
 * to the simulator user for a better understanding of the components.
 * 
 * To write back a class implementing this interface to an output stream, use
 * <pre>
 * BackToXMLWritable.BackWriter.writeBack(Writer writer, String rootElemName, BackToXMLWritable obj),
 * </pre>
 * where writer is the java.io.Writer to write to, rootElemName is the name of the root element
 * of the XML document, and obj is the configurable object that shall be written back.
 * 
 * You do not need to use a Writer, you also can get back a string containing your element.
 * For that, use
 * <pre>
 * BackToXMLWritable.BackWriter.getWrittenBackDoc(String rootElemName, BackToXMLWritable obj),
 * </pre>
 * which returns a string of the element. 
 * 
 * @author Leo Nobach
 *
 */
public interface BackToXMLWritable {
	
	/**
	 * Every class that implements this interface usually contains 
	 * multiple configurable values of these three types:
	 * <ul>
	 *  <li> simulation time units (long),
	 *  <li> a primitive type (String, int, long when not used as simulation time units)
	 *  <li> another complex configurable object that implements BackToXMLWritable.
	 * </ul>
	 * A class implementing BackToXMLWritable has to implement the method writeBackToXML(BackWriter bw). 
	 * For every value inside this class of these three types, writeBackToXML(...) has to call either:
	 * <ul>
	 *  <li> bw.writeTime(String name, long time),
	 *  <li> bw.writeSimpleType(String name, Object obj), or
	 *  <li> bw.writeComplexType(String name, BackToXMLWritable obj).
	 * </ul>
	 * where "name" is the name of the corresponding XML attribute (time, simple type) or the element (complex type).
	 * Maybe this class does not contain configurable values. Then this method may be empty.
	 * 
	 * @param bw
	 */
	public void writeBackToXML(BackWriter bw);
	
	public static class BackWriter {

		private Element curRootNode;

		private BackWriter(Element element) {
			this.curRootNode = element;
		}
		
		/**
		 * Writes a time in simulation time units.
		 * @param name
		 * @param time
		 */
		public void writeTime(String name, long time) {
			curRootNode.addAttribute(name, Time.getFormattedTime(time));
		}
		
		/**
		 * Writes a simple type (String, int, etc.)
		 * @param name
		 * @param obj
		 */
		public void writeSimpleType(String name, Object obj) {
			curRootNode.addAttribute(name, String.valueOf(obj));
		}
		
		/**
		 * Writes a complex type, that also implements BackToXMLWritable.
		 * @param name
		 * @param obj
		 */
		public void writeComplexType(String name, BackToXMLWritable obj) {
			if (obj == null) return;
			Element subElem = curRootNode.addElement(name);
			subElem.addAttribute("class", obj.getClass().getName());
			obj.writeBackToXML(new BackWriter(subElem));
		}
		
		/**
		 * Returns a written-back document of object obj as a string.
		 * @param rootElemName : the name of the root element, containing
		 * all other elements.
		 * @param obj : the object that shall be written back
		 * @return
		 */
		public static String getWrittenBackDoc(String rootElemName, BackToXMLWritable obj) {
			
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			OutputStreamWriter oswr = new OutputStreamWriter(os);
			try {
				writeBack(oswr, rootElemName, obj);
			} catch (IOException e) {
				//Should never happen
				throw new IllegalStateException(e);
			}
			return os.toString();
			
		}
		
		/**
		 * /**
		 * Writes back the BackToXMLWritable to the given writer.
		 * 
		 * @param writer : the writer to which shall be written back.
		 * @param rootElemName : the name of the root element, containing
		 * all other elements.
		 * @param obj : the object that shall be written back
		 * @throws IOException
		 */
		public static void writeBack(Writer writer, String rootElemName, BackToXMLWritable obj) throws IOException {
			final OutputFormat format = OutputFormat.createPrettyPrint();

			Document document = DocumentHelper.createDocument();
			Element rootElem = document.addElement(rootElemName);
			rootElem.addAttribute("class", obj.getClass().getName());
			obj.writeBackToXML(new BackWriter(rootElem));
			XMLWriter output = new XMLWriter(writer, format);
		        output.write(document);
		        output.close();
		}
		
	}
	
}
