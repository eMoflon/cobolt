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

package de.tud.kom.p2psim.impl.network.routed.routing.aodv;

import de.tudarmstadt.maki.simonstrator.api.Time;

/**
 * Configuration for AODV.
 * 
 * @author Christoph Neumann
 */
public interface AodvConstants {
	public static final int MAX_SEQ_NO = Integer.MAX_VALUE;
	public static final int INVALID_SEQ_NO = -1;

	public static final long ACTIVE_ROUTE_TIMEOUT = 6000 * Time.MILLISECOND;
	public static final long MY_ROUTE_TIMEOUT = 2 * ACTIVE_ROUTE_TIMEOUT;
	
	public static final long HELLO_INTERVAL = 1000 * Time.MILLISECOND;
	public static final long ALLOWED_HELLO_LOSS = 2;
	
	public static final long DELETE_PERIOD = 5 * ACTIVE_ROUTE_TIMEOUT;

	public static final byte NET_DIAMETER = 35;

	public static final long NODE_TRAVERSAL_TIME = 40 * Time.MILLISECOND;
	public static final long NET_TRAVERSAL_TIME = 2 * NODE_TRAVERSAL_TIME * NET_DIAMETER;
	//public static long NEXT_HOP_WAIT = NODE_TRAVERSAL_TIME + 10

	public static final long PATH_DISCOVERY_TIME = 2 * NET_TRAVERSAL_TIME;
	
	public static final byte TTL_START = 1;

	public static final byte TTL_INCREMENT = 2;

	public static final byte TTL_THRESHOLD = 7;

	public static final int TIMEOUT_BUFFER = 2;

	public static final byte RREQ_RETRIES = 2;
}
