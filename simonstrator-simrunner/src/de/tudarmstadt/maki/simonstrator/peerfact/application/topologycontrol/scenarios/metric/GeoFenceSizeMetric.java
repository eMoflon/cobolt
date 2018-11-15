package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.metric;

import de.tud.kom.p2psim.api.topology.movement.MovementModel;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.topology.movement.GeoFence;
import de.tud.kom.p2psim.impl.topology.movement.GeoFencedGaussMarkovMovement;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Oracle;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.ComponentFinder;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.PreinitializedMetric;
import de.tudarmstadt.maki.simonstrator.tc.monitoring.metrics.SimpleNumericMetricValue;

/**
 * This overall metric captures the mean of the size dimensions of the current
 * {@link GeoFence} used by the {@link GeoFencedGaussMarkovMovement}
 * 
 * If another movement model is used {@link #NOT_AVAILABLE} is returned.
 * 
 * @author Roland Kluge - Initial implementation
 *
 */
public class GeoFenceSizeMetric extends PreinitializedMetric {

	public static final double NOT_AVAILABLE = -1.0;

	public GeoFenceSizeMetric() {
		super("GeoFence size", MetricUnit.LENGTH, new SimpleNumericMetricValue<Double>(determineGeoFenceSize()));
	}

	private static double determineGeoFenceSize() {
		final TopologyControlComponent topologyControlComponent = ComponentFinder.findTopologyControlComponent();
		for (final Host host : Oracle.getAllHosts()) {
			if (!host.equals(topologyControlComponent.getHost())) {
				try {
					DefaultTopologyComponent defaultTopologyComponent = host
							.getComponent(DefaultTopologyComponent.class);
					MovementModel movementModel = defaultTopologyComponent.getMovementModel();
					if (movementModel instanceof GeoFencedGaussMarkovMovement) {
						final GeoFencedGaussMarkovMovement geoFencedGaussMarkovMovement = (GeoFencedGaussMarkovMovement) movementModel;
						final GeoFence currentGeoFence = geoFencedGaussMarkovMovement.getCurrentGeoFence();
						final double meanOfSizes = (currentGeoFence.getSizeX() + currentGeoFence.getSizeY()) / 2;
						return meanOfSizes;
					}
				} catch (ComponentNotAvailableException e) {
					// nop
				}
			}
		}
		return NOT_AVAILABLE;
	}

}
