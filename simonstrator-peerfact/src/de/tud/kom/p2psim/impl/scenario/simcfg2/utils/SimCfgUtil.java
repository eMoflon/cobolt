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

package de.tud.kom.p2psim.impl.scenario.simcfg2.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.After;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.Configure;
import de.tud.kom.p2psim.impl.scenario.simcfg2.annotations.UnknownSetting;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.util.Tuple;

/**
 * Contains a some convenience methods used by the SimCfgConfigurator and
 * its support classes.
 * 
 * @author Fabio Zöllner
 * @version 1.0, 25.04.2012
 */
public class SimCfgUtil {
	/**
	 * Converts the underscore names used in the configuration file
	 * to a camel case equivalent used by Java with the first letter
	 * in upper case.
	 * 
	 * @param name
	 * 	      The name of the identifier in underscore notation
	 * @return
	 * 		  The name in camel case with the first letter being in upper case.
	 */
	public static String convertToInternalSetterNameFormat(String name) {
        if (name == null) return null;

		if (name.startsWith("^")) {
			name = name.substring(1);
		}
		
		if (!name.contains("_")) {
			return name.substring(0, 1).toUpperCase() + name.substring(1, name.length());
		}
		
		String[] parts = name.split("_");
		
		StringBuffer buffer = new StringBuffer();
		for (String part : parts) {
			buffer.append(part.substring(0, 1).toUpperCase());
			buffer.append(part.substring(1, part.length()));
		}
		
		return buffer.toString();
	}

	/**
	 * Searched for a method annotated with UnknownSetting, two parameters from which one
	 * is String and the other is Value.
	 * 
	 * @param componentClass
	 * @return
	 */
	public static Method findMagicMethod(Class componentClass) {
		Method method = SimCfgUtil.findMethodWithAnnotation(componentClass, UnknownSetting.class);

		if (method != null && method.getParameterTypes() != null 
				&& method.getParameterTypes().length == 2
				&& method.getParameterTypes()[0].equals(String.class)
				&& (method.getParameterTypes()[1].equals(Value.class) // Replace Object with other accepted types, was Value before
						|| method.getParameterTypes()[1].equals(String.class))) {
			return method;
		} else if (method != null) {
			throw new ConfigurationException("The method annotated with @UnknownSetting must have the signature 'public void method(String, Value)'.");
		}
		
		return null;
	}
	
	/**
	 * Converts the underscore names used in the configuration file
	 * to a camel case equivalent used by Java with the first letter
	 * being in lower case.
	 * 
	 * @param name
	 * 	      The name of the identifier in underscore notation
	 * @return
	 * 		  The name in camel case with the first letter being in lower case.
	 */
	public static String convertToInternalParameterNameFormat(String name) {
		String internalSetterNameFormat = convertToInternalSetterNameFormat(name);
		
		return internalSetterNameFormat.substring(0, 1).toLowerCase() + internalSetterNameFormat.substring(1, internalSetterNameFormat.length());
	}
	
	/**
	 * Searches for all methods that are annotated with the Configure annotation
	 * and returns them as a list.
	 * 
	 * If no method with the annotation is found the list may be empty.
	 * 
	 * @param componentClass
	 * @return
	 */
	public static List<Method> findConfigureMethods(Class componentClass) {
		List<Method> methods = Lists.newArrayList();
		for (Method method : componentClass.getMethods()) {
			@SuppressWarnings("unchecked")
			Annotation configureAnnotation = method.getAnnotation(Configure.class);
			
			if (configureAnnotation != null) {
				methods.add(method);
			}
		}
		return methods;
	}
	
	//public static Tuple<List<Class>, List<Class>> getConfigureMethodDependencies(Method method) {
	//	Configure annoation = method.getAnnotation(Configure.class);
	//
	//	return new Tuple<List<Class>, List<Class>>(Lists.newArrayList(annoation.requires()), Lists.newArrayList(annoation.optional()));
	//}

	/**
	 * Finds and returns a method in the given class annotated with a specific annotation.
	 * 
	 * Returns null if no method with the annotation could be found.
	 * 
	 * @param componentClass
	 * @param annotation
	 * @return
	 */
	public static Method findMethodWithAnnotation(Class componentClass, Class annotation) {
		for (Method method : componentClass.getMethods()) {
			@SuppressWarnings("unchecked")
			Annotation configureAnnotation = method.getAnnotation(annotation);
			
			if (configureAnnotation != null) {
				return method;
			}
		}

		return null;
	}

	/**
	 * Searches and returns the method with a specific name
	 * on the given class.
	 * 
	 * Returns null if the method couldn't be found.
	 * 
	 * @param componentClass
	 * @param methodName
	 * @return
	 */
	public static Method findMethod(Class componentClass, String methodName) {
		for (Method method : componentClass.getMethods()) {
			if (method.getName().toLowerCase().equals(methodName.toLowerCase())) {
				return method;
			}
		}

		return null;
	}

	/**
	 * Similar to {@link #findMethod} but checks if the method has
	 * exactly one parameter.
	 * 
	 * @param componentClass
	 * @param setter
	 * @return
     *
     * TODO: Add a way to add parameter types to allow multiple methods with the same name
	 */
	public static Method findSetter(Class componentClass, String setter) {
		Method method = findMethod(componentClass, setter);

		if (method != null && method.getParameterTypes() != null
				&& method.getParameterTypes().length == 1)
			return method;

		return null;
	}

	public static After getConfigureAfterAnnotation(Method method) {
		Annotation configureAnnotation = method.getAnnotation(After.class);
		
		return (After) configureAnnotation;
	}

    public static Tuple<String, String> separateNamespace(String name) {
        String namespace = "";
        String flag = "";


        int n = StringUtils.countMatches(name, ".");

        if (n <= 1) {
            // flag
            flag = name;
        } else {
            // namespace
            String[] split = name.split("\\.");
            flag = split[split.length-1];
            namespace = name.substring(0, name.length() - (flag.length()+1));
        }

        return Tuple.create(namespace, flag);
    }
}
