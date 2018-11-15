package de.tudarmstadt.maki.simonstrator.tc.facade;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang.exception.ExceptionUtils;

/**
 * A reflection-based factory for {@link ITopologyControlFacade}s.
 */
public class TopologyControlFacadeFactory {

	/**
	 * This methods instantiates a class implementing {@link ITopologyControlFacade}.
	 * 
	 * The class must provide a default constructor.
	 * 
	 * @param className the fully-qualified name of the class to be instantiated
	 * @return the instantiated facade.
	 */
	public static ITopologyControlFacade create(final String className) {
		try {
			final Class<?> facadeClass = Class.forName(className);
			final Constructor<?> defaultConstructor = facadeClass.getConstructor();
			final Object newInstance = defaultConstructor.newInstance();
			return (ITopologyControlFacade) newInstance;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException
				| SecurityException | ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not create the facade. Reason: " + ExceptionUtils.getFullStackTrace(e), e);
		}
	}

}
