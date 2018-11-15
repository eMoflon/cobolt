package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;
import de.tudarmstadt.maki.simonstrator.api.component.transport.ProtocolNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransInfo;
import de.tudarmstadt.maki.simonstrator.api.component.transport.TransMessageListener;
import de.tudarmstadt.maki.simonstrator.api.component.transport.protocol.UDP;

public class EnergyMeasurementReceiverApplication extends EnergyMeasurementApplication
		implements TransMessageListener {

	public static NetID RECEIVER_IP_ADDRESS;
	private UDP transportProtocol;
	private int messageArrivedCount;
	private double receivedDataInMegabyte;

	public EnergyMeasurementReceiverApplication(EnergyMeasurementConfiguration config) {
		super(config);
		this.messageArrivedCount = 0;
		this.receivedDataInMegabyte = 0;
	}

	@Override
	public void initialize() {

		initializeNetworkInterface();

		new RecordBatteryLevelPeriodicOperation(this, config.batteryRecordingInterval)
				.scheduleWithDelay(config.batteryRecordingInterval);
	}

	// From TransMessageListener
	@Override
	public void messageArrived(Message msg, TransInfo sender, int commID) {
		// Monitor.log(getClass(), Level.INFO, "Message arrived %s", msg);
		++this.messageArrivedCount;
		this.receivedDataInMegabyte += msg.getSize() / 1e6;
		if (this.messageArrivedCount % 100000 == 0)
			Monitor.log(getClass(), Level.INFO, "Message count: %,d", this.messageArrivedCount);
	}

	@Override
	public List<String> getHeaderFields() {
		List<String> fields = new ArrayList<>(super.getHeaderFields());
		fields.add("receivedDataInMegabyte");
		fields.add("receivedDataRateInMegabytePerSecond");
		return Collections.unmodifiableList(fields);
	}

	@Override
	public List<Double> getData() {
		final double timeInSeconds = Simulator.getCurrentTime() / Time.SECOND;
		final List<Double> data = new ArrayList<>(super.getData());
		data.add(this.receivedDataInMegabyte);
		data.add(this.receivedDataInMegabyte / timeInSeconds);
		return Collections.unmodifiableList(data);
	}

	private void initializeNetworkInterface() {
		try {
			SimNetInterface networkInterface = getHost().getNetworkComponent().getSimNetworkInterfaces().iterator()
					.next();
			transportProtocol = getHost().getTransportComponent().getProtocol(UDP.class, networkInterface.getNetID(),
					PORT_RECV);

			RECEIVER_IP_ADDRESS = networkInterface.getNetID();
		} catch (ProtocolNotAvailableException e) {
			throw new IllegalStateException("Unable to bind transport protocol");
		}

		transportProtocol.setTransportMessageListener(this);
	}

	public int getMessageArrivedCount() {
		return messageArrivedCount;
	}

	@Override
	public String toString() {
		return String.format("%s [port=%s]", this.getClass().getSimpleName(), PORT_RECV);
	}
}
