package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tud.kom.p2psim.impl.util.stat.distributions.NormalDistribution;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

public class EnergyMeasurementSenderApplication extends EnergyMeasurementApplication {

	private UDP transportProtocol;

	private final Distribution sendDistribution;

	private double transmittedDataInMegabyte;

	public EnergyMeasurementSenderApplication(EnergyMeasurementConfiguration config) {
		super(config);

		this.transmittedDataInMegabyte = 0.0;

		final double absoluteStandardDeviation = config.transmissionFrequencyRelativeStdDev
				* config.transmissionFrequencyAverageInSeconds;
		this.sendDistribution = new NormalDistribution(//
				config.transmissionFrequencyAverageInSeconds, //
				absoluteStandardDeviation);
	}

	@Override
	public void initialize() {

		new RecordBatteryLevelPeriodicOperation(this, config.batteryRecordingInterval)
				.scheduleWithDelay(this.config.batteryRecordingInterval);

		new SendMessagePeriodicOperation(this.sendDistribution, Time.SECOND).scheduleImmediately();

		try {
			transportProtocol = getHost().getTransportComponent().getProtocol(UDP.class,
					getHost().getNetworkComponent().getSimNetworkInterfaces().iterator().next().getNetID(),
					EnergyMeasurementApplication.PORT_RECV);
		} catch (ProtocolNotAvailableException e) {
			throw new IllegalStateException("Unable to bind transport protocol");
		}
	}

	@Override
	public List<String> getHeaderFields() {
		List<String> fields = new ArrayList<>(super.getHeaderFields());
		fields.add("transmittedDataInMegabyte");
		fields.add("transmittedDataRateInMegabytePerSecond");
		return Collections.unmodifiableList(fields);
	}

	@Override
	public List<Double> getData() {
		final double timeInSeconds = Simulator.getCurrentTime() / Time.SECOND;
		List<Double> data = new ArrayList<>(super.getData());
		data.add(this.transmittedDataInMegabyte);
		data.add(this.transmittedDataInMegabyte / timeInSeconds);
		return Collections.unmodifiableList(data);
	}

	public double getTransmittedDataInMegabyte() {
		return transmittedDataInMegabyte;
	}

	private class SendMessagePeriodicOperation
			extends PeriodicOperation<EnergyMeasurementSenderApplication, Void> {

		private boolean firstExecution = true;

		protected SendMessagePeriodicOperation(Distribution intervalDistribution, long simTimeScaling) {
			super(EnergyMeasurementSenderApplication.this, null, intervalDistribution, simTimeScaling);
		}

		@Override
		protected void executeOnce() {
			// don't send a message at the first execution as this event is
			// triggered at each sensor at the same point in time (collisions!)
			if (firstExecution) {
				firstExecution = false;
				return;
			}

			Monitor.log(getClass(), Level.DEBUG, "Sending a message");
			if (!getHost().getEnergyModel().getInfo().getBattery().isEmpty()) {
				final DataCollectionMessage message = new DataCollectionMessage();

				transmittedDataInMegabyte += message.getSize() / 1e6;
				transportProtocol.send(message, EnergyMeasurementReceiverApplication.RECEIVER_IP_ADDRESS,
						EnergyMeasurementApplication.PORT_RECV);
			}

		}

		@Override
		public Void getResult() {
			return null;
		}
	}

	private class DataCollectionMessage implements Message {

		private static final long serialVersionUID = 8389152799174603539L;

		@Override
		public long getSize() {
			return config.messageSizeInBytes;
		}

		@Override
		public Message getPayload() {
			return null;
		}

	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
}
