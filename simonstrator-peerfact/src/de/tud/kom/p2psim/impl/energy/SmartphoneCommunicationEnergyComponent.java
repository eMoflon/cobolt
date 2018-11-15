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
import de.tud.kom.p2psim.api.energy.EnergyCommunicationComponent;
import de.tud.kom.p2psim.api.energy.EnergyEventListener;
import de.tud.kom.p2psim.api.energy.EnergyModel;
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tud.kom.p2psim.impl.simengine.Simulator;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.Monitor.Level;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * An energy component, which models the power consumption for the Wi-Fi ad ho
 * communication over a smartphone. The model and values are based on the Master
 * Thesis from Fabian Kaup.
 * 
 * @author stingl
 */
public class SmartphoneCommunicationEnergyComponent implements
		EnergyCommunicationComponent {
	
	private PhyType phy;
	
	private EnergyEventListener energyModel;
	
	/**
	 * The different states of this energy component.
	 */
	private final EnergyState IDLE, SEND, RECEIVE, OFF;
	
	/**
	 * Represents the state, this energy component is currently in.
	 */
	private EnergyState currentState;

	/**
	 * Represents the time, when the energy component entered the current energy
	 * state.
	 */
	private long lastStateChange;

	public SmartphoneCommunicationEnergyComponent(PhyType phy) {
		this.phy = phy;
		
		/*
		 * For a better comparison, e.g., with Feeney, the corresponding values
		 * for the power consumption are represented in µJW. IDLE consists of
		 * the WI-FI-IDLE-State (0.314W) and the Device-Lock (0.039W) and is
		 * always consumed. For that reason, the IDLE-State is always calculated
		 * over the whole online-time of a node, i.e., if a node goes offline or
		 * a simulation is finished.
		 */
		IDLE = new DefaultEnergyState("IDLE", (0.314 + 0.039) * 1000000);
		SEND = new DefaultEnergyState("SEND", 0.687 * 1000000);
		RECEIVE = new DefaultEnergyState("RECEIVE", 0.352 * 1000000);
		OFF = new DefaultEnergyState("OFF", 0);

		this.currentState = IDLE;
		this.lastStateChange = Simulator.getCurrentTime();
	}

	@Override
	public ComponentType getType() {
		return ComponentType.COMMUNICATION;
	}

	@Override
	public void turnOff() {
		if (!currentState.equals(OFF)) {
			doStateChange(OFF);
		}
	}

	@Override
	public boolean turnOn() {
		if (energyModel.turnOn(this)) {
			if (!currentState.equals(IDLE)) {
				doStateChange(IDLE);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isOn() {
		return !currentState.equals(OFF);
	}

	@Override
	public void setEnergyEventListener(EnergyEventListener listener) {
		this.energyModel = listener;
	}

	@Override
	public void eventOccurred(Object content, int type) {
		// not required
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void send(long duration, Message msg, boolean isBroadcast) {
		assert isOn();
		Monitor.log(
				SmartphoneCommunicationEnergyComponent.class,
				Level.DEBUG,
				Time.getFormattedTime()
				+ " "
				+ ((EnergyModel) energyModel).getHost().getHostId()
				+ " consumed "
				+ (SEND.getEnergyConsumption() * (duration/ (double) Simulator.SECOND_UNIT))
				+ " uJ in State " + SEND.getName() + " after spending "
				+ (duration / (double) Simulator.SECOND_UNIT) + " sec there.");
		energyModel.switchedState(this, SEND, null,
				duration);
	}

	@Override
	public void receive(long duration, Message msg, boolean isBroadcast,
			boolean isIntendedReceiver) {
		assert isOn();
		Monitor.log(
				SmartphoneCommunicationEnergyComponent.class,
				Level.DEBUG,
				Time.getFormattedTime()
						+ " "
						+ ((EnergyModel) energyModel).getHost().getHostId()
						+ " consumed "
						+ (RECEIVE.getEnergyConsumption() * (duration / (double) Simulator.SECOND_UNIT))
						+ " uJ in State " + RECEIVE.getName()
						+ " after spending "
						+ (duration / (double) Simulator.SECOND_UNIT)
						+ " sec there.");
		energyModel.switchedState(this, RECEIVE, null,
				duration);
	}

	public void doFakeStateChange() {
		doStateChange(currentState);
	}

	private void doStateChange(EnergyState newState){
		long timeSpentInState = Simulator.getCurrentTime() - lastStateChange;
		Monitor.log(
				SmartphoneCommunicationEnergyComponent.class,
				Level.DEBUG,
				Time.getFormattedTime()
						+ " "
						+ ((EnergyModel) energyModel).getHost().getHostId()
						+ " consumed "
						+ (currentState.getEnergyConsumption() * (timeSpentInState / (double) Simulator.SECOND_UNIT))
						+ " uJ in State " + currentState.getName()
						+ " after spending "
						+ (timeSpentInState / (double) Simulator.SECOND_UNIT)
						+ " sec there.");
		energyModel.switchedState(this, currentState, newState,
				timeSpentInState);
		currentState = newState;
		lastStateChange = Simulator.getCurrentTime();
	}

}
