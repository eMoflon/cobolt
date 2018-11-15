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
 * This is the implementation of a communication component that does care about
 * a state - it just consumes energy as soon as something is being sent or
 * received.
 * 
 * 
 * @author Christoph Muenker
 * @version 1.0, 07.02.2013
 */
public class StateEnergyCommunicationComponent implements
		EnergyCommunicationComponent {

	private PhyType phy;

	private EnergyEventListener energyModel;

	/**
	 * States supported by this component
	 */
	private final EnergyState SEND, IDLE, RECV, OFF;

	private EnergyState currentState;

	private long lastStateChange;

	/*
	 * Parameters from SWB-B23 Datasheet - Broadcom BCM4329 WLAN+BT Solution
	 */
	public static double volt = 3.3; // in volt
	public static double sendAmp = 0.2525; // in ampere
	public static double recvAmp = 0.07783; // in ampere
	public static double idleAmp = 0.0004909; // in ampere
	public static double sleepAmp = 0.000125; // in ampere

	/*
	 * Parameters from RN-174
	 * http://www.rovingnetworks.com/resources/download/14/RN_174 (broken)
	 */
//	public static double volt = 3.3; // in volt
//	public static double sendAmp = 0.240; // in ampere (12 Db)
//	public static double recvAmp = 0.040; // in ampere
//	public static double idleAmp = 0.040; // in ampere
//	public static double sleepAmp = 0.000125; // in ampere

	/**
	 * Create a stateless Component
	 * 
	 * @param phy
	 */
	public StateEnergyCommunicationComponent(PhyType phy) {
		this.phy = phy;

		IDLE = new DefaultEnergyState("IDLE", (idleAmp * volt) * 1000000);
		SEND = new DefaultEnergyState("SEND", (sendAmp * volt) * 1000000);
		RECV = new DefaultEnergyState("RECV", (recvAmp * volt) * 1000000);
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
		timeSpentInState = Math.max(0, timeSpentInState);
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

	@Override
	public void receive(long duration, Message msg, boolean isBroadcast,
			boolean isIntendedReceiver) {

		assert !currentState.equals(OFF);
		doStateChange(RECV);
		// we do this change manually, because we can save the event for the end
		// receive!
		energyModel.switchedState(this, RECV, IDLE, duration);
		currentState = IDLE;
		lastStateChange = Time.getCurrentTime() + duration;
	}

	@Override
	public void send(long duration, Message msg, boolean isBroadcast) {

		assert !currentState.equals(OFF);
		doStateChange(SEND);
		// we do this change manually, because we can save the event for the end
		// receive!
		energyModel.switchedState(this, SEND, IDLE, duration);
		currentState = IDLE;
		lastStateChange = Time.getCurrentTime() + duration;
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
		// will not happen!
	}

	@Override
	public String toString() {
		return phy.toString();
	}

}
