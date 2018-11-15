package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.tud.kom.p2psim.api.application.Application;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.impl.energy.Battery;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.topology.DefaultTopologyComponent;
import de.tud.kom.p2psim.impl.topology.PositionVector;
import de.tud.kom.p2psim.impl.util.oracle.GlobalOracle;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;

/**
 * Generic base class for re-using several components that run on sender and
 * receiver
 */
public abstract class EnergyMeasurementApplication implements Application {

	public static final int PORT_RECV = 5000;

	protected EnergyMeasurementConfiguration config;

	public EnergyMeasurementApplication(final EnergyMeasurementConfiguration config) {
		this.config = config;
	}

	@Override
	public SimHost getHost() {
		return (SimHost) config.host;
	}

	@Override
	public void shutdown() {
		// nop
	}

	public File getOutputFile() {
		return this.config.outputFile;
	}

	public List<Double> getData() {
		final Battery battery = getHost().getEnergyModel().getInfo().getBattery();

		final double timeInMinutes = 1.0 * Simulator.getCurrentTime() / Time.MINUTE;
		final double timeInSeconds = timeInMinutes * 60.0;
		final double batteryLevelPct = battery.getCurrentPercentage();
		final double consumedEnergyInJoule = battery.getConsumedEnergy() / 1e6;
		final double averagePowerInWatt = consumedEnergyInJoule / timeInSeconds;


		final List<SimHost> hosts = GlobalOracle.getHosts();
		if (hosts.size() != 2)
			throw new IllegalStateException(hosts.toString());

		double distanceInMeters = -1;
		try {
			PositionVector firstPosition = hosts.get(0).getComponent(DefaultTopologyComponent.class).getRealPosition();
			PositionVector secondPosition = hosts.get(1).getComponent(DefaultTopologyComponent.class).getRealPosition();
			distanceInMeters = firstPosition.distanceTo(secondPosition);
		} catch (ComponentNotAvailableException e) {
			Monitor.log(getClass(), Level.ERROR, "Problem while fetching positions: %s", e);
		}

		final List<Double> data = Arrays.asList(timeInMinutes, batteryLevelPct, consumedEnergyInJoule,
				averagePowerInWatt, distanceInMeters);

		return data;
	}

	public List<String> getHeaderFields() {
		return Arrays.asList("timeInMinutes", "batteryLevelInPercent", "consumedEnergyInJoule", "averagePowerInWatt",
				"distanceInMeters");
	}

	public EnergyMeasurementRole getRole() {
		return this.config.role;
	}

}
