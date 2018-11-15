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

public class SmartphoneCellularCommunicationEnergyComponent implements
EnergyCommunicationComponent {


	private PhyType phy;

	private EnergyEventListener energyModel;

	/**
	 * The different states of this energy component.
	 */
	private final EnergyState IDLE, TX, RX, RAMP_TX, RAMP_RX, TAIL_TX, TAIL_RX;

	private final long RAMP_RX_DURATION = 1994 * Simulator.MILLISECOND_UNIT;

	private final long TAIL_RX_DURATION = 3132 * Simulator.MILLISECOND_UNIT;

	private final long RAMP_TX_DURATION = 3096 * Simulator.MILLISECOND_UNIT;

	private final long TAIL_TX_DURATION = 4796 * Simulator.MILLISECOND_UNIT;

	private int tailCounter;

	/**
	 * Represents the state, this energy component is currently in.
	 */
	private EnergyState currentState;

	/**
	 * Represents the time, when the energy component entered the current energy
	 * state.
	 */
	private long lastStateChange;


	public SmartphoneCellularCommunicationEnergyComponent(PhyType phy) {
		this.phy = phy;
		IDLE = new DefaultEnergyState("IDLE", 0);
		TX = new DefaultEnergyState("TX", 1.105 * 1000000);
		RAMP_TX = new DefaultEnergyState("RAMP_TX", 0.749 * 1000000);
		TAIL_TX = new DefaultEnergyState("TAIL_TX", 0.563 * 1000000);
		RX = new DefaultEnergyState("RX", 0.786 * 1000000);
		RAMP_RX = new DefaultEnergyState("RAMP_RX", 0.505 * 1000000);
		TAIL_RX = new DefaultEnergyState("TAIL_RX", 0.483 * 1000000);
		tailCounter = 0;

		currentState = IDLE;
		lastStateChange = 0;
	}

	@Override
	public ComponentType getType() {
		return ComponentType.COMMUNICATION;
	}

	@Override
	public void turnOff() {
		if (!currentState.equals(IDLE)) {
			doStateChange(IDLE);
		}
		tailCounter++;
	}

	@Override
	public boolean turnOn() {
		return true;
	}

	@Override
	public boolean isOn() {
		if (energyModel.turnOn(this)) {
			doFakeStateChange();
			return true;
		}
		return false;
	}

	@Override
	public void setEnergyEventListener(EnergyEventListener listener) {
		this.energyModel = listener;
	}

	@Override
	public void eventOccurred(Object content, int type) {
		if (type == tailCounter) {
			assert currentState.equals(TAIL_RX) || currentState.equals(TAIL_TX) : "Assert1 SmartphoneCellularCommunicationEnergy";

			long timeInTailState = Simulator.getCurrentTime() - lastStateChange;

			if((TAIL_RX_DURATION-timeInTailState)!=0 && currentState.equals(TAIL_RX))
				Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.WARN, TAIL_RX_DURATION +" vs. " + timeInTailState);

			if((TAIL_TX_DURATION-timeInTailState)!=0 && currentState.equals(TAIL_TX))
				Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.WARN,TAIL_TX_DURATION +" vs. " + timeInTailState);

			assert timeInTailState==TAIL_TX_DURATION || currentState.equals(TAIL_RX) : "Assert2 SmartphoneCellularCommunicationEnergy";
//			assert timeInTailState==TAIL_TX_DURATION || timeInTailState==TAIL_RX_DURATION : "Assert2 SmartphoneCellularCommunicationEnergy";

			Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG, Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " "
					+ ((EnergyModel) energyModel).getHost().getHostId()
					+ " consumed "
					+ (currentState.getEnergyConsumption() * (timeInTailState / (double) Simulator.SECOND_UNIT))
					+ " uJ in State " + currentState.getName()
					+ " after spending "
					+ (timeInTailState / (double) Simulator.SECOND_UNIT)
					+ " sec there.");
			energyModel
			.switchedState(this, currentState, null, timeInTailState);
			tailCounter++;
			currentState = IDLE;
			lastStateChange = Simulator.getCurrentTime();
		}
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void send(long duration, Message msg, boolean isBroadcast) {
		if (currentState.equals(IDLE)) {
			/*
			 * Consume the energy for being in the ramp state. There is not
			 * explicit ramp state, because the node immediately sends the
			 * message. However, the energy for the ramp state before sending is
			 * consumed.
			 */
			energyModel.switchedState(this, RAMP_TX, null, RAMP_TX_DURATION);
		} else {
			assert currentState.equals(TAIL_RX) || currentState.equals(TAIL_TX);

			long timeInTailState = Simulator.getCurrentTime()
					- (lastStateChange + duration);
			Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG, Simulator.getFormattedTime(Simulator.getCurrentTime())
					+ " "
					+ ((EnergyModel) energyModel).getHost().getHostId()
					+ " consumed "
					+ (currentState.getEnergyConsumption() * (timeInTailState / (double) Simulator.SECOND_UNIT))
					+ " uJ in State " + currentState.getName()
					+ " after spending "
					+ (timeInTailState / (double) Simulator.SECOND_UNIT)
					+ " sec there before starting a transmission.");
			energyModel
			.switchedState(this, currentState, null, timeInTailState);
			tailCounter++;
		}

		/*
		 * Consume the energy for the transmission of data.
		 */
		energyModel.switchedState(this, TX, null, duration);
		Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG, Simulator.getFormattedTime(Simulator.getCurrentTime())
				+ " "
				+ ((EnergyModel) energyModel).getHost().getHostId()
				+ " consumed "
				+ (TX.getEnergyConsumption() * (duration / (double) Simulator.SECOND_UNIT))
				+ " uJ in State " + TX.getName() + " after spending "
				+ (duration / (double) Simulator.SECOND_UNIT) + " sec there.");

		/*
		 * Jump into the tail state and trigger the event to consume the energy
		 * for the tail state after the given amount of time if no message must
		 * be sent or received in the meantime.
		 */
		currentState = TAIL_TX;
		lastStateChange = Simulator.getCurrentTime();
		Simulator.getScheduler().scheduleIn(TAIL_TX_DURATION, this,
				TAIL_TX, tailCounter);
	}

	@Override
	public void receive(long duration, Message msg, boolean isBroadcast,
			boolean isIntendedReceiver) {
		if(currentState.equals(IDLE)){
			/*
			 * Consume the energy for being in the ramp state. There is not
			 * explicit ramp state, because the node immediately receives the
			 * message. However, the energy for the ramp state before receiving
			 * is consumed.
			 */
			energyModel.switchedState(this, RAMP_RX, null, RAMP_RX_DURATION);
		} else {
			assert currentState.equals(TAIL_RX) || currentState.equals(TAIL_TX);

			long timeInTailState = Simulator.getCurrentTime() + duration
					- lastStateChange;
			Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG,Simulator.getCurrentTime()
					+ " "
					+ ((EnergyModel) energyModel).getHost().getHostId()
					+ " consumed "
					+ (currentState.getEnergyConsumption() * (timeInTailState / (double) Simulator.SECOND_UNIT))
					+ " uJ in State " + currentState.getName()
					+ " after spending "
					+ (timeInTailState / (double) Simulator.SECOND_UNIT)
					+ " sec there before receiving a message.");
			energyModel
			.switchedState(this, currentState, null, timeInTailState);
			tailCounter++;
		}
		/*
		 * Consume the energy for the receiption of data.
		 */
		energyModel.switchedState(this, RX, null, duration);
		Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG,Simulator.getCurrentTime()
				+ " "
				+ ((EnergyModel) energyModel).getHost().getHostId()
				+ " consumed "
				+ (RX.getEnergyConsumption() * (duration / (double) Simulator.SECOND_UNIT))
				+ " uJ in State " + RX.getName() + " after spending "
				+ (duration / (double) Simulator.SECOND_UNIT) + " sec there.");

		currentState = TAIL_RX;
		lastStateChange = Simulator.getCurrentTime();
		Simulator.getScheduler().scheduleIn(TAIL_RX_DURATION, this, TAIL_RX,
				tailCounter);
	}

	@Override
	public void doFakeStateChange() {
		doStateChange(currentState);
	}

	private void doStateChange(EnergyState newState){
		long timeSpentInState = Simulator.getCurrentTime() - lastStateChange;
		Monitor.log(SmartphoneCellularCommunicationEnergyComponent.class, Level.DEBUG,Simulator.getFormattedTime(Simulator.getCurrentTime())
				+ " "
				+ ((EnergyModel) energyModel).getHost().getHostId()
				+ " consumed "
				+ (currentState.getEnergyConsumption() * (timeSpentInState/ (double) Simulator.SECOND_UNIT))
				+ " uJ in State " + currentState.getName() + " after spending "
				+ (timeSpentInState / (double) Simulator.SECOND_UNIT)
				+ " sec there.");
		energyModel.switchedState(this, currentState, newState,
				timeSpentInState);
		currentState = newState;
		if(!currentState.equals(TAIL_RX) && !currentState.equals(TAIL_TX))
			lastStateChange = Simulator.getCurrentTime();
	}

}
