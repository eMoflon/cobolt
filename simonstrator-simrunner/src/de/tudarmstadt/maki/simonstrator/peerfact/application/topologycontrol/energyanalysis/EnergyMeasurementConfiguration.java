package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.io.File;

import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Time;

public class EnergyMeasurementConfiguration {

	public static final File DEFAULT_OUTPUT_ROOT = new File("./output/energy_measurement");

	public static final long DEFAULT_RECORD_BATTERY_INTERVAL = 60 * Time.SECOND;
	public static final long DEFAULT_MESSAGE_SIZE_IN_BYTE = 1000;

	public static final double DEFAULT_TRANSMISSION_FREQUENCY_IN_SECONDS_AVG = 1.0;
	public static final double DEFAULT_TRANSMISSION_FREQUENCY_IN_SECONDS_STD_DEV = 0.05;

	public File configFile;
	public File outputFile;
	public EnergyMeasurementRole role;
	public Host host;
	public long batteryRecordingInterval = DEFAULT_RECORD_BATTERY_INTERVAL;
	public long seed;
	public int batteryCapacityInJoule;
	public PhyType phyType;
	public double movementStepSizeInMeters = 10.0;
	public long movementTimeInterval = 10 * Time.MINUTE; // e.g., 1m, 1s, 1h
	public double initialDistanceInMeters = 10.0;

	// Sender options
	public long messageSizeInBytes = DEFAULT_MESSAGE_SIZE_IN_BYTE;
	public double transmissionFrequencyAverageInSeconds = DEFAULT_TRANSMISSION_FREQUENCY_IN_SECONDS_AVG;
	public double transmissionFrequencyRelativeStdDev = DEFAULT_TRANSMISSION_FREQUENCY_IN_SECONDS_STD_DEV;

}
