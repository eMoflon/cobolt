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
import java.util.Set;
import java.util.Vector;

import de.tud.kom.p2psim.api.analyzer.EnergyAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.energy.ComponentType;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyEventListener;
import de.tud.kom.p2psim.api.energy.EnergyInfo;
import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Time;
import de.tudarmstadt.maki.simonstrator.api.common.graph.INodeID;
import de.tudarmstadt.maki.simonstrator.api.component.ComponentNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sensor.battery.BatterySensor;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSComponent;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSDataCallback;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInfoProperties;
import de.tudarmstadt.maki.simonstrator.api.component.sis.SiSInformationProvider.SiSProviderHandle;
import de.tudarmstadt.maki.simonstrator.api.component.sis.exception.InformationNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.sis.type.SiSTypes;

/**
 * The default energy model that is composed of different
 * {@link EnergyComponent}s and configured via the factory. In contrast to the
 * <i>old</i> EnergyModels these are Host components, ie. there is one instance
 * for each host.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class ModularEnergyModel
		implements EnergyModel, EnergyEventListener, EnergyInfo, BatterySensor {

	// private static Logger log =
	// SimLogger.getLogger(ModularEnergyModel.class);

	private SimHost host;

	private List<EnergyComponent> energyComponents;

	private Battery bat;

	public ModularEnergyModel(SimHost host, Battery bat) {
		energyComponents = new Vector<EnergyComponent>();
		this.bat = bat;
		this.host = host;
	}

	@Override
	public void initialize() {
		//
		try {
			SiSComponent sis = host.getComponent(SiSComponent.class);
			sis.provide().nodeState(SiSTypes.ENERGY_BATTERY_LEVEL,
					new SiSDataCallback<Double>() {

						Set<INodeID> localID = INodeID
								.getSingleIDSet(getHost().getId());

						@Override
						public Double getValue(INodeID nodeID,
								SiSProviderHandle providerHandle)
								throws InformationNotAvailableException {
							if (nodeID.equals(getHost().getId())) {
								return getCurrentPercentage();
							} else {
								throw new InformationNotAvailableException();
							}
						}

						@Override
						public Set<INodeID> getObservedNodes() {
							return localID;
						}

						@Override
						public SiSInfoProperties getInfoProperties() {
							return new SiSInfoProperties();
						}
					});

			sis.provide().nodeState(SiSTypes.ENERGY_BATTERY_CAPACITY,
					new SiSDataCallback<Double>() {

						Set<INodeID> localID = INodeID
								.getSingleIDSet(getHost().getId());

						@Override
						public Double getValue(INodeID nodeID,
								SiSProviderHandle providerHandle)
								throws InformationNotAvailableException {
							if (nodeID.equals(getHost().getId())) {
								return getCurrentEnergyLevel();
							} else {
								throw new InformationNotAvailableException();
							}
						}

						@Override
						public Set<INodeID> getObservedNodes() {
							return localID;
						}

						@Override
						public SiSInfoProperties getInfoProperties() {
							return new SiSInfoProperties();
						}
					});
		} catch (ComponentNotAvailableException e) {
			// OK
		}
	}

	@Override
	public void shutdown() {
		throw new AssertionError(
				"You are not supposed to shutdown this component.");
	}

	@Override
	public <T extends EnergyComponent> List<T> getComponents(ComponentType type,
			Class<T> componentClass) {
		List<T> componentsOfType = new Vector<T>();
		for (EnergyComponent energyComponent : energyComponents) {
			if (energyComponent.getType().equals(type)
					&& componentClass.isInstance(energyComponent)) {
				componentsOfType.add(componentClass.cast(energyComponent));
			}
		}
		return componentsOfType;
	}

	@Override
	public <T extends EnergyComponent> T getComponent(ComponentType type,
			Class<T> componentClass) {
		for (EnergyComponent energyComponent : energyComponents) {
			if (energyComponent.getType().equals(type)
					&& componentClass.isInstance(energyComponent)) {
				return componentClass.cast(energyComponent);
			}
		}
		return null;
	}

	@Override
	public EnergyInfo getInfo() {
		/*
		 * We export our status to interested applications via this interface
		 */
		return this;
	}

	@Override
	public Battery getBattery() {
		return bat;
	}

	@Override
	public double getCurrentPercentage() {
		return bat.getCurrentPercentage();
	}

	@Override
	public double getCurrentEnergyLevel() {
		return bat.getCurrentEnergyLevel();
	}

	@Override
	public void registerComponent(EnergyComponent comp) {
		comp.setEnergyEventListener(this);
		energyComponents.add(comp);
	}

	@Override
	public void switchedState(EnergyComponent component, EnergyState oldState,
			EnergyState newState, long timeSpentInOldState) {

		if (!bat.isEmpty()) {
			double consumedEnergy = oldState.getEnergyConsumption()
					* (timeSpentInOldState / (double) Time.SECOND);
			bat.consumeEnergy(consumedEnergy);
			try {
				EnergyAnalyzer analyzer = Monitor.get(EnergyAnalyzer.class);
				if (analyzer != null)
					analyzer.consumeEnergy(getHost(), consumedEnergy,
							component, oldState);
			} catch (AnalyzerNotAvailableException e1) {
				//
			}

			if (bat.isEmpty()) {
				/*
				 * Battery is now empty. Go offline.
				 */
				try {
					Monitor.get(EnergyAnalyzer.class).batteryIsEmpty(getHost());
				} catch (AnalyzerNotAvailableException e) {
					//
				}
				for (SimNetInterface net : getHost().getNetworkComponent()
						.getSimNetworkInterfaces()) {
					net.goOffline();
				}
			}
		}
	}

	@Override
	public boolean turnOn(EnergyComponent component) {
		return !bat.isEmpty();
	}

	@Override
	public SimHost getHost() {
		return host;
	}

	@Override
	public void reset() {
		bat.reset();
	}

}
