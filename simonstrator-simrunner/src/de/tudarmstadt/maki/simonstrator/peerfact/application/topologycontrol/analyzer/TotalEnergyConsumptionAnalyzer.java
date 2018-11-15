package de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;

import de.tud.kom.p2psim.api.analyzer.EnergyAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.peerfact.application.topologycontrol.scenarios.common.ComponentFinder;
import de.tudarmstadt.maki.simonstrator.tc.component.TopologyControlComponent;
import de.tudarmstadt.maki.simonstrator.tc.io.CSVLineSpecification;

public class TotalEnergyConsumptionAnalyzer implements EnergyAnalyzer {

	double totalEnergyConsumption = 0.0;

	/*
	 * Host -> (EnergyState -> Consumed energy in this state)
	 */
	private Map<SimHost, Map<EnergyState, Double>> energyConsumptionStatistics = new TreeMap<>(new HostIdComparator());

	@Override
	public void start() {
		//
	}

	@Override
	public void stop(Writer out) {
		try {
			out.write("TotalEnergyConsumption=" + totalEnergyConsumption + "\n");
			TopologyControlComponent tcComponent = ComponentFinder.findTopologyControlComponent();
			out.flush();

			final File energyConsumptionPerNodeFolder = new File(tcComponent.getConfiguration().outputFolder, "energyConsumptionPerNode");
			energyConsumptionPerNodeFolder.mkdirs();
			final File energyConsumptionPerNode = new File(energyConsumptionPerNodeFolder,
					String.format("%05d_energyConsumptionPerNode.csv",
							tcComponent.getConfiguration().configurationNumber));

			final List<CSVLineSpecification> csvLines = new ArrayList<>();
			final Collection<EnergyState> energyStates = collectEnergyStates(this.energyConsumptionStatistics);
			final CSVLineSpecification csvHeader = createCsvHeader(energyStates);
			csvLines.add(csvHeader);
			for (final SimHost host : this.energyConsumptionStatistics.keySet()) {
				final CSVLineSpecification csvLineSpec = new CSVLineSpecification(csvHeader.getExpectedLength());
				csvLineSpec.addSpecification("%s", host.getId().toString());
				final Map<EnergyState, Double> perHostMap = this.energyConsumptionStatistics.get(host);
				double totalEnergyConsumption = 0.0;
				for (final EnergyState state : energyStates) {
					final Double value = perHostMap.get(state);
					final double valueToUse = value == null ? 0.0 : value.doubleValue();
					totalEnergyConsumption += valueToUse;
					csvLineSpec.addSpecification("%.1f", valueToUse);
				}
				csvLineSpec.addSpecification("%.1f", totalEnergyConsumption);
				csvLines.add(csvLineSpec);
			}
			FileUtils.writeLines(energyConsumptionPerNode,
					csvLines.stream().map(CSVLineSpecification::format).collect(Collectors.toList()));

		} catch (final IOException e) {
			throw new IllegalStateException();
		}
	}

	private CSVLineSpecification createCsvHeader(final Collection<EnergyState> energyStates) {
		final List<String> energyStateList = energyStates.stream().map(Object::toString).collect(Collectors.toList());
		final CSVLineSpecification csvLine = new CSVLineSpecification(energyStateList.size() + 2);
		csvLine.addSpecification("%s", "ID");
		energyStateList.forEach(energyState -> csvLine.addSpecification("%s", energyState.toString()));
		csvLine.addSpecification("%s", "Total");
		return csvLine;
	}

	private Collection<EnergyState> collectEnergyStates(
			Map<SimHost, Map<EnergyState, Double>> energyConsumptionStatistics2) {
		final Set<EnergyState> energyStates = new LinkedHashSet<>();
		for (final SimHost host : this.energyConsumptionStatistics.keySet()) {
			final Map<EnergyState, Double> perHostMap = this.energyConsumptionStatistics.get(host);
			for (final EnergyState energyState : perHostMap.keySet()) {
				energyStates.add(energyState);
			}
		}
		return energyStates;
	}

	@Override
	public void consumeEnergy(SimHost host, double energy, EnergyComponent consumer, EnergyState energyState) {
		totalEnergyConsumption += energy;

		if (!this.energyConsumptionStatistics.containsKey(host))
			this.energyConsumptionStatistics.put(host, new TreeMap<>(new EnergyStateNameComparator()));

		Map<EnergyState, Double> perHostMap = this.energyConsumptionStatistics.get(host);

		if (!perHostMap.containsKey(energyState)) {
			perHostMap.put(energyState, 0.0);
		}

		perHostMap.put(energyState, perHostMap.get(energyState) + energy);

	}

	@Override
	public void batteryIsEmpty(SimHost host) {
		Monitor.log(getClass(), Level.INFO, "Battery of the following host is empty: %s", host);
	}

	@Override
	public void highPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		//
	}

	@Override
	public void lowPowerMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		//

	}

	@Override
	public void tailMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		//
	}

	@Override
	public void offMode(SimHost host, long time, double consumedEnergy, EnergyComponent component) {
		//
	}

	private final class HostIdComparator implements Comparator<SimHost> {
		@Override
		public int compare(final SimHost o1, final SimHost o2) {
			return o1.getId().compareTo(o2.getId());
		}
	}

	private final class EnergyStateNameComparator implements Comparator<EnergyState> {
		@Override
		public int compare(EnergyState o1, EnergyState o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}
}
