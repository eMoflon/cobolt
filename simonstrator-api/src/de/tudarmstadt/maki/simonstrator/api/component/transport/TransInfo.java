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



package de.tudarmstadt.maki.simonstrator.api.component.transport;

import de.tudarmstadt.maki.simonstrator.api.common.Transmitable;
import de.tudarmstadt.maki.simonstrator.api.component.network.NetID;


/**
 * This class encapsulates the <code>NetID</code> and port on which a specified
 * remote <code>TransMessageListener</code> is listening. Every time a message
 * is send through the <code>TransLayer</code>, the <code>NetID</code> and Port
 * of the destination is needed. Thereby, the message will be dispatched to the
 * appropriate remote <code>TransMessageListener</code>.
 * 
 * Comparable to java.net.InetSocketAddress
 * 
 * @author Sebastian Kaune
 * @author Konstantin Pussep
 * @version 3.0, 12/03/2007
 * 
 */
public interface TransInfo extends Transmitable {

	/**
	 * Returns the remote port of a <code>TransMessageListener</code>
	 * 
	 * @return the remote port of a <code>TransMessageListener</code>
	 */
	public int getPort();

	/**
	 * Returns the <code>NetID</code> of a remote
	 * <code>TransMessageListener</code>
	 * 
	 * @return the remote <code>NetID</code>
	 */
	public NetID getNetId();
}
