package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.energyanalysis;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.operation.PeriodicOperation;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponentEvaluationDataHelper;

public class RecordBatteryLevelPeriodicOperation extends PeriodicOperation<EnergyMeasurementApplication, Void> {

	private List<String> headerFields;

	protected RecordBatteryLevelPeriodicOperation(EnergyMeasurementApplication component, long interval) {
		super(component, null, interval);

		try {
			this.headerFields = component.getHeaderFields();
			FileUtils.writeLines(getComponent().getOutputFile(),
					Arrays.asList(headerFields.stream()
							.collect(Collectors.joining(TopologyControlComponentEvaluationDataHelper.CSV_SEP))),
					TopologyControlComponentEvaluationDataHelper.DO_NOT_APPEND);
		} catch (IOException ex) {
			Monitor.log(getClass(), Level.ERROR, "Failed to write to %s: %s", getComponent().getOutputFile(), ex);
		}
	}

	@Override
	protected void executeOnce() {
		try {
			final List<Double> data = this.getComponent().getData();

			if (data.size() != this.headerFields.size())
				throw new IllegalStateException(
						"Data length " + data.size() + " does not match header size " + this.headerFields.size());

			final List<String> files = Collections.nCopies(data.size(), "%f");
			final String dataLine = String.format(
					StringUtils.join(files, TopologyControlComponentEvaluationDataHelper.CSV_SEP),
					data.toArray());

			Monitor.log(RecordBatteryLevelPeriodicOperation.class, Level.INFO, "[%10s] Recording data: %s",
					getComponent().getRole(), dataLine.trim());

			FileUtils.writeLines(getComponent().getOutputFile(), Arrays.asList(dataLine),
					TopologyControlComponentEvaluationDataHelper.DO_APPEND);

		} catch (final IOException ex) {
			Monitor.log(getClass(), Level.ERROR, "Failed to write to %s: %s", getComponent().getOutputFile(), ex);
		}
	}

	@Override
	public Void getResult() {
		return null;
	}

}