package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common;

import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.TopologyControlInformationStoreComponent;

/**
 * Global-knowledge-enabled utilities to find hosts by component
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class ComponentFinder {

	/**
	 * Retrieves the globally unique {@link TopologyControlComponent} of the
	 * scenario
	 * 
	 * @throws IllegalStateException
	 *             if the component cannot be found
	 */
	public static TopologyControlComponent findTopologyControlComponent() {
		final Class<TopologyControlComponent> componentClass = TopologyControlComponent.class;

		return findComponent(componentClass);
	}

	/**
	 * Retrieves the globally unique
	 * {@link TopologyControlInformationStoreComponent} of the scenario
	 * 
	 * @throws IllegalStateException
	 *             if the component cannot be found
	 */
	public static TopologyControlInformationStoreComponent findInformationStoreComponent() {
		return findComponent(TopologyControlInformationStoreComponent.class);
	}

	/**
	 * Identifies some {@link Host} that provides a component of the given type
	 * and returns the corresponding {@link HostComponent}
	 * 
	 * @param componentClass
	 *            the type of the component to find
	 * @return the component
	 * @see Oracle#getAllHosts()
	 * @see Host#getComponent(Class)
	 */
	public static <T extends HostComponent> T findComponent(final Class<T> componentClass) {
		T component = null;

		for (final Host host : Oracle.getAllHosts()) {
			try {
				component = host.getComponent(componentClass);
				break;
			} catch (ComponentNotAvailableException e) {
				// Simply ignore
			}
		}

		if (component == null)
			throw new IllegalStateException(String.format("Could not find host with component '%s'.", componentClass));

		return component;
	}

}
