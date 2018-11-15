/*
 * Copyright (c) 2005-2010 KOM – Multimedia Communications Lab
 *
 * This file is part of PeerfactSim.KOM.
 * 
 * PeerfactSim.KOM is free software: you can redistribute it and/or modify
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

package de.tud.kom.p2psim.impl.energy;

import java.util.List;
import java.util.Random;
import java.util.Vector;

import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.EnergyConfiguration;
import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.scenario.ConfigurationException;
import de.tud.kom.p2psim.impl.util.stat.distributions.StaticDistribution;
import de.tud.kom.p2psim.impl.util.stat.distributions.UniformDistribution;
import de.tudarmstadt.maki.simonstrator.api.Host;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Randoms;
import de.tudarmstadt.maki.simonstrator.api.component.HostComponentFactory;
import de.tudarmstadt.maki.simonstrator.api.util.Distribution;

/**
 * This factory creates and configures an {@link EnergyModel} and its
 * components. Again, we use the LinkLayer-model of configuration presets to
 * ensure a proper configuration and integration, where the model is not
 * configured itself but the user select a configuration that provides some
 * checking of the selected parameters.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 25.02.2012
 */
public class EnergyModelFactory implements HostComponentFactory {

	private Random random;

	/**
	 * A List of the currently active MAC-Layer configurations
	 */
	private List<EnergyConfiguration> energyConfigurations;

	/**
	 * This is used to reset configurations if a new Group of Hosts is to be
	 * configured. We imply that the Model is fully configured for each Group or
	 * not at all to keep the settings of the previous group.
	 */
	private boolean configurationsUsed = false;

	private double batteryCapacity = 186480000;

	/**
	 * This distribution from which the percentage of freshly generated
	 * batteries is drawn. By default, there is no randomness, i.e., we use a
	 * static distribution with value 100%.
	 */
	private Distribution initialBatteryPercentageDistribution = new StaticDistribution(
			1.0);

	/**
	 * A new Factory
	 */
	public EnergyModelFactory() {
		energyConfigurations = new Vector<EnergyConfiguration>();
		random = Randoms.getRandom(EnergyModelFactory.class);
	}

	@Override
	public EnergyModel createComponent(Host pHost) {
		SimHost host = (SimHost) pHost;
		if (energyConfigurations.isEmpty()) {
			throw new ConfigurationException(
					"No Components were specified within the EnergyModel!\n"
							+ "Add at least one child element <Component class=\"...\" /> pointing to an EnergyConfiguration.");
		}
		configurationsUsed = true;

		// clone the battery from the provided model
		final double percentageOfFullBattery = this.initialBatteryPercentageDistribution
				.returnValue();
		double initialEnergy = batteryCapacity * percentageOfFullBattery;
		Battery bat = new SimpleBattery(batteryCapacity, initialEnergy);
		// Battery bat = batteryModel.clone();
		EnergyModel em = new ModularEnergyModel(host, bat);

		for (EnergyConfiguration config : energyConfigurations) {
			em.registerComponent(config.getConfiguredEnergyComponent(host));
		}
		return em;
	}

	/**
	 * Set max battery capacity in J (not uJ)
	 */
	public void setMaxBatteryCapacity(double capacity) {
		this.batteryCapacity = capacity;
	}

	/**
	 * @deprecated Use
	 *             {@link #setInitialBatteryPercentageDistribution(Distribution)}
	 *             instead
	 */
	@Deprecated
	public void setRandomizeInitialCapacity(boolean randomizeInitialCapacity) {
		final UniformDistribution uniformDistribution = new UniformDistribution(
				0.3, 1.0);
		uniformDistribution.setRandom(random);
		this.initialBatteryPercentageDistribution = uniformDistribution;

	}

	public void setInitialBatteryPercentageDistribution(
			Distribution initialBatteryPercentageDistribution) {
		this.initialBatteryPercentageDistribution = initialBatteryPercentageDistribution;
	}

	/**
	 * Load a configuration for this ModelFactory
	 * 
	 * @param energyConfig
	 */
	public void setComponent(EnergyConfiguration energyConfig) {
		if (configurationsUsed) {
			energyConfigurations.clear();
			configurationsUsed = false;
		}
		// check configuration
		if (!energyConfig.isWellConfigured()) {
			throw new ConfigurationException(
					"The EnergyModel was not well configured.\n"
							+ energyConfig.getHelp());
		}
		// just log the information
		Monitor.log(EnergyModelFactory.class, Level.INFO,
				"EnergyModel created - some information:\n %s",
				energyConfig.getHelp());
		energyConfigurations.add(energyConfig);
	}

}
