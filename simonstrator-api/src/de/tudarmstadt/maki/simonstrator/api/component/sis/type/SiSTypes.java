/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of Simonstrator.KOM.
 *
 * Simonstrator.KOM is free software: you can redistribute it and/or modify
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

package de.tudarmstadt.maki.simonstrator.api.component.sis.type;

import java.util.LinkedHashMap;
import java.util.Map;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.common.graph.Graph;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INode;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetworkComponent.NetInterfaceName;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.location.Location;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.aggregation.AbstractAggregation.AggregationDouble;
import de.tudarmstadt.maki.simonstrator.api.component.topology.TopologyID;

/**
 * Type registry (e.g., our taxonomy). Dependencies between the flat types are
 * defined via {@link SiSTypeDerivation}s that can be added to the individual
 * types programmatically. Derivations should become a part of the api
 * <strong>iff</strong> their types are also part of the API (e.g.,
 * TransitOverlayContact is to specific, but OverlayConteact might at some time
 * be added).
 *
 * @author Bjoern Richerzhagen
 *
 */
public final class SiSTypes {

	private static final Map<String, SiSType<?>> allTypes = new LinkedHashMap<>();

	/**
	 * [m] Physical distance property (i.e., the distance between two entities).
	 */
	public static final SiSType<Double> PHY_DISTANCE = create("PHY_DISTANCE",
			Double.class, new AggregationDouble());

	/**
	 * [Location] Physical location (coordinates) of an entity
	 */
	public static final SiSType<Location> PHY_LOCATION = create("PHY_LOCATION",
			Location.class, null);

	/**
	 * {@link Graph} containing all neighbors on the WiFi interface as
	 * {@link INode}
	 */
	public static final SiSType<Graph> NEIGHBORS_WIFI = create(
			"NEIGHBORS_WIFI", Graph.class, null);

	/**
	 * Energy (percentage) as double from 0 to 100
	 */
	public static final SiSType<Double> ENERGY_BATTERY_LEVEL = create(
			"ENERGY_BATTERY_LEVEL", Double.class, new AggregationDouble());

	/**
	 * Current energy of the battery in uJ as double from 0 to ...
	 */
	public static final SiSType<Double> ENERGY_BATTERY_CAPACITY = create(
			"ENERGY_BATTERY_CAPACITY", Double.class, new AggregationDouble());

	/**
	 * Current measured cellular latency in ms. Is only available in the
	 * {@link NetInterfaceName}.MOBILE.
	 */
	public static final SiSType<Double> LATENCY_CELL = create("LATENCY_CELL",
			Double.class, new AggregationDouble());


	/**
	 * Marker attribute (mainly for edges/links) to indicate to which topology a
	 * graph element belongs
	 */
	public static final SiSType<TopologyID> TOPOLOGY_ID = create("TOPOLOGY_ID",
			TopologyID.class, null);

	/**
	 * [none] Just a dummy Test attribute of type double. Do not use in
	 * production code.
	 */
	public static final SiSType<Double> TEST_DOUBLE = create("TEST_DOUBLE",
			Double.class, new AggregationDouble());

	/**
	 *
	 * @param name
	 * @param type
	 * @param aggregationFunction
	 * @deprecated Not actually deprecated, just not encouraged. Use this for
	 *             testing new types, but add them to the API once they are stable.
	 */
	@Deprecated
	public static <T> void registerType(String name, Class<T> type,
			SiSTypeAggregation<T> aggregationFunction) {
		Monitor.log(
				SiSType.class,
				Level.WARN,
				"Programmatically registered a type %s with name %s at the SiS. "
						+ "\n\tThis functionality is for testing only. "
						+ "\n\tPlease consider adding your types to the SiS-API later on.",
				type, name);
		create(name, type, aggregationFunction);
	}

	/*
	 * Static methods to add new types programmatically (for development, as you
	 * do not need to change the API every time). Long term goal is then to
	 * include the final versions into the static block above.
	 */

	/**
	 * UNSAFE method to retrieve a type that is <strong>not</strong> available via
	 * the API (e.g., static final members of this class).
	 *
	 * @param type
	 * @param name
	 * @return
	 */
	@Deprecated
	@SuppressWarnings("unchecked")
	public static <T> SiSType<T> getType(String name, Class<T> type) {
		return (SiSType<T>) allTypes.get(name);
	}

	/**
	 * Internal instantiation of the SiSType
	 *
	 * @param name
	 * @param type
	 * @param aggregationFunction
	 *            How to aggregate two values of the type?
	 * @return
	 */
	private static <T> SiSType<T> create(String name, Class<T> type,
			SiSTypeAggregation<T> mergeFunction) {
		SiSType<T> sisType = new SiSType<T>(name, type, mergeFunction);
		if (allTypes.containsKey(name)) {
			throw new AssertionError("Duplicate type with name " + name + "!");
		}
		allTypes.put(name, sisType);
		return sisType;
	}

}