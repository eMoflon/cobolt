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


package de.tud.kom.p2psim.impl.util.db.relational;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.tud.kom.p2psim.impl.util.toolkits.SAXToolkit;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import javanet.staxutils.IndentingXMLStreamWriter;

/**
 * <p>
 * An simple, high performance relational database for object-oriented environments. A key function 
 * is the ability to quick import from and export to an XML format. Every table is a class, every 
 * row is an object, relations to rows in other tables can easily be accessed by simply calling 
 * the getter function.
 * </p><p>
 * To create an own database with custom tables, simply create a class that extends this database.
 * </p><p>
 * To create a table, create an (inner) class of this database. With this inner class, extend either 
 * from DBObject or StringAddressableDBIDObject (inner classes of RelationalDB). The second one should 
 * be used if every row/object has a unique name, which can be looked up via a given String.
 * Every field accessible via a getter method (get*()) in a class will become a column of the table. 
 * Additionally, this class needs a special constructor with the only parameter IDBObjInstantiator 
 * (inner interface of RelationalDB). Using this object, all fields of this method will be instantiated 
 * when loading them from an XML.
 * </p><p>
 * We will give a very simple example. We have a table called Car. Every car has a String description
 * and a tire. A tire is a relation to another DB table, that is similarly built up. like in this example.
 * </p>
 * 
 * <pre>
 * 	public class Car extends StringAddressableDBIDObject {
 * 
 * 		private Tire tire;
 * 		private String description;
 * 		
 * 		public Tire getTire() {
 * 			return tire;
 * 		}
 *
 *		public Car(String name, Tire tire, String description) {
 *			super(name);
 *			this.tire = tire;
 *			this.description = description;
 *		}
 *		
 *		public Car(IDBObjInstantiator inst) throws DBInstantiationException {
 *			super(inst);
 *			this.tire = inst.getDBObj("tire", Tire.class);
 *			this.description = inst.getString("description");
 *		}
 *		
 *		public String getDescription() {
 *			return description;
 *		}
 *		
 *	}
 *	</pre>
 *
 * <p>
 * Note that the database currently only supports primitive types as DB fields, as well as other DB objects (relations)
 * and java.util.Lists of either primitive objects or other DB objects.
 * </p>
 * <p>
 * This database was written for the Modular Network Layer. So, for a more detailled example how to use this 
 * database, please look into de.tud.kom.p2psim.impl.network.advanced.db.NetMeasurementDB.
 * </p>
 * @see de.tud.kom.p2psim.impl.network.modular.db.NetMeasurementDB 
 * @author Leo Nobach
 *
 */
public abstract class RelationalDB implements XMLSerializable {

	public static String XML_URI = "urn:simulation:pfs:relational";
	
	int ARRAY_LIST_START_SZ = 100;
	
	static final boolean DISALLOW_NULL_REFS = true;
	
	Map<Class<? extends DBObject>, DBIDTypeMeta> idObjectInstances = new HashMap<Class<? extends DBObject>, DBIDTypeMeta>();
	Map<Class<? extends StringAddressableDBIDObject>, Map<String, StringAddressableDBIDObject>> objectsFromString = new HashMap<Class<? extends StringAddressableDBIDObject>, Map<String, StringAddressableDBIDObject>>();
	
	/**
	 * Releases any indices of this database. Saves a lot of memory, but you will not be able to make any subsequent queries in this database.
	 * However, objects that already were queried and stored somewhere else will continue to be accessible. It may be useful to call
	 * System.gc() after this release.
	 */
	public void release() {
		idObjectInstances = null;
		objectsFromString = null;
	}
	
	void checkReleased() {
		if (idObjectInstances == null) throw new IllegalStateException("The database was released to the heap. Can not make any queries or indirect access in it anymore.");
	}
	
	class DBIDTypeMeta {
		
		List<DBObject> l = new ArrayList<DBObject>(ARRAY_LIST_START_SZ);	//can get very large
		Map<Integer, DBObject> lookupByID = new HashMap<Integer, DBObject>();
		
		public List<DBObject> getInstances() {
			return l;
		}
		
		public DBObject getInstanceByID(int id) {
			return lookupByID.get(id);
		}
		
		public int addNewInstance(DBObject obj) {
			int id = l.size();
			l.add(obj);
			lookupByID.put(id, obj);
			return id;
		}
		
		public void addNewInstance(DBObject obj, int id) {
			l.add(obj);
			Object prevVal = lookupByID.put(id, obj);
			assert prevVal == null;		//ensures that the custom primary key is unique
		}
		
	}
	
	/**
	 * An object representing a row in this database. All objects of this
	 * class together, and the class itself will form a table.
	 * @author 
	 *
	 */
	protected abstract class DBObject implements XMLSerializable {
		
		private int id;
		
		/**
		 * Creates a new DBIDObject with an auto-incrementing id.
		 */
		public DBObject() {
			DBIDTypeMeta meta = idObjectInstances.get(this.getClass());
			if (meta == null) {
				meta = new DBIDTypeMeta();
				idObjectInstances.put(this.getClass(), meta);
			}
			this.id = meta.addNewInstance(this);
		}

		/**
		 * Creates a new DBIDObject with a custom ID (must be unique).
		 * @param id
		 */
		public DBObject(int id) {
			setID(id);
		}
		
		private void setID(int id) {
			DBIDTypeMeta meta = idObjectInstances.get(this.getClass());
			if (meta == null) {
				meta = new DBIDTypeMeta();
				idObjectInstances.put(this.getClass(), meta);
			}
			meta.addNewInstance(this, id);
			this.id = id;
		}
		
		public DBObject(IDBObjInstantiator inst) throws DBInstantiationException {
			this.id = inst.getInt("id");
			setID(id);
		}

		public int getId() {
			return id;
		}
		
		@Override
		public void writeToXML(XMLStreamWriter wr) throws XMLStreamException {
			try {
				Method[] methods = this.getClass().getMethods();
				for (Method m : methods) {
					if (!Modifier.isStatic(m.getModifiers())) {
						String mname = m.getName();
						if (mname.startsWith("get") && !"getClass".equals(mname)) {
							Class<?> rt = m.getReturnType();
							assert(isXmlLegalField(rt)):"The return type of the getter method " + mname + " is an unsupported type for serialization: " + rt.getCanonicalName();
							Object mVal = m.invoke(this);
							String strVal;
							strVal = getStrValFromObj(mVal);
							wr.writeAttribute(mname.substring(3).toLowerCase(), strVal);
						}
					}
				}
				
			} catch (IllegalAccessException e) {
				//May not happen
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				//May not happen
				throw new RuntimeException(e);
			} catch (InvocationTargetException e) {
				//May not happen
				throw new RuntimeException(e);
			}
		}

		private String getStrValFromObj(Object mVal) {
			if (mVal == null) return "NULL";
			if (mVal instanceof DBObject) {
				return String.valueOf(((DBObject)mVal).getId());
			}
			if (mVal instanceof List){
				return serializeList((List)mVal);
			}
			return mVal.toString();
		}

		private String serializeList(List mVal) {
			StringBuffer buf = new StringBuffer();
			boolean first = true;
			for (Object subVal : mVal) {
				assert isXmlLegalField(subVal.getClass());
				String subValStr = getStrValFromObj(subVal);
				//we separate strings with "||", we have to escape existing |s with "\|"
				subValStr = subValStr.replace("|", "\\|");
				if (!first) {
					buf.append("||");
				} else first = false;
				buf.append(subValStr);
			}
			return buf.toString();
		}
		
	}
	
	/**
	 * A DBObject that has a unique name that allows to locate it in the database.
	 * @author 
	 *
	 */
	protected abstract class StringAddressableDBIDObject extends DBObject {
		
		private String name;
		
		public StringAddressableDBIDObject(String name) {
			super();
			this.name = name;
			putToMap();
		}

		private void putToMap() {
			assert name != null : "The name of object" + this + " is null.";
			Map<String, StringAddressableDBIDObject> typeMap = objectsFromString.get(this.getClass());
			if (typeMap == null) {
				typeMap = new HashMap<String, StringAddressableDBIDObject>();
				objectsFromString.put(this.getClass(), typeMap);
			}
			Object prevVal = typeMap.put(name, this);
			assert prevVal == null;	//No previous element must exist.
		}
		
		public StringAddressableDBIDObject(IDBObjInstantiator inst) throws DBInstantiationException {
			super(inst);
			this.name = inst.getString("name");
			putToMap();
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * Returns the object that is of class 'type' and has the unique name 'addrStr'. Only works for
	 * StringAddressableDBIDObjects. Returns null if none was found in this DB.
	 * @param <T>
	 * @param type
	 * @param addrStr
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends StringAddressableDBIDObject> T getStringAddrObjFromStr(Class<T> type, String addrStr) {
		checkReleased();
		Map<String, StringAddressableDBIDObject> typeMap = objectsFromString.get(type);
		if (typeMap == null) return null;
		return (T) typeMap.get(addrStr);
	}
	
	/**
	 * Returns the DBObject that is of class 'type' and has the given unique ID.
	 * @param <T>
	 * @param type
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBObject> T getObjFromID(Class<T> type, int id) {
		checkReleased();
		DBIDTypeMeta meta = idObjectInstances.get(type);
		if (meta == null) return null;
		return  (T) meta.getInstanceByID(id);
	}

	/**
	 * Writes the complete database to the given XMLStreamWriter.
	 */
	@Override
	public void writeToXML(XMLStreamWriter wr) throws XMLStreamException {
		for (Class<? extends DBObject> tbl : getDependencySortedSerializationOrder()) {
			writeTableToXML(tbl, wr);
		}
	}
	
	/**
	 * Returns all objects of the given type. Throws an IllegalArgumentException, if this
	 * type was not found.
	 * @param <T>
	 * @param type
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T extends DBObject> List<T> getAllObjects(Class<T> type) {
		checkReleased();
		DBIDTypeMeta meta = idObjectInstances.get(type);
		if (meta == null) throw new IllegalArgumentException("The object of type" + type + " could not be found in the database.");
		return Collections.unmodifiableList((List<T>)meta.getInstances());
		
	}
		
	/**
	 * Writes the table declared by tblClass to the given XMLStreamWriter
	 * @param tblClass
	 * @param wr
	 * @throws XMLStreamException
	 */
	public void writeTableToXML(Class<? extends DBObject> tblClass, XMLStreamWriter wr) throws XMLStreamException {
		checkReleased();
		DBIDTypeMeta instList = idObjectInstances.get(tblClass);
		if (instList == null) {
			Monitor.log(
					RelationalDB.class,
					Level.WARN,
					"Instance table of class %s shall be serialized but is not existing or empty.",
					tblClass.getName());
			return;
		}
		wr.writeStartElement(tblClass.getSimpleName());
		wr.writeNamespace(null, XML_URI);
		wr.writeAttribute("class", tblClass.getName());
		wr.writeAttribute("count", String.valueOf(instList.getInstances().size()));	//Just for statistical purposes, should have no semantical effect.
		for (DBObject inst : instList.getInstances()) {
			wr.writeEmptyElement("r");
			inst.writeToXML(wr);
		}
		wr.writeEndElement();
		
	}

	/**
	 * Returns a list in which objects shall be serialized. Every row/object B having a relation to another 
	 * object A always needs to come AFTER A in this list.
	 * @return
	 */
	//FIXME: develop sorting algo
	public abstract List<Class<? extends DBObject>> getDependencySortedSerializationOrder();
	
	/**
	 * Writes the complete database to a file. The root element of this XML document is given by 'rootElement'
	 * @param file
	 * @param rootElement
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void writeToXMLFile(File file, String rootElement) throws XMLStreamException, IOException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		writeToXMLFile(file, factory, rootElement);
	}
	
	/**
	 * Writes the complete database to a file, by using the given XML out put factory 'factory' to create an XMLStreamWriter. 
	 * The root element of this XML document is given by 'rootElement'
	 * @param file
	 * @param rootElement
	 * @throws XMLStreamException
	 * @throws IOException
	 */
	public void writeToXMLFile(File file, XMLOutputFactory factory, String rootElement) throws XMLStreamException, IOException {
		checkReleased();
		FileWriter fwr = new FileWriter(file);
	    XMLStreamWriter writer = factory.createXMLStreamWriter(fwr);

	    IndentingXMLStreamWriter indwr = new IndentingXMLStreamWriter(writer);	//Ensures indentation of the XML document
	    
	    indwr.setIndent(" ");																				
	    writer = indwr;
	    
	    writer.writeStartDocument("1.0");
	    writer.writeStartElement(rootElement);
	    writeToXML(writer);
	    writer.writeEndElement();
	    writer.writeEndDocument();
	    
	    fwr.flush();
	    fwr.close();
	}

	/**
	 * Returns an XML SAX handler for reading in database tables and rows. 
	 * The handler will only scan for XML
	 * table elements in a given XML depth (root element is depth 0) 
	 * and read table rows contained in these elements.
	 * @param depthOfTableElements
	 * @return
	 */
	public DefaultHandler getXMLHandler(int depthOfTableElements) {
		return new XMLHandlerImpl(depthOfTableElements);
	}
	
	class XMLHandlerImpl extends DefaultHandler {

		int depth = 0;
		
		public XMLHandlerImpl(int depthOfTableElements) {
			this.depth = - depthOfTableElements;
		}
		
		private Class<? extends DBObject> currentClass = null;

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
		}

		@Override
		public void endDocument() throws SAXException {
			super.endDocument();
		}

		@SuppressWarnings("unchecked")
		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes atts) throws SAXException {
			if (depth == 1 && currentClass != null && localName.equals("r")) {
				try {
					createNewDBIDObject(currentClass, SAXToolkit.attributesToMap(atts, false));
				} catch (DBInstantiationException e) {
					throw new SAXException(e);
				}
			} else if (depth == 0 && XML_URI.equals(uri)) {
				String instClass = atts.getValue("class");
				if (instClass != null) {
					try {
						currentClass  = (Class<? extends DBObject>) Class.forName(instClass);
					} catch (ClassNotFoundException e) {
						throw new SAXException(new DBInstantiationException(e));
					}
				}
			}
			
			depth++;
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			depth--;
			
			if (depth == 0) currentClass = null; 
			
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			super.characters(ch, start, length);
		}

		@Override
		public void skippedEntity(String name) throws SAXException {
			super.skippedEntity(name);
		}
		
	}

	public DBObject createNewDBIDObject(Class<? extends DBObject> newObjClass,
			Map<String, String> fieldList) throws DBInstantiationException {
		// TODO Auto-generated method stub
		Constructor<?>[] cons = newObjClass.getConstructors();
		
		for (Constructor<?> con : cons) {
			DBObject obj = tryConstructor(con, fieldList, newObjClass);
			if (obj != null) return obj;
		}
		throw new IllegalArgumentException("No constructor of " + newObjClass + " with one parameter of class DBObjInstantiator does exist (beneath the hidden inner class constructor parameter). " +
				"This is needed for a new DB object to be deserialized.");
	}

	private DBObject tryConstructor(Constructor<?> con, Map<String, String> fieldList, Class<?> clazz) throws DBInstantiationException {
		Class<?>[] pts = con.getParameterTypes();
		if (pts.length != 2) return null;	//the outer DB class of the DBIDObject and the Map parameter 
		if (!pts[0].equals(this.getClass())) throw new DBInstantiationException ("The object of class " + clazz + " that shall be instantiated is not an inner class of " + this.getClass() + ".");
		if (!pts[1].equals(IDBObjInstantiator.class)) return null;
		try {
			DBObject obj = (DBObject)con.newInstance(RelationalDB.this, new DBObjInstantiatorImpl(fieldList));
			//Field [] fs = obj.getClass().getFields();
			return obj;
		} catch (IllegalArgumentException e) {
			throw new DBInstantiationException(e);
		} catch (InstantiationException e) {
			throw new DBInstantiationException(e);
		} catch (IllegalAccessException e) {
			throw new DBInstantiationException(e);
		} catch (InvocationTargetException e) {
			throw new DBInstantiationException(e.getTargetException());
		}
	}

	public class DBObjInstantiatorImpl implements IDBObjInstantiator {

		private Map<String, String> fieldList;

		public DBObjInstantiatorImpl(Map<String, String> fieldList) {
			this.fieldList = fieldList;
		}
		
		/*
		@Override
		public void instantiateFields(DBObject obj, Class<? extends DBObject> clazz) throws DBInstantiationException {
			try {
				Field [] fs = clazz.getDeclaredFields();
				System.out.println("Instantiating Fields");
				for (Field f : fs) {
					if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers())) {
						String fVal = fieldList.get(f.getName());
						if (!"this$0".equals(f.getName())) {	//this is the outer class reference
							if (fVal == null) throw new DBInstantiationException("No parameter there for instantiating field " + f.getName() + " in class " + clazz);
							setField(f, obj, fVal);
						}
					}
				}
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void setField(Field f, DBObject obj, String fVal) throws DBInstantiationException, IllegalAccessException {
			Class<?> ft = f.getType();
			System.out.println("Setting field " + f + " to " + fVal);
			if (String.class.equals(ft)) f.set(obj, fVal);
			else if (Integer.class.equals(ft)) f.set(obj, Integer.parseInt(fVal));
			else if (Long.class.equals(ft)) f.set(obj, Long.parseLong(fVal));
			else if (Byte.class.equals(ft)) f.set(obj, Byte.parseByte(fVal));
			else if (Short.class.equals(ft)) f.set(obj, Short.parseShort(fVal));
			else if (Double.class.equals(ft)) f.set(obj, Double.parseDouble(fVal));
			else if (Float.class.equals(ft)) f.set(obj, Float.parseFloat(fVal));
			else if (DBObject.class.isAssignableFrom(ft)) {
				//we have a relation to another DBIDObject in this field!
				Class<? extends DBObject> dbft = (Class<? extends DBObject>) ft;
				DBObject fObj = getObjFromID(dbft, Integer.parseInt(fVal));
				if (fObj == null) throw new DBInstantiationException("The DBIDObject of type " + ft + " with the ID " + fVal + ", referenced by " + obj + " does not exist.");
				f.set(obj, fObj);
			}
		}
		*/

		private String getRaw(String fName) throws DBInstantiationException {
			String result = fieldList.get(fName);
			if (result == null) throw new DBInstantiationException("The serialized object does not contain a field with " + fName);
			return result;
		}
		
		@Override
		public int getInt(String fName) throws DBInstantiationException {
			try {
				return Integer.parseInt(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public char getChar(String fName) throws DBInstantiationException {
			if (fName.length() != 1) throw new DBInstantiationException("Can not extract a single char from string " + fName);
			return fName.charAt(0);
		}

		@Override
		public boolean getBoolean(String fName) throws DBInstantiationException {
			try {
				return Boolean.parseBoolean(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public String getString(String fName) throws DBInstantiationException {
			return getRaw(fName);
		}

		@Override
		public double getDouble(String fName) throws DBInstantiationException {
			try {
				return Double.parseDouble(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public float getFloat(String fName) throws DBInstantiationException {
			try {
				return Float.parseFloat(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public short getShort(String fName) throws DBInstantiationException {
			try {
				return Short.parseShort(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public byte getByte(String fName) throws DBInstantiationException {
			try {
				return Byte.parseByte(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public long getLong(String fName) throws DBInstantiationException {
			try {
				return Long.parseLong(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
		}

		@Override
		public <T extends DBObject> T getDBObj(String fName, Class<? extends T> clazz)
				throws DBInstantiationException {
			//we have a relation to another DBIDObject in this field!
			int id;
			try {
				id = Integer.parseInt(getRaw(fName));
			} catch (NumberFormatException e) {
				throw new DBInstantiationException(e);
			}
			T fObj = getObjFromID(clazz, id);
			if (fObj == null && DISALLOW_NULL_REFS) throw new DBInstantiationException("The DBIDObject of type " + clazz + " with the ID " + id + 
					" is referenced in the serialized format, but does not exist.");
			return fObj;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public <T> List<T> getList(String fName, Class<? extends T> listElemClass) throws DBInstantiationException {
			String serList = getRaw(fName);
			assert isXmlLegalField(listElemClass);
		
			String[] elements = serList.split("\\|\\|");
			List<T> result = new ArrayList<T>(elements.length);
			if (elements.length == 1 && "".equals(elements[0])) return result; 	//Even an empty XML list ("") returns one element in the array. 
																				//We have to circumvent this problem and explicitly return an empty list.
			for (String elemStr : elements) {
				elemStr = elemStr.replace("\\|", "|");
				T elem;
				if (Integer.class.equals(listElemClass)) {
					elem = (T)new Integer(Integer.parseInt(elemStr));
				} else if (Double.class.equals(listElemClass)) {
					elem = (T)new Double(Double.parseDouble(elemStr));
				} else if (DBObject.class.isAssignableFrom(listElemClass)) {
					int id = Integer.parseInt(elemStr);
					elem = (T)getObjFromID((Class<? extends DBObject>)listElemClass, id);
					if (elem == null) throw new DBInstantiationException("The DBIDObject of type " + listElemClass + " with the ID " + id + " does not exist.");
				} else {
					throw new DBInstantiationException("The class " + listElemClass + " as a list type is currently unsupported by the DB.");
				}
				result.add(elem);
			}
			return result;
		}
		
	}
	
	public static interface IDBObjInstantiator {
		
		//public void instantiateFields(DBObject obj, Class<? extends DBObject> clazz) throws DBInstantiationException;
		
		public int getInt(String fName) throws DBInstantiationException;
		
		public char getChar(String fName) throws DBInstantiationException;
		
		public boolean getBoolean(String fName) throws DBInstantiationException;
		
		public String getString(String fName) throws DBInstantiationException;
		
		public double getDouble(String fName) throws DBInstantiationException;
		
		public float getFloat(String fName) throws DBInstantiationException;
		
		public short getShort(String fName) throws DBInstantiationException;
		
		public byte getByte(String fName) throws DBInstantiationException;
		
		public long getLong(String fName) throws DBInstantiationException;
		
		public <T extends DBObject> T getDBObj(String fName, Class<? extends T> clazz) throws DBInstantiationException;
		
		public <T> List<T> getList(String fName, Class<? extends T> listElemClass) throws DBInstantiationException;
		
		
	}
	
	public String getStats() {
		StringBuffer buf = new StringBuffer();
		buf.append("Tables: " + idObjectInstances.size() + "\n");
		for (Entry<Class<? extends DBObject>, DBIDTypeMeta> e : idObjectInstances.entrySet()) {
			buf.append(e.getKey().getSimpleName() + ": " + e.getValue().getInstances().size() + " rows" + "\n");
		}
		return buf.toString();
	}
	
	/**
	 * 
	 * @param fc
	 * @return
	 */
	static boolean isXmlLegalField(Class<?> fc) {
		return legalClasses.contains(fc) || 
		DBObject.class.isAssignableFrom(fc) ||
		List.class.isAssignableFrom(fc);
	}
	
	static Set<Class<?>> legalClasses = new HashSet<Class<?>>();
	
	static {
		legalClasses.add(String.class);
		legalClasses.add(Integer.class);
		legalClasses.add(Long.class);
		legalClasses.add(Byte.class);
		legalClasses.add(Character.class);
		legalClasses.add(Float.class);
		legalClasses.add(Double.class);
		legalClasses.add(Short.class);
		legalClasses.add(int.class);
		legalClasses.add(long.class);
		legalClasses.add(byte.class);
		legalClasses.add(char.class);
		legalClasses.add(float.class);
		legalClasses.add(double.class);
		legalClasses.add(short.class);
	}
	
	public void readFromXMLFile(InputStream in, SAXParserFactory f) throws SAXException, IOException, ParserConfigurationException {
		DefaultHandler hdlr = getXMLHandler(1);
		f.setNamespaceAware(true);
		SAXParser parser = f.newSAXParser();
		parser.parse(in, hdlr);
	}
	
	public void readFromXMLFile(InputStream in) throws SAXException, IOException, ParserConfigurationException {
		SAXParserFactory f = SAXParserFactory.newInstance();
		readFromXMLFile(in, f);
	}

	
}













