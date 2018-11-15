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

package de.tud.kom.p2psim.api.scenario.simcfg.converter;

import java.util.HashMap;

import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.scenario.simcfg2.configuration.beans.Value;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.BooleanConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.ByteConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.ClassArrayConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.ClassConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.DoubleConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.IntegerConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.LongConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.ShortConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.StringArrayConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.StringConverter;
import de.tud.kom.p2psim.impl.scenario.simcfg2.utils.converter.TypeConverter;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;

public class SimCfgTypeConverter {
	
	public static final String LOSS_OF_PRECISION = "and can lead to a loss of precision";
	public static final String FAULTY_VALUES = "and can lead to faulty values";
	public static final String FAULTY_VALUES_AND_LOSS_OF_PRECISION = "and can lead to faulty values and/or a loss of precision";

	private static HashMap<Class<?>, TypeConverter> converters = new HashMap<Class<?>, TypeConverter>();
	
	private static void addConverter(TypeConverter converter) {
		converters.put(converter.responsibleForType(), converter);
	}
	
	static {
		addConverter(new BooleanConverter());
		addConverter(new ByteConverter());
		addConverter(new ClassArrayConverter());
		addConverter(new DoubleConverter());
		addConverter(new IntegerConverter());
		addConverter(new LongConverter());
		addConverter(new ShortConverter());
        addConverter(new ClassConverter());
        addConverter(new StringArrayConverter());
        addConverter(new StringConverter());
	}
	
	public static Object convertTo(String name, Value value, Class<?> type) {
		Object parameter = null;
		
		TypeConverter converter = converters.get(type);
		
		if (converter == null) {
			throw new ConfigurationException("Error: There is no converter for type '" + type.getSimpleName() + "'.");
		}
		
		parameter = converter.convert(name, value);
		
		if (parameter == null) {
			throw new ConfigurationException("Error: Conversion of parameter '" + name +
					"' of type '" + value.getClass().getSimpleName() + 
					"' to type '" + type.getSimpleName() + "' is not supported.");
		}
		
		return parameter;
	}

	public static void warnAboutConversion(String parameterName, Object value, Class type, String canLeadTo) {
		Monitor.log(SimCfgTypeConverter.class, Level.WARN,
				"Converted parameter '%s' of type '%s' to type '%s', this may not be wanted %s.",
				parameterName, value.getClass().getSimpleName(),
				type.getSimpleName(), canLeadTo);
	}

}
