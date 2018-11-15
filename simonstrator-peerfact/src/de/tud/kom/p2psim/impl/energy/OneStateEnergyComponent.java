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

import de.tud.kom.p2psim.api.energy.ComponentType;
import de.tud.kom.p2psim.api.energy.EnergyComponent;
import de.tud.kom.p2psim.api.energy.EnergyEventListener;
import de.tudarmstadt.maki.simonstrator.api.Event;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This class provides an energy Component with only one state. Ok there are two
 * states (offline), but it gives only one state, which is running all the time.<br>
 * 
 * The component creates an energy component, which consume every second the
 * given power. It is starting with consuming at the creation of this component!
 * 
 * The component can be stopped and startet with the turnOff and turnOn methods
 * of this class.
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.03.2013
 */
public class OneStateEnergyComponent implements EnergyComponent {

	private DefaultEnergyState state = null;

	private ComponentType componentType = null;

	private EnergyEventListener energyModel;

	boolean on = true;

	/**
	 * Creates the OneStateEnergyComponent with the given power consumption.
	 * 
	 * @param energyConsumption
	 *            The basic energy consumption in uWatt.
	 */
	public OneStateEnergyComponent(double energyConsumptionUwatt,
			ComponentType componentType) {
		this.componentType = componentType;
		state = new DefaultEnergyState(componentType.toString(),
				energyConsumptionUwatt);
		Event.scheduleWithDelay(Time.SECOND, this, this, 0);
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (isOn()) {
			energyModel.switchedState(this, state, state, Time.SECOND);
			if (energyModel.turnOn(this)) {
				Event.scheduleWithDelay(Time.SECOND, this, null, 0);
			} else {
				this.turnOff();
			}
		}
	}

	@Override
	public ComponentType getType() {
		return componentType;
	}

	@Override
	public void turnOff() {
		this.on = false;
	}

	@Override
	public boolean turnOn() {
		this.on = true;
		Event.scheduleWithDelay(Time.SECOND, this, this, 0);
		return true;
	}

	@Override
	public boolean isOn() {
		return on;
	}

	@Override
	public void setEnergyEventListener(EnergyEventListener listener) {
		energyModel = listener;
	}

}
