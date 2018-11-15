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

package de.tud.kom.p2psim.impl.scenario;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.scenario.Builder;
import de.tud.kom.p2psim.api.scenario.Composable;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.api.scenario.Configurator;
import de.tud.kom.p2psim.impl.util.toolkits.Dom4jToolkit;
import de.tudarmstadt.maki.simonstrator.api.Binder;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Rate;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.Component;
import de.tudarmstadt.maki.simonstrator.api.component.GlobalComponent;
import de.tudarmstadt.maki.simonstrator.api.util.XMLConfigurableConstructor;

/**
 * The default implementation of the configuration mechanism. For a detailed
 * explanation, see {@link Configurator}.
 * 
 * @author Konstantin Pussep
 * @author Sebastian Kaune
 * @version 3.0, 14.12.2007
 * 
 */
public class DefaultConfigurator implements Configurator {

	/**
	 * <p>
	 * Elements in the XML config tree may be encapsulated into an element with
	 * this name. They will be treated as if they were direct children of the
	 * parent of this fake container element.
	 * </p>
	 * 
	 * <p>
	 * Needed to work around the lack of support for XPointer in the XInclude
	 * implementation of Apache Xerces (which should be the most common
	 * implementation). Allows to include multiple elements into the XML
	 * configuration files simultaneously by putting it into a root element
	 * named like FAKE_CONTAINER_ELEMENT
	 * </p>
	 */
	private static final String FAKE_CONTAINER_ELEMENT = "IncludeRoot";

	/**
	 * Prefix for a variable inside the config-file. This is used to clarify,
	 * that the provided value for an attribute is not the name of the variable,
	 * but the value, that is represented by the variable.
	 */
	public static final String CONFIG_VARIABLE_PREFIX_TAG = "$";

	/**
	 * The prefix is combined with the name of the currently processed
	 * attribute, that is used to configure the component specified by the
	 * XML-element in the config-file. Via reflection, the newly created
	 * method-name of the implementing class is invoked. So if an XML-element
	 * contains an attribute <code>someAttribute</code>, it is concatenated to
	 * <code>setSomeAttribute</code> and the respective method of the
	 * implementing class, fitting to the created method-signature, is called.
	 */
	public static final String SET_METHOD_PREFIX_TAG = "set";

	/**
	 * Predefined name for the attribute, that specifies the name of the static
	 * method, which instantiates or retrieves the instance of an object.
	 */
	public static final String STATIC_CREATION_METHOD_TAG = "static";

	/**
	 * Predefined name for the attribute, that contains the fully qualified name
	 * of the class, implementing the associated component.
	 */
	public static final String CLASS_TAG = "class";

	/**
	 * If the parser add a XInclude element, then will be add the attribute
	 * "xml:base" to the root element of the adding XML file.
	 */
	public static final String X_INCLUDE_ATTRIBUTE = "base";

	/**
	 * If an attribute within a XML-element contains multiple classes, these
	 * classes are separated by the specified character.
	 */
	protected static final String CLASS_SEPARATOR = ";";

	private Map<String, Object> configurables = new LinkedHashMap<>();

	private LinkedList<Object> componentList = new LinkedList<>();

	private File configFile;

	private Map<String, String> variables = new LinkedHashMap<>();

	/**
	 * Create new configurator instance with the configuration data in the given
	 * XML file.
	 * 
	 * @param file
	 *            XML config file
	 */
	public DefaultConfigurator(String file) {
		configFile = new File(file);
	}

	/**
	 * Gets the name of the configuration file.
	 * 
	 * @return Name of the configuration file.
	 */
	@Override
	public File getConfigFile() {
		return configFile;
	}

	/**
	 * Return a copy of the map with variables.
	 * 
	 * @return A copy of stored variables.
	 */
	@Override
	public Map<String, String> getVariables() {
		Map<String, String> copy = new LinkedHashMap<>();
		for (String key : variables.keySet()) {
			// to copy
			String value = "" + variables.get(key);
			String key2 = "" + key;
			copy.put(key2, value);
		}
		return copy;
	}

	/**
	 * Register a specific component module by the provided name.
	 * 
	 * @param name
	 *            unique name for the component module
	 * @param component
	 *            component module
	 */
	// TODO maybe we should allow it only for internal usage in this class
	@Override
	public void register(String name, Component component) {
		configurables.put(name, component);
		if (component instanceof GlobalComponent) {
			// register in the binder
			Binder.registerComponent((GlobalComponent) component);
		}
	}

	/**
	 * Configure all components of the simulator. The single components are
	 * either registered via the <code>register(name, component)</code> method
	 * or specified in the config file.
	 * 
	 * @return a collection of components.
	 * @throws ConfigurationException
	 */
	@Override
	public Collection<Object> configureAll() throws ConfigurationException {
		Monitor.log(DefaultConfigurator.class, Level.INFO,
				"Configure system from file " + configFile);
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			try {
				factory.setXIncludeAware(true);
			} catch (UnsupportedOperationException e) {
				System.err.println("XInclude aware: false.");

				/*
				 * XInclude not supported!
				 */
			}

			factory.setNamespaceAware(false);
			factory.setValidating(false);

			SAXParser parser = factory.newSAXParser();

			SAXReader reader = new SAXReader(parser.getXMLReader());
			reader.setEntityResolver(new EntityResolver() {
				@Override
				public InputSource resolveEntity(String arg0, String arg1)
						throws SAXException, IOException {
					return new InputSource(arg1);
				}

			});
			Document configuration = reader.read(configFile);

			Element root = configuration.getRootElement();
			assert root.getName().equals(Configurator.CONFIGURATION_ROOT_TAG);
			configureFirstLevel(root);

			return componentList;
		} catch (DocumentException e) {
			throw new ConfigurationException(
					"Failed to load configuration from file " + configFile, e);
		} catch (ParserConfigurationException e) {
			throw new ConfigurationException(
					"Failed to load configuration from file " + configFile, e);
		} catch (SAXException e) {
			throw new ConfigurationException(
					"Failed to load configuration from file " + configFile, e);
		}

	}

	/**
	 * Process the XML subtree.
	 * 
	 * @param parent
	 *            root of the subtree
	 */
	private void configureFirstLevel(Element parent) {
		Monitor.log(DefaultConfigurator.class, Level.DEBUG,
				"Configure simulator using " + parent.asXML());
		for (Object obj : parent.elements()) {
			Element elem = (Element) obj;

			if (elem.getName().equalsIgnoreCase(FAKE_CONTAINER_ELEMENT)) {
				configureFirstLevel(elem);
			} else if (elem.getName().equals(Configurator.DEFAULT_TAG)) {
				for (Iterator iter = elem.elementIterator(); iter.hasNext();) {
					Element variable = (Element) iter.next();
					if (variable != null) {
						String name = variable
								.attributeValue(Configurator.VARIABLE_NAME_TAG);
						String value = variable.attributeValue(
								Configurator.VARIABLE_VALUE_TAG);
						if (!variables.containsKey(name)) {
							// set to default only if not set yet
							variables.put(name, parseValue(value));
						}
					}
				}
			} else {
				configureComponent(elem);
			}
		}
	}

	/**
	 * Create (if not existent yet) and configure a configurable component by
	 * parsing the XML subtree.
	 * 
	 * @param elem
	 *            XML subtree with configuration data
	 * @return configured component
	 */
	public Object configureComponent(Element elem) {

		String name = elem.getName();
		if (Configurator.SPECIAL_IF_EQUAL_STR.equalsIgnoreCase(name)) {
			processIfEqualStr(elem, new ToConfigureCallback() {
				@Override
				public void run(Element elemToConfigure) {
					configureComponent(elemToConfigure);
				}
			});
			return null;
		}

		if (Configurator.SPECIAL_IF_NOT_EQUAL_STR.equalsIgnoreCase(name)) {
			processIfNotEqualStr(elem, new ToConfigureCallback() {
				@Override
				public void run(Element elemToConfigure) {
					configureComponent(elemToConfigure);
				}
			});
			return null;
		}

		Monitor.log(DefaultConfigurator.class, Level.DEBUG,
				"Configure component " + name);

		/*
		 * FIXED (BR) - if a component specifies a class-tag, do NOT reuse the
		 * old component for configuration. Instead, create a new component.
		 */
		// Constructor Attributes
		Object component = configurables.get(name);
		Set<String> consAttrs = new HashSet<>();
		String clazz = getAttributeValue(elem.attribute(CLASS_TAG));
		if (clazz != null) {
			// Create component
			component = createComponent(elem, consAttrs);
		}

		// configure it
		if (component != null) {
			Monitor.log(DefaultConfigurator.class, Level.INFO,
					"Configure component "
							+ component.getClass().getSimpleName()
							+ " with element " + name);
			configureAttributes(component, elem, consAttrs);
			// configure subcomponents
			if (component instanceof Builder) {
				Monitor.log(DefaultConfigurator.class, Level.INFO,
						"Configure builder " + component);
				Builder builder = (Builder) component;
				builder.parse(elem, this);
			} else {
				for (Iterator iter = elem.elementIterator(); iter.hasNext();) {
					Element child = (Element) iter.next();
					if (!consAttrs.contains(child.getName().toLowerCase())) {
						processChild(component, child, consAttrs);
					}
				}
			}
		} else {
			// component cannot be created and has not been registered
			Monitor.log(DefaultConfigurator.class, Level.WARN,
					"Skip element " + name);
		}
		return component;
	}

	private Object createComponent(Element elem, Set<String> consAttrs) {
		if (elem.attribute(CLASS_TAG) == null)
			return null;

		Object component;
		String className = getAttributeValue(elem.attribute(CLASS_TAG));
		Monitor.log(DefaultConfigurator.class, Level.DEBUG, "Create component "
				+ className + " with element " + elem.getName());
		component = createInstance(className,
				getAttributeValue(elem.attribute(STATIC_CREATION_METHOD_TAG)),
				consAttrs, elem);
		if (component instanceof Component) {
			register(elem.getName(), (Component) component);
		}
		// composable can use other components
		if (component instanceof Composable) {
			Monitor.log(DefaultConfigurator.class, Level.DEBUG,
					"Compose composable " + component);
			((Composable) component).compose(this);
		}
		return component;
	}

	/**
	 * 
	 * @param component
	 * @param child
	 * @param consAttrs
	 */
	protected void processChild(final Object component, Element child,
			final Set<String> consAttrs) {

		String name = child.getName();

		if (FAKE_CONTAINER_ELEMENT.equalsIgnoreCase(name)) {
			for (Iterator iter = child.elementIterator(); iter.hasNext();) {
				Element child2 = (Element) iter.next();
				if (!consAttrs.contains(child2.getName().toLowerCase())) {
					processChild(component, child2, consAttrs);
				}
			}
			return;
		}

		if (Configurator.SPECIAL_IF_EQUAL_STR.equalsIgnoreCase(name)) {
			processIfEqualStr(child, new ToConfigureCallback() {
				@Override
				public void run(Element elemToConfigure) {
					processChild(component, elemToConfigure, consAttrs);
				}
			});
			return;
		}

		if (Configurator.SPECIAL_IF_NOT_EQUAL_STR.equalsIgnoreCase(name)) {
			processIfNotEqualStr(child, new ToConfigureCallback() {
				@Override
				public void run(Element elemToConfigure) {
					processChild(component, elemToConfigure, consAttrs);
				}
			});
			return;
		}

		Object subcomponent = configureComponent(child);

		String prefix = SET_METHOD_PREFIX_TAG;
		String methodName = getMethodName(prefix, child.getName());
		Method[] methods = component.getClass().getMethods();
		Method match = null;
		for (int i = 0; i < methods.length; i++) {
			if (methodName.equals(methods[i].getName())) {
				match = methods[i];
				Monitor.log(DefaultConfigurator.class, Level.DEBUG,
						"Match " + match);
				break;
			}
		}
		if (match == null) {
			Monitor.log(DefaultConfigurator.class, Level.WARN,
					"Cannot set " + subcomponent + " as there is no method "
							+ methodName + " declared in " + component);
			throw new ConfigurationException(
					"Cannot set " + subcomponent + " as there is no method "
							+ methodName + " declared in " + component);
		} else {
			Class[] types = match.getParameterTypes();
			Monitor.log(DefaultConfigurator.class, Level.DEBUG,
					"Param types" + Arrays.asList(types));
			if (types.length == 1) {
				try {
					match.invoke(component, types[0].cast(subcomponent));
				} catch (Exception e) {
					throw new ConfigurationException(
							"Failed to configure " + methodName + " in "
									+ component + " with " + subcomponent,
							e);
				}
			} else {
				throw new ConfigurationException("Wrong number of params for "
						+ methodName + " in " + component);
			}
		}
	}

	public void configureAttributes(Object component, Element elem) {
		Set<String> set = Collections.emptySet();
		configureAttributes(component, elem, set);
	}

	private void configureAttributes(Object component, Element elem,
			Set<String> consAttrs) {

		for (Iterator iter = elem.attributeIterator(); iter.hasNext();) {
			Attribute attr = (Attribute) iter.next();
			String name = attr.getName();
			if (!name.equals(CLASS_TAG)
					&& !name.equals(STATIC_CREATION_METHOD_TAG)
					&& !consAttrs.contains(name.toLowerCase())
					&& !name.equals(X_INCLUDE_ATTRIBUTE)) {
				try {
					// try to configure as boolean, int, double, String, or long
					String value = getAttributeValue(attr);
					Method method = null;

					String methodName = getMethodName(SET_METHOD_PREFIX_TAG,
							name);

					Class<? extends Object> classToConfigure = component
							.getClass();
					Method[] methods = classToConfigure.getMethods();
					for (int i = 0; i < methods.length; i++) {
						if (methods[i].getName().equals(methodName)
								&& methods[i].getParameterTypes().length == 1) {
							if (method == null) {
								method = methods[i];
							} else {
								Monitor.log(DefaultConfigurator.class,
										Level.ERROR,
										"Found two possible methods " + method
												+ " and " + methods[i]);
								throw new IllegalArgumentException(
										"Cannot set property " + name
												+ " as there are more than one matching methods in "
												+ classToConfigure);
							}
						}
					}
					if (method == null) {
						/*
						 * Enables a setProperty and getProperty for the
						 * HostProperties that is used as fallback, if no setter
						 * is found.
						 */
						for (int i = 0; i < methods.length; i++) {
							if (methods[i].getName().equals("setProperty")
									&& methods[i]
											.getParameterTypes().length == 2) {
								method = methods[i];
							}
						}
						if (method != null) {
							// Found a 2-param setProperties-Method
							Class typeClass1 = method.getParameterTypes()[0];
							Object param1 = convertValue(name, typeClass1);
							Class typeClass2 = method.getParameterTypes()[1];
							Object param2 = convertValue(value, typeClass2);
							method.invoke(component, param1, param2);
						} else {
							/*
							 * Still nothing?
							 */
							throw new IllegalArgumentException(
									"Cannot set property " + name
											+ " as there are no matching methods in class "
											+ classToConfigure);
						}
					} else {
						Class typeClass = method.getParameterTypes()[0];
						Object param = convertValue(value, typeClass);
						method.invoke(component, param);
					}
				} catch (Exception e) {
					throw new ConfigurationException(
							"Failed to set the property " + name + " in "
									+ component,
							e);
				}
			}
		}

	}

	/**
	 * Automagically convert the string value to desired type. Supported types
	 * are all simple types, i.e. boolean, int, long, double.
	 * 
	 * @param value
	 * @param typeClass
	 * @return converted
	 */
	public static Object convertValue(String value, Class typeClass) {
		Object param;
		if (typeClass == boolean.class || typeClass == Boolean.class) {
			param = Boolean.valueOf(value.equalsIgnoreCase("true")
					|| value.equalsIgnoreCase("yes")
					|| value.equalsIgnoreCase("1"));
		} else if (typeClass == int.class  || typeClass == Integer.class) {
			param = parseNumber(value, Integer.class);
		} else if (typeClass == long.class || typeClass == Long.class) {
			param = parseNumber(value, Long.class);
		} else if (typeClass == double.class || typeClass == Double.class) {
			param = parseNumber(value, Double.class);
		} else if (typeClass == byte.class || typeClass == Byte.class) {
			param = parseNumber(value, Byte.class);
		} else if (typeClass == String.class) {
			param = value;
		} else if (typeClass == short.class || typeClass == Short.class) {
			param = parseNumber(value, Short.class);
		} else if (typeClass == Class.class) {
			param = convertToClass(value);
		} else if (typeClass == File.class) {
			param = convertToFile(value);
		} else if (typeClass.isArray()
				&& typeClass.getComponentType() == Class.class) {
			String[] valueList = value.split(CLASS_SEPARATOR);
			Class[] paramList = new Class[valueList.length];
			for (int i = 0; i < paramList.length; i++) {
				paramList[i] = convertToClass(valueList[i].trim());
			}
			param = paramList;
		} else if (typeClass == String[].class) {
			param = value.split(CLASS_SEPARATOR);
		} else if (typeClass == long[].class) {
			String[] vals = value.split(CLASS_SEPARATOR);
			long[] lvals = new long[vals.length];
			for (int i = 0; i < vals.length; i++) {
				lvals[i] = parseNumber(vals[i], Long.class);
			}
			param = lvals;
		} else {
			throw new IllegalArgumentException("Parameter type " + typeClass
					+ " is not supported");
		}
		return param;
	}

	private static File convertToFile(String value) {
		return new File(value);
	}

	private static Class convertToClass(String value) {
		try {
			return Class.forName(value);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(
					"Failed to parse class object from " + value, e);
		}
	}

	/**
	 * Can be either a variable (if starts with $) or a plain value
	 * 
	 * @param attr
	 * @return proper value
	 */
	private String getAttributeValue(Attribute attr) {
		// TODO implement some arithmetics
		if (attr == null)
			return null;
		String value = attr.getValue();
		value = parseValue(value);
		if (value == null)
			throw new IllegalStateException(
					"Variable " + attr.getValue() + " has not been set");
		return value;
	}

	@Override
	public String parseValue(String value) {
		if (value.trim().startsWith(CONFIG_VARIABLE_PREFIX_TAG)) {
			int posDollar = value.indexOf(CONFIG_VARIABLE_PREFIX_TAG);
			String varName = value.substring(posDollar + 1, value.length());
			value = variables.get(varName);
			Monitor.log(DefaultConfigurator.class, Level.DEBUG,
					"Fetched variable " + varName + " as " + value);
		} else {
			// The following allows for inner-value substitution. For example
			// when using
			// outputFile="outputs/${scenarioName}.dat", the file name will be
			// replaced by the value
			// of a variable named "scenarioName", if existing.

			for (Entry<String, String> e : variables.entrySet()) {
				if (!value.contains(CONFIG_VARIABLE_PREFIX_TAG + "{"))
					break;
				value = value.replace(
						CONFIG_VARIABLE_PREFIX_TAG + "{" + e.getKey() + "}",
						e.getValue());
			}
		}
		return value;
	}

	private String getMethodName(String prefix, String fieldName) {
		return prefix + Character.toUpperCase(fieldName.charAt(0))
				+ fieldName.substring(1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.tud.kom.p2psim.impl.scenario.ConfigurablesManager#getComponent(java
	 * .lang.String)
	 */
	// TODO return ComponentFactory?
	@Override
	public Object getConfigurable(String name) {// TODO we could use
		// Classesinstead of
		// Strings for type
		// safety ...
		return configurables.get(name);
	}

	/**
	 * Create an instance via the reflection of a class by using the given
	 * (full) class name and the optional method name. If the method name is
	 * null, the default constructor will be used. The method's signature should
	 * have no arguments.
	 * 
	 * @param className
	 * @param staticMethod
	 * @param consAttrs
	 * @return create instance
	 * @throws ConfigurationException
	 */
	private Object createInstance(String className, String staticMethod,
			Set<String> consAttrs, Element element2createfrom)
			throws ConfigurationException {
		try {
			Class<?> forName = Class.forName(className);
			Object component = null;
			if (staticMethod == null) {

				Constructor[] cs = forName.getConstructors();

				for (Constructor<?> c : cs) {
					XMLConfigurableConstructor a = c
							.getAnnotation(XMLConfigurableConstructor.class);
					if (a != null) {
						String[] cArgs = a.value();
						Class<?>[] types = c.getParameterTypes();
						if (cArgs.length != types.length)
							throw new ConfigurationException(
									"The size of the argument list of the XML configurable constructor ("
											+ cArgs
											+ ") is unequal to the size of arguments of the constructor is was applied to.");

						// Constructor can be called with the given XML
						// attributes.
						Object[] consArgs = new Object[cArgs.length];

						boolean incompatible = false;
						for (int i = 0; i < consArgs.length; i++) {
							Attribute attr = element2createfrom
									.attribute(cArgs[i]);
							if (attr == null) {
								// Element elem =
								// element2createfrom.element(cArgs[i]);
								Element elem = Dom4jToolkit
										.getSubElementFromStrCaseInsensitive(
												element2createfrom, cArgs[i]);
								if (elem == null) {
									incompatible = true;
									break;
								}
								consArgs[i] = configureComponent(elem);
								if (consArgs[i].getClass()
										.isAssignableFrom(types[i]))
									throw new ConfigurationException(
											"The type of the component configured for the parameter '"
													+ cArgs[i] + "', type is "
													+ consArgs[i].getClass()
															.getSimpleName()
													+ " and is not equal to the type "
													+ types[i].getSimpleName()
													+ " required by the constructor as the argument "
													+ i);
							} else {
								consArgs[i] = convertValue(
										parseValue(attr.getValue()), types[i]);
							}
						}

						if (!incompatible) {
							component = c.newInstance(consArgs);

							for (String consAttr : cArgs) {
								consAttrs.add(consAttr.toLowerCase());
							}
							break;
						}
					}
				}

				if (component == null)
					component = forName.newInstance();

			} else
				component = forName
						.getDeclaredMethod(staticMethod, new Class[0])
						.invoke(null, new Object[0]);

			componentList.add(component);

			return component;
		} catch (Exception e) {
			throw new ConfigurationException(
					"Failed to create configurable " + className, e);
		}
	}

	/**
	 * Set variables with values which replace the variable names in the
	 * configuration file. Default values will be overwritten.
	 * 
	 * @param variables
	 */
	@Override
	public void setVariables(Map<String, String> variables) {
		if (variables.size() != 0) {
			Monitor.log(DefaultConfigurator.class, Level.WARN,
					"Set variables " + variables);
		}
		this.variables.putAll(variables);
	}

	/**
	 * Parse the time according to the following rule: <code>value</code> is a
	 * number followed by a "ms", "s", "m" or "h" for milliseconds, seconds
	 * etc.. The conversion is done according to the constants defined in the
	 * {@link Time} class.
	 * 
	 * If no time-unit is found, we search for bandwidth-units and parse
	 * according to the unit, and if that does not work as well, the long is
	 * returned.
	 * 
	 * @param value
	 *            - time value to parse
	 * @return parsed value
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Number> T parseNumber(String value,
			Class<T> targetClass) {
		assert value != null;
		String number = value;
		double factor = 1;
		// Time
		if (value.matches("\\d+(ms|s|m|h)")) {
			if (value.matches("\\d+(ms)")) {
				number = value.substring(0, value.length() - 2);
				factor = Time.MILLISECOND;
			} else {
				number = value.substring(0, value.length() - 1);
				factor = 1;
				char unit = value.charAt(value.length() - 1);
				switch (unit) {
				case 'h':
					factor *= 60;
				case 'm':
					factor *= 60;
				case 's':
					factor *= Time.SECOND;
					break;
				default:
					throw new IllegalStateException(
							"time unit " + unit + " is not allowed");
				}
			}
		}
		// Bandwidth (internally used in bit/s)
		else if (value.matches("\\d+(Mbits|kbits|bits)")) {
			if (value.matches("\\d+(Mbits)")) {
				factor = Rate.Mbit_s;
				number = value.substring(0, value.length() - 5);
			} else if (value.matches("\\d+(kbits)")) {
				factor = Rate.kbit_s;
				number = value.substring(0, value.length() - 5);
			} else if (value.matches("\\d+(bits)")) {
				factor = Rate.bit_s;
				number = value.substring(0, value.length() - 4);
			} else {
				throw new IllegalStateException("Invalid bandwidth unit.");
			}
		}
		// Size (Byte)
		if (value.matches("\\d+(B|KB|MB)")) {
			if (value.matches("\\d+(B)")) {
				factor = 1;
				number = value.substring(0, value.length() - 1);
			} else if (value.matches("\\d+(KB)")) {
				factor = 1024;
				number = value.substring(0, value.length() - 2);
			} else if (value.matches("\\d+(MB)")) {
				factor = 1024 * 1024;
				number = value.substring(0, value.length() - 2);
			} else {
				throw new IllegalStateException("Invalid memory unit.");
			}
		}

		// Convert type
		double dNum = Double.valueOf(number);
		if (targetClass.equals(Integer.class)) {
			return (T) Integer.valueOf((int) (dNum * factor));
		} else if (targetClass.equals(Double.class)) {
			return (T) Double.valueOf(dNum * factor);
		} else if (targetClass.equals(Long.class)) {
			return (T) Long.valueOf((long) (dNum * factor));
		} else if (targetClass.equals(Short.class)) {
			return (T) Short.valueOf((short) (dNum * factor));
		} else if (targetClass.equals(Byte.class)) {
			return (T) Byte.valueOf((byte) (dNum * factor));
		}
		throw new AssertionError();
	}

	/**
	 * You can create elements like
	 * 
	 * &lt;IfEqualStr arg0="$variable" arg1="value"&gt; [...your configuration
	 * ... ] &lt;/IfEqualStr&gt;, and they will be applied only if the strings
	 * are equal.
	 * 
	 * @param ifClause
	 * @param toExecuteOnTrue
	 * @return
	 */
	private void processIfEqualStr(Element ifClause,
			ToConfigureCallback toExecuteOnTrue) {
		String arg0 = ifClause.attributeValue("arg0");
		String arg1 = ifClause.attributeValue("arg1");

		String arg0p = parseValue(arg0);
		String arg1p = parseValue(arg1);

		if (arg0p == null)
			throw new RuntimeException(
					"Variable " + arg0 + " not set or null.");
		if (arg1p == null)
			throw new RuntimeException(
					"Variable " + arg1 + " not set or null.");

		if (arg0p.equals(arg1p)) {

			Iterator iter = ifClause.elementIterator();
			if (!iter.hasNext())
				Monitor.log(DefaultConfigurator.class, Level.WARN,
						"No component to configure in the ifEqualStr-clause (arg0="
								+ arg0 + ", arg1=" + arg1 + ").");
			else {
				while (iter.hasNext()) {
					Element child = (Element) iter.next();
					toExecuteOnTrue.run(child);
				}
			}
		}
	}

	/**
	 * You can create elements like
	 * 
	 * &lt;IfNotEqualStr arg0="$variable" arg1="value"&gt; [...your
	 * configuration ... ] &lt;/IfNotEqualStr&gt;, and they will be applied only
	 * if the strings are NOT equal.
	 * 
	 * @param ifClause
	 * @param toExecuteOnFalse
	 * @return
	 */
	private void processIfNotEqualStr(Element ifClause,
			ToConfigureCallback toExecuteOnFalse) {
		String arg0 = ifClause.attributeValue("arg0");
		String arg1 = ifClause.attributeValue("arg1");

		String arg0p = parseValue(arg0);
		String arg1p = parseValue(arg1);

		if (arg0p == null)
			throw new RuntimeException(
					"Variable " + arg0 + " not set or null.");
		if (arg1p == null)
			throw new RuntimeException(
					"Variable " + arg1 + " not set or null.");

		if (!arg0p.equals(arg1p)) {

			Iterator iter = ifClause.elementIterator();
			if (!iter.hasNext())
				Monitor.log(DefaultConfigurator.class, Level.WARN,
						"No component to configure in the ifNotEqualStr-clause (arg0="
								+ arg0 + ", arg1=" + arg1 + ").");
			else {
				while (iter.hasNext()) {
					Element child = (Element) iter.next();
					toExecuteOnFalse.run(child);
				}
			}
		}
	}

	/**
	 * Gets the parsed XML-Configuration File. So are the included tags
	 * resolved.
	 * 
	 * @param configFile
	 *            The File of the config
	 * @return The parsed config as text.
	 */
	@Override
	public String getResolvedConfiguration() {
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			factory.setXIncludeAware(true);
			factory.setNamespaceAware(false);
			factory.setValidating(false);

			SAXParser parser = factory.newSAXParser();
			SAXReader reader = new SAXReader(parser.getXMLReader());
			Document configuration = reader.read(configFile);

			StringWriter strWriter = new StringWriter();
			XMLWriter writer = new XMLWriter(strWriter);
			writer.write(configuration);
			return strWriter.toString();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (DocumentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "Cannot parse/read the configuration";
	}

	@Override
	public List<Object> getConfigurable(Class type) {
		List<Object> components = Lists.newArrayList();
		for (Object obj : configurables.values()) {
			if (type.isAssignableFrom(obj.getClass())) {
				components.add(obj);
			}
		}
		return components;
	}

	protected interface ToConfigureCallback {
		public void run(Element elemToConfigure);
	}
}
