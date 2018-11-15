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

package de.tud.kom.p2psim.impl.transport.modular;

import de.tud.kom.p2psim.api.analyzer.TransportAnalyzer;
import de.tud.kom.p2psim.api.common.SimHost;
import de.tud.kom.p2psim.api.network.NetProtocol;
import de.tud.kom.p2psim.api.network.SimNetInterface;
import de.tudarmstadt.maki.simonstrator.api.Message;
import de.tudarmstadt.maki.simonstrator.api.Monitor;
import de.tudarmstadt.maki.simonstrator.api.component.core.MonitorComponent.AnalyzerNotAvailableException;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;

/**
 * This provides the base class for a Transport-Protocol implementation for the
 * {@link ModularTransLayer}.
 * 
 * @author Bjoern Richerzhagen
 * @version 1.0, 07.05.2012
 */
public abstract class AbstractTransProtocol implements ITransProtocol {

	private final NetProtocol netProtocol;

	private SimHost host;

	private final SimNetInterface netLayer;

	protected boolean hasAnalyzer = false;

	protected TransportAnalyzer transportAnalyzerProxy;

	public AbstractTransProtocol(SimHost host, SimNetInterface netLayer,
			NetProtocol netProtocol) {
		this.netProtocol = netProtocol;
		this.netLayer = netLayer;
		this.host = host;
		try {
			transportAnalyzerProxy = Monitor.get(TransportAnalyzer.class);
			hasAnalyzer = true;
		} catch (AnalyzerNotAvailableException e) {
			// no analyzer, no problem
		}
	}

	public SimNetInterface getNetInterface() {
		return netLayer;
	}

	public SimHost getHost() {
		return host;
	}

	/**
	 * Send a message by passing it to the Netlayer
	 * 
	 * @param msg
	 * @param receiver
	 */
	public void doSend(Message msg, NetID receiver) {
		netLayer.send(msg, receiver, netProtocol);
	}

}
