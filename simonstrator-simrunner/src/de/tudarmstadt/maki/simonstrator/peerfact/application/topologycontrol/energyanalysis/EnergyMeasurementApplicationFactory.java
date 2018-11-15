package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.io.File;
import java.util.regex.Pattern;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponent;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.tc.utils.DateHelper;

public class EnergyMeasurementApplicationFactory implements HostComponentFactory {

	private EnergyMeasurementConfiguration config = new EnergyMeasurementConfiguration();

	private String filename;

	public void setOutputFile(final String filename) {
		this.filename = filename;
	}

	public void setRole(final String role) {
		config.role = EnergyMeasurementRole.valueOf(role);
	}

	public void setTransmissionFrequencyAverageInSeconds(final double transmissionFrequencyAverageInSeconds) {
		config.transmissionFrequencyAverageInSeconds = transmissionFrequencyAverageInSeconds;
	}

	public void setMessageSizeInBytes(final long messageSizeInBytes) {
		config.messageSizeInBytes = messageSizeInBytes;
	}

	@Override
	public HostComponent createComponent(final Host host) {
		config.host = host;
		config.seed = Simulator.getSeed();

		final double dataRate = config.messageSizeInBytes / config.transmissionFrequencyAverageInSeconds / 1e6;

		if (filename != null && !filename.isEmpty())
			config.outputFile = new File(filename.replaceAll(Pattern.quote("[ROLE]"), config.role.toString()));
		else
			config.outputFile = new File(EnergyMeasurementConfiguration.DEFAULT_OUTPUT_ROOT,
					String.format("sosym/energy/energy-calib_%s/rateInMBPerSec%.3f/%s/s%d.csv",
							DateHelper.getFormattedDate(), dataRate, this.config.role,
							config.seed));

		switch (config.role) {
		case RECEIVER:
			return new EnergyMeasurementReceiverApplication(config);
		case SENDER:
			return new EnergyMeasurementSenderApplication(config);
		default:
			throw new IllegalStateException("Cannot handle role: " + config.role);
		}
	}

}
