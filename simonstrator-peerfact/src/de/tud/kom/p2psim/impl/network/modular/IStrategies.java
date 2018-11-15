/*
 * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
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


package de.tud.kom.p2psim.impl.network.modular;

import de.tud.kom.p2psim.impl.network.modular.st.FragmentingStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.JitterStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.LatencyStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PLossStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PacketSizingStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.PositioningStrategy;
import de.tud.kom.p2psim.impl.network.modular.st.TrafficControlStrategy;

/**
 * The set of strategies used by the current NetLayer
 * @author 
 *
 */
public interface IStrategies {

	public PacketSizingStrategy getPacketSizingStrategy();
	
	public TrafficControlStrategy getTrafficControlStrategy();
	
	public PLossStrategy getPLossStrategy();
	
	public LatencyStrategy getLatencyStrategy();
	
	public PositioningStrategy getPositioningStrategy();

	public FragmentingStrategy getFragmentingStrategy();
	
	public JitterStrategy getJitterStrategy();
	
}
