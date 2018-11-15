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
import de.tud.kom.p2psim.api.energy.EnergyState;
import de.tud.kom.p2psim.api.linklayer.mac.PhyType;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * This is the implementation of a communication component that does not care
 * about a state - it just consumes energy as soon as something is being sent.
 * In the context of the {@link ModularEnergyModel} we mimic a state-aware
 * behavior. Please take a look at the comment in the Constructor if your
 * analyzers seem to return odd results with this component...
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 27.02.2012
 */
public class StatelessCommunicationComponent implements
		EnergyCommunicationComponent {

	private PhyType phy;

	private EnergyEventListener energyModel;

	/**
	 * States supported by this component
	 */
	private final EnergyState ON, IDLE, OFF;

	private EnergyState currentState;

	private long lastStateChange;

	/*
	 * Parameters in µW as proposed by Feeney
	 */

	private double m_send = 1.89;

	private double b_send = 246;

	private double m_recv = 0.494;

	private double b_recv = 56.1;

	private double m_discard = -0.49;

	private double b_discard = 97.2;

	// private double m_recv_promise = 0.388;

	// private double b_recv_promise = 136;

	private double b_sendctl = 120;

	private double b_recvctl = 29;

	/**
	 * Create a stateless Component
	 * 
	 * @param phy
	 */
	public StatelessCommunicationComponent(PhyType phy) {
		this.phy = phy;

		double baseline = 808000; // 808mW as proposed by Feeney
		/*
		 * To explain the 1000 in ON-State: We want to use the Feeney model
		 * inside the State-Based models. As a state has a fixed energy
		 * consumption assigned with it we have to alter the time the component
		 * spent in the state in order to get a message-size related behavior.
		 * We calculate the energy message based and then normalize it with the
		 * energy value in this state to get the time we have to pass to the
		 * energyModel. The component is all the time in idle, we use the
		 * virtual time trick to add ON-Energy
		 * 
		 * Of course, it makes no sense to analyze the timing-values for this
		 * component - if we have no real states, we have no real time spent
		 * inside a state...
		 */
		ON = new DefaultEnergyState("ON", 1000);
		IDLE = new DefaultEnergyState("IDLE", baseline);
		OFF = new DefaultEnergyState("OFF", 0);

		this.currentState = IDLE;
		this.lastStateChange = Time.getCurrentTime();
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

	private void doStateChange(EnergyState newState) {
		long timeSpentInState = Time.getCurrentTime() - lastStateChange;
		energyModel.switchedState(this, currentState, newState,
				timeSpentInState);
		currentState = newState;
		lastStateChange = Time.getCurrentTime();
	}

	/**
	 * Just to ensure that the energy calculation is correct, once you rely on
	 * the energy level in an analyzer you should call this method prior to
	 * querying the battery.
	 */
	public void doFakeStateChange() {
		doStateChange(currentState);
	}

	/**
	 * This is used to translate an absolute energy consumption into some kind
	 * of state * time like consumption (IDLE -> ACTIVE -> IDLE)
	 * 
	 * @param energyConsumed
	 */
	private void simulateHighPowerState(double energyConsumed) {
		long timeHP = (long) (energyConsumed * Time.SECOND / ON
				.getEnergyConsumption());

		// System.out.println("Simulated HP-Time would be " + timeHP);

		// switch to HP for zero seconds :)
		doStateChange(ON);

		// switch back to IDLE with fake timeHP
		energyModel.switchedState(this, ON, IDLE, timeHP);
		currentState = IDLE;
		lastStateChange = Time.getCurrentTime();
	}

	@Override
	public void receive(long duration, Message msg, boolean isBroadcast,
			boolean isIntendedReceiver) {

		assert !currentState.equals(OFF);
		double consumedEnergy = 0;
		if (isBroadcast) {
			consumedEnergy = m_recv * msg.getSize() + b_recv;
		} else if (isIntendedReceiver) {
			// Paper: Equation 4
			consumedEnergy = b_recvctl + b_sendctl + m_recv * msg.getSize()
					+ b_recv + b_sendctl;
		} else {
			// Paper: Equation 5
			consumedEnergy = b_recvctl + b_sendctl + m_discard * msg.getSize()
					+ b_discard + b_sendctl;
		}

		simulateHighPowerState(consumedEnergy);
	}

	@Override
	public void send(long duration, Message msg, boolean isBroadcast) {

		assert !currentState.equals(OFF);
		double consumedEnergy = 0;
		if (isBroadcast) {
			consumedEnergy = m_send * msg.getSize() + b_send;
		} else {
			// Paper: Equation 3
			consumedEnergy = b_sendctl + b_recvctl + m_send * msg.getSize()
					+ b_send + b_recvctl;
		}

		simulateHighPowerState(consumedEnergy);
	}

	@Override
	public ComponentType getType() {
		return ComponentType.COMMUNICATION;
	}

	@Override
	public PhyType getPhyType() {
		return phy;
	}

	@Override
	public void setEnergyEventListener(EnergyEventListener listener) {
		energyModel = listener;
	}

	@Override
	public void eventOccurred(Object content, int type) {
		// will not happen
	}

	@Override
	public String toString() {
		return phy.toString();
	}

}
